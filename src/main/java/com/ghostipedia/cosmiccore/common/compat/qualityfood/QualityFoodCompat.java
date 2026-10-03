package com.ghostipedia.cosmiccore.common.compat.qualityfood;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import de.cadentem.quality_food.core.attachments.LevelData;
import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.util.QualityUtils;
import de.cadentem.quality_food.util.Utils;

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

    public static void applyPlacedBlockQuality(Level level, BlockPos pos, ItemStack stack) {
        if (!ModList.get().isLoaded(MOD_ID) || !Utils.isValidBlock(level.getBlockState(pos).getBlock())) return;
        Quality quality = QualityUtils.getQuality(stack);
        LevelData.set(level, pos, quality == Quality.NONE ? Quality.PLAYER_PLACED : quality);
    }

    public static ItemStack recipeOutput(ItemStack result, ItemStack input) {
        ItemStack copy = result.copy();
        QualityUtils.applyQuality(copy, QualityUtils.getQuality(input));
        return copy;
    }
}
