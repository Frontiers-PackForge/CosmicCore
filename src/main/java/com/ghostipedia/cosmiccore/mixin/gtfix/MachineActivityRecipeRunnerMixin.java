package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;
import com.ghostipedia.cosmiccore.common.machine.trait.activity.MachineActivityRuntime;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeRunner;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipeRunner.class, remap = false)
public abstract class MachineActivityRecipeRunnerMixin {

    @Shadow
    @Final
    private boolean simulated;
    @Unique
    private IRecipeCapabilityHolder cosmiccore$activityHolder;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cosmiccore$holder(GTRecipe recipe, IO io, boolean isTick, IRecipeCapabilityHolder holder,
                                   Map<RecipeCapability<?>, ?> caches, boolean simulated, CallbackInfo ci) {
        cosmiccore$activityHolder = holder;
    }

    @WrapMethod(method = "handle")
    private ActionResult cosmiccore$scope(Map<RecipeCapability<?>, ?> entries, Operation<ActionResult> original) {
        try (ActivityScope ignored = MachineActivityRuntime.scope(cosmiccore$activityHolder, simulated)) {
            return original.call(entries);
        }
    }
}
