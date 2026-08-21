package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.GTUtil;

public record RecipeTierBoostState(long highestInputVoltage, long highestInputAmperage,
                                   int highestInputContainerCount, long maximumThroughput,
                                   boolean boostApplied) {

    public int inputTier() {
        return GTUtil.getFloorTierByVoltage(highestInputVoltage);
    }

    public long maximumRecipeVoltage() {
        if (!boostApplied) return highestInputVoltage;
        return GTValues.V[Math.min(inputTier() + 1, GTValues.MAX)];
    }
}
