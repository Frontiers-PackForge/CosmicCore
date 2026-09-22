package com.ghostipedia.cosmiccore.common.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

public final class PhysicalRecipeParallel {

    private PhysicalRecipeParallel() {}

    public static int highestMatchingWithoutEnergy(IRecipeCapabilityHolder holder, GTRecipe recipe, int maximum) {
        int lower = 0;
        int upper = Math.max(0, maximum);
        while (lower < upper) {
            int parallel = lower + (upper - lower + 1) / 2;
            GTRecipe candidate = recipe.copy(ContentModifier.multiplier(parallel), false);
            candidate.tickInputs.remove(EURecipeCapability.CAP);
            if (RecipeHelper.matchContents(holder, candidate).isSuccess()) {
                lower = parallel;
            } else {
                upper = parallel - 1;
            }
        }
        return lower;
    }
}
