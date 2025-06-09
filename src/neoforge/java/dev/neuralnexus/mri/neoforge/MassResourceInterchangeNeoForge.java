/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.neuralnexus.mri.Constants;
import dev.neuralnexus.mri.common.CommonClass;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

@Mod(Constants.MOD_ID)
public class MassResourceInterchangeNeoForge {
    public MassResourceInterchangeNeoForge(IEventBus eventBus) {
        CommonClass.init();

        NeoForge.EVENT_BUS.<RegisterCommandsEvent>addListener(
                event -> {
                    Predicate<CommandSourceStack> predicate =
                            css -> css.hasPermission(Commands.LEVEL_OWNERS);
                    LiteralArgumentBuilder<CommandSourceStack> save =
                            literal("save")
                                    .requires(predicate)
                                    .executes(
                                            ctx -> {
                                                CommandSourceStack source = ctx.getSource();
                                                ServerPlayer player = source.getPlayer();
                                                if (player == null) {
                                                    source.sendSystemMessage(
                                                            Component.literal(
                                                                    "You need to be a player!"));
                                                    return Command.SINGLE_SUCCESS;
                                                }
                                                save(player);
                                                return Command.SINGLE_SUCCESS;
                                            });

                    LiteralArgumentBuilder<CommandSourceStack> load =
                            literal("load")
                                    .requires(predicate)
                                    .executes(
                                            ctx -> {
                                                CommandSourceStack source = ctx.getSource();
                                                ServerPlayer player = source.getPlayer();
                                                if (player == null) {
                                                    source.sendSystemMessage(
                                                            Component.literal(
                                                                    "You need to be a player!"));
                                                    return Command.SINGLE_SUCCESS;
                                                }
                                                load(player);
                                                return Command.SINGLE_SUCCESS;
                                            });

                    event.getDispatcher().register(save);
                    event.getDispatcher().register(load);

                    BackpackCommand.registerCommand(event.getDispatcher());
                });

        NeoForge.EVENT_BUS.register(new InventorySync());
    }

    public static void save(ServerPlayer player) {
        ItemStack playerItem = player.getMainHandItem();
        RegistryAccess access = player.registryAccess();

        if (playerItem.isEmpty()) {
            player.sendSystemMessage(
                    Component.literal("You need to hold an item in your main hand to save it!"));
            return;
        }

        ItemStack item = playerItem.copyWithCount(1);
        playerItem.setCount(playerItem.getCount() - 1);

        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), access);
        ItemStack.STREAM_CODEC.encode(buffer, item);

        try {
            Path path = Paths.get("config").resolve(Constants.MOD_NAME).resolve("save.nbt");
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (var outputStream = Files.newOutputStream(path)) {
                outputStream.write(buffer.array());
                outputStream.flush();
                Constants.logger().info("Saved item to file: {}", path);
            } catch (IOException e) {
                Constants.logger().warn("Failed to write item to file", e);
            }
        } catch (IOException e) {
            Constants.logger().warn("Failed to save item", e);
        }
    }

    public static void load(ServerPlayer player) {
        Path path = Paths.get("config").resolve(Constants.MOD_NAME).resolve("save.nbt");
        if (!Files.exists(path)) {
            Constants.logger().warn("Save file does not exist: {}", path);
            return;
        }

        try {
            byte[] data = Files.readAllBytes(path);
            ByteBuf buffer = Unpooled.wrappedBuffer(data);
            ItemStack item =
                    ItemStack.STREAM_CODEC.decode(
                            new RegistryFriendlyByteBuf(buffer, player.registryAccess()));
            player.addItem(item);
            Files.delete(path);
            Constants.logger().info("Loaded item from file: {}", path);
        } catch (IOException e) {
            Constants.logger().warn("Failed to load item from file", e);
        }
    }
}
