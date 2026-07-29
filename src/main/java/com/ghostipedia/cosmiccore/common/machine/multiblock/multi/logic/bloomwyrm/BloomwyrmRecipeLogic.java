package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.Optional;

public class BloomwyrmRecipeLogic extends RecipeLogic {

    public Optional<BloomwyrmWorkRequest> createRequest() {
        BloomwyrmUnitMachine unit = getUnit();
        int desiredParallel = unit.getDesiredParallel();
        int maximumCandidate = unit.supportsParallelControl() ? BloomwyrmUnitMachine.MAX_DESIRED_PARALLEL : 1;
        unit.recordParallelEligibility(0);
        Iterator<GTRecipe> recipes = searchRecipe();
        while (recipes.hasNext()) {
            GTRecipe recipe = recipes.next();
            if (!RecipeHelper.checkConditions(recipe, this).isSuccess()) {
                continue;
            }
            int recipeLimit = recipe.data.contains(BloomwyrmRecipeKeys.MAX_PARALLEL) ?
                    Math.max(1, recipe.data.getInt(BloomwyrmRecipeKeys.MAX_PARALLEL)) :
                    maximumCandidate;
            int eligibleParallel = ParallelLogic.getParallelAmountWithoutEU(
                    unit,
                    recipe,
                    Math.min(maximumCandidate, recipeLimit));
            if (eligibleParallel <= 0) {
                continue;
            }
            unit.recordParallelEligibility(eligibleParallel);
            int requestedParallel = Math.min(desiredParallel, eligibleParallel);
            unit.recordParallelRequest(requestedParallel);
            return Optional.of(unit.createWorkRequest(
                    recipe,
                    requestedParallel,
                    eligibleParallel));
        }
        return Optional.empty();
    }

    public boolean startPlannedRecipe(GTRecipe recipe) {
        checkMatchedRecipeAvailable(recipe);
        return isWorking();
    }

    @Override
    public void handleRecipeWorking() {
        int previousProgress = getProgress();
        super.handleRecipeWorking();
        if (getProgress() > previousProgress) {
            getUnit().deliverChargeForProgress(getProgress(), getDuration());
        }
    }

    @Override
    public ActionResult handleTickRecipe(GTRecipe recipe) {
        if (!getUnit().hasCampusPowerThisTick()) {
            return ActionResult.fail(
                    Component.translatable("cosmiccore.bloomwyrm.waiting_for_heart_power"),
                    null,
                    null);
        }
        return super.handleTickRecipe(recipe);
    }

    @Override
    public void onRecipeFinish() {
        long producedCharge = getUnit().completeAllocation();
        markLastRecipeDirty();
        super.onRecipeFinish();
        getUnit().deliverCharge(producedCharge);
    }

    @Override
    protected void regressRecipe() {}

    private BloomwyrmUnitMachine getUnit() {
        return (BloomwyrmUnitMachine) getMachine();
    }
}
