/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import dev.neuralnexus.mri.CommonClass;
import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.MRIAPI;
import dev.neuralnexus.mri.modules.BackpackModule;
import dev.neuralnexus.mri.modules.CrateModule;
import dev.neuralnexus.mri.modules.Module;
import dev.neuralnexus.mri.modules.PlayerSyncModule;
import dev.neuralnexus.mri.neoforge.events.RegisterTypesEvent;
import dev.neuralnexus.mri.neoforge.wip.backpack.BackpackCommand;
import dev.neuralnexus.mri.neoforge.wip.crate.CrateHandler;
import dev.neuralnexus.mri.neoforge.wip.playersync.InventorySync;

import net.minecraft.Util;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constants.MOD_ID)
public class MassResourceInterchangeNeoForge {

    @SuppressWarnings("Convert2MethodRef")
    public MassResourceInterchangeNeoForge(IEventBus eventBus, ModContainer container) {
        CommonClass.scheduler().replaceBackgroundScheduler(() -> Util.backgroundExecutor(), false);

        eventBus.<FMLCommonSetupEvent>addListener(
                EventPriority.HIGHEST, event -> eventBus.post(new RegisterTypesEvent(container)));
        eventBus.<RegisterTypesEvent>addListener(
                EventPriority.HIGHEST, event -> CommonClass.registerTypes(event.registry()));
        eventBus.addListener(
                EventPriority.LOWEST, MassResourceInterchangeNeoForge::afterRegisterTypes);
    }

    public static void afterRegisterTypes(RegisterTypesEvent event) {
        CommonClass.init();

        MRIAPI api = MRIAPI.getInstance();
        api.getModule(BackpackModule.class)
                .filter(Module::enabled)
                .ifPresent(module -> NeoForge.EVENT_BUS.register(new BackpackCommand()));
        api.getModule(CrateModule.class)
                .filter(Module::enabled)
                .ifPresent(module -> NeoForge.EVENT_BUS.register(new CrateHandler()));
        api.getModule(PlayerSyncModule.class)
                .filter(Module::enabled)
                .ifPresent(module -> NeoForge.EVENT_BUS.register(new InventorySync()));
    }
}
