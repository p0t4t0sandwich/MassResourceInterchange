/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import dev.neuralnexus.mri.CommonClass;
import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.config.MRIConfig;
import dev.neuralnexus.mri.config.MRIConfigLoader;
import dev.neuralnexus.mri.neoforge.events.RegisterTypesEvent;
import dev.neuralnexus.mri.neoforge.wip.backpack.BackpackCommand;
import dev.neuralnexus.mri.neoforge.wip.crate.CrateHandler;

import dev.neuralnexus.mri.neoforge.wip.playersync.InventorySync;
import net.minecraft.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constants.MOD_ID)
public class MassResourceInterchangeNeoForge {

    @SuppressWarnings("Convert2MethodRef")
    public MassResourceInterchangeNeoForge(IEventBus eventBus) {
        CommonClass.scheduler().replaceBackgroundScheduler(() -> Util.backgroundExecutor(), false);

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onCommonInit(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.post(new RegisterTypesEvent());

        CommonClass.init();

        MRIConfig config = MRIConfigLoader.config();

        config.getModuleByName("backpack").ifPresent( module ->
                NeoForge.EVENT_BUS.register(new BackpackCommand()));
        config.getModuleByName("crate").ifPresent(module ->
                NeoForge.EVENT_BUS.register(new CrateHandler()));
        config.getModuleByName("playersync").ifPresent(module ->
                NeoForge.EVENT_BUS.register(new InventorySync()));
    }

    @SubscribeEvent
    public void onRegisterTypes(RegisterTypesEvent event) {
        CommonClass.registerTypes(event.registry());
    }
}
