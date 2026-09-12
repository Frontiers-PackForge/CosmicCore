package com.ghostipedia.cosmiccore.common.rate;

import org.jetbrains.annotations.Nullable;

public interface RateCalculatorRecipeHistory {

    @Nullable
    String cosmiccore$getLastCompletedRecipeId();

    void cosmiccore$setLastCompletedRecipeId(@Nullable String id);
}
