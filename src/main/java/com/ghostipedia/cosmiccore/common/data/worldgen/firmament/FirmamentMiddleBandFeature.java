package com.ghostipedia.cosmiccore.common.data.worldgen.firmament;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

final class FirmamentMiddleBandFeature extends Feature<NoneFeatureConfiguration> {

    private static final int LOWER_MIN_Y = 32;
    private static final int LOWER_MAX_Y = 112;
    private static final int UPPER_MIN_Y = 128;
    private static final int UPPER_MAX_Y = 232;
    private static final int ANCHOR_ATTEMPTS = 18;

    FirmamentMiddleBandFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        long seed = level.getSeed();
        if (!FirmamentMiddleBandLayout.isBridgeChunk(seed, chunkX, chunkZ)) return false;

        RandomSource random = RandomSource.create(FirmamentMiddleBandLayout.mix(seed, chunkX, chunkZ));
        int chunkMinX = chunkX << 4;
        int chunkMinZ = chunkZ << 4;
        for (int attempt = 0; attempt < ANCHOR_ATTEMPTS; attempt++) {
            int lowerX = chunkMinX + 5 + random.nextInt(6);
            int lowerZ = chunkMinZ + 5 + random.nextInt(6);
            int upperX = lowerX + random.nextInt(7) - 3;
            int upperZ = lowerZ + random.nextInt(7) - 3;
            double middleX = (lowerX + upperX) * 0.5;
            double middleZ = (lowerZ + upperZ) * 0.5;
            if (FirmamentMiddleBandLayout.sampleWind(middleX, middleZ).strength() > 0.32) continue;

            BlockPos lower = findLowerAnchor(level, lowerX, lowerZ);
            BlockPos upper = findUpperAnchor(level, upperX, upperZ);
            if (lower == null || upper == null || upper.getY() - lower.getY() < 16) continue;
            return placeBridge(level, seed, random, lower, upper);
        }
        return false;
    }

    private static BlockPos findLowerAnchor(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, LOWER_MAX_Y, z);
        for (int y = LOWER_MAX_Y; y >= LOWER_MIN_Y; y--) {
            cursor.setY(y);
            if (isTerrain(level.getBlockState(cursor)) && !isTerrain(level.getBlockState(cursor.above()))) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static BlockPos findUpperAnchor(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, UPPER_MIN_Y, z);
        for (int y = UPPER_MIN_Y; y <= UPPER_MAX_Y; y++) {
            cursor.setY(y);
            if (isTerrain(level.getBlockState(cursor)) && !isTerrain(level.getBlockState(cursor.below()))) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean placeBridge(WorldGenLevel level, long seed, RandomSource random, BlockPos lower,
                                       BlockPos upper) {
        int height = upper.getY() - lower.getY();
        double deltaX = upper.getX() - lower.getX();
        double deltaZ = upper.getZ() - lower.getZ();
        double length = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double bow = 1.25 + random.nextDouble() * 1.75;
        double bowSign = random.nextBoolean() ? 1.0 : -1.0;
        double perpendicularX;
        double perpendicularZ;
        if (length < 0.5) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            perpendicularX = Math.cos(angle) * bow;
            perpendicularZ = Math.sin(angle) * bow;
        } else {
            perpendicularX = -deltaZ / length * bow * bowSign;
            perpendicularZ = deltaX / length * bow * bowSign;
        }
        double radiusPhase = random.nextDouble() * Math.PI * 2.0;
        BlockState stone = CosmicBlocks.FIRMAMENT_SAPROLITE.getDefaultState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int step = 0; step <= height; step++) {
            double progress = step / (double) height;
            double arc = Math.sin(progress * Math.PI);
            double centerX = Mth.lerp(progress, lower.getX(), upper.getX()) + perpendicularX * arc;
            double centerZ = Mth.lerp(progress, lower.getZ(), upper.getZ()) + perpendicularZ * arc;
            double radius = 3.1 - 1.25 * arc + 0.35 * Math.sin(progress * Math.PI * 4.0 + radiusPhase);
            int range = Mth.ceil(radius + 0.5);
            int y = lower.getY() + step;
            for (int offsetX = -range; offsetX <= range; offsetX++) {
                for (int offsetZ = -range; offsetZ <= range; offsetZ++) {
                    int x = Mth.floor(centerX + offsetX);
                    int z = Mth.floor(centerZ + offsetZ);
                    double dx = x + 0.5 - centerX;
                    double dz = z + 0.5 - centerZ;
                    double roughness = (((FirmamentMiddleBandLayout.mix(seed ^ y, x, z) >>> 40) & 0xFF) / 255.0 - 0.5) *
                            0.75;
                    if (dx * dx + dz * dz > radius * radius + roughness) continue;
                    cursor.set(x, y, z);
                    BlockState existing = level.getBlockState(cursor);
                    if (!existing.isAir() && !isTerrain(existing)) continue;
                    if (existing.isAir()) {
                        level.setBlock(cursor, stone, 2);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    private static boolean isTerrain(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(CosmicBlocks.FIRMAMENT_SAPROLITE.get()) ||
                state.is(CosmicBlocks.FIRMAMENT_SAPROLITE_SLAB.get()) ||
                state.is(CosmicBlocks.ASTRAL_REGOLITH.get()) || state.is(CosmicBlocks.STARDUST_TURF.get());
    }
}
