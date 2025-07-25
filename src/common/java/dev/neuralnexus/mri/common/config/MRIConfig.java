/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common.config;

import dev.neuralnexus.mri.common.datastore.DataStore;

import java.util.List;
import java.util.Map;

public interface MRIConfig {
    /**
     * Get the version of the configuration.
     *
     * @return The version of the configuration.
     */
    int version();

    /**
     * Get the modules in the configuration.
     *
     * @return The modules in the configuration.
     */
    Map<String, Boolean> modules();

    /**
     * Get the list of configured data stores.
     *
     * @return The list of configured data stores.
     */
    List<DataStore<?>> datastores();
}
