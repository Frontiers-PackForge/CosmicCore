package com.ghostipedia.cosmiccore.common.data.recipe;

import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.MagneticFieldMachine;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import java.util.Optional;

public class CosmicRecipeModifiers {

    public static ModifierFunction vomahineReactorOC(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof MagneticFieldMachine magnetMachine)) {
            return RecipeModifier.nullWrongType(MagneticFieldMachine.class, machine);
        }
        final var magnetStrength = magnetMachine.getFieldStrength();
        long EUt = RecipeHelper.getOutputEUt(recipe);
        int actualParallel = ParallelLogic.getParallelAmount(magnetMachine, recipe, 16);
        long maxReactorVoltage = magnetMachine.getOverclockVoltage();
        float recipeDuration = (recipe.duration);
        float durationModifier = recipeDuration * actualParallel / 20;
        // Parallel is ALWAYS capped to 16
        // Check that the damn thing actually creates EU
        if (EUt <= 0 || maxReactorVoltage <= EUt) return ModifierFunction.NULL;
        if (!recipe.data.contains("min_field") || recipe.data.getInt("min_field") > magnetStrength) {
            return ModifierFunction.NULL;
        }
        if (!magnetMachine.isGenerator()) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > magnetMachine.getTier()) {
                return ModifierFunction.NULL;
            }
        }
        // EU Outputs is always 16A of the respective recipe (If it can).
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(actualParallel))
                // .durationMultiplier(durationModifier) this just actually causes hell on earth so ignore for now
                .eutMultiplier(actualParallel)
                .parallels(actualParallel)
                .build();
    }
    public static ModifierFunction chemicalVatLogic(MetaMachine machine, GTRecipe recipe){
        if (machine instanceof WorkableMultiblockMachine vatMachine) {
            Optional<IParallelHatch> optionalIParallelHatch = vatMachine.getParts().stream().filter(IParallelHatch.class::isInstance).map(IParallelHatch.class::cast).findAny();
            if (optionalIParallelHatch.isPresent()){
                IParallelHatch parallelHatch = optionalIParallelHatch.get();
                var actualParallel = 1;
                if (parallelHatch.getCurrentParallel() != 0) {
                    long EUt = RecipeHelper.getInputEUt(recipe);
                    actualParallel = ParallelLogic.getParallelAmount(vatMachine,recipe, parallelHatch.getCurrentParallel());

                }
                return  ModifierFunction.builder()
                        .modifyAllContents(ContentModifier.multiplier(actualParallel))
                        .eutMultiplier(actualParallel)
                        .parallels(actualParallel)
                        .durationMultiplier(actualParallel * 0.25F)
                        .build();
            }
        }
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
