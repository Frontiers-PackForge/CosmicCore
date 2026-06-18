package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.mojang.datafixers.util.Pair;

import static com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents.FLUID_IN;
import static com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents.FLUID_OUT;
import static com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents.VALID_CAPS;

public class CosmicRecipeCapabilities {

    public static final SoulRecipeCapability SOUL = SoulRecipeCapability.CAP;
    public static final SterileRecipeCapability STERILE = SterileRecipeCapability.CAP;
    // TODO(embers): EMBER recipe capability shelved with Embers (bead cosmiccore-42.14)

    public static void init() {
        GTRegistries.RECIPE_CAPABILITIES.register(SOUL.name, SOUL);
        GTRegistries.RECIPE_CAPABILITIES.register(STERILE.name, STERILE);

        VALID_CAPS.put(STERILE, Pair.of(FLUID_IN, FLUID_OUT));
    }
}
