/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.config.serializers;

import dev.neuralnexus.mri.MRIAPI;
import dev.neuralnexus.mri.datastores.DataStore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Arrays;

@SuppressWarnings("rawtypes")
public final class DataStoreSerializer implements TypeSerializer<DataStore> {
    public static final DataStoreSerializer INSTANCE = new DataStoreSerializer();

    private static final String NAME = "name";
    private static final String TYPE = "datastore";
    private static final String CONFIG = "config";

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
    public DataStore<?> deserialize(
            final @NotNull Type type, final @NotNull ConfigurationNode source)
            throws SerializationException {
        final String typeStr = nonVirtualNode(source, TYPE).getString();
        final String nameStr = nonVirtualNode(source, NAME).getString();
        if (typeStr == null || nameStr == null) {
            throw new SerializationException("Missing required fields, or they are not strings");
        }

        Class<? extends DataStore<?>> typeClass =
                MRIAPI.getInstance().typeRegistry().getDataStoreType(typeStr);
        if (typeClass == null) {
            throw new SerializationException("Unknown datastore: " + typeStr);
        }
        Class<?> configClass = MRIAPI.getInstance().typeRegistry().getMiscType(typeStr);
        if (configClass == null) {
            throw new SerializationException("No config found for datastore: " + typeStr);
        }

        Constructor<? extends DataStore<?>> constructor;
        try {
            constructor = typeClass.getConstructor(String.class, configClass);
        } catch (NoSuchMethodException e) {
            throw new SerializationException(
                    "Type " + typeStr + " does not have a valid constructor for deserialization");
        }
        try {
            return constructor.newInstance(nameStr, source.node(CONFIG).get(configClass));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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
        CommentedConfigurationNode nameNode =
                CommentedConfigurationNode.root()
                        .comment(
                                "The name of the datastore, used when referencing it in other configs")
                        .set(ds.name());
        target.node(NAME).set(nameNode);

        CommentedConfigurationNode typeNode =
                CommentedConfigurationNode.root()
                        .comment(
                                "The datastore's datastore, can be a built-in datastore or a custom one registered by another mod/plugin")
                        .set(ds.type());
        target.node(TYPE).set(typeNode);

        CommentedConfigurationNode configNode =
                CommentedConfigurationNode.root()
                        .comment(
                                "The configuration for this datastore, used to store connection details and other settings")
                        .set(ds.config());
        target.node(CONFIG).set(configNode);
    }
}
