/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import static dev.neuralnexus.mri.neoforge.ContainerUtils.ContainerSize.setupMenu;
import static dev.neuralnexus.mri.neoforge.ContainerUtils.loadContainerNBT;

import static net.minecraft.network.chat.Component.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class CrateHandler {
    private static final Map<UUID, Crate> CRATES = new HashMap<>();

    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event) {
        Command<CommandSourceStack> createCommand =
                context -> {
                    CommandSourceStack source = context.getSource();
                    if (source.getPlayer() == null) {
                        source.sendFailure(literal("This command can only be used by players."));
                        return Command.SINGLE_SUCCESS;
                    }
                    ServerPlayer player = source.getPlayer();

                    // String strCrateId = StringArgumentType.getString(context, "crateId");
                    // UUID crateId;
                    // if (strCrateId.isEmpty()) {
                    //     source.sendFailure(literal("Crate ID cannot be empty."));
                    //     return Command.SINGLE_SUCCESS;
                    // }
                    // try {
                    //     crateId = UUID.fromString(strCrateId);
                    // } catch (IllegalArgumentException e) {
                    //     source.sendFailure(literal("Invalid crate ID format. Must be a UUID."));
                    //     return Command.SINGLE_SUCCESS;
                    // }
                    UUID crateId = UUID.randomUUID();
                    String crateName = StringArgumentType.getString(context, "crateName");
                    if (crateName.isEmpty()) {
                        source.sendFailure(literal("Crate name cannot be empty."));
                        return Command.SINGLE_SUCCESS;
                    }

                    int size = IntegerArgumentType.getInteger(context, "size");
                    int x = IntegerArgumentType.getInteger(context, "x");
                    int y = IntegerArgumentType.getInteger(context, "y");
                    int z = IntegerArgumentType.getInteger(context, "z");
                    BlockPos pos = new BlockPos(x, y, z);

                    source.sendSuccess(
                            () ->
                                    literal(
                                            "Created crate '"
                                                    + crateName
                                                    + "' with ID "
                                                    + crateId
                                                    + " at ("
                                                    + x
                                                    + ", "
                                                    + y
                                                    + ", "
                                                    + z
                                                    + ")"),
                            true);

                    Util.backgroundExecutor()
                            .execute(
                                    () -> {
                                        CompoundTag tag = Crate.load(crateId, size);
                                        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
                                        // int storedSize = tag.getByte("Size") & 255;
                                        Crate crate = new Crate(size, crateId, pos);
                                        CRATES.put(crateId, crate);
                                        loadContainerNBT(player.registryAccess(), crate, items);
                                    });

                    return Command.SINGLE_SUCCESS;
                };

        Predicate<CommandSourceStack> hasPermission =
                source -> source.hasPermission(Commands.LEVEL_OWNERS);

        SuggestionProvider<CommandSourceStack> crateIdSuggestions =
                (context, builder) -> {
                    for (UUID id : CRATES.keySet()) {
                        builder.suggest(id.toString());
                    }
                    return builder.buildFuture();
                };

        RequiredArgumentBuilder<CommandSourceStack, String> crateIdArgument =
                Commands.argument("crateId", StringArgumentType.string())
                        .suggests(crateIdSuggestions);

        RequiredArgumentBuilder<CommandSourceStack, String> crateNameArgument =
                Commands.argument("crateName", StringArgumentType.word());

        RequiredArgumentBuilder<CommandSourceStack, Integer> sizeArgument =
                Commands.argument("size", IntegerArgumentType.integer(1, 54));

        RequiredArgumentBuilder<CommandSourceStack, Integer> xArgument =
                Commands.argument("x", IntegerArgumentType.integer())
                        .suggests(
                                (context, builder) -> {
                                    Entity entity = context.getSource().getEntity();
                                    if (entity != null) {
                                        builder.suggest(entity.blockPosition().getX());
                                    }
                                    return builder.buildFuture();
                                });

        RequiredArgumentBuilder<CommandSourceStack, Integer> yArgument =
                Commands.argument("y", IntegerArgumentType.integer())
                        .suggests(
                                (context, builder) -> {
                                    Entity entity = context.getSource().getEntity();
                                    if (entity != null) {
                                        builder.suggest(entity.blockPosition().getY());
                                    }
                                    return builder.buildFuture();
                                });

        RequiredArgumentBuilder<CommandSourceStack, Integer> zArgument =
                Commands.argument("z", IntegerArgumentType.integer())
                        .suggests(
                                (context, builder) -> {
                                    Entity entity = context.getSource().getEntity();
                                    if (entity != null) {
                                        builder.suggest(entity.blockPosition().getZ());
                                    }
                                    return builder.buildFuture();
                                });

        LiteralArgumentBuilder<CommandSourceStack> createCrate =
                Commands.literal("create")
                        .requires(hasPermission)
                        .then(
                                crateNameArgument.then(
                                        sizeArgument.then(
                                                xArgument.then(
                                                        yArgument.then(
                                                                zArgument.executes(
                                                                        createCommand))))));

        LiteralArgumentBuilder<CommandSourceStack> crate =
                Commands.literal("crate").requires(hasPermission).then(createCrate);

        event.getDispatcher().register(crate);
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Crate crate =
                CRATES.values().stream()
                        .filter(c -> c.pos().equals(event.getPos()))
                        .findFirst()
                        .orElse(null);

        if (crate == null) {
            return;
        }

        MenuProvider menu = new SimpleMenuProvider(setupMenu(crate), Crate.INV_NAME);
        player.openMenu(menu);
    }
}
