/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.datastores;

public interface DataStore<T> {
    /**
     * Get the name of the datastore
     *
     * @return The name of the datastore
     */
    String name();

    /**
     * Get the datastore of the datastore
     *
     * @return The datastore of the datastore
     */
    String type();

    /** Connect to the datastore */
    void connect();

    /**
     * Get the configuration for this datastore
     *
     * @return The configuration for this datastore
     */
    T config();
}
