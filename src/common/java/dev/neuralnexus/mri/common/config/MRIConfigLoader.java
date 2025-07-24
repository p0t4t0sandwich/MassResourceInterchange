/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common.config;

import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.common.config.serializers.DataStoreSerializer;
import dev.neuralnexus.mri.common.config.transformations.ConfigTransform;
import dev.neuralnexus.mri.common.config.versions.MRIConfig_V1;
import dev.neuralnexus.mri.common.datastore.DataStore;
import dev.neuralnexus.mri.common.datastore.MySQLStore;
import dev.neuralnexus.mri.common.datastore.PostgreSQLStore;
import dev.neuralnexus.mri.common.datastore.SQLiteStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/** A class for loading MassResourceInterchange configs. */
public class MRIConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger("MRIConfigLoader");
    private static final Path configPath =
            Paths.get(
                    "config"
                            + File.separator
                            + Constants.MOD_ID
                            + File.separator
                            + Constants.MOD_ID
                            + ".conf");
    private static HoconConfigurationLoader loader;
    private static MRIConfig config;
    private static final Map<String, Class<? extends DataStore>> typeRegistry = new HashMap<>();

    static {
        registerType("mysql", MySQLStore.class);
        registerType("mariadb", MySQLStore.class); // TODO: Update impl at some point?
        registerType("postgres", PostgreSQLStore.class);
        registerType("sqlite", SQLiteStore.class);
    }

    private static void logError(String verb, Throwable t) {
        logger.error("An error occurred while {} the configuration: {}", verb, t.getMessage());
        if (t.getCause() != null) {
            logger.error("Caused by: ", t.getCause());
        }
    }

    public static void registerType(String type, Class<? extends DataStore> clazz) {
        typeRegistry.put(type, clazz);
    }

    public static Class<? extends DataStore> getType(String type) {
        return typeRegistry.get(type);
    }

    /** Load the configuration from the file. */
    public static void load() {
        loader =
                HoconConfigurationLoader.builder()
                        .path(configPath)
                        .defaultOptions(
                                opts ->
                                        opts.serializers(
                                                build ->
                                                        build.register(
                                                                DataStore.class,
                                                                DataStoreSerializer.INSTANCE)))
                        .build();
        CommentedConfigurationNode node = null;
        try {
            node = loader.load();
        } catch (ConfigurateException e) {
            logError("loading", e);
        }
        if (node == null) {
            return;
        }

        try {
            ConfigTransform.updateNode(node);
        } catch (ConfigurateException e) {
            logError("updating", e);
        }

        try {
            config = node.get(MRIConfig_V1.class);
        } catch (SerializationException e) {
            logError("deserializing", e);
        }

        try {
            loader.save(node);
        } catch (ConfigurateException e) {
            logError("saving", e);
        }
    }

    /** Unload the configuration. */
    public static void unload() {
        config = null;
    }

    /** Save the configuration to the file. */
    public static void save() {
        if (config == null) {
            return;
        }
        if (loader == null) {
            return;
        }
        CommentedConfigurationNode node = null;
        try {
            node = loader.load();
        } catch (ConfigurateException e) {
            logError("loading", e);
        }
        if (node == null) {
            return;
        }

        try {
            node.set(MRIConfig_V1.class, config);
        } catch (SerializationException e) {
            logError("serializing", e);
        }

        try {
            loader.save(node);
        } catch (ConfigurateException e) {
            logError("saving", e);
        }
    }

    /**
     * Get the loaded configuration.
     *
     * @return The loaded configuration.
     */
    public static MRIConfig config() {
        if (config == null) {
            load();
        }
        return config;
    }
}
