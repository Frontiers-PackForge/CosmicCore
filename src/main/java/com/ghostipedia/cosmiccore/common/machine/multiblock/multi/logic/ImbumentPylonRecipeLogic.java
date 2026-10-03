package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.network.chat.Component;

public final class ImbumentPylonRecipeLogic extends RecipeLogic {

    private ImbumentPylonMachine pylon() {
        return (ImbumentPylonMachine) getMachine();
    }

    @Override
    public ActionResult handleTickRecipe(GTRecipe recipe) {
        if (pylon().campusAccess().isEmpty()) {
            return ActionResult.fail(Component.translatable("cosmiccore.machine.imbument_pylon.status.unlinked"), null,
                    null);
        }
        if (!pylon().canProcessRecipe(recipe)) {
            return ActionResult.fail(
                    Component.translatable("cosmiccore.machine.imbument_pylon.status.insufficient_rating"), null, null);
        }
        return super.handleTickRecipe(recipe);
    }

    @Override
    public void setupRecipe(GTRecipe recipe) {
        super.setupRecipe(recipe);
        if (isWorking()) pylon().captureInputs();
    }

    @Override
    protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
        ActionResult result = super.handleRecipeIO(recipe, io);
        if (io == IO.OUT && result.isSuccess()) pylon().captureOutput(recipe);
        return result;
    }
}
