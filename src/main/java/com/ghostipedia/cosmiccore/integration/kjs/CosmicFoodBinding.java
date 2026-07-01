package com.ghostipedia.cosmiccore.integration.kjs;

import com.ghostipedia.cosmiccore.common.food.CosmicFoodRegistry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

public final class CosmicFoodBinding {

    private CosmicFoodBinding() {}

    public static void define(String itemId, Consumer<CosmicFoodBuilder> callback) {
        ResourceLocation id = ResourceLocation.parse(itemId);
        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            throw new IllegalArgumentException("CosmicFood.define: unknown item " + itemId);
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        CosmicFoodBuilder builder = new CosmicFoodBuilder(item);
        callback.accept(builder);
        CosmicFoodRegistry.register(item, builder.build());
    }
}
