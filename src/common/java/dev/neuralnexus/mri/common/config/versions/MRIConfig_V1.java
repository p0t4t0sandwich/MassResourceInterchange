/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common.config.versions;

import dev.neuralnexus.mri.common.config.MRIConfig;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public final class MRIConfig_V1 implements MRIConfig {
    @Comment("Config version, DO NOT CHANGE THIS")
    @Required
    private int version = 1;

    @Comment("Enable or disable modules")
    private Map<String, Boolean> modules = new HashMap<>();

    {
        modules.put("Crates", false);
        modules.put("Backpacks", false);
        modules.put("PlayerSync", false);
    }

    @Override
    public int version() {
        return this.version;
    }

    @Override
    public Map<String, Boolean> modules() {
        return this.modules;
    }
}
