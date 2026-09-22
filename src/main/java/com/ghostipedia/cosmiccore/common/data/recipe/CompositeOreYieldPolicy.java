package com.ghostipedia.cosmiccore.common.data.recipe;

public final class CompositeOreYieldPolicy {

    private CompositeOreYieldPolicy() {}

    public static int baseAmount(int index) {
        return switch (index) {
            case 0 -> 4;
            case 1 -> 2;
            default -> 1;
        };
    }

    public static int outputAmount(int index, int yieldMultiplier, int outputBonus) {
        return baseAmount(index) * yieldMultiplier + outputBonus;
    }
}
