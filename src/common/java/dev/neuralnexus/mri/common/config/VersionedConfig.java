/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common.config;

import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;

@ConfigSerializable
public class VersionedConfig {
    @Comment("Config version, DO NOT CHANGE THIS")
    @Required
    private int version;

    public int version() {
        return this.version;
    }

    /**
     * Attempt to get the version of the configuration.
     *
     * @param node The configuration node to get the version from.
     * @param logger The logger to log errors to.
     * @return The version of the configuration, or 0 if it could not be loaded.
     */
    public static int tryGetVersion(ConfigurationNode node, Logger logger) {
        try {
            VersionedConfig vConfig = node.get(VersionedConfig.class);
            if (vConfig != null) {
                return vConfig.version();
            } else {
                logger.error("Failed to load the configuration version");
            }
        } catch (Exception e) {
            logger.error(
                    "An error occurred while loading the configuration version: " + e.getMessage());
            if (e.getCause() != null) {
                logger.error("Caused by: ", e.getCause());
            }
        }
        return 0;
    }
}
