/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.datastores;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import dev.neuralnexus.mri.CommonClass;
import dev.neuralnexus.mri.Constants;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class SQLiteStore extends AbstractDataStore<SQLiteStore.Config> {
    public static final String DEFAULT_NAME = "aSQLiteDatabase";

    public SQLiteStore() {
        this(DEFAULT_NAME, new SQLiteStore.Config());
    }

    public SQLiteStore(String name, SQLiteStore.Config config) {
        super(name, "sqlite", config);
    }

    @Override
    public void connect() {
        Constants.logger().info("Connecting to SQLite database at {}", this.config().filePath);

        HikariConfig config = new HikariConfig();
        Path dbPath = CommonClass.worldFolder.resolve(this.config().filePath);
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

        this.startUp();
    }

    @ConfigSerializable
    public static class Config {
        @Comment(
                "The path to the SQLite database file, relative to the current world/save directory.")
        @Required
        @Setting("filePath")
        private String filePath;

        {
            this.filePath = Constants.MOD_NAME + File.separator + "datastore.db";
        }
    }
}
