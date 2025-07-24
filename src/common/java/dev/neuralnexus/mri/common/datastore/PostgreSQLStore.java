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

import java.sql.Connection;
import java.sql.SQLException;

@ConfigSerializable
public final class PostgreSQLStore extends AbstractDataStore {
    private HikariDataSource ds;

    @Comment("The hostname of the PostgreSQL server")
    @Required
    private String host;

    @Comment("The port of the PostgreSQL server")
    @Required
    private int port;

    @Comment("The username to connect to the PostgreSQL server")
    @Required
    private String username;

    @Comment("The password to connect to the PostgreSQL server")
    @Required
    private String password;

    {
        this.host = "localhost";
        this.port = 5432;
        this.username = "someuser";
        this.password = "asecurepassword";
    }

    @Comment("The name of the database to connect to")
    @Required
    private String database;

    {
        this.database = "mass_resource_interchange";
    }

    @Override
    public void connect() {
        HikariConfig config = new HikariConfig();
        config.setUsername(this.username);
        config.setPassword(this.password);
        config.addDataSourceProperty("databaseName", this.database);
        config.addDataSourceProperty("serverName", this.host);
        config.addDataSourceProperty("portNumber", this.port);
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.setDriverClassName("org.postgresql.Driver");
        config.setPoolName(Constants.MOD_NAME + "PostgreSQLPool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.ds = new HikariDataSource(config);
    }

    private Connection getConnection() throws SQLException {
        return this.ds.getConnection();
    }
}
