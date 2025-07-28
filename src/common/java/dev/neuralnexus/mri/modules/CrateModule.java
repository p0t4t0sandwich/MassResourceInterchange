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

public final class CrateModule extends AbstractModule<CrateModule.Config> {

    public CrateModule() {
        this(false, SQLiteStore.DEFAULT_NAME, new CrateModule.Config());
    }

    public CrateModule(boolean enabled, String datastore, CrateModule.Config config) {
        super("crate", enabled, datastore, config);
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
