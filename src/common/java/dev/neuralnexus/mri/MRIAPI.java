/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri;

import dev.neuralnexus.mri.config.MRIConfigLoader;
import dev.neuralnexus.mri.modules.Module;

import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

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
     * Get a module by its name.
     *
     * @param name The name of the module.
     * @return The module, or null if not found.
     */
    public Optional<Module<?>> getModuleByName(String name) {
        return MRIConfigLoader.config().modules().stream()
                .filter(module -> module.name().equalsIgnoreCase(name))
                .findFirst();
    }
}
