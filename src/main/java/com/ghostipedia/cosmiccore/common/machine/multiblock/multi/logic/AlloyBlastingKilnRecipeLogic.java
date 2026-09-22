package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.network.chat.Component;

public final class AlloyBlastingKilnRecipeLogic extends RecipeLogic {

    @Override
    public void setupRecipe(GTRecipe recipe) {
        AlloyBlastingKilnMachine kiln = kiln();
        if (!kiln.reservePyroflux(recipe)) {
            setStatus(Status.IDLE);
            return;
        }
        super.setupRecipe(recipe);
        if (!isWorking()) kiln.releasePyrofluxReservation();
    }

    @Override
    public ActionResult handleTickRecipe(GTRecipe recipe) {
        AlloyBlastingKilnMachine kiln = kiln();
        if (!kiln.ensureRunningReservation(recipe, getProgress(), getDuration())) {
            return ActionResult.fail(
                    Component.translatable("cosmiccore.machine.alloy_blasting_kiln.waiting_pyroflux"),
                    EURecipeCapability.CAP,
                    IO.IN);
        }
        ActionResult result = super.handleTickRecipe(recipe);
        if (!result.isSuccess()) return result;
        return kiln.consumePyrofluxForProgress(getProgress(), getDuration()) ? result : ActionResult.fail(
                Component.translatable("cosmiccore.machine.alloy_blasting_kiln.waiting_pyroflux"),
                EURecipeCapability.CAP,
                IO.IN);
    }

    @Override
    public void onRecipeFinish() {
        kiln().completePyrofluxReservation();
        super.onRecipeFinish();
    }

    @Override
    public void interruptRecipe() {
        kiln().releasePyrofluxReservation();
        super.interruptRecipe();
    }

    @Override
    public void resetRecipeLogic() {
        if (getMachine() instanceof AlloyBlastingKilnMachine kiln) kiln.releasePyrofluxReservation();
        super.resetRecipeLogic();
    }

    @Override
    protected void regressRecipe() {}

    private AlloyBlastingKilnMachine kiln() {
        return (AlloyBlastingKilnMachine) getMachine();
    }
}
