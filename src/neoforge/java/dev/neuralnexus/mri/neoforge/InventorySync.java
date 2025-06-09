/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import dev.neuralnexus.mri.Constants;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class InventorySync {
    private static final Map<UUID, BlockPos> frozenPlayers = new ConcurrentHashMap<>();

    // TODO: Remove
    private static final Path testPath =
            Paths.get("config").resolve(Constants.MOD_NAME).resolve("inventory");

    private static final Path snapshotPath =
            Paths.get("world")
                    .resolve("playerdata")
                    .resolve(Constants.MOD_NAME)
                    .resolve("inv_snapshots");

    // Actually important logic
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        snapshotPlayerInventory(player);
        player.getInventory().clearContent();

        frozenPlayers.put(player.getUUID(), player.blockPosition());
        Util.backgroundExecutor().execute(loadPlayerInventory(player));
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        Util.backgroundExecutor().execute(savePlayerInventory(event.getEntity()));
    }

    /**
     * Take a snapshot of the player's inventory and save it to disk
     *
     * @param player the player whose inventory to snapshot
     */
    public static void snapshotPlayerInventory(Player player) {
        ListTag inventory = player.getInventory().save(new ListTag());
        if (inventory.isEmpty()) {
            return;
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Inventory", inventory);

        try {
            if (!Files.exists(snapshotPath)) {
                Files.createDirectories(snapshotPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to create inventory snapshot directory for player: "
                            + player.getName().getString(),
                    e);
        }

        try (Stream<Path> snapshots = Files.list(snapshotPath)) {
            List<File> sorted =
                    snapshots
                            .filter(
                                    path ->
                                            path.getFileName()
                                                    .toString()
                                                    .startsWith(player.getUUID().toString()))
                            .map(Path::toFile)
                            .sorted(Comparator.comparingLong(File::lastModified))
                            .toList();
            if (sorted.size() > 9) {
                for (int i = 0; i < sorted.size() - 9; i++) {
                    Files.deleteIfExists(sorted.get(i).toPath());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to clean up old inventory snapshots for player: "
                            + player.getName().getString(),
                    e);
        }

        try {
            long timestamp = System.currentTimeMillis();
            Path path = snapshotPath.resolve(player.getUUID() + "_" + timestamp + ".dat");
            NbtIo.write(tag, path);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to save inventory snapshot for player: " + player.getName().getString(),
                    e);
        }
    }

    /**
     * Save the player's inventory
     *
     * @param player the player whose inventory to save
     * @return a Runnable that saves the player's inventory
     */
    public static Runnable savePlayerInventory(Player player) {
        return () -> {
            ListTag inventory = player.getInventory().save(new ListTag());
            CompoundTag tag = new CompoundTag();
            tag.put("Inventory", inventory);

            save(player, tag);
        };
    }

    /**
     * Load the player's inventory in a separate thread
     *
     * @param player the player whose inventory to load
     * @return a Runnable that loads the player's inventory
     */
    public static Runnable loadPlayerInventory(Player player) {
        return () -> {
            // Some stuff
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            CompoundTag tag = load(player);
            ListTag inventory = tag.getList("Inventory", Tag.TAG_COMPOUND);
            player.getInventory().load(inventory);

            frozenPlayers.remove(player.getUUID());
        };
    }

    public static void save(Player player, CompoundTag tag) {
        try {
            if (!Files.exists(testPath)) {
                Files.createDirectories(testPath);
            }
            Path path = testPath.resolve(player.getUUID() + ".dat");
            NbtIo.write(tag, path);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to save inventory for player: " + player.getName().getString(), e);
        }
    }

    public static CompoundTag load(Player player) {
        try {
            Path path = testPath.resolve(player.getUUID() + ".dat");
            if (!path.toFile().exists()) {
                CompoundTag emptyTag = new CompoundTag();
                emptyTag.put("Inventory", new ListTag());
                save(player, emptyTag);
            }
            CompoundTag tag = NbtIo.read(path);
            if (tag == null || !tag.contains("Inventory", Tag.TAG_LIST)) {
                throw new RuntimeException(
                        "Malformed inventory data for player: " + player.getName().getString());
            }
            return tag;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load inventory for player: " + player.getName().getString(), e);
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
    @SubscribeEvent
    public void cancelPlayerAction(PlayerInteractEvent.EntityInteract event) {
        this.cancelPlayerAction(event.getEntity(), event);
    }

    /**
     * Cancelling all cancellable player interact events
     *
     * @param event the event
     */
    @SubscribeEvent
    public void cancelPlayerAction(PlayerInteractEvent.EntityInteractSpecific event) {
        this.cancelPlayerAction(event.getEntity(), event);
    }

    /**
     * Cancelling all cancellable player interact events
     *
     * @param event the event
     */
    @SubscribeEvent
    public void cancelPlayerAction(PlayerInteractEvent.LeftClickBlock event) {
        this.cancelPlayerAction(event.getEntity(), event);
    }

    /**
     * Cancelling all cancellable player interact events
     *
     * @param event the event
     */
    @SubscribeEvent
    public void cancelPlayerAction(PlayerInteractEvent.RightClickBlock event) {
        this.cancelPlayerAction(event.getEntity(), event);
    }

    /**
     * Cancelling all cancellable player interact events
     *
     * @param event the event
     */
    @SubscribeEvent
    public void cancelPlayerAction(PlayerInteractEvent.RightClickItem event) {
        this.cancelPlayerAction(event.getEntity(), event);
    }

    /**
     * Prevents the player from opening containers while loading
     *
     * @param event the event
     */
    @SubscribeEvent
    public void cancelPlayerAction(PlayerContainerEvent.Open event) {
        this.cancelPlayerAction(event.getEntity(), () -> event.getEntity().closeContainer());
    }

    /**
     * Cancels a player dealing or receiving damage
     *
     * @param event the event
     */
    @SubscribeEvent
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
    @SubscribeEvent
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
    @SubscribeEvent
    public void cancelPlayerAction(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            this.cancelPlayerAction(player, event);
        }
    }
}
