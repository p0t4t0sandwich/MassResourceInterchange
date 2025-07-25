/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common.datastore;

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

public final class SQLiteStore extends AbstractDataStore<SQLiteStore.Config> {
    private HikariDataSource ds;

    public SQLiteStore() {
        super("aSQLiteDatabase", "sqlite", new SQLiteStore.Config());
    }

    public SQLiteStore(String nameStr, SQLiteStore.Config config) {
        super("sqlite", nameStr, config);
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
        config.setJdbcUrl("jdbc:sqlite:" + dbPath.toAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName(Constants.MOD_NAME + "SQLitePool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.ds = new HikariDataSource(config);
    }

    private Connection getConnection() throws SQLException {
        return this.ds.getConnection();
    }

    @ConfigSerializable
    public static class Config {
        @Comment("The path to the SQLite database file")
        @Required
        @Setting("filePath")
        private String filePath;

        {
            this.filePath = "world/MassResourceInterchange/datastore.db";
        }
    }
}
