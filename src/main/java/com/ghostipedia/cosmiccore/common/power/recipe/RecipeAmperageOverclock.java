package com.ghostipedia.cosmiccore.common.power.recipe;

public final class RecipeAmperageOverclock {

    private RecipeAmperageOverclock() {}

    public static long getEffectiveRecipeEUt(long totalEUt, long packetVoltage) {
        return packetVoltage > 0 ? packetVoltage : totalEUt;
    }

    public static long getMaximumPacketVoltage(long maximumVoltage, long totalEUt, long packetVoltage,
                                               long maximumThroughput) {
        if (maximumVoltage <= 0 || totalEUt <= 0 || packetVoltage <= 0) {
            return maximumVoltage;
        }
        long recipeAmperage = totalEUt / packetVoltage;
        if (totalEUt % packetVoltage != 0) recipeAmperage++;
        if (recipeAmperage <= 0) return maximumVoltage;
        return Math.min(maximumVoltage, maximumThroughput / recipeAmperage);
    }
}
