package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicFavorite;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.runtime.EmiFavorite;
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
    private static void cosmiccore$onAddFavorite(EmiIngredient stack, EmiRecipe context, CallbackInfo ci) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (EmiInput.isControlDown()) {
            long amount = stack.getAmount();
            if (!stack.getEmiStacks().isEmpty()) {
                EmiStack first = stack.getEmiStacks().get(0);
                amount = first.getAmount();
            }

            CosmicBookmarkManager.getInstance().addFavoriteWithAmount(stack, context, amount);
            ci.cancel();
            return;
        }

        CosmicBookmarkManager.getInstance().addFavorite(stack, context);
        ci.cancel();
    }

    @Inject(method = "addFavoriteAt",
            at = @At("HEAD"),
            cancellable = true)
    private static void cosmiccore$onAddFavoriteAt(EmiIngredient stack, int offset, CallbackInfo ci) {
        EmiFavorite favorite;

        // Check if it's already an EmiFavorite
        if (stack instanceof EmiFavorite fav) {
            // If CTRL is held and it's not already a CosmicFavorite with amount, add amount
            if (EmiInput.isControlDown() && !(fav instanceof CosmicFavorite cf && cf.hasCustomAmount())) {
                long amount = fav.getStack().getAmount();
                favorite = CosmicFavorite.withAmount(fav.getStack(), fav.getRecipe(), amount);
            } else {
                favorite = fav;
            }
        } else {
            // New favorite being added
            if (EmiInput.isControlDown()) {
                long amount = stack.getAmount();
                favorite = CosmicFavorite.withAmount(stack, null, amount);
            } else {
                favorite = new EmiFavorite(stack, null);
            }
        }

        CosmicBookmarkManager.getInstance().addFavoriteAt(favorite, offset);
        ci.cancel();
    }
}
