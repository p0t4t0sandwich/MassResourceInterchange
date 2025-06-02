/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * The project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import static net.minecraft.network.chat.Component.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import dev.neuralnexus.mri.Constants;

import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class BackpackHandler {
    private static final List<UUID> OPEN_BACKPACKS =
            Collections.synchronizedList(new ArrayList<>());

    @SuppressWarnings("DataFlowIssue")
    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        Command<CommandSourceStack> command =
                context -> {
                    CommandSourceStack source = context.getSource();
                    if (source.getPlayer() == null) {
                        source.sendFailure(literal("This command can only be used by players."));
                        return Command.SINGLE_SUCCESS;
                    }
                    ServerPlayer player = source.getPlayer();
                    if (OPEN_BACKPACKS.contains(player.getUUID())) {
                        source.sendFailure(literal("You already have a backpack open."));
                        return Command.SINGLE_SUCCESS;
                    }
                    source.sendSuccess(() -> literal("Opening backpack..."), false);
                    OPEN_BACKPACKS.add(player.getUUID());

                    Util.backgroundExecutor()
                            .execute(
                                    () -> {
                                        CompoundTag tag = load(player, 9);
                                        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
                                        int size = tag.getByte("Size") & 255;
                                        Backpack backpack = new Backpack(size);
                                        loadContainerNBT(player.registryAccess(), backpack, items);

                                        MenuProvider menu =
                                                new SimpleMenuProvider(
                                                        setupMenu(backpack),
                                                        BACKPACK_NAME.apply(player));
                                        player.openMenu(menu);
                                    });

                    return Command.SINGLE_SUCCESS;
                };

        dispatcher.register(
                Commands.literal("backpack")
                        .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(command));
    }

    public static final Component S_BACKPACK = literal("'s Backpack");
    public static final Function<Player, Component> BACKPACK_NAME =
            player -> player.getDisplayName().copy().append(S_BACKPACK);

    public static class Backpack extends SimpleContainer {
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
    }

    public static MenuConstructor setupMenu(Container container) {
        ContainerSize containerSize = ContainerSize.fromSize(container.getContainerSize());
        return (contCount, inventory, player) ->
                switch (containerSize) {
                    case Rows_1 -> new DispenserMenu(contCount, inventory, container);
                    case Rows_2 ->
                            new ChestMenu(MenuType.GENERIC_9x2, contCount, inventory, container, 2);
                    case Rows_3 ->
                            new ChestMenu(MenuType.GENERIC_9x3, contCount, inventory, container, 3);
                    case Rows_4 ->
                            new ChestMenu(MenuType.GENERIC_9x4, contCount, inventory, container, 4);
                    case Rows_5 ->
                            new ChestMenu(MenuType.GENERIC_9x5, contCount, inventory, container, 5);
                    case Rows_6 ->
                            new ChestMenu(MenuType.GENERIC_9x6, contCount, inventory, container, 6);
                };
    }

    public static ListTag saveContainerNBT(RegistryAccess access, Container container) {
        ListTag items = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) {
                CompoundTag tag = new CompoundTag();
                tag.putByte("Slot", (byte) i);
                Tag item = container.getItem(i).save(access, tag);
                items.add(item);
            }
        }
        return items;
    }

    public static void loadContainerNBT(RegistryAccess access, Container container, ListTag items) {
        container.clearContent();
        for (int i = 0; i < items.size(); i++) {
            CompoundTag tag = items.getCompound(i);
            int slot = tag.getByte("Slot") & 255;
            ItemStack item = ItemStack.parse(access, tag).orElse(ItemStack.EMPTY);
            if (!item.isEmpty()) {
                if (slot < container.getContainerSize()) {
                    container.setItem(slot, item);
                }
            }
        }
    }

    private static final Path backpackPath =
            Paths.get("world")
                    .resolve("playerdata")
                    .resolve(Constants.MOD_NAME)
                    .resolve("backpacks");

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

    public enum ContainerSize {
        Rows_1(9),
        Rows_2(18),
        Rows_3(27),
        Rows_4(36),
        Rows_5(45),
        Rows_6(54);

        private final int size;

        ContainerSize(int size) {
            this.size = size;
        }

        public int size() {
            return this.size;
        }

        public static ContainerSize fromSize(int size) {
            return switch (size) {
                case 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 -> Rows_1;
                case 10, 11, 12, 13, 14, 15, 16, 17, 18 -> Rows_2;
                case 19, 20, 21, 22, 23, 24, 25, 26, 27 -> Rows_3;
                case 28, 29, 30, 31, 32, 33, 34, 35, 36 -> Rows_4;
                case 37, 38, 39, 40, 41, 42, 43, 44, 45 -> Rows_5;
                case 46, 47, 48, 49, 50, 51, 52, 53, 54 -> Rows_6;
                default -> throw new IllegalArgumentException("Invalid size: " + size);
            };
        }
    }
}
