package com.ghostipedia.cosmiccore.gtbridge;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import net.minecraft.server.level.ServerLevel;
import com.ghostipedia.cosmiccore.gtbridge.machine.kinetic.AlternatorMachine;

import javax.annotation.Nullable;

public class AlternatorLogic extends RecipeLogic {

    public AlternatorLogic(AlternatorMachine machine) {
        super(machine);
    }

    @Override
    public AlternatorMachine getMachine() {
        return (AlternatorMachine)super.getMachine();
    }

    @Override
    public GTRecipe.ActionResult handleTickRecipe(GTRecipe recipe) {
        if (recipe.hasTick()) {
            var result = recipe.matchRecipe(IO.IN,getMachine(),recipe.tickInputs,false);
            if (result.isSuccess()) {
                recipe.handleTickRecipeIO(IO.IN, this.machine);
                result = recipe.matchRecipe(IO.OUT,getMachine(),recipe.tickOutputs,false);
                if( result.isSuccess()){
                    recipe.handleTickRecipeIO(IO.OUT, this.machine);
                }

            } else {
                return result;
            }
        }
        return GTRecipe.ActionResult.SUCCESS;
    }

    @Override
    public void findAndHandleRecipe() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            var match = getAlternatorRecipe();
            if (match != null) {
                if (match.matchRecipe(this.machine).isSuccess() && match.matchTickRecipe(this.machine).isSuccess()) {
                    setupRecipe(match);
                }
            }
        }
    }

    /**
     * Retrieves the recipe for the Alternator machine.
     *
     * @return The recipe for the Alternator machine, or null if no recipe is found.
     */
    @Nullable
    private GTRecipe getAlternatorRecipe() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            long totalEU = getMachine().outputEnergyContainer.getOutputVoltage();
            var recipe = GTRecipeBuilder.ofRaw()
                    .rpm(64.0001f, true)
                    .perTick(true)
                    .inputStress(totalEU*4)
                    .duration(Integer.MAX_VALUE)
                    .EUt(-totalEU)
                    .buildRawRecipe();
            if (getMachine().outputEnergyContainer.getEnergyStored() >= getMachine().outputEnergyContainer.getEnergyCapacity() - getMachine().outputEnergyContainer.getEnergyCapacity()/10) {
                recipe.getTickOutputContents(EURecipeCapability.CAP).clear();
            }
            if (recipe.matchRecipe(getMachine()).isSuccess() && recipe.matchTickRecipe(getMachine()).isSuccess()) {
                return recipe;
            }

        }
        return null;
    }

}
