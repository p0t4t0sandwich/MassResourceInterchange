/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.config.serializers;

import dev.neuralnexus.mri.TypeRegistry;
import dev.neuralnexus.mri.config.MRIConfigLoader;
import dev.neuralnexus.mri.modules.Module;

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
public final class ModuleSerializer implements TypeSerializer<Module> {
    public static final ModuleSerializer INSTANCE = new ModuleSerializer();

    private static final String NAME = "name";
    private static final String ENABLED = "enabled";
    private static final String DATASTORE = "datastore";
    private static final String CONFIG = "config";

    private ModuleSerializer() {}

    private ConfigurationNode nonVirtualNode(final ConfigurationNode source, final Object... path)
            throws SerializationException {
        if (!source.hasChild(path)) {
            throw new SerializationException(
                    "Required field " + Arrays.toString(path) + " was not present in node");
        }
        return source.node(path);
    }

    @Override
    public Module<?> deserialize(final @NotNull Type type, final @NotNull ConfigurationNode source)
            throws SerializationException {
        final String nameStr = nonVirtualNode(source, NAME).getString();
        final boolean enabled = nonVirtualNode(source, ENABLED).getBoolean(false);
        final String dataStoreStr = nonVirtualNode(source, DATASTORE).getString();
        if (dataStoreStr == null || nameStr == null) {
            throw new SerializationException("Missing required fields, or they are not strings");
        }

        Class<? extends Module<?>> typeClass = TypeRegistry.getInstance().getModuleType(nameStr);
        if (typeClass == null) {
            throw new SerializationException("Unknown module: " + nameStr);
        }
        Class<?> configClass = TypeRegistry.getInstance().getMiscType(nameStr);
        if (configClass == null) {
            throw new SerializationException(
                    "No config found for module: " + nameStr);
        }

        Constructor<? extends Module<?>> constructor;
        try {
            constructor = typeClass.getConstructor(Boolean.class, String.class, configClass);
        } catch (NoSuchMethodException e) {
            throw new SerializationException(
                    "Type " + nameStr + " does not have a valid constructor for deserialization");
        }
        try {
            return constructor.newInstance(enabled, dataStoreStr, source.node(CONFIG).get(configClass));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void serialize(
            final @NotNull Type type,
            final @Nullable Module module,
            final @NotNull ConfigurationNode target)
            throws SerializationException {
        if (module == null) {
            target.raw(null);
            return;
        }
        CommentedConfigurationNode nameNode =
                CommentedConfigurationNode.root()
                        .comment("The name of the module")
                        .set(module.name());
        target.node(NAME).set(nameNode);

        CommentedConfigurationNode enabledNode =
                CommentedConfigurationNode.root()
                        .comment("Whether the module is enabled or not")
                        .set(module.enabled());
        target.node(ENABLED).set(enabledNode);

        CommentedConfigurationNode dataStoreNode =
                CommentedConfigurationNode.root()
                        .comment(
                                "The module's datastore, can be a built-in datastore or a custom one registered by another mod/plugin")
                        .set(module.datastore());
        target.node(DATASTORE).set(dataStoreNode);

        CommentedConfigurationNode configNode =
                CommentedConfigurationNode.root()
                        .comment(
                                "The configuration for this module, used to store connection details and other settings")
                        .set(module.config());
        target.node(CONFIG).set(configNode);
    }
}
