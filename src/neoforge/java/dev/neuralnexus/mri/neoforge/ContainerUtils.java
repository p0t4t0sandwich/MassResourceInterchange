/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.neoforge;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ContainerUtils {
    public static ListTag saveContainerNBT(
            HolderLookup.Provider registryAccess, Container container) {
        ListTag items = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) {
                CompoundTag tag = new CompoundTag();
                tag.putByte("Slot", (byte) i);
                Tag item = container.getItem(i).save(registryAccess, tag);
                items.add(item);
            }
        }
        return items;
    }

    public static void loadContainerNBT(
            HolderLookup.Provider registryAccess, Container container, ListTag items) {
        container.clearContent();
        for (int i = 0; i < items.size(); i++) {
            CompoundTag tag = items.getCompound(i);
            int slot = tag.getByte("Slot") & 255;
            ItemStack item = ItemStack.parse(registryAccess, tag).orElse(ItemStack.EMPTY);
            if (!item.isEmpty()) {
                if (slot < container.getContainerSize()) {
                    container.setItem(slot, item);
                }
            }
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

        public static MenuConstructor setupMenu(Container container) {
            ContainerSize containerSize = ContainerSize.fromSize(container.getContainerSize());
            return (contCount, inventory, player) ->
                    switch (containerSize) {
                        case Rows_1 -> new DispenserMenu(contCount, inventory, container);
                        case Rows_2 ->
                                new ChestMenu(
                                        MenuType.GENERIC_9x2, contCount, inventory, container, 2);
                        case Rows_3 ->
                                new ChestMenu(
                                        MenuType.GENERIC_9x3, contCount, inventory, container, 3);
                        case Rows_4 ->
                                new ChestMenu(
                                        MenuType.GENERIC_9x4, contCount, inventory, container, 4);
                        case Rows_5 ->
                                new ChestMenu(
                                        MenuType.GENERIC_9x5, contCount, inventory, container, 5);
                        case Rows_6 ->
                                new ChestMenu(
                                        MenuType.GENERIC_9x6, contCount, inventory, container, 6);
                    };
        }
    }
}
