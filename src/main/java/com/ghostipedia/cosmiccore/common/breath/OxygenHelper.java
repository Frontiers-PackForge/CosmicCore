package com.ghostipedia.cosmiccore.common.breath;

import com.ghostipedia.cosmiccore.common.airControl.OxygenRules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Helper for checking air quality using CosmicCore's oxygen system.
 * Used by Create diving helmet integration.
 */
public class OxygenHelper {

    /**
     * Check if the air quality at the entity's location should activate breathing equipment.
     * Returns true if air is not SAFE (i.e., THIN, TOXIC, ABYSS, or NO_AIR).
     */
    public static boolean airQualityActivatesHelmet(LivingEntity entity) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();

        // Check if eyes are in fluid - always needs helmet
        BlockPos eyePos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        if (!level.getFluidState(eyePos).isEmpty()) {
            return true;
        }

        // Check our air quality system
        OxygenRules.AirRanges range = OxygenRules.getRanges(level.dimension(), pos.getY());
        if (range == null) {
            return false; // No range defined = SAFE
        }

        return range.quality != OxygenRules.AirQuality.SAFE;
    }
}
