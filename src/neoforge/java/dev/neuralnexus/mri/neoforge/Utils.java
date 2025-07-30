/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import com.mojang.math.Transformation;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Predicate;

public class Utils {
    public static final String TAG_ENTITY_POS = "Pos";

    public static Transformation createScaleTransform(Vector3f scale) {
        return new Transformation(
                new Vector3f(0.0F, 0.0F, 0.0F),
                new Quaternionf().identity(),
                scale,
                new Quaternionf().identity());
    }

    public static ListTag newDoubleList(double... doubles) {
        ListTag listtag = new ListTag();
        for (double d : doubles) {
            listtag.add(DoubleTag.valueOf(d));
        }
        return listtag;
    }

    public static boolean hasPermission(
            ServerPlayer player, String permission, int fallbackPermissionLevel) {
        return PermissionAPI.getRegisteredNodes().stream()
                .filter(
                        node ->
                                node.getNodeName().equals(permission)
                                        && node.getType() == PermissionTypes.BOOLEAN)
                .map(
                        node ->
                                ((PermissionNode<Boolean>) node)
                                        .getDefaultResolver()
                                        .resolve(player, player.getUUID()))
                .reduce(Boolean::logicalOr)
                .orElseGet(() -> player.hasPermissions(fallbackPermissionLevel));
    }

    public static Predicate<CommandSourceStack> hasPermission(
            String permission, int fallbackPermissionLevel) {
        return source -> {
            ServerPlayer player = source.getPlayer();
            if (player == null) {
                return false;
            }
            return hasPermission(player, permission, fallbackPermissionLevel);
        };
    }
}
