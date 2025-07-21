/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common.datastore;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import dev.neuralnexus.mri.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class DataSource {
    private static Map<String, String> sql_config;
    private static HikariDataSource ds;

    private DataSource() {}

    public static void init() {
        HikariConfig config = new HikariConfig();

        String dbType = sql_config.get("type").toLowerCase();
        switch (dbType) {
            case "sqlite":
                Path dbPath = Paths.get(sql_config.get("path"));
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
                                        "Failed to created SQLite database.  Error: {}",
                                        exception.getMessage());
                    }
                }

                config.setJdbcUrl("jdbc:sqlite:" + dbPath.toAbsolutePath());
                config.setDriverClassName("org.sqlite.JDBC");
                config.setPoolName(Constants.MOD_NAME + "SQLitePool");
                break;
            case "mysql":
                config.setUsername(sql_config.get("username"));
                config.setPassword(sql_config.get("password"));
                config.setJdbcUrl(
                        "jdbc:mysql://"
                                + sql_config.get("host")
                                + ":"
                                + sql_config.get("port")
                                + "/"
                                + sql_config.get("database"));
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                config.setPoolName(Constants.MOD_NAME + "MySQLPool");
                break;
            case "postgresql":
                config.setUsername(sql_config.get("username"));
                config.setPassword(sql_config.get("password"));
                config.addDataSourceProperty("databaseName", sql_config.get("database"));
                config.addDataSourceProperty("serverName", sql_config.get("host"));
                config.addDataSourceProperty("portNumber", sql_config.get("port"));
                config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
                config.setDriverClassName("org.postgresql.Driver");
                config.setPoolName(Constants.MOD_NAME + "PostgreSQLPool");
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
