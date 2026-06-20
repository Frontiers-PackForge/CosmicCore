package com.ghostipedia.cosmiccore.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import org.jetbrains.annotations.Nullable;

/**
 * Duck interface for accessing recipe data on RecipeScreen via mixin.
 */
public interface RecipeScreenAccessor {

    EmiIngredient getHoveredStack(int mouseX, int mouseY);

    @Nullable
    EmiRecipe getHoveredRecipe(int mouseX, int mouseY);
}
