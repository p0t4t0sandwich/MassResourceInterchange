/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.common.CommonClass;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Constants.MOD_ID)
public class MassResourceInterchangeNeoForge {
    public MassResourceInterchangeNeoForge(IEventBus eventBus) {
        CommonClass.init();

        NeoForge.EVENT_BUS.<RegisterCommandsEvent>addListener(
                event -> BackpackCommand.registerCommand(event.getDispatcher()));

        //        NeoForge.EVENT_BUS.register(new InventorySync());
        NeoForge.EVENT_BUS.register(new CrateHandler());
    }
}
