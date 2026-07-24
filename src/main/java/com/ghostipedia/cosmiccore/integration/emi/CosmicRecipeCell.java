package com.ghostipedia.cosmiccore.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import org.jetbrains.annotations.Nullable;

public final class CosmicRecipeCell extends CosmicFavorite {

    public CosmicRecipeCell(EmiIngredient stack, long amount, @Nullable EmiRecipe recipe) {
        super(stack, amount, recipe);
    }

    @Override
    protected boolean shouldRenderRecipeFavoriteIndicator() {
        return false;
    }

    @Override
    public EmiIngredient copy() {
        return new CosmicRecipeCell(getStack().copy(), getAmount(), getRecipe());
    }
}
