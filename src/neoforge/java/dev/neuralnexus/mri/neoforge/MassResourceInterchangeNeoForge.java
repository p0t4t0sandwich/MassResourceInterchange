/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import dev.neuralnexus.mri.CommonClass;
import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.MRIAPI;
import dev.neuralnexus.mri.config.MRIConfigLoader;
import dev.neuralnexus.mri.modules.BackpackModule;
import dev.neuralnexus.mri.modules.CrateModule;
import dev.neuralnexus.mri.modules.Module;
import dev.neuralnexus.mri.modules.PlayerSyncModule;
import dev.neuralnexus.mri.neoforge.backpack.BackpackHandler;
import dev.neuralnexus.mri.neoforge.events.RegisterTypesEvent;
import dev.neuralnexus.mri.neoforge.playersync.PlayerSyncHandler;
import dev.neuralnexus.mri.neoforge.playersync.PlayerSyncUtils;
import dev.neuralnexus.mri.neoforge.wip.crate.CrateHandler;

import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

// TODO: Figure out client-side world-loading/exiting and load/reload datastores based on that
@Mod(value = Constants.MOD_ID, dist = Dist.DEDICATED_SERVER)
public class MassResourceInterchangeNeoForge {

    @SuppressWarnings("Convert2MethodRef")
    public MassResourceInterchangeNeoForge(IEventBus eventBus, ModContainer container) {
        CommonClass.scheduler().replaceBackgroundScheduler(() -> Util.backgroundExecutor(), false);

        eventBus.<FMLCommonSetupEvent>addListener(
                EventPriority.HIGHEST, event -> eventBus.post(new RegisterTypesEvent(container)));
        eventBus.<RegisterTypesEvent>addListener(
                EventPriority.HIGHEST, event -> CommonClass.registerTypes(event.registry()));
        eventBus.<RegisterTypesEvent>addListener(
                EventPriority.LOWEST,
                event -> {
                    MRIConfigLoader.load();

                    MRIAPI api = MRIAPI.getInstance();
                    api.getModule(BackpackModule.class)
                            .filter(Module::enabled)
                            .ifPresent(
                                    module -> NeoForge.EVENT_BUS.register(new BackpackHandler()));
                    api.getModule(CrateModule.class)
                            .filter(Module::enabled)
                            .ifPresent(module -> NeoForge.EVENT_BUS.register(new CrateHandler()));
                    api.getModule(PlayerSyncModule.class)
                            .filter(Module::enabled)
                            .ifPresent(
                                    module -> NeoForge.EVENT_BUS.register(new PlayerSyncHandler()));
                });

        NeoForge.EVENT_BUS.<ServerStartingEvent>addListener(
                event -> {
                    MinecraftServer server = event.getServer();
                    CommonClass.worldFolder =
                            server.getServerDirectory()
                                    .resolve(server.getWorldPath(LevelResource.ROOT));
                    CommonClass.playerDataFolder =
                            server.getServerDirectory()
                                    .resolve(server.getWorldPath(LevelResource.PLAYER_DATA_DIR));
                    CommonClass.starting();

                    MRIAPI.getInstance()
                            .getModule(PlayerSyncModule.class)
                            .filter(Module::enabled)
                            .ifPresent(module -> PlayerSyncUtils.init());
                });

        NeoForge.EVENT_BUS.<ServerStoppingEvent>addListener(
                EventPriority.HIGHEST,
                event -> {
                    CommonClass.shutdown();
                });
    }
}
