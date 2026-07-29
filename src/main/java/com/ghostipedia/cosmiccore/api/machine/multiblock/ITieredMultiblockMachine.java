package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

public interface ITieredMultiblockMachine {

    int getStructureTier();

    void setStructureTier(int tier);

    default int getStructureTierStreak() {
        return 0;
    }

    default boolean matchesStructureTierStreak(GTRecipe recipe) {
        return false;
    }

    default void beginStructureTierRecipe(GTRecipe recipe) {}

    default void completeStructureTierRecipe(GTRecipe recipe) {}
}
