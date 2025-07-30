/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge.wip.backpack;

import dev.neuralnexus.mri.CommonClass;
import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.MRIAPI;
import dev.neuralnexus.mri.modules.BackpackModule;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Optional;
import java.util.UUID;

public class BackpackHandler {
    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event) {
        BackpackCommand.onRegisterCommand(event);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        BackpackModule module = MRIAPI.getInstance().backpack();
        BackpackModule.Config config = module.config();
        if (config.giveBackpacksByDefault) {
            CommonClass.scheduler()
                    .runAsync(
                            () -> {
                                String playerName = event.getEntity().getName().getString();
                                UUID playerId = event.getEntity().getUUID();
                                Optional<BackpackModule.BackpackInfo> info =
                                        module.getBackpackInfo(playerId);
                                if (info.isEmpty()) {
                                    UUID backpackId = UUID.randomUUID();
                                    int size = config.defaultBackpackSize;
                                    if (Backpack.createBackpack(backpackId, size)
                                            && module.createBackpack(playerId, backpackId, size)) {
                                        Constants.logger()
                                                .info(
                                                        "Created default backpack for player: {}",
                                                        playerName);
                                    } else {
                                        Constants.logger()
                                                .error(
                                                        "Failed to create default backpack for player: {}",
                                                        playerName);
                                    }
                                }
                            });
        }
    }
}
