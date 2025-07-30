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

public final class MySQLStore extends AbstractDataStore<MySQLStore.Config> {
    public MySQLStore() {
        this("aMySQLDatabase", new MySQLStore.Config());
    }

    public MySQLStore(String name, MySQLStore.Config config) {
        super(name, "mysql", config);
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
        config.setJdbcUrl(
                "jdbc:mysql://"
                        + this.config().host
                        + ":"
                        + this.config().port
                        + "/"
                        + this.config().database);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setPoolName(Constants.MOD_NAME + "MySQLPool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.ds = new HikariDataSource(config);

        this.startUp();
    }

    @ConfigSerializable
    public static class Config {
        @Comment("The hostname of the MySQL server")
        @Required
        private String host;

        @Comment("The port of the MySQL server")
        @Required
        private int port;

        @Comment("The username to connect to the MySQL server")
        @Required
        private String username;

        @Comment("The password to connect to the MySQL server")
        @Required
        private String password;

        @Comment("The name of the database to connect to")
        @Required
        private String database;

        {
            this.host = "localhost";
            this.port = 3306;
            this.username = "someuser";
            this.password = "asecurepassword";
            this.database = "mass_resource_interchange";
        }
    }
}
