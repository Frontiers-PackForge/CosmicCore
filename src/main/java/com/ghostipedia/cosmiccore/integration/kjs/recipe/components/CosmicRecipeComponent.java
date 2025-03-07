package com.ghostipedia.cosmiccore.integration.kjs.recipe.components;

import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;

import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;

import dev.latvian.mods.kubejs.recipe.component.NumberComponent;

public class CosmicRecipeComponent {

    public static final ContentJS<Integer> SOUL_IN = new ContentJS<>(NumberComponent.ANY_INT, SoulRecipeCapability.CAP,
            false);
    public static final ContentJS<Integer> SOUL_OUT = new ContentJS<>(NumberComponent.ANY_INT, SoulRecipeCapability.CAP,
            true);
}
