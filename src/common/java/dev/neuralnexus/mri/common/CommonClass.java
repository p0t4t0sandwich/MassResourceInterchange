/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.common;

import dev.neuralnexus.mri.common.config.MRIConfig;
import dev.neuralnexus.mri.common.config.MRIConfigLoader;
import dev.neuralnexus.mri.common.datastore.DataStore;
import dev.neuralnexus.mri.common.scheduler.Scheduler;
import dev.neuralnexus.mri.common.scheduler.SchedulerImpl;

public class CommonClass {
    private static final Scheduler scheduler = new SchedulerImpl();

    /** Get the scheduler */
    public static Scheduler scheduler() {
        return scheduler;
    }

    public static void init() {
        MRIConfigLoader.load();

        MRIConfig config = MRIConfigLoader.config();

        for (DataStore store : config.datastores()) {
            // TODO: Filter based on references in other parts of the config
            // No need to connect to a database if it's not being used
            CommonClass.scheduler().runAsync(store::connect);
        }
    }
}
