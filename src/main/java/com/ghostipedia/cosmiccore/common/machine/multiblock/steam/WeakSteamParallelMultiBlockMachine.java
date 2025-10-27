package com.ghostipedia.cosmiccore.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

import org.jetbrains.annotations.NotNull;

public class WeakSteamParallelMultiBlockMachine extends SteamParallelMultiblockMachine implements IDisplayUIMachine {

    // if in millibuckets, this is 0.5, Meaning 0.5mb of steam -> 1 EU
    private static final double CONVERSION_RATE = 0.5D;

    public WeakSteamParallelMultiBlockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        setMaxParallels(4);
    }

    @Override
    public double getConversionRate() {
        return CONVERSION_RATE;
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (RecipeHelper.getRecipeEUtTier(recipe) > GTValues.LV) return ModifierFunction.NULL;
//        long euTick = RecipeHelper.getRecipeEUtTier(recipe);
        int parallel = ParallelLogic.getParallelAmount(machine, recipe, 4);
//         double eutMulti = (euTick * 0.5 * parallel <= 32) ? (parallel * 0.5) : (32.0 / euTick);

        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallel))
                .outputModifier(ContentModifier.multiplier(parallel))
                .durationMultiplier(parallel * 0.75)
                .parallels(parallel)
                .build();
    }
}
