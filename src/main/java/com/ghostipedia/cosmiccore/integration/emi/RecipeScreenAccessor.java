package com.ghostipedia.cosmiccore.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;

/**
 * Duck interface for accessing getHoveredStack on RecipeScreen via mixin.
 */
public interface RecipeScreenAccessor {

    EmiIngredient getHoveredStack();
}
