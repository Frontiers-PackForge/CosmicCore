package com.ghostipedia.cosmiccore.common.compat.qualityfood;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import de.cadentem.quality_food.core.attachments.LevelData;
import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.registry.QFComponents;
import de.cadentem.quality_food.util.QualityUtils;
import de.cadentem.quality_food.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class QualityFoodCompat {

    public static final String MOD_ID = "quality_food";
    public static final List<String> TIERS = List.of("normal", "iron", "gold", "diamond");
    private static final ResourceLocation IRON_STAR = ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "quality_icon/iron");
    private static final ResourceLocation GOLD_STAR = ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "quality_icon/gold");
    private static final ResourceLocation DIAMOND_STAR = ResourceLocation.fromNamespaceAndPath(MOD_ID,
            "quality_icon/diamond");

    private QualityFoodCompat() {}

    public static @Nullable ResourceLocation star(int level) {
        return switch (level) {
            case 1 -> IRON_STAR;
            case 2 -> GOLD_STAR;
            case 3 -> DIAMOND_STAR;
            default -> null;
        };
    }

    public static int level(ItemStack stack) {
        if (stack.isEmpty() || !ModList.get().isLoaded(MOD_ID)) return 0;
        return Math.clamp(QualityUtils.getQuality(stack).level(), 0, 3);
    }

    public static boolean supportsQuality(ItemStack stack) {
        return ModList.get().isLoaded(MOD_ID) && Utils.isValidItem(stack);
    }

    public static void setPreviewQuality(ItemStack stack, int level) {
        if (level == 0 || !ModList.get().isLoaded(MOD_ID)) return;
        var tier = ResourceLocation.fromNamespaceAndPath(MOD_ID, TIERS.get(level));
        QualityUtils.applyQuality(stack, new Quality(tier, level, Optional.empty()));
    }

    public static double multiplier(int quality) {
        return switch (quality) {
            case 1 -> 1.25;
            case 2 -> 1.5;
            case 3 -> 1.75;
            default -> 1.0;
        };
    }

    public static void copyQuality(ItemStack source, ItemStack target) {
        if (!ModList.get().isLoaded(MOD_ID)) return;
        var quality = source.get(QFComponents.QUALITY_DATA_COMPONENT.get());
        if (quality != null) QualityUtils.applyQuality(target, quality);
    }

    public static int scaleDuration(int ticks, int quality) {
        return (int) Math.round(ticks * multiplier(quality));
    }

    public static void applyPlacedBlockQuality(Level level, BlockPos pos, ItemStack stack) {
        if (!ModList.get().isLoaded(MOD_ID) || !Utils.isValidBlock(level.getBlockState(pos).getBlock())) return;
        Quality quality = QualityUtils.getQuality(stack);
        LevelData.set(level, pos, quality == Quality.NONE ? Quality.PLAYER_PLACED : quality);
    }
}
