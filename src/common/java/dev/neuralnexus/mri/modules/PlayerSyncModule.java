/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.modules;

import dev.neuralnexus.mri.datastores.SQLiteStore;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

public final class PlayerSyncModule extends AbstractModule<PlayerSyncModule.Config> {

    public PlayerSyncModule() {
        this(false, SQLiteStore.DEFAULT_NAME, new PlayerSyncModule.Config());
    }

    public PlayerSyncModule(boolean enabled, String datastore, PlayerSyncModule.Config config) {
        super("playersync", enabled, datastore, config);
    }

    @ConfigSerializable
    public static class Config {
        @Comment("")
        @Required
        @Setting("something")
        boolean something;

        {
            this.something = true;
        }
    }
}
