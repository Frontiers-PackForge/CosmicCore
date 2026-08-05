package com.ghostipedia.cosmiccore.common.data.worldgen.firmament;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.tag.block.CosmicBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    private static final int UPPER_MIN_Y = 208;
    private static final int UPPER_MAX_Y = 312;
    private static final int ANCHOR_ATTEMPTS = 24;

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
            int upperX = lowerX + random.nextInt(13) - 6;
            int upperZ = lowerZ + random.nextInt(13) - 6;
            double middleX = (lowerX + upperX) * 0.5;
            double middleZ = (lowerZ + upperZ) * 0.5;
            if (FirmamentMiddleBandLayout.sampleWind(middleX, middleZ).strength() > 0.32) continue;

            BlockPos lower = findLowerAnchor(level, lowerX, lowerZ);
            BlockPos upper = findUpperAnchor(level, upperX, upperZ);
            if (lower == null || upper == null || upper.getY() - lower.getY() < 72) continue;
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
        double bow = 2.5 + random.nextDouble() * 5.5;
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
        double thicknessRoll = random.nextDouble();
        double thicknessBonus = thicknessRoll < 0.22 ? 2.0 + random.nextDouble() * 1.25 :
                thicknessRoll < 0.58 ? 0.7 + random.nextDouble() * 1.15 : 0.0;
        double endpointRadius = 3.5 + random.nextDouble() * 2.0 + thicknessBonus;
        double waistRadius = 1.65 + random.nextDouble() * 1.25 + thicknessBonus * 0.82;
        BlockState stone = CosmicBlocks.FIRMAMENT_SAPROLITE.getDefaultState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int step = 0; step <= height; step++) {
            SpireSection section = sectionAt(lower, upper, height, perpendicularX, perpendicularZ, radiusPhase,
                    endpointRadius, waistRadius, step);
            int range = Mth.ceil(section.radius() + 0.5);
            for (int offsetX = -range; offsetX <= range; offsetX++) {
                for (int offsetZ = -range; offsetZ <= range; offsetZ++) {
                    int x = Mth.floor(section.centerX() + offsetX);
                    int z = Mth.floor(section.centerZ() + offsetZ);
                    double dx = x + 0.5 - section.centerX();
                    double dz = z + 0.5 - section.centerZ();
                    double roughness = (((FirmamentMiddleBandLayout.mix(seed ^ section.y(), x, z) >>> 40) & 0xFF) /
                            255.0 - 0.5) * 0.75;
                    if (dx * dx + dz * dz > section.radius() * section.radius() + roughness) continue;
                    cursor.set(x, section.y(), z);
                    BlockState existing = level.getBlockState(cursor);
                    if (!existing.isAir() && !isTerrain(existing)) continue;
                    if (existing.isAir()) {
                        level.setBlock(cursor, stone, 2);
                        placed = true;
                    }
                }
            }
        }
        placed |= placeBridgeEcology(level, random, lower, upper, height, perpendicularX, perpendicularZ,
                radiusPhase, endpointRadius, waistRadius);
        return placed;
    }

    private static SpireSection sectionAt(BlockPos lower, BlockPos upper, int height, double perpendicularX,
                                          double perpendicularZ, double radiusPhase, double endpointRadius,
                                          double waistRadius, int step) {
        double progress = step / (double) height;
        double arc = Math.sin(progress * Math.PI);
        double centerX = Mth.lerp(progress, lower.getX(), upper.getX()) + perpendicularX * arc;
        double centerZ = Mth.lerp(progress, lower.getZ(), upper.getZ()) + perpendicularZ * arc;
        double radius = Mth.lerp(arc, endpointRadius, waistRadius) +
                arc * 0.28 * Math.sin(progress * Math.PI * 2.0 + radiusPhase);
        return new SpireSection(centerX, centerZ, radius, lower.getY() + step);
    }

    private static boolean placeBridgeEcology(WorldGenLevel level, RandomSource random, BlockPos lower,
                                              BlockPos upper, int height, double perpendicularX,
                                              double perpendicularZ, double radiusPhase, double endpointRadius,
                                              double waistRadius) {
        int minimumStep = Math.max(0, Mth.ceil(FirmamentMiddleBandLayout.WIND_FULL_MIN_Y - lower.getY()));
        int maximumStep = Math.min(height, Mth.floor(FirmamentMiddleBandLayout.WIND_FULL_MAX_Y - lower.getY()));
        if (minimumStep > maximumStep) return false;
        boolean placed = false;
        int ribCount = 1 + random.nextInt(3);
        for (int rib = 0; rib < ribCount; rib++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            int available = maximumStep - minimumStep + 1;
            int runLength = Math.min(available, 30 + random.nextInt(35));
            int startStep = minimumStep + random.nextInt(Math.max(1, available - runLength + 1));
            BlockPos previousAnchor = null;
            boolean started = false;
            for (int step = startStep; step < startStep + runLength; step++) {
                SpireSection section = sectionAt(lower, upper, height, perpendicularX, perpendicularZ, radiusPhase,
                        endpointRadius, waistRadius, step);
                SurfaceAnchor anchor = findRadialSurface(level, section, angle);
                if (anchor == null) {
                    if (started) break;
                    continue;
                }
                if (previousAnchor != null) {
                    if (previousAnchor.distManhattan(anchor.position()) > 3) break;
                    placed |= placeConnectedSegment(level, previousAnchor, anchor.position(),
                            CosmicBlocks.RESONANT_SPIRESTONE.getDefaultState(), true);
                }
                double progress = (step - startStep) / (double) Math.max(1, runLength - 1);
                placed |= placeResonantButtressSection(level, anchor, progress);
                previousAnchor = anchor.position();
                started = true;
            }
        }

        int growthCount = 3 + random.nextInt(3);
        for (int growth = 0; growth < growthCount; growth++) {
            int step = minimumStep + random.nextInt(maximumStep - minimumStep + 1);
            SpireSection section = sectionAt(lower, upper, height, perpendicularX, perpendicularZ, radiusPhase,
                    endpointRadius, waistRadius, step);
            double angle = random.nextDouble() * Math.PI * 2.0;
            SurfaceAnchor anchor = findRadialSurface(level, section, angle);
            if (anchor == null) continue;
            placed |= placeStormglassCollar(level, anchor, random);
            placed |= placeFulguriteCrown(level, anchor, section.radius(), random);
        }
        return placed;
    }

    private static boolean placeResonantButtressSection(WorldGenLevel level, SurfaceAnchor anchor, double progress) {
        BlockState resonant = CosmicBlocks.RESONANT_SPIRESTONE.getDefaultState();
        int normalX = anchor.face().getStepX();
        int normalZ = anchor.face().getStepZ();
        int tangentX = -normalZ;
        int tangentZ = normalX;
        double swell = Math.sin(progress * Math.PI);
        int protrusion = swell >= 0.36 ? 1 : 0;
        int inwardDepth = swell >= 0.62 ? 2 : 1;
        int width = swell >= 0.72 ? 2 : 1;
        boolean placed = false;
        for (int outward = -inwardDepth; outward <= protrusion; outward++) {
            for (int tangent = -width; tangent <= width; tangent++) {
                BlockPos position = anchor.position().offset(normalX * outward + tangentX * tangent, 0,
                        normalZ * outward + tangentZ * tangent);
                BlockState existing = level.getBlockState(position);
                if (outward <= 0 ? !isTerrain(existing) : !existing.isAir()) continue;
                level.setBlock(position, resonant, 2);
                placed = true;
            }
        }
        return placed;
    }

    private static SurfaceAnchor findRadialSurface(WorldGenLevel level, SpireSection section, double angle) {
        double normalX = Math.cos(angle);
        double normalZ = Math.sin(angle);
        Direction face = Math.abs(normalX) >= Math.abs(normalZ) ?
                (normalX >= 0.0 ? Direction.EAST : Direction.WEST) :
                (normalZ >= 0.0 ? Direction.SOUTH : Direction.NORTH);
        int maximumRadius = Mth.ceil(section.radius() + 2.0);
        for (int distance = maximumRadius; distance >= 0; distance--) {
            int x = Mth.floor(section.centerX() + normalX * distance);
            int z = Mth.floor(section.centerZ() + normalZ * distance);
            BlockPos position = new BlockPos(x, section.y(), z);
            if (!isTerrain(level.getBlockState(position))) continue;
            BlockPos outside = position.relative(face);
            if (!level.isEmptyBlock(outside)) continue;
            return new SurfaceAnchor(position, face);
        }
        return null;
    }

    private static boolean placeStormglassCollar(WorldGenLevel level, SurfaceAnchor anchor, RandomSource random) {
        level.setBlock(anchor.position(), CosmicBlocks.STORMGLASS.getDefaultState(), 2);
        boolean placed = true;
        int normalX = anchor.face().getStepX();
        int normalZ = anchor.face().getStepZ();
        int tangentX = -normalZ;
        int tangentZ = normalX;
        for (int depth = 0; depth <= 2; depth++) {
            for (int vertical = -2; vertical <= 2; vertical++) {
                for (int tangent = -3; tangent <= 3; tangent++) {
                    double profile = tangent * tangent / 9.0 + vertical * vertical / 4.0 + depth * depth / 7.0;
                    if (profile > 1.08 || profile > 0.72 && random.nextFloat() < 0.28F) continue;
                    BlockPos position = anchor.position().offset(tangentX * tangent - normalX * depth, vertical,
                            tangentZ * tangent - normalZ * depth);
                    if (!isTerrain(level.getBlockState(position))) continue;
                    level.setBlock(position, CosmicBlocks.STORMGLASS.getDefaultState(), 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean placeFulguriteCrown(WorldGenLevel level, SurfaceAnchor anchor, double hostRadius,
                                               RandomSource random) {
        BlockState crystal = CosmicBlocks.TEMPEST_FULGURITE.getDefaultState();
        int trunkLength = Mth.clamp(3 + Mth.floor(hostRadius) + random.nextInt(3), 5, 9);
        double baseRadius = Mth.clamp(0.72 + hostRadius * 0.18, 1.0, 1.55);
        double verticalDrift = (random.nextDouble() - 0.5) * 0.26;
        double normalX = anchor.face().getStepX();
        double normalZ = anchor.face().getStepZ();
        boolean placed = placeCrystalRay(level, anchor.position(), normalX, verticalDrift,
                normalZ, trunkLength, baseRadius, crystal);
        int branches = 2 + random.nextInt(3);
        double tangentX = -normalZ;
        double tangentZ = normalX;
        double trunkMagnitude = Math.sqrt(normalX * normalX + verticalDrift * verticalDrift + normalZ * normalZ);
        for (int branch = 0; branch < branches; branch++) {
            int branchStart = 2 + random.nextInt(Math.max(1, trunkLength - 2));
            BlockPos branchRoot = rayPosition(anchor.position(), normalX / trunkMagnitude,
                    verticalDrift / trunkMagnitude, normalZ / trunkMagnitude, branchStart);
            double side = random.nextBoolean() ? 1.0 : -1.0;
            double branchX = tangentX * side * (0.72 + random.nextDouble() * 0.35) + normalX * 0.3;
            double branchZ = tangentZ * side * (0.72 + random.nextDouble() * 0.35) + normalZ * 0.3;
            double branchY = (random.nextBoolean() ? 1.0 : -1.0) * (0.35 + random.nextDouble() * 0.45);
            double scale = Math.max(Math.max(Math.abs(branchX), Math.abs(branchY)), Math.abs(branchZ));
            branchX /= scale;
            branchY /= scale;
            branchZ /= scale;
            placed |= placeCrystalRay(level, branchRoot, branchX, branchY, branchZ, 2 + random.nextInt(3),
                    0.72 + random.nextDouble() * 0.25, crystal);
        }
        return placed;
    }

    private static boolean placeCrystalRay(WorldGenLevel level, BlockPos root, double directionX,
                                           double directionY, double directionZ, int length, double baseRadius,
                                           BlockState state) {
        boolean placed = false;
        double magnitude = Math.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);
        if (magnitude < 1.0E-6) return false;
        double unitX = directionX / magnitude;
        double unitY = directionY / magnitude;
        double unitZ = directionZ / magnitude;
        if (level.isEmptyBlock(root)) {
            level.setBlock(root, state, 2);
            placed = true;
        }
        BlockPos previousCenter = root;
        int samples = length * 2;
        for (int sample = 1; sample <= samples; sample++) {
            double distance = sample * 0.5;
            BlockPos center = rayPosition(root, unitX, unitY, unitZ, distance);
            if (center.getY() < FirmamentMiddleBandLayout.WIND_FULL_MIN_Y ||
                    center.getY() > FirmamentMiddleBandLayout.WIND_FULL_MAX_Y)
                break;
            double progress = distance / length;
            double eased = progress * progress * (3.0 - 2.0 * progress);
            double radius = 0.55 + (baseRadius - 0.55) * (1.0 - eased);
            placed |= placeConnectedSegment(level, previousCenter, center, state, false);
            placed |= placeAirSphere(level, center, radius, state);
            previousCenter = center;
        }
        return placed;
    }

    private static BlockPos rayPosition(BlockPos root, double directionX, double directionY, double directionZ,
                                        double distance) {
        return root.offset(roundToInt(directionX * distance), roundToInt(directionY * distance),
                roundToInt(directionZ * distance));
    }

    private static boolean placeAirSphere(WorldGenLevel level, BlockPos center, double radius, BlockState state) {
        int range = Mth.ceil(radius);
        boolean placed = false;
        for (int offsetX = -range; offsetX <= range; offsetX++) {
            for (int offsetY = -range; offsetY <= range; offsetY++) {
                for (int offsetZ = -range; offsetZ <= range; offsetZ++) {
                    if (offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ > radius * radius) continue;
                    BlockPos position = center.offset(offsetX, offsetY, offsetZ);
                    if (position.getY() < FirmamentMiddleBandLayout.WIND_FULL_MIN_Y ||
                            position.getY() > FirmamentMiddleBandLayout.WIND_FULL_MAX_Y ||
                            !level.isEmptyBlock(position))
                        continue;
                    level.setBlock(position, state, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean placeConnectedSegment(WorldGenLevel level, BlockPos start, BlockPos end, BlockState state,
                                                 boolean replaceTerrain) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        boolean placed = false;
        while (!cursor.equals(end)) {
            if (cursor.getX() != end.getX()) cursor.move(cursor.getX() < end.getX() ? Direction.EAST : Direction.WEST);
            else if (cursor.getY() != end.getY())
                cursor.move(cursor.getY() < end.getY() ? Direction.UP : Direction.DOWN);
            else if (cursor.getZ() != end.getZ())
                cursor.move(cursor.getZ() < end.getZ() ? Direction.SOUTH : Direction.NORTH);
            BlockState existing = level.getBlockState(cursor);
            if (!existing.isAir() && (!replaceTerrain || !isTerrain(existing))) continue;
            level.setBlock(cursor, state, 2);
            placed = true;
        }
        return placed;
    }

    private static int roundToInt(double value) {
        return (int) Math.round(value);
    }

    private static boolean isTerrain(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(CosmicBlocks.FIRMAMENT_SAPROLITE.get()) ||
                state.is(CosmicBlocks.FIRMAMENT_SAPROLITE_SLAB.get()) ||
                state.is(CosmicBlocks.ASTRAL_REGOLITH.get()) || state.is(CosmicBlocks.STARDUST_TURF.get()) ||
                state.is(CosmicBlockTags.FIRMAMENT_RESOURCE_BLOCKS);
    }

    private record SpireSection(double centerX, double centerZ, double radius, int y) {}

    private record SurfaceAnchor(BlockPos position, Direction face) {}
}
