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
}
