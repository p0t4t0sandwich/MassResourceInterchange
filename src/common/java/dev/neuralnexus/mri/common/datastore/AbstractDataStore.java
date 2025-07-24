/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common.datastore;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;

@ConfigSerializable
public abstract class AbstractDataStore implements DataStore {
    @Comment("The name of the datastore, used when referencing it in other configs")
    @Required
    private String name;

    @Comment(
            "The datastore's type, can be a built-in type or a custom one registered by another mod/plugin")
    @Required
    private String type;

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String type() {
        return this.type;
    }
}
