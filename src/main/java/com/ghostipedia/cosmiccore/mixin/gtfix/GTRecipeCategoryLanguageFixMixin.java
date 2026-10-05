package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GTRecipeCategory.class, remap = false)
public abstract class GTRecipeCategoryLanguageFixMixin {

    @Inject(method = "getLanguageKey()Ljava/lang/String;", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$useDefaultRecipeTypeLanguage(CallbackInfoReturnable<String> cir) {
        GTRecipeCategory category = (GTRecipeCategory) (Object) this;
        if (category.id.equals(category.getRecipeType().id)) {
            cir.setReturnValue(category.getRecipeType().getTranslationKey());
        }
    }
}
