/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge.backpack;

import static dev.neuralnexus.mri.neoforge.ContainerUtils.ContainerSize.setupMenu;
import static dev.neuralnexus.mri.neoforge.ContainerUtils.loadContainerNBT;

import static net.minecraft.network.chat.Component.literal;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import dev.neuralnexus.mri.CommonClass;
import dev.neuralnexus.mri.MRIAPI;
import dev.neuralnexus.mri.datastores.DataStore;
import dev.neuralnexus.mri.modules.BackpackModule;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Optional;
import java.util.UUID;

public class BackpackUtils {
    public static void openBackpack(
            UUID backpackId, ServerPlayer target, CommandSourceStack source) {
        BackpackModule module = MRIAPI.getInstance().backpack();
        DataStore<?> dataStore = module.datastore();

        if (source.getPlayer() == null) {
            source.sendFailure(literal("This method can only be used by players."));
            return;
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
                                                        + target.getDisplayName().getString()
                                                        + ". See console for details."));
                                return;
                            }

                            ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
                            int size = tag.getByte("Size") & 255;
                            Backpack backpack = new Backpack(size);
                            loadContainerNBT(source.registryAccess(), backpack, items);

                            MenuProvider menu =
                                    new SimpleMenuProvider(
                                            setupMenu(backpack),
                                            Backpack.BACKPACK_NAME.apply(target));
                            source.getPlayer().openMenu(menu);
                        });
    }

    private static ItemStack BACKPACK_ITEM_BASE;

    public static ItemStack createBackpackItem(UUID backpackId, ServerPlayer player) {
        ItemStack itemStack;
        if (BACKPACK_ITEM_BASE == null) {
            itemStack = new ItemStack(Items.PLAYER_HEAD, 1);

            Component name = Backpack.BACKPACK_NAME.apply(player);
            itemStack.set(DataComponents.CUSTOM_NAME, name);

            PropertyMap properties = new PropertyMap();
            Property textures =
                    new Property(
                            "textures",
                            MRIAPI.getInstance().backpack().config().backpackItemTexture);
            properties.put("textures", textures);
            ResolvableProfile profile =
                    new ResolvableProfile(Optional.empty(), Optional.empty(), properties);
            itemStack.set(DataComponents.PROFILE, profile);

            BACKPACK_ITEM_BASE = itemStack.copy();
        } else {
            itemStack = BACKPACK_ITEM_BASE.copy();
        }

        if (itemStack.has(DataComponents.CUSTOM_DATA)) {
            itemStack
                    .get(DataComponents.CUSTOM_DATA)
                    .update(tag -> tag.putString(Backpack.CUSTOM_DATA_ID, backpackId.toString()));
        } else {
            CompoundTag tag = new CompoundTag();
            tag.putString(Backpack.CUSTOM_DATA_ID, backpackId.toString());
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        return itemStack;
    }
}
