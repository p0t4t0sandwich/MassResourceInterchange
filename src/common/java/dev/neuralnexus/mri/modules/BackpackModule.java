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

public final class BackpackModule extends AbstractModule<BackpackModule.Config> {

    public BackpackModule() {
        this(true, SQLiteStore.DEFAULT_NAME, new BackpackModule.Config());
    }

    public BackpackModule(boolean enabled, String datastore, BackpackModule.Config config) {
        super("backpack", enabled, datastore, config);
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
