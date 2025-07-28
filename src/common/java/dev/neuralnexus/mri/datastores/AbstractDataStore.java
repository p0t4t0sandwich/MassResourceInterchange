/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.datastores;

import com.zaxxer.hikari.HikariDataSource;

import dev.neuralnexus.mri.Constants;

import java.sql.Connection;
import java.sql.SQLException;
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

    @Override
    public T config() {
        return this.config;
    }

    Connection getConnection() throws SQLException {
        return this.ds.getConnection();
    }

    void createTablesIfNotExists() {
        try (Connection conn = this.getConnection()) {
            String createTableSQL =
                    "CREATE TABLE IF NOT EXISTS resources ("
                            + "id TEXT PRIMARY KEY NOT NULL,"
                            + "data BLOB NOT NULL);";
            conn.createStatement().execute(createTableSQL);
        } catch (SQLException e) {
            Constants.logger().error("Failed to create tables in database: {}", e.getMessage());
        }
    }

    private static final String STORE_SQL =
            "INSERT OR REPLACE INTO resources (id, data) VALUES (?, ?);";

    @Override
    public boolean store(UUID id, byte[] data) {
        try (Connection conn = this.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(STORE_SQL)) {
                preparedStatement.setString(1, id.toString());
                preparedStatement.setBytes(2, data);
                preparedStatement.executeUpdate();
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

    private static final String RETRIEVE_SQL = "SELECT data FROM resources WHERE id = ?;";

    @Override
    public byte[] retrieve(UUID id) {
        try (Connection conn = this.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(RETRIEVE_SQL)) {
                preparedStatement.setString(1, id.toString());
                var resultSet = preparedStatement.executeQuery();
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
}
