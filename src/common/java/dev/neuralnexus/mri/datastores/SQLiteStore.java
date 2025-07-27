/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.datastores;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import dev.neuralnexus.mri.Constants;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public final class SQLiteStore extends AbstractDataStore<SQLiteStore.Config> {
    public static final String DEFAULT_NAME = "aSQLiteDatabase";

    private HikariDataSource ds;

    public SQLiteStore() {
        this(DEFAULT_NAME, new SQLiteStore.Config());
    }

    public SQLiteStore(String name, SQLiteStore.Config config) {
        super(name, "sqlite", config);
    }

    @Override
    public void connect() {
        HikariConfig config = new HikariConfig();
        Path dbPath = Paths.get(this.config().filePath);
        File databaseFile = dbPath.toFile();
        if (!databaseFile.getParentFile().exists()) {
            databaseFile.getParentFile().mkdirs();
        }
        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile();
            } catch (IOException exception) {
                Constants.logger()
                        .warn(
                                "Failed to create SQLite database.  Error: {}",
                                exception.getMessage());
            }
        }
        config.setJdbcUrl("jdbc:sqlite:file:" + databaseFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName(Constants.MOD_NAME + "SQLitePool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.ds = new HikariDataSource(config);

        this.createTablesIfNotExists();
    }

    private Connection getConnection() throws SQLException {
        return this.ds.getConnection();
    }

    private void createTablesIfNotExists() {
        try (Connection conn = this.getConnection()) {
            String createTableSQL =
                    "CREATE TABLE IF NOT EXISTS resources ("
                            + "id TEXT PRIMARY KEY NOT NULL,"
                            + "data BLOB NOT NULL);";
            conn.createStatement().execute(createTableSQL);
        } catch (SQLException e) {
            Constants.logger()
                    .error("Failed to create tables in SQLite database: {}", e.getMessage());
        }
    }

    private static final String STORE_SQL =
            "INSERT OR REPLACE INTO resources (id, data) VALUES (?, ?);";

    public boolean store(UUID id, byte[] data) {
        try (Connection conn = this.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(STORE_SQL)) {
                preparedStatement.setString(1, id.toString());
                preparedStatement.setBytes(2, data);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                Constants.logger()
                        .error("Failed to insert data into SQLite database: {}", e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            Constants.logger().error("Failed to store data in SQLite database: {}", e.getMessage());
            return false;
        }
        return true;
    }

    private static final String RETRIEVE_SQL = "SELECT data FROM resources WHERE id = ?;";

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
                        .error("Failed to retrieve data from SQLite database: {}", e.getMessage());
            }
        } catch (SQLException e) {
            Constants.logger().error("Failed to connect to SQLite database: {}", e.getMessage());
        }
        return null;
    }

    @ConfigSerializable
    public static class Config {
        @Comment("The path to the SQLite database file")
        @Required
        @Setting("filePath")
        private String filePath;

        // TODO: Create a way to set the default to the world's folder
        {
            this.filePath = "MassResourceInterchange/datastore.db";
        }
    }
}
