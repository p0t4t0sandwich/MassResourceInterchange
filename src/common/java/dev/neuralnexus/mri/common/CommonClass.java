/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common;

import dev.neuralnexus.mri.common.config.MRIConfig;
import dev.neuralnexus.mri.common.config.MRIConfigLoader;
import dev.neuralnexus.mri.common.datastore.DataStore;

public class CommonClass {
    public static void init() {
        MRIConfigLoader.load();

        MRIConfig config = MRIConfigLoader.config();

        for (DataStore store : config.datastores()) {
            // TODO: Create some runnable abstraction to use MC's scheduler
            store.connect();
        }
    }
}
