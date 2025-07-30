/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge.playersync;

import static net.minecraft.network.chat.Component.literal;

import dev.neuralnexus.mri.CommonClass;
import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.MRIAPI;
import dev.neuralnexus.mri.datastores.DataStore;
import dev.neuralnexus.mri.modules.PlayerSyncModule;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class PlayerSyncUtils {
    public static final Map<UUID, BlockPos> frozenPlayers = new ConcurrentHashMap<>();

    public static Path snapshotPath;

    /** Init the player inventory snapshot directory when the server starts */
    public static void init() {
        if (snapshotPath == null) {
            snapshotPath =
                    CommonClass.playerDataFolder
                            .resolve(Constants.MOD_NAME)
                            .resolve("inv_snapshots");
        }

        try {
            if (!Files.exists(snapshotPath)) {
                Files.createDirectories(snapshotPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create inventory snapshot directory", e);
        }
    }

    /**
     * Take a local snapshot of the player's inventory and save it to disk
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
     * Restore the player's inventory from the last snapshot
     *
     * @param player the player whose inventory to restore
     */
    public static void restoreLastSnapshot(Player player) {
        try (Stream<Path> snapshots = Files.list(snapshotPath)) {
            Optional<Path> lastSnapshot =
                    snapshots
                            .filter(
                                    path ->
                                            path.getFileName()
                                                    .toString()
                                                    .startsWith(player.getUUID().toString()))
                            .max(Comparator.comparingLong(path -> path.toFile().lastModified()));

            if (lastSnapshot.isPresent()) {
                CompoundTag tag = NbtIo.read(lastSnapshot.get());
                ListTag inventory = tag.getList("Inventory", Tag.TAG_COMPOUND);
                player.getInventory().load(inventory);
            } else {
                Constants.logger()
                        .warn(
                                "No inventory snapshot found for player: {}",
                                player.getName().getString());
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to restore last inventory snapshot for player: "
                            + player.getName().getString(),
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
            PlayerSyncModule module = MRIAPI.getInstance().playerSync();
            UUID playerId = player.getUUID();
            UUID id = module.getOrCreateId(playerId);

            ListTag inventory = player.getInventory().save(new ListTag());
            CompoundTag tag = new CompoundTag();
            tag.put("Inventory", inventory);
            save(playerId, id, tag);
        };
    }

    /**
     * Load the player's inventory in a separate thread
     *
     * @param player the player whose inventory to load
     * @return a Runnable that loads the player's inventory
     */
    public static Runnable loadPlayerInventory(ServerPlayer player) {
        return () -> {
            PlayerSyncModule module = MRIAPI.getInstance().playerSync();
            DataStore<?> dataStore = module.datastore();
            UUID playerId = player.getUUID();
            Optional<UUID> id = module.getId(playerId);

            if (id.isEmpty()) {
                restoreLastSnapshot(player);

                UUID newId = UUID.randomUUID();
                module.createPlayerEntry(newId, playerId);

                ListTag inventory = player.getInventory().save(new ListTag());
                CompoundTag tag = new CompoundTag();
                tag.put("Inventory", inventory);
                save(playerId, newId, tag);

                frozenPlayers.remove(playerId);
                return;
            }

            Optional<UUID> locked = dataStore.isLocked(id.get());
            if (locked.isPresent()) {
                if (MRIAPI.getInstance().serverId().equals(locked.get())) {
                    dataStore.unlock(id.get());
                    Constants.logger()
                            .warn(
                                    "There was a lock owned by this server in the datastore for player {}. This should not happen, but it has been removed.",
                                    player.getName().getString());
                } else {
                    player.connection.disconnect(
                            literal(
                                    "Your inventory is currently being loaded by another server. If you believe this is an error, please contact the server administrator."));
                }
            }

            dataStore.lock(id.get());
            CompoundTag tag = load(playerId, id.get());
            ListTag inventory = tag.getList("Inventory", Tag.TAG_COMPOUND);
            player.getInventory().load(inventory);

            frozenPlayers.remove(playerId);
        };
    }

    public static void save(UUID playerId, UUID id, CompoundTag tag) {
        try {
            DataStore<?> dataStore = MRIAPI.getInstance().playerSync().datastore();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            try (dataOutputStream) {
                NbtIo.write(tag, dataOutputStream);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to write inventory data for player: " + playerId, e);
            }

            byte[] bytes = outputStream.toByteArray();
            dataStore.store(id, bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save inventory for player: " + playerId, e);
        }
    }

    public static @Nullable CompoundTag load(UUID playerId, UUID id) {
        try {
            DataStore<?> dataStore = MRIAPI.getInstance().playerSync().datastore();
            byte[] bytes = dataStore.retrieve(id);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            CompoundTag tag;
            try (dataInputStream) {
                tag = NbtIo.read(dataInputStream);
            } catch (IOException e) {
                tag = null;
            }

            if (tag != null && !tag.contains("Inventory", Tag.TAG_LIST)) {
                throw new RuntimeException("Malformed inventory data for player: " + playerId);
            }
            return tag;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load inventory for player: " + playerId, e);
        }
    }
}
