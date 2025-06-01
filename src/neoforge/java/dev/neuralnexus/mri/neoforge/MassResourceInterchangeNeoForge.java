/**
 * Copyright (c) 2025 p0t4t0sandwich - person@example.com
 * The project is Licensed under <a href="https://github.com/Example/TestMod/blob/dev/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.common.CommonClass;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class MassResourceInterchangeNeoForge {
    public MassResourceInterchangeNeoForge(IEventBus eventBus) {
        Constants.logger().info("Hello NeoForge world!");
        CommonClass.init();
    }
}
