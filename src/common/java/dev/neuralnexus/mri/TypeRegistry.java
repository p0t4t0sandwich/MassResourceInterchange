/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri;

import dev.neuralnexus.mri.datastores.DataStore;
import dev.neuralnexus.mri.modules.Module;

import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Experimental
public final class TypeRegistry {
    TypeRegistry() {}

    private static final Map<String, Class<? extends DataStore<?>>> dataStoreTypes =
            new ConcurrentHashMap<>();
    private static final Map<String, Class<? extends Module<?>>> moduleTypes =
            new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> miscTypes = new ConcurrentHashMap<>();

    public void registerDataStoreType(String type, Class<? extends DataStore<?>> clazz) {
        dataStoreTypes.put(type, clazz);
    }

    public Class<? extends DataStore<?>> getDataStoreType(String type) {
        return dataStoreTypes.get(type);
    }

    public void registerModuleType(String type, Class<? extends Module<?>> clazz) {
        moduleTypes.put(type, clazz);
    }

    public Class<? extends Module<?>> getModuleType(String type) {
        return moduleTypes.get(type);
    }

    public void registerMiscType(String type, Class<?> clazz) {
        miscTypes.put(type, clazz);
    }

    public Class<?> getMiscType(String type) {
        return miscTypes.get(type);
    }
}
