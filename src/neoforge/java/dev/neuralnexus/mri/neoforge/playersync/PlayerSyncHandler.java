/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge.playersync;

import static dev.neuralnexus.mri.neoforge.playersync.PlayerSyncUtils.frozenPlayers;
import static dev.neuralnexus.mri.neoforge.playersync.PlayerSyncUtils.loadPlayerInventory;
import static dev.neuralnexus.mri.neoforge.playersync.PlayerSyncUtils.savePlayerInventory;
import static dev.neuralnexus.mri.neoforge.playersync.PlayerSyncUtils.snapshotPlayerInventory;

import static net.minecraft.network.chat.Component.literal;

import dev.neuralnexus.mri.CommonClass;
import dev.neuralnexus.mri.MRIAPI;
import dev.neuralnexus.mri.datastores.DataStore;
import dev.neuralnexus.mri.modules.PlayerSyncModule;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.UUID;

public class PlayerSyncHandler {
    private final int autoSaveInterval;
    private int autoSaveTicker = 0;

    public PlayerSyncHandler() {
        this.autoSaveInterval = MRIAPI.getInstance().playerSync().config().autoSaveInterval;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!PlayerSyncModule.INITIALIZED) {
            player.connection.disconnect(
                    literal("DataStore connection not established, please reconnect in a moment."));
        }

        snapshotPlayerInventory(player);
        player.getInventory().clearContent();

        frozenPlayers.put(player.getUUID(), player.blockPosition());
        CommonClass.scheduler().runAsync(loadPlayerInventory(player));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        CommonClass.scheduler()
                .runAsync(
                        () -> {
                            savePlayerInventory(event.getEntity()).run();
                            PlayerSyncModule module = MRIAPI.getInstance().playerSync();
                            DataStore<?> dataStore = module.datastore();
                            UUID id = module.getOrCreateId(event.getEntity().getUUID());
                            dataStore.unlock(id);
                        });
    }

    @SubscribeEvent
    public void saveTimer(ServerTickEvent.Pre event) {
        if (autoSaveInterval <= 0) {
            return;
        }
        if (autoSaveTicker / 20 >= autoSaveInterval) {
            autoSaveTicker = 0;
            for (Player player : event.getServer().getPlayerList().getPlayers()) {
                // TODO: Consider snapshotting the inventory before saving
                CommonClass.scheduler().runAsync(savePlayerInventory(player));
            }
        } else {
            autoSaveTicker++;
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Cancel an event if the player is "frozen"
     *
     * @param player the player
     * @param event the event
     */
    public void cancelPlayerAction(Player player, ICancellableEvent event) {
        if (frozenPlayers.containsKey(player.getUUID())) {
            event.setCanceled(true);
        }
    }

    /**
     * "Cancel" (undo) an uncancellable player action
     *
     * @param player the player
     * @param lambda the lambda to run
     */
    public void cancelPlayerAction(Player player, Runnable lambda) {
        if (frozenPlayers.containsKey(player.getUUID())) {
            lambda.run();
        }
    }

    /**
     * Effectively cancelling an "OnPlayerMove" event
     *
     * @param event the event
     */
    @SubscribeEvent
    public void cancelPlayerAction(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        this.cancelPlayerAction(
                player,
                () -> {
                    BlockPos pos = frozenPlayers.get(player.getUUID());
                    if (!player.blockPosition().equals(pos)) {
                        player.teleportTo(pos.getX(), pos.getY(), pos.getZ());
                    }
                });
    }

    /**
     * Cancelling all cancellable player interact events
     *
     * @param event the event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelPlayerAction(PlayerInteractEvent.EntityInteract event) {
        this.cancelPlayerAction(event.getEntity(), event);
    }

    /**
     * Cancelling all cancellable player interact events
     *
     * @param event the event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelPlayerAction(PlayerInteractEvent.EntityInteractSpecific event) {
        this.cancelPlayerAction(event.getEntity(), event);
    }

    /**
     * Cancelling all cancellable player interact events
     *
     * @param event the event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelPlayerAction(PlayerInteractEvent.LeftClickBlock event) {
        this.cancelPlayerAction(event.getEntity(), event);
    }

    /**
     * Cancelling all cancellable player interact events
     *
     * @param event the event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelPlayerAction(PlayerInteractEvent.RightClickBlock event) {
        this.cancelPlayerAction(event.getEntity(), event);
    }

    /**
     * Cancelling all cancellable player interact events
     *
     * @param event the event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelPlayerAction(PlayerInteractEvent.RightClickItem event) {
        this.cancelPlayerAction(event.getEntity(), event);
    }

    /**
     * Prevents the player from opening containers while loading
     *
     * @param event the event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelPlayerAction(PlayerContainerEvent.Open event) {
        this.cancelPlayerAction(event.getEntity(), () -> event.getEntity().closeContainer());
    }

    /**
     * Cancels a player dealing or receiving damage
     *
     * @param event the event
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void cancelPlayerAction(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player) {
            this.cancelPlayerAction(player, () -> event.setNewDamage(0.0f));
        } else if (event.getSource().getEntity() instanceof Player player) {
            this.cancelPlayerAction(player, () -> event.setNewDamage(0.0f));
        }
    }

    /**
     * Don't want the player to receive knockback (might be redundant, but better to be safe than
     * sorry with modded)
     *
     * @param event the event
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void cancelPlayerAction(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player player) {
            this.cancelPlayerAction(player, event);
        }
    }

    /**
     * Don't want the player dying while their inventory is loading
     *
     * @param event the event
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void cancelPlayerAction(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            this.cancelPlayerAction(player, event);
        }
    }
}
