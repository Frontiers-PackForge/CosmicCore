package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.capability.recipe.EmberRecipeCapability;
import com.ghostipedia.cosmiccore.gtbridge.EmberRecipeUI;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GTRecipeTypeUILayout.Builder.class, remap = false)
public abstract class RecipeUILayoutEmberMixin {

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void cosmiccore$registerEmberDisplay(GTRecipeType recipeType, CallbackInfo ci) {
        GTRecipeTypeUILayout.Builder self = (GTRecipeTypeUILayout.Builder) (Object) this;
        self.setRecipeViewerLayoutCapabilityLayoutBuilder(EmberRecipeCapability.CAP, EmberRecipeUI.LAYOUT);
        self.setCapabilityContentBuilder(EmberRecipeCapability.CAP, EmberRecipeUI.CONTENT);
    }
}
