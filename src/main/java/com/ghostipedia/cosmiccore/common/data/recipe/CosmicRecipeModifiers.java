package com.ghostipedia.cosmiccore.common.data.recipe;

import com.ghostipedia.cosmiccore.api.machine.multiblock.MagnetWorkableElectricMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.MagneticFieldMachine;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import org.jetbrains.annotations.NotNull;

public class CosmicRecipeModifiers {
    public static GTRecipe magnetRecipe(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof MagneticFieldMachine magnetMachine) {
            final var magnetStrength = magnetMachine.getFieldStrength();
            if (!recipe.data.contains("min_field") || recipe.data.getInt("min_field") > magnetStrength) {
                return null;
            }
            if(!magnetMachine.isGenerator()){
                if (RecipeHelper.getRecipeEUtTier(recipe) > magnetMachine.getTier()) {
                    return null;
                }
            }

            return recipe;
        }
        return null;
    }
}
