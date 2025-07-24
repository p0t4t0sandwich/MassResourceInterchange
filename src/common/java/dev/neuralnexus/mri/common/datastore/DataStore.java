/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common.datastore;

public interface DataStore {
    /**
     * Get the name of the config
     *
     * @return The name of the config
     */
    String name();

    /**
     * Get the type of the config
     *
     * @return The type of the config
     */
    String type();

    /** Connect to the datastore */
    void connect();
}
