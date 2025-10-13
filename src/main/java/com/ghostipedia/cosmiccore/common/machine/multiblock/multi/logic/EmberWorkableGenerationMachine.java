package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

public class EmberWorkableGenerationMachine extends WorkableElectricMultiblockMachine {

    public EmberWorkableGenerationMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    //Machine Fails and recipe voids if per-Tick is not satisfied.
    @Override
    public boolean onWorking() {
        var recipe = recipeLogic.getLastRecipe();
        if (recipe != null) {
            var validIngredients = RecipeHelper.matchTickRecipe(this.getRecipeLogic().machine, recipe);
            if (!validIngredients.isSuccess()){
                recipeLogic.interruptRecipe();
                recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
                return false;
            }
            return true;
        }
        return super.onWorking();
    }
}
