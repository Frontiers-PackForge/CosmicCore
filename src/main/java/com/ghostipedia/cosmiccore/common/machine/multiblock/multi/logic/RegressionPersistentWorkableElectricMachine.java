package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

public class RegressionPersistentWorkableElectricMachine extends WorkableElectricMultiblockMachine {

    public RegressionPersistentWorkableElectricMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    // TODO: Machine Fails and recipe voids if per-Tick is not satisfied.
    @Override
    public boolean onWorking() {
        // var logic = this.getRecipeLogic();
        // var recipe = recipeLogic.getLastRecipe();
        //
        // if (recipe != null && recipeLogic.isWorking()) {
        // var validIngredients = RecipeHelper.matchTickRecipe(this.getRecipeLogic().machine, recipe);
        // if (!validIngredients.isSuccess()) {
        // recipeLogic.interruptRecipe();
        // recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
        // return false;
        // }
        // return super.onWorking();
        // }
        return super.onWorking();
    }
}
