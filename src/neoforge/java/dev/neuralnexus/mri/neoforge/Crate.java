/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import static dev.neuralnexus.mri.neoforge.ContainerUtils.saveContainerNBT;

import static net.minecraft.network.chat.Component.literal;

import dev.neuralnexus.mri.Constants;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class Crate extends SimpleContainer {
    private static final Path cratePath =
            Paths.get("world").resolve(Constants.MOD_NAME).resolve("crates");

    public static final Component INV_NAME = literal("Storage Crate");

    private final UUID crateId;
    private final BlockPos pos;

    public Crate(int size, UUID crateId, BlockPos pos) {
        super(size);
        this.crateId = crateId;
        this.pos = pos;
    }

    public UUID crateId() {
        return crateId;
    }

    public BlockPos pos() {
        return pos;
    }

    // TODO: Look into following the world's save "event", would also conform to save-off that way
    @Override
    public void stopOpen(@NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ListTag items = saveContainerNBT(serverPlayer.registryAccess(), this);
            CompoundTag tag = new CompoundTag();
            tag.putByte("Size", (byte) this.getContainerSize());
            tag.put("Items", items);
            save(this.crateId, tag);
        }
    }

    public static void save(UUID crateId, CompoundTag tag) {
        try {
            if (!Files.exists(cratePath)) {
                Files.createDirectories(cratePath);
            }
            Path path = cratePath.resolve(crateId + ".dat");
            NbtIo.write(tag, path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save storage crate: " + crateId, e);
        }
    }

    public static CompoundTag load(UUID crateId, int defaultSize) {
        try {
            Path path = cratePath.resolve(crateId + ".dat");
            if (!path.toFile().exists()) {
                CompoundTag emptyTag = new CompoundTag();
                emptyTag.putByte("Size", (byte) defaultSize);
                emptyTag.put("Items", new ListTag());
                save(crateId, emptyTag);
            }
            CompoundTag tag = NbtIo.read(path);
            if (tag == null || !tag.contains("Items", Tag.TAG_LIST)) {
                throw new RuntimeException("Malformed storage crate: " + crateId);
            }
            return tag;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save storage crate: " + crateId, e);
        }
    }
}
