package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiFavorites;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiFavorites.class, remap = false)
public class EmiFavoritesMixin {

    @Inject(method = "addFavorite(Ldev/emi/emi/api/stack/EmiIngredient;Ldev/emi/emi/api/recipe/EmiRecipe;)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void cosmiccore$routeFavorite(EmiIngredient ingredient, EmiRecipe recipe, CallbackInfo ci) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (!manager.isReady() || manager.isProjecting() || !manager.getActiveGroup().isRecipeGroup()) return;
        manager.toggleStack(ingredient, null, recipe);
        ci.cancel();
    }

    @Inject(method = "addFavoriteAt", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$rejectRecipeCellReorder(EmiIngredient ingredient, int offset, CallbackInfo ci) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (manager.isReady() && !manager.isProjecting() && manager.getActiveGroup().isRecipeGroup()) {
            ci.cancel();
        }
    }
}
