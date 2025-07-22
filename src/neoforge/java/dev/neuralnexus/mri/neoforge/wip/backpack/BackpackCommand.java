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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
    public static int createBackPack(CommandContext<CommandSourceStack> ctx) {
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
        source.sendSuccess(() -> literal("Opening backpack..."), false);
        OPEN_BACKPACKS.add(player.getUUID());

        Util.backgroundExecutor()
                .execute(
                        () -> {
                            CompoundTag tag = Backpack.load(player, 9);
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

    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> backpack =
                Commands.literal("backpack")
                        .requires(hasPermission("backpack.open", Commands.LEVEL_GAMEMASTERS))
                        .executes(BackpackCommand::createBackPack);

        event.getDispatcher().register(backpack);
    }
}
