package com.ghostipedia.cosmiccore.common.power.steam;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import java.util.List;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ALLOY_SMELTER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.BENDER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.COMPRESSOR_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.EXTRACTOR_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORGE_HAMMER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.FURNACE_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ROCK_BREAKER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.WIREMILL_RECIPES;

public final class SteamRecipeViewerRegistration {

    private static final List<GTRecipeType> RECIPE_TYPES = List.of(
            EXTRACTOR_RECIPES,
            MACERATOR_RECIPES,
            COMPRESSOR_RECIPES,
            FORGE_HAMMER_RECIPES,
            FURNACE_RECIPES,
            ALLOY_SMELTER_RECIPES,
            ROCK_BREAKER_RECIPES,
            BENDER_RECIPES,
            WIREMILL_RECIPES);

    private SteamRecipeViewerRegistration() {}

    public static void init() {
        for (var recipeType : RECIPE_TYPES) {
            var layout = recipeType.getUiLayout();
            if (layout == null) {
                throw new IllegalStateException("Missing recipe UI layout for " + recipeType);
            }
            if (!layout.getRecipeUIModifiers().contains(SteamRecipeViewerModifier.INSTANCE)) {
                layout.getRecipeUIModifiers().add(SteamRecipeViewerModifier.INSTANCE);
            }
        }
    }
}
