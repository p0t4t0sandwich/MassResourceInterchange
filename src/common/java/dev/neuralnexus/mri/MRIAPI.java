/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri;

import dev.neuralnexus.mri.config.MRIConfigLoader;
import dev.neuralnexus.mri.datastores.DataStore;
import dev.neuralnexus.mri.modules.BackpackModule;
import dev.neuralnexus.mri.modules.CrateModule;
import dev.neuralnexus.mri.modules.Module;
import dev.neuralnexus.mri.modules.PlayerSyncModule;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@ApiStatus.Experimental
public final class MRIAPI {
    private static final MRIAPI INSTANCE = new MRIAPI();
    private static final TypeRegistry typeRegistry = new TypeRegistry();

    private MRIAPI() {}

    public static MRIAPI getInstance() {
        return INSTANCE;
    }

    public TypeRegistry typeRegistry() {
        return typeRegistry;
    }

    /**
     * Get the server's ID
     *
     * @return The server's ID
     */
    public UUID serverId() {
        return MRIConfigLoader.config().serverId();
    }

    /**
     * Get a module by its name
     *
     * @param name The name of the module
     * @return The module, or null if not found
     */
    public Optional<Module<?>> getModule(String name) {
        return MRIConfigLoader.config().modules().stream()
                .filter(module -> module.name().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Get a module by its type
     *
     * @param type The type of the module
     * @return The module, or null if not found
     */
    public <T extends Module<?>> Optional<T> getModule(Class<T> type) {
        return MRIConfigLoader.config().modules().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }

    /**
     * Get a datastore by its name
     *
     * @param name The name of the datastore
     * @return The datastore, or null if not found
     */
    public Optional<DataStore<?>> getDataStore(String name) {
        return MRIConfigLoader.config().datastores().stream()
                .filter(dataStore -> dataStore.name().equalsIgnoreCase(name))
                .findFirst();
    }

    /** Get the backpack module */
    public @NotNull BackpackModule backpack() {
        return this.getModule(BackpackModule.class).orElseThrow();
    }

    /** Get the crate module */
    public @NotNull CrateModule crate() {
        return this.getModule(CrateModule.class).orElseThrow();
    }

    /** Get the playerSync module */
    public @NotNull PlayerSyncModule playerSync() {
        return this.getModule(PlayerSyncModule.class).orElseThrow();
    }
}
