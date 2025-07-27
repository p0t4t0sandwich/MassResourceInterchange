/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.modules;

public abstract class AbstractModule<T> implements Module<T> {
    private final String name;

    private final boolean enabled;

    private final String datastore;

    private final T config;

    AbstractModule(String name, boolean enabled, String datastore, T config) {
        this.name = name;
        this.enabled = enabled;
        this.datastore = datastore;
        this.config = config;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public boolean enabled() {
        return this.enabled;
    }

    @Override
    public String datastore() {
        return this.datastore;
    }

    @Override
    public T config() {
        return this.config;
    }
}
