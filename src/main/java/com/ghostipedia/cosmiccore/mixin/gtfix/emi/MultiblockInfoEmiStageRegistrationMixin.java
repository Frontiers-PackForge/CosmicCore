package com.ghostipedia.cosmiccore.mixin.gtfix.emi;

import com.ghostipedia.cosmiccore.integration.emi.MultiblockStageEmiRecipe;

import com.gregtechceu.gtceu.integration.recipeviewer.emi.MultiblockInfoEmiCategory;

import dev.emi.emi.api.EmiRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MultiblockInfoEmiCategory.class, remap = false)
public class MultiblockInfoEmiStageRegistrationMixin {

    @Inject(method = "registerDisplays", at = @At("TAIL"))
    private static void cosmiccore$registerVariableStages(EmiRegistry registry, CallbackInfo ci) {
        MultiblockStageEmiRecipe.register(registry);
    }
}
