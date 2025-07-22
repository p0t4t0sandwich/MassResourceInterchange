/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import com.mojang.logging.LogUtils;

import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.common.CommonClass;
import dev.neuralnexus.mri.neoforge.wip.backpack.BackpackCommand;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

import org.slf4j.Logger;

@Mod(Constants.MOD_ID)
public class MassResourceInterchangeNeoForge {
    public static final Logger LOGGER = LogUtils.getLogger();

    public MassResourceInterchangeNeoForge(IEventBus eventBus) {
        CommonClass.init();

        // NeoForge.EVENT_BUS.register(new InventorySync());
        NeoForge.EVENT_BUS.register(new BackpackCommand());
        NeoForge.EVENT_BUS.register(new CrateHandler());
    }
}
