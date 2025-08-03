package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.machine.trait.MultiRecipeLogic;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

public class WorkableElectricMultiLogicMultiblockMachine extends WorkableElectricMultiblockMachine {

    private int logics;

    public WorkableElectricMultiLogicMultiblockMachine(IMachineBlockEntity holder, int logics) {
        super(holder, logics);
        this.logics = logics;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        var rl = new MultiRecipeLogic(this);
        if(args[0] instanceof Number number) {
            logics = number.intValue();
            for (int i = 0; i < logics; i++) {
                rl.addLogic(new RecipeLogic(this));
            }
        }
        return rl;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        ((MultiRecipeLogic)recipeLogic).resetAllLogics();
    }


}
