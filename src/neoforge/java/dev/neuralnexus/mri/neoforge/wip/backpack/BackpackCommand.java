/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge.wip.backpack;

import static dev.neuralnexus.mri.neoforge.ContainerUtils.ContainerSize.setupMenu;
import static dev.neuralnexus.mri.neoforge.ContainerUtils.loadContainerNBT;
import static dev.neuralnexus.mri.neoforge.Utils.hasPermission;

import static net.minecraft.network.chat.Component.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BackpackCommand {
    public static final List<UUID> OPEN_BACKPACKS = Collections.synchronizedList(new ArrayList<>());

    @SuppressWarnings("SameReturnValue")
    public static int openBackPack(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (source.getPlayer() == null) {
            source.sendFailure(literal("This command can only be used by players."));
            return Command.SINGLE_SUCCESS;
        }
        ServerPlayer player = source.getPlayer();

        if (OPEN_BACKPACKS.contains(player.getUUID())) {
            source.sendFailure(literal("You already have a backpack open."));
            return Command.SINGLE_SUCCESS;
        }

        Util.backgroundExecutor()
                .execute(
                        () -> {
                            CompoundTag tag = Backpack.load(player);

                            if (tag == null) {
                                source.sendFailure(literal("No backpack found for this player."));
                                return;
                            }

                            source.sendSuccess(() -> literal("Opening backpack..."), false);
                            OPEN_BACKPACKS.add(player.getUUID());

                            ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
                            int size = tag.getByte("Size") & 255;
                            Backpack backpack = new Backpack(size);
                            loadContainerNBT(player.registryAccess(), backpack, items);

                            MenuProvider menu =
                                    new SimpleMenuProvider(
                                            setupMenu(backpack),
                                            Backpack.BACKPACK_NAME.apply(player));
                            player.openMenu(menu);
                        });

        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("SameReturnValue")
    public static int createBackPack(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        if (Backpack.hasBackpack(player)) {
            source.sendFailure(literal("This player already has a backpack."));
            return Command.SINGLE_SUCCESS;
        }

        int size = ctx.getArgument("size", Integer.class);
        if (size % 9 != 0) {
            source.sendFailure(literal("Size must be a multiple of 9 and between 9 and 54."));
            return Command.SINGLE_SUCCESS;
        }

        if (Backpack.createBackpack(player, size)) {
            source.sendSuccess(() -> literal("Created backpack for " + player.getDisplayName().getString()), true);
        } else {
            source.sendFailure(literal("Failed to create backpack for " + player.getDisplayName().getString() + ", see console for details."));
        }

        return Command.SINGLE_SUCCESS;
    }

    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event) {
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> playerArgument =
                Commands.argument("player", EntityArgument.player());
        RequiredArgumentBuilder<CommandSourceStack, Integer> sizeArgument =
                Commands.argument("size", IntegerArgumentType.integer(1, 54));
        sizeArgument.suggests(
                (ctx, builder) ->
                        SharedSuggestionProvider.suggest(
                                List.of("9", "18", "27", "36", "45", "54"), builder));

        sizeArgument.executes(BackpackCommand::createBackPack);
        playerArgument.then(sizeArgument);

        LiteralArgumentBuilder<CommandSourceStack> create =
                Commands.literal("create")
                        .requires(hasPermission("backpack.create", Commands.LEVEL_GAMEMASTERS))
                        .then(playerArgument);

        LiteralArgumentBuilder<CommandSourceStack> backpack =
                Commands.literal("backpack")
                        .executes(BackpackCommand::openBackPack)
                        .then(create);

        event.getDispatcher().register(backpack);
    }
}
