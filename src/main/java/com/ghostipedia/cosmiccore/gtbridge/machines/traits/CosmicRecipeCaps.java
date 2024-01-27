package com.ghostipedia.cosmiccore.gtbridge.machines.traits;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistries;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

public class CosmicRecipeCaps {

    public static final AirRecipeCapability PRESSURE = AirRecipeCapability.CAP;




    public static void init() {
        GTRegistries.RECIPE_CAPABILITIES.register(PRESSURE.name, PRESSURE);

    }
}
