/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.modules;

public interface Module<T> {
    /**
     * Get the name of the module
     *
     * @return The name of the module
     */
    String name();

    /**
     * Get if the module is enabled
     *
     * @return True if the module is enabled, false otherwise
     */
    boolean enabled();

    /**
     * Get the module's datastore
     *
     * @return The module's datastore
     */
    String datastore();

    /**
     * Get the configuration for this module
     *
     * @return The configuration for this module
     */
    T config();
}
