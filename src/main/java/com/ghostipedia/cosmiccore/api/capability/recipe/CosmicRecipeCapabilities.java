package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import static com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents.FLUID;
import static com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents.VALID_CAPS;

public class CosmicRecipeCapabilities {

    public static final SoulRecipeCapability SOUL = SoulRecipeCapability.CAP;
    public static final SterileRecipeCapability STERILE = SterileRecipeCapability.CAP;
    public static final EmberRecipeCapability EMBER = EmberRecipeCapability.CAP;

    public static void init() {
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, SOUL.id, SOUL);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, STERILE.id, STERILE);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, EMBER.id, EMBER);

        VALID_CAPS.put(STERILE, FLUID);
    }
}
