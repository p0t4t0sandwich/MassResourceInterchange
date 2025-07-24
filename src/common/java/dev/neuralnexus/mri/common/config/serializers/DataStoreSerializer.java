/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common.config.serializers;

import dev.neuralnexus.mri.common.config.MRIConfigLoader;
import dev.neuralnexus.mri.common.datastore.DataStore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;

public final class DataStoreSerializer implements TypeSerializer<DataStore> {
    public static final DataStoreSerializer INSTANCE = new DataStoreSerializer();

    private static final String NAME = "name";
    private static final String TYPE = "type";

    private DataStoreSerializer() {}

    private ConfigurationNode nonVirtualNode(final ConfigurationNode source, final Object... path)
            throws SerializationException {
        if (!source.hasChild(path)) {
            throw new SerializationException(
                    "Required field " + Arrays.toString(path) + " was not present in node");
        }
        return source.node(path);
    }

    @Override
    public DataStore deserialize(final @NotNull Type type, final @NotNull ConfigurationNode source)
            throws SerializationException {
        final String typeStr = nonVirtualNode(source, TYPE).getString();
        final String nameStr = nonVirtualNode(source, NAME).getString();
        if (typeStr == null || nameStr == null) {
            throw new SerializationException("Missing required fields, or they are not strings");
        }

        Class<? extends DataStore> typeClass = MRIConfigLoader.getType(typeStr);
        if (typeClass == null) {
            throw new SerializationException("Unknown datastore type: " + typeStr);
        }
        return source.get(typeClass);
    }

    @Override
    public void serialize(
            final @NotNull Type type,
            final @Nullable DataStore ds,
            final @NotNull ConfigurationNode target)
            throws SerializationException {
        if (ds == null) {
            target.raw(null);
            return;
        }

        Class<? extends DataStore> typeClass = MRIConfigLoader.getType(ds.type());
        if (typeClass == null) {
            throw new SerializationException("Unknown datastore type: " + ds.type());
        }
        target.set(typeClass, ds);
    }
}
