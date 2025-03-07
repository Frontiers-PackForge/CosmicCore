package com.ghostipedia.cosmiccore.common.data.recipe;

import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.MagneticFieldMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

public class CosmicRecipeModifiers {

    public static ModifierFunction vomahineReactorOC(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof MagneticFieldMachine magnetMachine)) {
            return RecipeModifier.nullWrongType(MagneticFieldMachine.class, machine);
        }
        final var magnetStrength = magnetMachine.getFieldStrength();
        // Clowns are Real, Ghostipedia is alive in Spirit, this implementation makes the generator parallel to update
        // power demands vs just..... Doing whatever the hell we did before.
        long EUt = RecipeHelper.getOutputEUt(recipe);
        long maxVomahineReactorVoltage = magnetMachine.getOverclockVoltage();
        // Check that the damn thing actually creates EU
        if (EUt <= 0 || maxVomahineReactorVoltage <= EUt) return ModifierFunction.NULL;
        // WARNING; DO NOT PARALLEL, YOU WILL **DELETE** PLAYERS ~~BASES~~ **WORLDS**.
        if (!recipe.data.contains("min_field") || recipe.data.getInt("min_field") > magnetStrength) {
            return ModifierFunction.NULL;
        }
        if (!magnetMachine.isGenerator()) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > magnetMachine.getTier()) {
                return ModifierFunction.NULL;
            }
        }
        // There is No Modification Done, just out the Recipe EU
        return ModifierFunction.IDENTITY;
    }

    // TODO; FIX IT!
    // public static GTRecipe vomahineChemicalPlantParallel(MetaMachine machine, @NotNull GTRecipe recipe, OCParams
    // ocParams, OCResult ocResult) {
    // if (machine instanceof WorkableElectricMultiblockMachine vomahineMachine) {
    // Optional<IParallelHatch> optional = vomahineMachine.getParts().stream().filter(IParallelHatch.class::isInstance)
    // .map(IParallelHatch.class::cast).findAny();
    // if (optional.isPresent()) {
    // IParallelHatch hatch = optional.get();
    // if (hatch.getCurrentParallel() != 0) {
    // var result = GTRecipeModifiers.accurateParallel(machine, recipe, hatch.getCurrentParallel(), false);
    // recipe = result.getFirst() == recipe ? result.getFirst().copy() : result.getFirst();
    // var smartDuration = (recipe.duration * hatch.getCurrentParallel()) / 2;
    // int parallelValue = result.getSecond();
    // recipe.duration = smartDuration;
    // ocResult.init(RecipeHelper.getInputEUt(recipe), smartDuration, parallelValue, ocResult.getOcLevel());
    // return recipe;
    // }
    // }
    // var result = GTRecipeModifiers.accurateParallel(machine, recipe, 0, false);
    // recipe = result.getFirst() == recipe ? result.getFirst().copy() : result.getFirst();
    // var smartDuration = recipe.duration / 2;
    // int parallelValue = result.getSecond();
    // recipe.duration = smartDuration;
    // ocResult.init(RecipeHelper.getInputEUt(recipe), smartDuration, parallelValue, ocResult.getOcLevel());
    // return recipe;
    // }
    // return null;
    // }
}
