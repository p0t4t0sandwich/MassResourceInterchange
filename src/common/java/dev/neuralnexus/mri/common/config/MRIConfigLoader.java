/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common.config;

import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.common.config.transformations.ConfigTransform;
import dev.neuralnexus.mri.common.config.versions.MRIConfig_V1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    /** Load the configuration from the file. */
    public static void load() {
        loader = HoconConfigurationLoader.builder().path(configPath).build();
        CommentedConfigurationNode node = null;
        try {
            node = loader.load();
        } catch (ConfigurateException e) {
            logger.error("An error occurred while loading the configuration: " + e.getMessage());
            if (e.getCause() != null) {
                logger.error("Caused by: ", e.getCause());
            }
        }
        if (node == null) {
            return;
        }

        try {
            ConfigTransform.updateNode(node);
        } catch (ConfigurateException e) {
            logger.error("An error occurred while updating the configuration: " + e.getMessage());
            if (e.getCause() != null) {
                logger.error("Caused by: ", e.getCause());
            }
        }

        try {
            config = node.get(MRIConfig_V1.class);
        } catch (SerializationException e) {
            logger.error(
                    "An error occurred while loading the modules configuration: "
                            + e.getMessage());
            if (e.getCause() != null) {
                logger.error("Caused by: ", e.getCause());
            }
        }

        try {
            loader.save(node);
        } catch (ConfigurateException e) {
            logger.error("An error occurred while saving this configuration: " + e.getMessage());
            if (e.getCause() != null) {
                logger.error("Caused by: ", e.getCause());
            }
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
            logger.error("An error occurred while loading the configuration: " + e.getMessage());
            if (e.getCause() != null) {
                logger.error("Caused by: ", e.getCause());
            }
        }
        if (node == null) {
            return;
        }

        try {
            node.set(MRIConfig_V1.class, config);
        } catch (SerializationException e) {
            logger.error("An error occurred while updating the configuration: " + e.getMessage());
            if (e.getCause() != null) {
                logger.error("Caused by: ", e.getCause());
            }
        }

        try {
            loader.save(node);
        } catch (ConfigurateException e) {
            logger.error("An error occurred while saving this configuration: " + e.getMessage());
            if (e.getCause() != null) {
                logger.error("Caused by: ", e.getCause());
            }
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
