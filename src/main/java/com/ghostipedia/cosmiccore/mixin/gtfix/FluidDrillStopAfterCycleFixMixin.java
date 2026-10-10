package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.common.machine.trait.FluidDrillLogic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidDrillLogic.class, remap = false)
public abstract class FluidDrillStopAfterCycleFixMixin extends RecipeLogic {

    @Inject(method = "onRecipeFinish",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/common/machine/trait/FluidDrillLogic;depleteVein()V",
                     shift = At.Shift.AFTER),
            cancellable = true)
    private void cosmiccore$pauseAfterCurrentCycle(CallbackInfo ci) {
        if (!suspendAfterFinish) return;
        setStatus(Status.SUSPEND);
        consecutiveRecipes = 0;
        progress = 0;
        duration = 0;
        isActive = false;
        lastRecipe = null;
        lastUnrolledRecipe = null;
        syncDataHolder.resyncAllFields();
        ci.cancel();
    }
}
