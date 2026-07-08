package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.nbt.CompoundTag;

public record CookbookPage(String key, String dishName, long day) {

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("key", key);
        tag.putString("name", dishName);
        tag.putLong("day", day);
        return tag;
    }

    public static CookbookPage fromTag(CompoundTag tag) {
        return new CookbookPage(tag.getString("key"), tag.getString("name"), tag.getLong("day"));
    }
}
