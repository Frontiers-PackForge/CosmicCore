package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.rate.RateCalculatorRecipeHistory;
import com.ghostipedia.cosmiccore.common.rate.RateCalculatorTracker;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RecipeLogic.class, remap = false)
public abstract class RateCalculatorRecipeLogicMixin implements RateCalculatorRecipeHistory {

    @Unique
    @SaveField(nbtKey = "cosmiccore_rate_calculator_last_completed_recipe")
    private String cosmiccore$lastCompletedRecipeId;

    @Override
    public String cosmiccore$getLastCompletedRecipeId() {
        return cosmiccore$lastCompletedRecipeId;
    }

    @Override
    public void cosmiccore$setLastCompletedRecipeId(String id) {
        cosmiccore$lastCompletedRecipeId = id;
    }

    @WrapOperation(method = "setupRecipe",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/machine/trait/recipe/RecipeLogic;handleRecipeIO(Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;)Lcom/gregtechceu/gtceu/api/recipe/ActionResult;"))
    private ActionResult cosmiccore$recordSuccessfulStart(RecipeLogic logic, GTRecipe recipe, IO io,
                                                          Operation<ActionResult> original) {
        ActionResult result = original.call(logic, recipe, io);
        if (result.isSuccess() && io == IO.IN) RateCalculatorTracker.onRecipeStarted(logic, recipe);
        return result;
    }

    @WrapOperation(method = "onRecipeFinish",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/machine/trait/recipe/RecipeLogic;handleRecipeIO(Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;)Lcom/gregtechceu/gtceu/api/recipe/ActionResult;"))
    private ActionResult cosmiccore$recordSuccessfulCompletion(RecipeLogic logic, GTRecipe recipe, IO io,
                                                               Operation<ActionResult> original) {
        ActionResult result = original.call(logic, recipe, io);
        if (result.isSuccess() && io == IO.OUT) RateCalculatorTracker.onRecipeCompleted(logic,
                logic.getLastOriginRecipe());
        return result;
    }
}
