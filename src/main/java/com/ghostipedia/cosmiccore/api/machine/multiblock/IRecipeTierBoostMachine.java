package com.ghostipedia.cosmiccore.api.machine.multiblock;

public interface IRecipeTierBoostMachine {

    boolean supportsRecipeTierBoost();

    RecipeTierBoostState getRecipeTierBoostState();
}
