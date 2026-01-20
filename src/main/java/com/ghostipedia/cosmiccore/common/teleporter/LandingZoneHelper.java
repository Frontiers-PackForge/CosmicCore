package com.ghostipedia.cosmiccore.common.teleporter;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class LandingZoneHelper {

    private static final int MIN_SEARCH_HEIGHT_BUFFER = 5; // Don't search below world limit + this buffer
    private static final int CLEAR_AIR_HEIGHT = 3; // Clear this many blocks above platform for headroom

    public static class PlatformOptions {

        public final Block platformMaterial;
        public final Block padBlock;
        public final int platformRadius;

        // Pplatform options.
        public PlatformOptions(Block platformMaterial, Block padBlock, int platformRadius) {
            this.platformMaterial = platformMaterial;
            this.padBlock = padBlock;
            this.platformRadius = platformRadius;
        }
    }

    // Search downward from startY to find solid ground.
    // Searches down to minBuildHeight + 5, then falls back to startY if no ground found.
    public static BlockPos findSafeYLevel(ServerLevel level, int x, int z, int startY) {
        for (int y = startY; y >= level.getMinBuildHeight() + MIN_SEARCH_HEIGHT_BUFFER; y--) {
            BlockPos checkPos = new BlockPos(x, y, z);
            if (level.getBlockState(checkPos.below()).isSolid()) {
                // Found solid ground
                return checkPos;
            }
        }

        return new BlockPos(x, startY, z);
    }

    // Check if a pad block is intact at the given position.
    public static boolean isPadIntact(ServerLevel level, BlockPos pos, Block expectedPad) {
        return level.getBlockState(pos).is(expectedPad);
    }

    // Build a landing platform with escape pad at center.
    public static void buildPlatform(ServerLevel level, BlockPos center, PlatformOptions options) {
        int radius = options.platformRadius;

        // Build platform 1 block below center (so players stand on it, pad is at center)
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos platformPos = center.offset(x, -1, z);
                level.setBlock(platformPos, options.platformMaterial.defaultBlockState(), 3);
            }
        }

        // Place pad at center
        level.setBlock(center, options.padBlock.defaultBlockState(), 3);

        // Clear air above for headroom
        for (int y = 0; y < CLEAR_AIR_HEIGHT; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos airPos = center.offset(x, y, z);
                    if (airPos.equals(center)) continue; // Don't clear the pad itself
                    if (level.getBlockState(airPos).isSolid()) {
                        level.setBlock(airPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
