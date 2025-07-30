/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.modules;

import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.datastores.SQLiteStore;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class PlayerSyncModule extends AbstractModule<PlayerSyncModule.Config> {
    public static boolean INITIALIZED = false;

    public PlayerSyncModule() {
        this(false, SQLiteStore.DEFAULT_NAME, new PlayerSyncModule.Config());
    }

    public PlayerSyncModule(boolean enabled, String datastore, PlayerSyncModule.Config config) {
        super("playersync", enabled, datastore, config);
    }

    public void init() {
        Constants.logger().info("Initializing PlayerSync Module");
        this.createTablesIfNotExists();
        INITIALIZED = true;
    }

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS playersync ("
                    + "id VARCHAR(36) PRIMARY KEY,"
                    + "player_id VARCHAR(36) NOT NULL);";

    private static final String CREATE_TABLE_POSTGRESQL =
            "CREATE TABLE IF NOT EXISTS playersync ("
                    + "id UUID PRIMARY KEY,"
                    + "player_id UUID NOT NULL);";

    public void createTablesIfNotExists() {
        try (var conn = this.datastore().getConnection()) {
            String createTableSql =
                    this.datastore().type().equals("postgresql")
                            ? CREATE_TABLE_POSTGRESQL
                            : CREATE_TABLE_SQL;
            try (var stmt = conn.createStatement()) {
                stmt.execute(createTableSql);
            } catch (SQLException e) {
                Constants.logger().error("Failed to create playersync table", e);
            }
        } catch (SQLException e) {
            Constants.logger().error("Failed to get database connection", e);
        }
    }

    private static final String SELECT_ID_SQL = "SELECT id FROM playersync WHERE player_id = ?;";

    public Optional<UUID> getId(UUID playerId) {
        try (var conn = this.datastore().getConnection();
                var stmt = conn.prepareStatement(SELECT_ID_SQL)) {
            stmt.setString(1, playerId.toString());
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String idString = rs.getString("id");
                    UUID id = UUID.fromString(idString);
                    return Optional.of(id);
                }
            } catch (IllegalArgumentException e) {
                Constants.logger().error("Invalid UUID format for player ID: {}", playerId, e);
            } catch (SQLException e) {
                Constants.logger().error("Failed to execute query for player ID", e);
            }
        } catch (SQLException e) {
            Constants.logger().error("Failed to get ID by player ID", e);
        }
        return Optional.empty();
    }

    private static final String INSERT_PLAYER_SQL =
            "INSERT INTO playersync (id, player_id) VALUES (?, ?);";

    public boolean createPlayerEntry(UUID id, UUID playerId) {
        try (var conn = this.datastore().getConnection();
                var stmt = conn.prepareStatement(INSERT_PLAYER_SQL)) {
            stmt.setString(1, id.toString());
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            Constants.logger().error("Failed to insert player entry into playersync table", e);
            return false;
        }
    }

    public UUID getOrCreateId(UUID playerId) {
        return this.getId(playerId)
                .orElseGet(
                        () -> {
                            UUID newId = UUID.randomUUID();
                            if (this.createPlayerEntry(newId, playerId)) {
                                return newId;
                            } else {
                                Constants.logger()
                                        .error("Failed to create player entry for {}", playerId);
                                throw new RuntimeException(
                                        "Failed to create player entry for " + playerId);
                            }
                        });
    }

    @ConfigSerializable
    public static class Config {
        @Comment(
                "Set the auto-save interval in seconds, set to 0 or -1 to disable auto-saving."
                        + "\nYou may want to increase this if you have a lot of players online and aren't prone to crashes.")
        @Required
        @Setting("autoSaveInterval")
        public int autoSaveInterval;

        {
            this.autoSaveInterval = 300;
        }
    }
}
