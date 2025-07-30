/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.datastores;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * Get the configuration for this datastore
     *
     * @return The configuration for this datastore
     */
    T config();

    /** Connect to the datastore */
    void connect();

    /**
     * Get a connection to the datastore
     *
     * @return A connection to the datastore
     * @throws SQLException If an error occurs while getting the connection
     */
    Connection getConnection() throws SQLException;

    /**
     * Store general data in the datastore
     *
     * @param id The unique identifier for the data
     * @param data The data to store
     * @return True if the data was stored successfully, false otherwise
     */
    boolean store(UUID id, byte[] data);

    /**
     * Retrieve general data from the datastore
     *
     * @param id The unique identifier for the data
     * @return The data associated with the identifier, or null if not found
     */
    byte[] retrieve(UUID id);

    /**
     * Delete general data from the datastore
     *
     * @param id The unique identifier for the data to delete
     * @return True if the data was deleted successfully, false otherwise
     */
    boolean delete(UUID id);

    /**
     * Lock an entry in the datastore
     *
     * @param id The unique identifier for the data to lock
     * @return True if the entry was successfully locked, false otherwise
     */
    boolean lock(UUID id);

    /**
     * Unlock an entry in the datastore
     *
     * @param id The unique identifier for the data to unlock
     */
    void unlock(UUID id);

    /**
     * Check if an entry in the datastore is locked
     *
     * @param id The unique identifier for the data to check
     * @return An Optional containing the server ID that has locked the entry
     */
    Optional<UUID> isLocked(UUID id);
}
