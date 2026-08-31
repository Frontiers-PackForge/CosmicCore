package com.ghostipedia.cosmiccore.common.recipe.ingredient;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class CosmicIngredientTypes {

    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.Keys.INGREDIENT_TYPES, CosmicCore.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<SoulEntityIngredient>> SOUL_ENTITY = INGREDIENT_TYPES
            .register("soul_entity", () -> new IngredientType<>(SoulEntityIngredient.CODEC));

    private CosmicIngredientTypes() {}

    public static void register(IEventBus bus) {
        INGREDIENT_TYPES.register(bus);
    }
}
