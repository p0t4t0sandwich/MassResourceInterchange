/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.datastores;

import com.zaxxer.hikari.HikariDataSource;

import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.MRIAPI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractDataStore<T> implements DataStore<T> {
    private final String name;

    private final String type;

    private final T config;

    HikariDataSource ds;

    AbstractDataStore(String name, String type, T config) {
        this.name = name;
        this.type = type;
        this.config = config;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String type() {
        return this.type;
    }

    // TODO: Decide where to stick a table prefix in the config
    @Override
    public T config() {
        return this.config;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.ds.getConnection();
    }

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS store ("
                    + "id VARCHAR(36) PRIMARY KEY NOT NULL,"
                    + "data BLOB NOT NULL,"
                    + "locked_by VARCHAR(36) DEFAULT NULL,"
                    + "locked_at TIMESTAMP DEFAULT NULL);";

    private static final String CREATE_TABLE_POSTGRESQL =
            "CREATE TABLE IF NOT EXISTS store ("
                    + "id UUID PRIMARY KEY NOT NULL,"
                    + "data BYTEA NOT NULL,"
                    + "locked_by UUID DEFAULT NULL,"
                    + "locked_at TIMESTAMP DEFAULT NULL);";

    void startUp() {
        this.createTablesIfNotExists();
        this.clearLocks();
    }

    // TODO: See if `conn.getMetaData().getDatabaseProductName()` would be of any use
    void createTablesIfNotExists() {
        try (Connection conn = this.getConnection()) {
            String statement =
                    this.type.equalsIgnoreCase("postgresql")
                            ? CREATE_TABLE_POSTGRESQL
                            : CREATE_TABLE_SQL;
            conn.createStatement().execute(statement);
        } catch (SQLException e) {
            Constants.logger().error("Failed to create tables in database: {}", e.getMessage());
        }
    }

    private static final String UPDATE_SQL = "UPDATE store SET data = ? WHERE id = ?;";

    private static final String INSERT_SQL = "INSERT INTO store (id, data) VALUES (?, ?);";

    // TODO: Combine these into a single db-dependent statement that can be overridden
    @Override
    public boolean store(UUID id, byte[] data) {
        try (Connection conn = this.getConnection()) {
            try (var update = conn.prepareStatement(UPDATE_SQL)) {
                update.setBytes(1, data);
                update.setString(2, id.toString());
                if (update.executeUpdate() > 0) {
                    return true;
                }
            } catch (SQLException e) {
                Constants.logger().error("Failed to update data in database: {}", e.getMessage());
                return false;
            }
            // INSERT if the row doesn't exist
            try (var insert = conn.prepareStatement(INSERT_SQL)) {
                insert.setString(1, id.toString());
                insert.setBytes(2, data);
                insert.executeUpdate();
            } catch (SQLException e) {
                Constants.logger().error("Failed to insert data into database: {}", e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            Constants.logger().error("Failed to store data in database: {}", e.getMessage());
            return false;
        }
        return true;
    }

    private static final String SELECT_SQL = "SELECT data FROM store WHERE id = ?;";

    // TODO: Play with returning an optional, or annotate it as nullable, see what vibes better
    @Override
    public byte[] retrieve(UUID id) {
        try (Connection conn = this.getConnection()) {
            try (var select = conn.prepareStatement(SELECT_SQL)) {
                select.setString(1, id.toString());
                var resultSet = select.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getBytes("data");
                }
            } catch (SQLException e) {
                Constants.logger()
                        .error("Failed to retrieve data from database: {}", e.getMessage());
            }
        } catch (SQLException e) {
            Constants.logger().error("Failed to connect to database: {}", e.getMessage());
        }
        return null;
    }

    // TODO: Simplify these into db-dependent combo statements
    private static final String LOCK_SQL =
            "UPDATE store SET locked_by = ?, locked_at = CURRENT_TIMESTAMP WHERE id = ? AND locked_by IS NULL;";

    @Override
    public boolean lock(UUID id) {
        try (Connection conn = this.getConnection()) {
            try (var lockStmt = conn.prepareStatement(LOCK_SQL)) {
                lockStmt.setString(1, MRIAPI.getInstance().serverId().toString());
                lockStmt.setString(2, id.toString());
                return lockStmt.executeUpdate() > 0;
            } catch (SQLException e) {
                Constants.logger().error("Failed to lock data in database: {}", e.getMessage());
            }
        } catch (SQLException e) {
            Constants.logger()
                    .error("Failed to connect to database for locking: {}", e.getMessage());
        }
        return false;
    }

    private static final String UNLOCK_SQL =
            "UPDATE store SET locked_by = NULL, locked_at = NULL WHERE id = ? AND locked_by = ?;";

    @Override
    public void unlock(UUID id) {
        try (Connection conn = this.getConnection()) {
            try (var unlockStmt = conn.prepareStatement(UNLOCK_SQL)) {
                unlockStmt.setString(1, id.toString());
                unlockStmt.setString(2, MRIAPI.getInstance().serverId().toString());
                unlockStmt.executeUpdate();
            } catch (SQLException e) {
                Constants.logger().error("Failed to unlock data in database: {}", e.getMessage());
            }
        } catch (SQLException e) {
            Constants.logger()
                    .error("Failed to connect to database for unlocking: {}", e.getMessage());
        }
    }

    private static final String IS_LOCKED_SQL =
            "SELECT locked_by FROM store WHERE id = ? AND locked_by IS NOT NULL;";

    @Override
    public Optional<UUID> isLocked(UUID id) {
        try (Connection conn = this.getConnection()) {
            try (var stmt = conn.prepareStatement(IS_LOCKED_SQL)) {
                stmt.setString(1, id.toString());
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String lockedBy = rs.getString("locked_by");
                        return Optional.of(UUID.fromString(lockedBy));
                    }
                } catch (SQLException e) {
                    Constants.logger()
                            .error("Failed to check if data is locked: {}", e.getMessage());
                } catch (IllegalArgumentException e) {
                    Constants.logger()
                            .error("Invalid UUID format for locked_by: {}", e.getMessage());
                }
            }
        } catch (SQLException e) {
            Constants.logger()
                    .error(
                            "Failed to connect to database to check if data is locked: {}",
                            e.getMessage());
        }
        return Optional.empty();
    }

    private static final String CLEAR_LOCKS_SQL =
            "UPDATE store SET locked_by = NULL, locked_at = NULL WHERE locked_by = ?;";

    // TODO: Add another trigger for this when a server shuts down
    public void clearLocks() {
        try (Connection conn = this.getConnection()) {
            try (var clearStmt = conn.prepareStatement(CLEAR_LOCKS_SQL)) {
                clearStmt.setString(1, MRIAPI.getInstance().serverId().toString());
                clearStmt.executeUpdate();
            } catch (SQLException e) {
                Constants.logger().error("Failed to clear locks in database: {}", e.getMessage());
            }
        } catch (SQLException e) {
            Constants.logger()
                    .error("Failed to connect to database for clearing locks: {}", e.getMessage());
        }
    }
}
