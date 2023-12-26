package com.ghostipedia.cosmiccore.gtbridge;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import net.minecraft.server.level.ServerLevel;
import com.ghostipedia.cosmiccore.gtbridge.machine.kinetic.Alternator;

import javax.annotation.Nullable;

public class AlternatorLogic extends RecipeLogic {

    public AlternatorLogic(Alternator machine) {
        super(machine);
    }

    @Override
    public Alternator getMachine() {
        return (Alternator)super.getMachine();
    }

    @Override
    public void findAndHandleRecipe() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            lastRecipe = null;
            var match = getFluidDrillRecipe();
            if (match != null) {
                if (match.matchRecipe(this.machine).isSuccess() && match.matchTickRecipe(this.machine).isSuccess()) {
                    setupRecipe(match);
                }
            }
        }
    }

    @Nullable
    private GTRecipe getFluidDrillRecipe() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            long TotalEU = getMachine().getOutputEnergyContainer().getOutputVoltage();
            var recipe = GTRecipeBuilder.ofRaw()
                    .perTick(true)
                    .inputStress(TotalEU*4)
                    .duration(1)
                    .EUt(-TotalEU)
                    .buildRawRecipe();
            if (recipe.matchRecipe(getMachine()).isSuccess() && recipe.matchTickRecipe(getMachine()).isSuccess()) {
                return recipe;
            }
        }
        return null;
    }

}
