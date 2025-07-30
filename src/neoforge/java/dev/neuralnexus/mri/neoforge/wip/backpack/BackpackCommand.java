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

import dev.neuralnexus.mri.CommonClass;
import dev.neuralnexus.mri.MRIAPI;
import dev.neuralnexus.mri.datastores.DataStore;
import dev.neuralnexus.mri.modules.BackpackModule;

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
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BackpackCommand {
    @SuppressWarnings("SameReturnValue")
    public static int openBackPack(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (source.getPlayer() == null) {
            source.sendFailure(literal("This command can only be used by players."));
            return Command.SINGLE_SUCCESS;
        }
        ServerPlayer player = source.getPlayer();

        BackpackModule module = MRIAPI.getInstance().backpack();
        DataStore<?> dataStore = module.datastore();

        Optional<BackpackModule.BackpackInfo> info = module.getBackpackInfo(player.getUUID());
        if (info.isEmpty()) {
            source.sendFailure(literal("This player does not have a backpack."));
            return Command.SINGLE_SUCCESS;
        }
        UUID backpackId = info.get().id();

        Optional<UUID> locked = dataStore.isLocked(backpackId);
        if (locked.isPresent()) {
            if (MRIAPI.getInstance().serverId().equals(locked.get())) {
                source.sendFailure(literal("You already have a backpack open."));
            } else {
                source.sendFailure(
                        literal(
                                "You cannot open your backpack while it's open on another server ("
                                        + MRIAPI.getInstance().serverId()
                                        + "). Please close it there first."));
            }
            return Command.SINGLE_SUCCESS;
        }

        CommonClass.scheduler()
                .runAsync(
                        () -> {
                            CompoundTag tag = Backpack.load(backpackId);

                            if (tag == null) {
                                source.sendFailure(
                                        literal("No backpack found, check console for details."));
                                return;
                            }

                            if (dataStore.lock(backpackId)) {
                                source.sendSuccess(() -> literal("Opening backpack..."), false);
                            } else {
                                source.sendFailure(
                                        literal(
                                                "Failed to acquire lock for backpack with ID "
                                                        + backpackId
                                                        + " for player "
                                                        + player.getDisplayName().getString()
                                                        + ". See console for details."));
                                return;
                            }

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

    // TODO: Abstract both of these into a common helper method
    @SuppressWarnings("SameReturnValue")
    public static int openBackPackOther(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();

        if (source.getPlayer() == null) {
            source.sendFailure(literal("This command can only be used by players."));
            return Command.SINGLE_SUCCESS;
        }

        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

        BackpackModule module = MRIAPI.getInstance().backpack();
        DataStore<?> dataStore = module.datastore();

        Optional<BackpackModule.BackpackInfo> info = module.getBackpackInfo(player.getUUID());
        if (info.isEmpty()) {
            source.sendFailure(literal("This player does not have a backpack."));
            return Command.SINGLE_SUCCESS;
        }
        UUID backpackId = info.get().id();

        Optional<UUID> locked = dataStore.isLocked(backpackId);
        if (locked.isPresent()) {
            if (MRIAPI.getInstance().serverId().equals(locked.get())) {
                source.sendFailure(literal("This player's backpack is already open."));
            } else {
                source.sendFailure(
                        literal(
                                "You cannot open this player's backpack while it's open on another server ("
                                        + MRIAPI.getInstance().serverId()
                                        + "). Please close it there first."));
            }
            return Command.SINGLE_SUCCESS;
        }

        CommonClass.scheduler()
                .runAsync(
                        () -> {
                            CompoundTag tag = Backpack.load(backpackId);

                            if (tag == null) {
                                source.sendFailure(
                                        literal("No backpack found, check console for details."));
                                return;
                            }

                            if (dataStore.lock(backpackId)) {
                                source.sendSuccess(() -> literal("Opening backpack..."), false);
                            } else {
                                source.sendFailure(
                                        literal(
                                                "Failed to acquire lock for backpack with ID "
                                                        + backpackId
                                                        + " for player "
                                                        + player.getDisplayName().getString()
                                                        + ". See console for details."));
                                return;
                            }

                            ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
                            int size = tag.getByte("Size") & 255;
                            Backpack backpack = new Backpack(size);
                            loadContainerNBT(player.registryAccess(), backpack, items);

                            MenuProvider menu =
                                    new SimpleMenuProvider(
                                            setupMenu(backpack),
                                            Backpack.BACKPACK_NAME.apply(player));
                            source.getPlayer().openMenu(menu);
                        });

        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("SameReturnValue")
    public static int createBackPack(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

        BackpackModule module = MRIAPI.getInstance().backpack();
        Optional<BackpackModule.BackpackInfo> info = module.getBackpackInfo(player.getUUID());

        if (info.isPresent()) {
            source.sendFailure(literal("This player already has a backpack."));
            return Command.SINGLE_SUCCESS;
        }

        int size = ctx.getArgument("size", Integer.class);
        if (size % 9 != 0) {
            source.sendFailure(literal("Size must be a multiple of 9 and between 9 and 54."));
            return Command.SINGLE_SUCCESS;
        }

        CommonClass.scheduler()
                .runAsync(
                        () -> {
                            UUID backpackId = UUID.randomUUID();
                            if (Backpack.createBackpack(backpackId, size)
                                    && module.createBackpack(player.getUUID(), backpackId, size)) {
                                source.sendSuccess(
                                        () ->
                                                literal(
                                                        "Created backpack for "
                                                                + player.getDisplayName()
                                                                        .getString()),
                                        true);
                            } else {
                                source.sendFailure(
                                        literal(
                                                "Failed to create backpack for "
                                                        + player.getDisplayName().getString()
                                                        + ", see console for details."));
                            }
                        });
        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("SameReturnValue")
    public static int deleteBackPack(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

        BackpackModule module = MRIAPI.getInstance().backpack();
        DataStore<?> dataStore = module.datastore();
        Optional<BackpackModule.BackpackInfo> info = module.getBackpackInfo(player.getUUID());

        if (info.isEmpty()) {
            source.sendFailure(literal("This player does not have a backpack."));
            return Command.SINGLE_SUCCESS;
        }

        CommonClass.scheduler()
                .runAsync(
                        () -> {
                            if (module.deleteBackpack(player.getUUID())
                                    && dataStore.delete(info.get().id())) {
                                source.sendSuccess(
                                        () ->
                                                literal(
                                                        "Deleted backpack for "
                                                                + player.getDisplayName()
                                                                        .getString()),
                                        true);
                            } else {
                                source.sendFailure(
                                        literal(
                                                "Failed to delete backpack for "
                                                        + player.getDisplayName().getString()
                                                        + ", see console for details."));
                            }
                        });
        return Command.SINGLE_SUCCESS;
    }

    public static void onRegisterCommand(RegisterCommandsEvent event) {
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> playerArgument =
                Commands.argument("player", EntityArgument.player());
        RequiredArgumentBuilder<CommandSourceStack, Integer> sizeArgument =
                Commands.argument("size", IntegerArgumentType.integer(9, 54));
        sizeArgument.suggests(
                (ctx, builder) ->
                        SharedSuggestionProvider.suggest(
                                List.of("9", "18", "27", "36", "45", "54"), builder));

        sizeArgument.executes(BackpackCommand::createBackPack);
        playerArgument.then(sizeArgument);

        LiteralArgumentBuilder<CommandSourceStack> create =
                Commands.literal("create")
                        .requires(hasPermission("mri.backpack.create", Commands.LEVEL_GAMEMASTERS))
                        .then(playerArgument);

        LiteralArgumentBuilder<CommandSourceStack> delete =
                Commands.literal("delete")
                        .requires(hasPermission("mri.backpack.delete", Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .executes(BackpackCommand::deleteBackPack));

        LiteralArgumentBuilder<CommandSourceStack> backpack =
                Commands.literal("backpack")
                        .requires(
                                source ->
                                        MRIAPI.getInstance().backpack().config().allowCommandAccess
                                                || hasPermission(
                                                                "mri.backpack.open",
                                                                Commands.LEVEL_GAMEMASTERS)
                                                        .test(source))
                        .executes(BackpackCommand::openBackPack)
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .requires(
                                                hasPermission(
                                                        "mri.backpack.open.others",
                                                        Commands.LEVEL_GAMEMASTERS))
                                        .executes(BackpackCommand::openBackPackOther));

        backpack.then(create);
        backpack.then(delete);

        event.getDispatcher().register(backpack);
    }
}
