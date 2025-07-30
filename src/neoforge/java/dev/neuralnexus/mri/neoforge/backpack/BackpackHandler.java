/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge.backpack;

import static dev.neuralnexus.mri.neoforge.Utils.hasPermission;

import static net.minecraft.network.chat.Component.literal;

import dev.neuralnexus.mri.CommonClass;
import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.MRIAPI;
import dev.neuralnexus.mri.modules.BackpackModule;

import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.UUID;

public class BackpackHandler {
    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event) {
        BackpackCommand.onRegisterCommand(event);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        BackpackModule module = MRIAPI.getInstance().backpack();
        BackpackModule.Config config = module.config();
        if (config.giveBackpacksByDefault) {
            CommonClass.scheduler()
                    .runAsync(
                            () -> {
                                String playerName = player.getName().getString();
                                UUID playerId = player.getUUID();
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

                                        player.addItem(
                                                BackpackUtils.createBackpackItem(
                                                        backpackId, player));
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

    // TODO: Consider adding backpacks that are not owned by players
    // Sounds like a problem for future me when I want to go through server-side custom recipe hell
    // Maybe a setting where players can remove items from another player's backpack, but not add
    // items to it?
    private void onPlayerInteract(PlayerInteractEvent event, ICancellableEvent cancellable) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        BackpackModule module = MRIAPI.getInstance().backpack();
        BackpackModule.Config config = module.config();

        if (!config.allowBackpackItem
                || !hasPermission(player, "mri.backpack.open", Commands.LEVEL_GAMEMASTERS)) {
            return;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        ItemStack backPackItem;
        if (mainHandItem.is(Items.PLAYER_HEAD) && mainHandItem.has(DataComponents.CUSTOM_DATA)) {
            backPackItem = mainHandItem;
        } else if (offHandItem.is(Items.PLAYER_HEAD)
                && offHandItem.has(DataComponents.CUSTOM_DATA)) {
            backPackItem = offHandItem;
        } else {
            return;
        }

        CustomData customData = backPackItem.get(DataComponents.CUSTOM_DATA);
        if (customData == null || !customData.contains(Backpack.CUSTOM_DATA_ID)) {
            return;
        }

        // They're most likely opening a backpack, cancel the interaction
        cancellable.setCanceled(true);

        UUID backpackId;
        String strUuid = customData.copyTag().getString(Backpack.CUSTOM_DATA_ID);
        try {
            backpackId = UUID.fromString(strUuid);
        } catch (IllegalArgumentException e) {
            Constants.logger().error("Invalid UUID format for backpack ID: {}", strUuid);
            return;
        }

        CommonClass.scheduler()
                .runAsync(
                        () -> {
                            Optional<BackpackModule.BackpackInfo> info =
                                    module.getBackpackInfo(player.getUUID());
                            if (info.isEmpty() || !info.get().id().equals(backpackId)) {
                                // Player is opening someone else's backpack
                                if (hasPermission(
                                        player,
                                        "mri.backpack.open.others",
                                        Commands.LEVEL_GAMEMASTERS)) {
                                    player.sendSystemMessage(
                                            literal(
                                                    "You are opening another player's backpack. This action may be logged."));
                                    Constants.logger()
                                            .info(
                                                    "Player {} opened another player's backpack: {}",
                                                    player.getName().getString(),
                                                    backpackId);
                                } else {
                                    player.sendSystemMessage(
                                            literal("You do not own this backpack!"));
                                    Constants.logger()
                                            .warn(
                                                    "Player {} tried to open a backpack they do not own: {}",
                                                    player.getName().getString(),
                                                    backpackId);
                                }
                            }

                            BackpackUtils.openBackpack(
                                    backpackId, player, player.createCommandSourceStack());
                        });
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        this.onPlayerInteract(event, event);
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        this.onPlayerInteract(event, event);
    }
}
