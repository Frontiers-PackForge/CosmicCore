package com.ghostipedia.cosmiccore.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

/**
 * Bridges the removed 1.20.1 ItemStack NBT API onto 1.21 DataComponents.CUSTOM_DATA.
 * readTag returns a copy (never null); mutateTag applies changes and writes back.
 */
public final class ItemData {

    private ItemData() {}

    public static CompoundTag readTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void mutateTag(ItemStack stack, Consumer<CompoundTag> mutator) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(mutator));
    }

    public static void writeTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static CompoundTag readElement(ItemStack stack, String key) {
        return readTag(stack).getCompound(key);
    }

    public static void mutateElement(ItemStack stack, String key, Consumer<CompoundTag> mutator) {
        mutateTag(stack, root -> {
            CompoundTag element = root.getCompound(key);
            mutator.accept(element);
            root.put(key, element);
        });
    }
}
