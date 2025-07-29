/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.config;

import dev.neuralnexus.mri.datastores.DataStore;
import dev.neuralnexus.mri.datastores.MySQLStore;
import dev.neuralnexus.mri.datastores.PostgreSQLStore;
import dev.neuralnexus.mri.datastores.SQLiteStore;
import dev.neuralnexus.mri.modules.BackpackModule;
import dev.neuralnexus.mri.modules.CrateModule;
import dev.neuralnexus.mri.modules.Module;
import dev.neuralnexus.mri.modules.PlayerSyncModule;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ConfigSerializable
public final class MRIConfig {
    @Comment("Config version, DO NOT CHANGE THIS")
    @Required
    private int version = 1;

    @Comment(
            "Server ID, used to identify the server in the datastore."
                    + "DO NOT CHANGE THIS unless you are migrating a server and need to realign the ID.")
    @Required
    @Setting("serverId")
    private UUID serverId = UUID.randomUUID();

    @Comment("Enable or disable modules")
    @Required
    private Set<Module<?>> modules = new HashSet<>();

    {
        this.modules.add(new BackpackModule());
        this.modules.add(new CrateModule());
        this.modules.add(new PlayerSyncModule());
    }

    @Comment("List of configured datastores")
    @Required
    private Set<DataStore<?>> datastores = new HashSet<>();

    {
        this.datastores.add(new MySQLStore());
        this.datastores.add(new PostgreSQLStore());
        this.datastores.add(new SQLiteStore());
    }

    @Comment(
            "Advanced configuration options\n"
                    + "We are not responsible for any issues caused by changing these settings.")
    @Required
    private Advanced advanced = new Advanced();

    /**
     * Get the server ID.
     *
     * @return The server ID.
     */
    public UUID serverId() {
        return this.serverId;
    }

    /**
     * Get the modules in the configuration.
     *
     * @return The modules in the configuration.
     */
    public Set<Module<?>> modules() {
        return this.modules;
    }

    /**
     * Get the list of configured data stores.
     *
     * @return The list of configured data stores.
     */
    public Set<DataStore<?>> datastores() {
        return this.datastores;
    }

    /**
     * Get the advanced configuration options.
     *
     * @return The advanced configuration options.
     */
    public Advanced advanced() {
        return this.advanced;
    }

    @ConfigSerializable
    public static class Advanced {
        @Comment("Disable MC version checks when connecting to a datastore")
        @Required
        @Setting("disableVersionChecks")
        public boolean disableVersionChecks = false;

        @Comment("Disable modlist checks when connecting to a datastore")
        @Required
        @Setting("disableModlistChecks")
        public boolean disableModlistChecks = false;
    }
}
