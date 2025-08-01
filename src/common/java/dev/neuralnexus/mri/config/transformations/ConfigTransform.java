/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.config.transformations;

import static org.spongepowered.configurate.NodePath.path;

import dev.neuralnexus.mri.config.MRIConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

public final class ConfigTransform {
    private static final Logger logger = LoggerFactory.getLogger("MRI-ConfigTransform");

    private static final int VERSION_LATEST = 1;

    private ConfigTransform() {}

    public static ConfigurationTransformation.Versioned create() {
        return ConfigurationTransformation.versionedBuilder()
                .addVersion(VERSION_LATEST, initialTransform())
                .build();
    }

    public static ConfigurationTransformation initialTransform() {
        return ConfigurationTransformation.builder()
                .addAction(
                        path(),
                        (path, value) -> {
                            value.set(MRIConfig.class, new MRIConfig());
                            return null;
                        })
                .build();
    }

    public static <N extends ConfigurationNode> N updateNode(final N node)
            throws ConfigurateException {
        if (!node.virtual()) { // we only want to migrate existing data
            final ConfigurationTransformation.Versioned trans = create();
            final int startVersion = trans.version(node);
            trans.apply(node);
            final int endVersion = trans.version(node);
            if (startVersion != endVersion) { // we might not have made any changes
                logger.info("Updated config schema from {} to {}", startVersion, endVersion);
            }
        }
        return node;
    }
}
