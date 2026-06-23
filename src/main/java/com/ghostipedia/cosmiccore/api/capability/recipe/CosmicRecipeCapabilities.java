package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import static com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents.FLUID;
import static com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents.VALID_CAPS;

public class CosmicRecipeCapabilities {

    public static final SoulRecipeCapability SOUL = SoulRecipeCapability.CAP;
    public static final SterileRecipeCapability STERILE = SterileRecipeCapability.CAP;
    public static final EmberRecipeCapability EMBER = EmberRecipeCapability.CAP;

    public static void init() {
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, CosmicCore.id(SOUL.name), SOUL);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, CosmicCore.id(STERILE.name), STERILE);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, CosmicCore.id(EMBER.name), EMBER);

        VALID_CAPS.put(STERILE, FLUID);
    }
}
