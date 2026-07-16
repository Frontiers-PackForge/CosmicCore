package com.ghostipedia.cosmiccore.common.compat.qualityfood;

import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import de.cadentem.quality_food.util.QualityUtils;

public final class QualityFoodCompat {

    public static final String MOD_ID = "quality_food";

    private QualityFoodCompat() {}

    public static int level(ItemStack stack) {
        if (stack.isEmpty() || !ModList.get().isLoaded(MOD_ID)) return 0;
        return Math.clamp(QualityUtils.getQuality(stack).level(), 0, 3);
    }

    public static double multiplier(int quality) {
        return switch (quality) {
            case 1 -> 1.25;
            case 2 -> 1.5;
            case 3 -> 1.75;
            default -> 1.0;
        };
    }

    public static int scaleDuration(int ticks, int quality) {
        return (int) Math.round(ticks * multiplier(quality));
    }
}
