package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

public record SignatureMeal(String key, String dishName, Item dish, double heartBonus, double regenBonus,
                            long inscribedDay) {

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("key", key);
        tag.putString("name", dishName);
        tag.putString("dish", BuiltInRegistries.ITEM.getKey(dish).toString());
        tag.putDouble("hearts", heartBonus);
        tag.putDouble("regen", regenBonus);
        tag.putLong("day", inscribedDay);
        return tag;
    }

    @Nullable
    public static SignatureMeal fromTag(CompoundTag tag) {
        Item dish = FoodNbt.item(tag.getString("dish"));
        if (dish == null) return null;
        return new SignatureMeal(tag.getString("key"), tag.getString("name"), dish, tag.getDouble("hearts"),
                tag.getDouble("regen"), tag.getLong("day"));
    }
}
