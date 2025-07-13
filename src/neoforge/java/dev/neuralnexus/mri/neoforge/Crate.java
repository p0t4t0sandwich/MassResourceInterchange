/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import static dev.neuralnexus.mri.neoforge.ContainerUtils.loadContainerNBT;
import static dev.neuralnexus.mri.neoforge.ContainerUtils.saveContainerNBT;
import static dev.neuralnexus.mri.neoforge.CrateHandler.CRATES;

import static net.minecraft.network.chat.Component.literal;

import com.mojang.math.Transformation;

import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.mixin.neoforge.BlockDisplayAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

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
    private final ResourceKey<Level> level;

    public Crate(
            HolderLookup.Provider registryAccess,
            int size,
            UUID crateId,
            BlockPos pos,
            ResourceKey<Level> level) {
        super(size);
        this.crateId = crateId;
        this.pos = pos;
        this.level = level;

        CompoundTag tag = Crate.load(crateId, size);
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        loadContainerNBT(registryAccess, this, items);
    }

    public UUID crateId() {
        return crateId;
    }

    public BlockPos pos() {
        return pos;
    }

    // TODO: Maybe alter to return the level object through the server
    public ResourceKey<Level> level() {
        return level;
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

    public static void create(
            HolderLookup.Provider registryAccess,
            int size,
            UUID crateId,
            BlockPos pos,
            ResourceKey<Level> levelKey,
            Level level) {
        Crate crate = new Crate(registryAccess, size, crateId, pos, levelKey);
        CRATES.put(crateId, crate);

        Display.BlockDisplay display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
        CompoundTag nbt = new CompoundTag();

        nbt.put(
                Utils.TAG_ENTITY_POS,
                Utils.newDoubleList(pos.getX() + 0.1, pos.getY() + 0.1, pos.getZ() + 0.1));

        Vector3f scale = new Vector3f(0.8F, 1.0F, 0.8F);
        Transformation.EXTENDED_CODEC
                .encodeStart(NbtOps.INSTANCE, Utils.createScaleTransform(scale))
                .ifSuccess(tag -> nbt.put(Display.TAG_TRANSFORMATION, tag));

        BlockState blockState = Blocks.BARREL.defaultBlockState();
        nbt.put(Display.BlockDisplay.TAG_BLOCK_STATE, NbtUtils.writeBlockState(blockState));
        ((BlockDisplayAccessor) display).mri$readAdditionalSaveData(nbt);

        display.load(nbt);
        level.addFreshEntity(display);
        level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
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
