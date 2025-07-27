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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ConfigSerializable
public final class MRIConfig {
    @Comment("Config version, DO NOT CHANGE THIS")
    @Required
    private int version = 1;

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

    /**
     * Get the version of the configuration.
     *
     * @return The version of the configuration.
     */
    public int version() {
        return this.version;
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
     * Get a module by its name.
     *
     * @param name The name of the module.
     * @return The module, or null if not found.
     */
    public Optional<Module<?>> getModuleByName(String name) {
        return this.modules.stream()
                .filter(module -> module.name().equalsIgnoreCase(name))
                .findFirst();
    }
}
