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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class BackpackModule extends AbstractModule<BackpackModule.Config> {

    public BackpackModule() {
        this(true, SQLiteStore.DEFAULT_NAME, new BackpackModule.Config());
    }

    public BackpackModule(boolean enabled, String datastore, BackpackModule.Config config) {
        super("backpack", enabled, datastore, config);
    }

    public void init() {
        this.createTablesIfNotExists();
    }

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS backpacks ("
                    + "id TEXT PRIMARY KEY,"
                    + "player_id TEXT NOT NULL,"
                    + "size INTEGER NOT NULL);";

    private static final String CREATE_TABLE_POSTGRESQL =
            "CREATE TABLE IF NOT EXISTS backpacks ("
                    + "id UUID PRIMARY KEY,"
                    + "player_id UUID NOT NULL,"
                    + "size INTEGER NOT NULL);";

    public void createTablesIfNotExists() {
        try (Connection conn = this.datastore().getConnection()) {
            String createTableSql =
                    this.datastore().type().equals("postgresql")
                            ? CREATE_TABLE_POSTGRESQL
                            : CREATE_TABLE_SQL;
            try (var stmt = conn.createStatement()) {
                stmt.execute(createTableSql);
            } catch (SQLException e) {
                Constants.logger().error("Failed to create backpacks table", e);
            }
        } catch (SQLException e) {
            Constants.logger().error("Failed to get database connection", e);
        }
    }

    public record BackpackInfo(UUID id, int size) {}

    private static final String BACKPACK_INFO_SQL =
            "SELECT id, size FROM backpacks WHERE player_id = ?";

    public Optional<BackpackInfo> getBackpackInfo(UUID playerId) {
        try (Connection conn = this.datastore().getConnection();
                var stmt = conn.prepareStatement(BACKPACK_INFO_SQL)) {
            stmt.setObject(1, playerId);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String strUuid = rs.getString("id");
                    UUID backpackId = UUID.fromString(strUuid);
                    int size = rs.getInt("size");
                    return Optional.of(new BackpackInfo(backpackId, size));
                }
            } catch (SQLException e) {
                Constants.logger().error("Failed to execute query for backpack ID", e);
            } catch (IllegalArgumentException e) {
                Constants.logger()
                        .error("Invalid UUID format for player's backpack: {}", playerId, e);
            }
        } catch (SQLException e) {
            Constants.logger().error("Failed to get database connection", e);
        }
        return Optional.empty();
    }

    private static final String CREATE_BACKPACK_SQL =
            "INSERT INTO backpacks (id, player_id, size) VALUES (?, ?, ?)";

    public boolean createBackpack(UUID playerId, UUID backpackId, int size) {
        try (Connection conn = this.datastore().getConnection();
                var stmt = conn.prepareStatement(CREATE_BACKPACK_SQL)) {
            stmt.setObject(1, backpackId);
            stmt.setObject(2, playerId);
            stmt.setInt(3, size);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Constants.logger().error("Failed to create backpack for player: {}", playerId, e);
            return false;
        }
    }

    @ConfigSerializable
    public static class Config {
        @Comment("Whether to give backpacks to players on their first join")
        @Required
        @Setting("giveBackpacksByDefault")
        public boolean giveBackpacksByDefault;

        @Comment(
                "Allows players to access their backpacks using the /backpack command, disable if you want to"
                        + "restrict command access via a permissions manager using the \"mri.backpack.open\" permission.")
        @Required
        @Setting("allowCommandAccess")
        public boolean allowCommandAccess;

        @Comment("The default size of the backpack. Can be multiple of 9 from 9 to 54.")
        @Required
        @Setting("defaultBackpackSize")
        public int defaultBackpackSize;

        {
            this.giveBackpacksByDefault = true;
            this.allowCommandAccess = true;
            this.defaultBackpackSize = 27;
        }
    }
}
