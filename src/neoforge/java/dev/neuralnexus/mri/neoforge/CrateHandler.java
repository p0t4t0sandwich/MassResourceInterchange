/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import static dev.neuralnexus.mri.neoforge.ContainerUtils.ContainerSize.setupMenu;

import static net.minecraft.network.chat.Component.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import com.mojang.math.Transformation;
import dev.neuralnexus.mri.mixin.neoforge.BlockDisplayAccessor;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrateHandler {
    static final Map<UUID, Crate> CRATES = new HashMap<>();

    @SuppressWarnings("SameReturnValue")
    public static int createCrate(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (source.getPlayer() == null) {
            source.sendFailure(literal("This command can only be used by players."));
            return Command.SINGLE_SUCCESS;
        }
        ServerPlayer player = source.getPlayer();

        UUID crateId = UUID.randomUUID();
        String crateName = ctx.getArgument("crateName", String.class);
        if (crateName.isEmpty()) {
            source.sendFailure(literal("Crate name cannot be empty."));
            return Command.SINGLE_SUCCESS;
        }

        int size = ctx.getArgument("size", int.class);
        Coordinates location = ctx.getArgument("location", Coordinates.class);
        BlockPos pos = location.getBlockPos(source).above();

        String worldName = ctx.getArgument("world", String.class);
        ResourceLocation world = ResourceLocation.parse(worldName);
        ResourceKey<Level> levelKey =
                source.getServer().levelKeys().stream()
                        .filter(key -> key.location().equals(world))
                        .findFirst()
                        .orElse(null);

        if (levelKey == null) {
            source.sendFailure(literal("World '" + worldName + "' not found."));
            return Command.SINGLE_SUCCESS;
        }

        source.sendSuccess(
                () ->
                        literal(
                                "Created crate '"
                                        + crateName
                                        + "' with ID "
                                        + crateId
                                        + " at "
                                        + pos),
                true);

        Util.backgroundExecutor()
                .execute(
                        () -> {
                            Crate crate =
                                    new Crate(player.registryAccess(), size, crateId, pos, levelKey);
                            CRATES.put(crateId, crate);

                            Level level = source.getServer().getLevel(levelKey);
                            if (level == null) {
                                source.sendFailure(literal("Level not found: " + levelKey.location()));
                                return;
                            }

                            Display.BlockDisplay display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
                            display.setPos(pos.getX(), pos.getY(), pos.getZ());

                            // AABB boundingBox = display.getBoundingBox().inflate(0.1, 0.1, 0.1);
                            // display.setBoundingBox(boundingBox);

                            CompoundTag blockState = new CompoundTag();
                            blockState.putString(StateHolder.NAME_TAG, "minecraft:barrel");

                            CompoundTag nbt = new CompoundTag();
                            nbt.put("block_state", blockState);
                            nbt.putFloat("width", 0.5f);
                            nbt.putFloat("height", 0.5f);

                            ((BlockDisplayAccessor) display).mri$readAdditionalSaveData(nbt);

                            level.addFreshEntity(display);

                            level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                        });

        return Command.SINGLE_SUCCESS;
    }

    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event) {
        RequiredArgumentBuilder<CommandSourceStack, String> crateNameArgument =
                Commands.argument("crateName", StringArgumentType.word());
        RequiredArgumentBuilder<CommandSourceStack, Integer> sizeArgument =
                Commands.argument("size", IntegerArgumentType.integer(1, 54));
        RequiredArgumentBuilder<CommandSourceStack, Coordinates> locationArgument =
                Commands.argument("location", BlockPosArgument.blockPos());
        RequiredArgumentBuilder<CommandSourceStack, String> worldArgument =
                Commands.argument("world", StringArgumentType.string());
        worldArgument.suggests(
                (ctx, builder) ->
                        SharedSuggestionProvider.suggest(
                                ctx.getSource().getServer().levelKeys().stream()
                                        .map(ResourceKey::location)
                                        .map(ResourceLocation::toString)
                                        .map(str -> "\"" + str + "\""),
                                builder));

        worldArgument.executes(CrateHandler::createCrate);
        locationArgument.then(worldArgument);
        sizeArgument.then(locationArgument);
        crateNameArgument.then(sizeArgument);

        LiteralArgumentBuilder<CommandSourceStack> createCrate =
                Commands.literal("create").then(crateNameArgument);

        LiteralArgumentBuilder<CommandSourceStack> crate =
                Commands.literal("crate")
                        .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                        .then(createCrate);

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
