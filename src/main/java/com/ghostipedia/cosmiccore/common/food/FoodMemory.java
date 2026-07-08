package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

public record FoodMemory(String dishName, Item dish, double heartBonus, double regenBonus, int quality,
                         int sharedWith, long day) {

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", dishName);
        tag.putString("dish", BuiltInRegistries.ITEM.getKey(dish).toString());
        tag.putDouble("hearts", heartBonus);
        tag.putDouble("regen", regenBonus);
        tag.putInt("quality", quality);
        tag.putInt("shared", sharedWith);
        tag.putLong("day", day);
        return tag;
    }

    @Nullable
    public static FoodMemory fromTag(CompoundTag tag) {
        if (!tag.contains("dish")) return null;
        Item dish = FoodNbt.item(tag.getString("dish"));
        if (dish == null) return null;
        return new FoodMemory(tag.getString("name"), dish, tag.getDouble("hearts"), tag.getDouble("regen"),
                tag.getInt("quality"), tag.getInt("shared"), tag.getLong("day"));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(dishName);
        buf.writeVarInt(BuiltInRegistries.ITEM.getId(dish));
        buf.writeDouble(heartBonus);
        buf.writeDouble(regenBonus);
        buf.writeVarInt(quality);
        buf.writeVarInt(sharedWith);
        buf.writeVarLong(day);
    }

    public static FoodMemory read(FriendlyByteBuf buf) {
        return new FoodMemory(buf.readUtf(), BuiltInRegistries.ITEM.byId(buf.readVarInt()), buf.readDouble(),
                buf.readDouble(), buf.readVarInt(), buf.readVarInt(), buf.readVarLong());
    }
}
