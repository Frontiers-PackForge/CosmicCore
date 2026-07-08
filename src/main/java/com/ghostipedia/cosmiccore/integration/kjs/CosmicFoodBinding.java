package com.ghostipedia.cosmiccore.integration.kjs;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodRegistry;
import com.ghostipedia.cosmiccore.common.food.FoodArchetypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class CosmicFoodBinding {

    private CosmicFoodBinding() {}

    public static void define(String itemId, Consumer<CosmicFoodBuilder> callback) {
        Item item = find(itemId, "define");
        if (item == null) return;
        CosmicFoodBuilder builder = new CosmicFoodBuilder(item);
        callback.accept(builder);
        CosmicFoodRegistry.register(item, builder.build());
    }

    public static void archetype(String name, Consumer<CosmicFoodArchetypeBuilder> callback) {
        CosmicFoodArchetypeBuilder builder = new CosmicFoodArchetypeBuilder(name);
        callback.accept(builder);
        FoodArchetypes.register(builder.build());
        CosmicFoodRegistry.clearResolved();
    }

    public static void assign(String pattern, String archetypeName) {
        FoodArchetypes.assign(pattern, archetypeName);
        CosmicFoodRegistry.clearResolved();
    }

    public static void exclude(String itemId) {
        Item item = find(itemId, "exclude");
        if (item == null) return;
        CosmicFoodRegistry.exclude(item);
    }

    public static void vile(String itemId) {
        Item item = find(itemId, "vile");
        if (item == null) return;
        CosmicFoodRegistry.vile(item);
    }

    public static void tailor(String itemId, Consumer<CosmicFoodTailorBuilder> callback) {
        Item item = find(itemId, "tailor");
        if (item == null) return;
        CosmicFoodTailorBuilder builder = new CosmicFoodTailorBuilder();
        callback.accept(builder);
        CosmicFoodRegistry.tailor(item, builder.build());
    }

    @Nullable
    private static Item find(String itemId, String verb) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            CosmicCore.LOGGER.warn("CosmicFood.{}: unknown item {}, skipping", verb, itemId);
            return null;
        }
        return BuiltInRegistries.ITEM.get(id);
    }
}
