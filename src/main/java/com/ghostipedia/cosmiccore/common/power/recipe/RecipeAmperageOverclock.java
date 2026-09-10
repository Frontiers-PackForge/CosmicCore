package com.ghostipedia.cosmiccore.common.power.recipe;

public final class RecipeAmperageOverclock {

    private RecipeAmperageOverclock() {}

    public static long getEffectiveRecipeEUt(long totalEUt, long packetVoltage) {
        return packetVoltage > 0 ? packetVoltage : totalEUt;
    }
}
