package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import static com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents.FLUID;
import static com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents.VALID_CAPS;

public class CosmicRecipeCapabilities {

    public static final SoulRecipeCapability SOUL = SoulRecipeCapability.CAP;
    public static final SterileRecipeCapability STERILE = SterileRecipeCapability.CAP;
    public static final EmberRecipeCapability EMBER = EmberRecipeCapability.CAP;

    public static void init() {
        CosmicRegistration.REGISTRATE.simple(SOUL.id.getPath(), GTRegistries.Keys.RECIPE_CAPABILITY, () -> SOUL);
        CosmicRegistration.REGISTRATE.simple(STERILE.id.getPath(), GTRegistries.Keys.RECIPE_CAPABILITY, () -> STERILE);
        CosmicRegistration.REGISTRATE.simple(EMBER.id.getPath(), GTRegistries.Keys.RECIPE_CAPABILITY, () -> EMBER);

        VALID_CAPS.put(STERILE, FLUID);
    }
}
