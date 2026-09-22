package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

public final class BiomeldVivariumRecipeLogic extends IndependentBloomwyrmRecipeLogic {

    @Override
    public BiomeldVivariumMachine getMachine() {
        return (BiomeldVivariumMachine) super.getMachine();
    }

    @Override
    public @NotNull Iterator<GTRecipe> searchRecipe() {
        return getMachine().createCultivationRecipe()
                .<Iterator<GTRecipe>>map(recipe -> Collections.singleton(recipe).iterator())
                .orElseGet(Collections::emptyIterator);
    }

    @Override
    protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
        ActionResult result = super.handleRecipeIO(recipe, io);
        if (io == IO.OUT && result.isSuccess()) {
            getMachine().outputCultivationByproducts(recipe);
        }
        return result;
    }

    @Override
    public void onRecipeFinish() {
        super.onRecipeFinish();
        getMachine().advanceCultivationSequence();
    }
}
