package com.ghostipedia.cosmiccore.gtbridge.recipe;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

public class CosmicRecipeCaps {
    public static final CosmicPressureRecipeCapability PRESSURE = CosmicPressureRecipeCapability.CAP;


    public static void init() {
        GTRegistries.RECIPE_CAPABILITIES.register(PRESSURE.name, PRESSURE);
    }
}
