package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorkableMultiblockMachine.class, remap = false)
public abstract class WorkableMultiblockRecipeTypeCacheFixMixin {

    @WrapOperation(
                   method = "cycleActiveRecipeType()V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/machine/trait/recipe/RecipeLogic;updateTickSubscription()V",
                            remap = false),
                   require = 1,
                   remap = false)
    private void cosmiccore$dirtyCycledRecipeType(RecipeLogic recipeLogic, Operation<Void> original) {
        original.call(recipeLogic);
        recipeLogic.markLastRecipeDirty();
    }

    @Inject(method = "formStructure(Ljava/lang/String;)V", at = @At("TAIL"), require = 1, remap = false)
    private void cosmiccore$restoreCycledRecipeTypeInvalidation(String substructureName, CallbackInfo ci) {
        WorkableMultiblockMachine machine = (WorkableMultiblockMachine) (Object) this;
        RecipeLogic recipeLogic = machine.getRecipeLogic();
        var lastRecipe = recipeLogic.getLastRecipe();
        if (lastRecipe != null && lastRecipe.getType() != machine.getRecipeType()) {
            recipeLogic.markLastRecipeDirty();
        }
    }
}
