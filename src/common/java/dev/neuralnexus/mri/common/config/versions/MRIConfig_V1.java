/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common.config.versions;

import dev.neuralnexus.mri.common.config.MRIConfig;
import dev.neuralnexus.mri.common.datastore.DataStore;
import dev.neuralnexus.mri.common.datastore.MySQLStore;
import dev.neuralnexus.mri.common.datastore.PostgreSQLStore;
import dev.neuralnexus.mri.common.datastore.SQLiteStore;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public final class MRIConfig_V1 implements MRIConfig {
    @Comment("Config version, DO NOT CHANGE THIS")
    @Required
    private int version = 1;

    @Comment("Enable or disable modules")
    @Required
    private Map<String, Boolean> modules = new HashMap<>();

    {
        this.modules.put("Backpacks", false);
        this.modules.put("Crates", false);
        this.modules.put("PlayerSync", false);
    }

    @Comment("List of configured datastores")
    @Required
    private List<DataStore<?>> datastores = new ArrayList<>();

    {
        this.datastores.add(new MySQLStore());
        this.datastores.add(new PostgreSQLStore());
        this.datastores.add(new SQLiteStore());
    }

    @Override
    public int version() {
        return this.version;
    }

    @Override
    public Map<String, Boolean> modules() {
        return this.modules;
    }

    @Override
    public List<DataStore<?>> datastores() {
        return this.datastores;
    }
}
