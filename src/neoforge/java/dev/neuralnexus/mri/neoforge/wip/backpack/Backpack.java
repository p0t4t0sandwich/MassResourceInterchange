/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge.wip.backpack;

import static dev.neuralnexus.mri.neoforge.ContainerUtils.saveContainerNBT;
import static dev.neuralnexus.mri.neoforge.wip.backpack.BackpackCommand.OPEN_BACKPACKS;

import static net.minecraft.network.chat.Component.literal;

import dev.neuralnexus.mri.Constants;

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
import java.util.function.Function;

public class Backpack extends SimpleContainer {
    private static final Path backpackPath =
            Paths.get("world")
                    .resolve("playerdata")
                    .resolve(Constants.MOD_NAME)
                    .resolve("backpacks");

    public static final Component S_BACKPACK = literal("'s Backpack");
    public static final Function<Player, Component> BACKPACK_NAME =
            player -> player.getDisplayName().copy().append(S_BACKPACK);

    public Backpack(int size) {
        super(size);
    }

    @Override
    public void stopOpen(@NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ListTag items = saveContainerNBT(serverPlayer.registryAccess(), this);
            CompoundTag tag = new CompoundTag();
            tag.putByte("Size", (byte) this.getContainerSize());
            tag.put("Items", items);
            save(serverPlayer, tag);
            OPEN_BACKPACKS.remove(serverPlayer.getUUID());
        }
    }

    public static void save(ServerPlayer player, CompoundTag tag) {
        try {
            if (!Files.exists(backpackPath)) {
                Files.createDirectories(backpackPath);
            }
            Path path = backpackPath.resolve(player.getUUID() + ".dat");
            NbtIo.write(tag, path);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to save backpack for player: " + player.getName().getString(), e);
        }
    }

    public static CompoundTag load(ServerPlayer player, int defaultSize) {
        try {
            Path path = backpackPath.resolve(player.getUUID() + ".dat");
            if (!path.toFile().exists()) {
                CompoundTag emptyTag = new CompoundTag();
                emptyTag.putByte("Size", (byte) defaultSize);
                emptyTag.put("Items", new ListTag());
                save(player, emptyTag);
            }
            CompoundTag tag = NbtIo.read(path);
            if (tag == null || !tag.contains("Items", Tag.TAG_LIST)) {
                throw new RuntimeException(
                        "Malformed backpack data for player: " + player.getName().getString());
            }
            return tag;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load backpack for player: " + player.getName().getString(), e);
        }
    }
}
