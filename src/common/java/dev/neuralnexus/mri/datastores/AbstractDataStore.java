/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.datastores;

import java.util.UUID;

public abstract class AbstractDataStore<T> implements DataStore<T> {
    private final String name;

    private final String type;

    private final T config;

    AbstractDataStore(String name, String type, T config) {
        this.name = name;
        this.type = type;
        this.config = config;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String type() {
        return this.type;
    }

    @Override
    public T config() {
        return this.config;
    }

    // TODO: Implement for others
    @Override
    public boolean store(UUID id, byte[] data) {
        return false;
    }

    // TODO: Implement for others
    @Override
    public byte[] retrieve(UUID id) {
        return null;
    }
}
