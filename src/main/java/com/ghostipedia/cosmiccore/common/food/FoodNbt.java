package com.ghostipedia.cosmiccore.common.food;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

public final class FoodNbt {

    private FoodNbt() {}

    @Nullable
    public static Item item(String rawId) {
        ResourceLocation id = ResourceLocation.tryParse(rawId);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            CosmicCore.LOGGER.warn("Dropping saved food entry for unresolvable item {}", rawId);
            return null;
        }
        return BuiltInRegistries.ITEM.get(id);
    }
}
