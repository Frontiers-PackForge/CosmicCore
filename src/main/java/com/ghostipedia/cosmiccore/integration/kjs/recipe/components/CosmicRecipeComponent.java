package com.ghostipedia.cosmiccore.integration.kjs.recipe.components;

import com.ghostipedia.cosmiccore.api.capability.recipe.EmberRecipeCapability;
import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;

import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;

import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Wrapper;

public class CosmicRecipeComponent {

    public static final ContentJS<Double> EMBER_IN = new ContentJS<>(NumberComponent.ANY_DOUBLE,
            EmberRecipeCapability.CAP,
            false);
    public static final ContentJS<Double> EMBER_OUT = new ContentJS<>(NumberComponent.ANY_DOUBLE,
            EmberRecipeCapability.CAP,
            true);

    public static final RecipeComponent<SoulIngredient> SOUL_STACK = new RecipeComponent<>() {

        @Override
        public Class<?> componentClass() {
            return SoulIngredient.class;
        }

        @Override
        public String componentType() {
            return "soul_stack";
        }

        @Override
        public JsonElement write(RecipeJS recipeJS, SoulIngredient soulIngredient) {
            return SoulIngredient.CODEC.encodeStart(JsonOps.INSTANCE, soulIngredient).result().orElse(null);
        }

        @Override
        public SoulIngredient read(RecipeJS recipeJS, Object o) {
            if (o instanceof Wrapper w) o = w.unwrap();

            if (o instanceof  SoulIngredient soulIngredient) {
                return soulIngredient;
            } else {
                return null;
            }
        }
    };

    public static final ContentJS<SoulIngredient> SOUL_IN = new ContentJS<>(SOUL_STACK, SoulRecipeCapability.CAP,
            false);
    public static final ContentJS<SoulIngredient> SOUL_OUT = new ContentJS<>(SOUL_STACK, SoulRecipeCapability.CAP,
            true);
}
