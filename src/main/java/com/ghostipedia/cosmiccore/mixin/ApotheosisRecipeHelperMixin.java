package com.ghostipedia.cosmiccore.mixin;

import dev.shadowsoffire.placebo.recipe.RecipeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("deprecation")
@Mixin(value = RecipeHelper.RecipeFactory.class, remap = false)
public class ApotheosisRecipeHelperMixin {

    @Inject(method = "addShaped", at = @At("HEAD"), cancellable = true)
    public void cosmicCore$injectAddShaped(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "addShapeless", at = @At("HEAD"), cancellable = true)
    public void cosmicCore$injectAddShapeless(CallbackInfo ci) {
        ci.cancel();
    }
}
