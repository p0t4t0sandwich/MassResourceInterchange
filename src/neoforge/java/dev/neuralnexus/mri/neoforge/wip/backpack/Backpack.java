/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge.wip.backpack;

import static dev.neuralnexus.mri.neoforge.ContainerUtils.saveContainerNBT;

import static net.minecraft.network.chat.Component.literal;

import dev.neuralnexus.mri.MRIAPI;
import dev.neuralnexus.mri.datastores.DataStore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Function;

public class Backpack extends SimpleContainer {
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

            // TODO: Change this to use db lookup for backpack "owner"
            DataStore<?> dataStore = MRIAPI.getInstance().backpack().datastore();
            dataStore.unlock(player.getUUID());
        }
    }

    public static void save(ServerPlayer player, CompoundTag tag) {
        try {
            DataStore<?> dataStore = MRIAPI.getInstance().backpack().datastore();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            try (dataOutputStream) {
                NbtIo.write(tag, dataOutputStream);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to write backpack data for player: " + player.getName().getString(),
                        e);
            }

            byte[] bytes = outputStream.toByteArray();
            dataStore.store(player.getUUID(), bytes);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to save backpack for player: " + player.getName().getString(), e);
        }
    }

    public static @Nullable CompoundTag load(ServerPlayer player) {
        try {
            DataStore<?> dataStore = MRIAPI.getInstance().backpack().datastore();

            if (!hasBackpack(player)) {
                return null;
            }

            // TODO: Change this to use db lookup for backpack "owner"
            byte[] bytes = dataStore.retrieve(player.getUUID());

            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            CompoundTag tag;
            try (dataInputStream) {
                tag = NbtIo.read(dataInputStream);
            } catch (IOException e) {
                tag = null;
            }

            if (tag != null && !tag.contains("Items", Tag.TAG_LIST)) {
                throw new RuntimeException(
                        "Malformed backpack data for player: " + player.getName().getString());
            }
            return tag;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load backpack for player: " + player.getName().getString(), e);
        }
    }

    public static boolean hasBackpack(ServerPlayer player) {
        DataStore<?> dataStore = MRIAPI.getInstance().backpack().datastore();
        byte[] bytes = dataStore.retrieve(player.getUUID());
        return bytes != null && bytes.length != 0;
    }

    public static boolean createBackpack(ServerPlayer player, int size) {
        CompoundTag emptyTag = new CompoundTag();
        emptyTag.putByte("Size", (byte) size);
        emptyTag.put("Items", new ListTag());
        try {
            save(player, emptyTag);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
