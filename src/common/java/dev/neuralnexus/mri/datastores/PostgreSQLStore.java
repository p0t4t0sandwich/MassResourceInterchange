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

public final class PostgreSQLStore extends AbstractDataStore<PostgreSQLStore.Config> {
    public PostgreSQLStore() {
        this("aPostgreSQLDatabase", new PostgreSQLStore.Config());
    }

    public PostgreSQLStore(String name, PostgreSQLStore.Config config) {
        super(name, "postgresql", config);
    }

    @Override
    public void connect() {
        Constants.logger()
                .info(
                        "Connecting to MySQL database at {}:{}",
                        this.config().host,
                        this.config().port);

        HikariConfig config = new HikariConfig();
        config.setUsername(this.config().username);
        config.setPassword(this.config().password);
        config.addDataSourceProperty("databaseName", this.config().database);
        config.addDataSourceProperty("serverName", this.config().host);
        config.addDataSourceProperty("portNumber", this.config().port);
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        // config.setDriverClassName("org.postgresql.Driver");
        config.setPoolName(Constants.MOD_NAME + "PostgreSQLPool");

        // config.addDataSourceProperty("cachePrepStmts", "true");
        // config.addDataSourceProperty("prepStmtCacheSize", "250");
        // config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.ds = new HikariDataSource(config);

        this.startUp();
    }

    @ConfigSerializable
    public static class Config {
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

        @Comment("The name of the database to connect to")
        @Required
        private String database;

        {
            this.host = "localhost";
            this.port = 5432;
            this.username = "someuser";
            this.password = "asecurepassword";
            this.database = "mass_resource_interchange";
        }
    }
}
