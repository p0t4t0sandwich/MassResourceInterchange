/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri;

import dev.neuralnexus.mri.config.MRIConfig;
import dev.neuralnexus.mri.config.MRIConfigLoader;
import dev.neuralnexus.mri.datastores.MySQLStore;
import dev.neuralnexus.mri.datastores.PostgreSQLStore;
import dev.neuralnexus.mri.datastores.SQLiteStore;
import dev.neuralnexus.mri.modules.BackpackModule;
import dev.neuralnexus.mri.modules.CrateModule;
import dev.neuralnexus.mri.modules.Module;
import dev.neuralnexus.mri.modules.PlayerSyncModule;
import dev.neuralnexus.mri.scheduler.Scheduler;
import dev.neuralnexus.mri.scheduler.SchedulerImpl;

import java.nio.file.Path;
import java.util.stream.Collectors;

public final class CommonClass {
    private static final Scheduler scheduler = new SchedulerImpl();
    public static Path worldFolder;

    /** Get the scheduler */
    public static Scheduler scheduler() {
        return scheduler;
    }

    public static void registerTypes(TypeRegistry registry) {
        registry.registerDataStoreType("mysql", MySQLStore.class);
        registry.registerDataStoreType(
                "mariadb", MySQLStore.class); // TODO: Update impl at some point?
        registry.registerDataStoreType("postgresql", PostgreSQLStore.class);
        registry.registerDataStoreType("sqlite", SQLiteStore.class);

        registry.registerMiscType("mysql", MySQLStore.Config.class);
        registry.registerMiscType("mariadb", MySQLStore.Config.class);
        registry.registerMiscType("postgresql", PostgreSQLStore.Config.class);
        registry.registerMiscType("sqlite", SQLiteStore.Config.class);

        registry.registerModuleType("backpack", BackpackModule.class);
        registry.registerModuleType("crate", CrateModule.class);
        registry.registerModuleType("playersync", PlayerSyncModule.class);

        registry.registerMiscType("backpack", BackpackModule.Config.class);
        registry.registerMiscType("crate", CrateModule.Config.class);
        registry.registerMiscType("playersync", PlayerSyncModule.Config.class);
    }

    public static void starting() {
        MRIConfig config = MRIConfigLoader.config();
        config.modules().stream()
                .filter(Module::enabled)
                .map(Module::datastore)
                .collect(Collectors.toSet())
                .forEach(ds -> CommonClass.scheduler().runAsync(ds::connect));
    }
}
