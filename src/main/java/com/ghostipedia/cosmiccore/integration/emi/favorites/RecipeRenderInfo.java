package com.ghostipedia.cosmiccore.integration.emi.favorites;

import com.ghostipedia.cosmiccore.integration.emi.CosmicRecipeFavorite;

public record RecipeRenderInfo(CosmicRecipeFavorite recipe, int startRow, int rowCount) {}
