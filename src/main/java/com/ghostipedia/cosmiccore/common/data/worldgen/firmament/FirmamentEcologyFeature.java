package com.ghostipedia.cosmiccore.common.data.worldgen.firmament;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.tag.block.CosmicBlockTags;
import com.ghostipedia.cosmiccore.common.firmament.FirmamentEnvironment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

final class FirmamentEcologyFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_Y = 1;
    private static final int MAX_Y = 318;
    private static final int LOWER_BAND_MAX_Y = 119;
    private static final int UPPER_BAND_MIN_Y = 192;
    private static final int SITE_RESERVOIR = 96;
    private static final int SPIRE_SITE_RESERVOIR = 48;
    private static final int RIME_SITE_RESERVOIR = 48;
    private static final int LANDMARK_RADIUS_CHUNKS = 2;
    private static final Direction[] FAULT_FACES = { Direction.NORTH, Direction.SOUTH, Direction.WEST };
    private static final long SURFACE_SALT = 0x8F4D73B5C921E607L;
    private static final long LANDMARK_SALT = 0xD1B54A32D192ED03L;
    private static final long FAULT_SALT = 0x94D049BB133111EBL;
    private static final long TIDE_SALT = 0xDB4F0B9175AE2165L;
    private static final long RIME_PAD_SALT = 0xA0761D6478BD642FL;
    private static final long STORMGLASS_SALT = 0xE11C47D4A2F16B9DL;
    private static final long SUNSCALD_SALT = 0xE7037ED1A0B428DBL;
    private static final long UMBRAL_SALT = 0x8EBC6AF09C88C6E3L;
    private static final long FULGURITE_SALT = 0xA24BAED4963EE407L;

    FirmamentEcologyFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        long seed = context.level().getSeed();
        SurfaceScan scan = scanSurfaces(level, chunkX, chunkZ, seed, random);
        boolean placed = placeStormglassShelf(level, seed, chunkX, chunkZ);

        if (!scan.rimeSites().isEmpty() && isRegionalMaximum(seed ^ RIME_PAD_SALT, chunkX, chunkZ, 1)) {
            SurfaceSite rimeSite = selectSite(scan.rimeSites(), seed ^ RIME_PAD_SALT);
            placed |= placeRimePad(level, rimeSite, random, seed);
        }

        Landmark landmark = null;
        SurfaceSite landmarkSite = null;
        if (!scan.sites().isEmpty() && isLandmarkChunk(seed, chunkX, chunkZ)) {
            landmark = selectLandmark(seed, chunkX, chunkZ);
            landmarkSite = selectLandmarkSite(level, scan.sites(), landmark, seed);
            if (landmarkSite == null) {
                landmark = Landmark.PRIMORDIAL_REMNANT;
                landmarkSite = scan.sites().get(random.nextInt(scan.sites().size()));
            }
        }
        if (landmarkSite != null && landmark != null) {
            placed |= placeLandmark(level, landmarkSite, landmark, random, seed);
        }

        if (!scan.spireSites().isEmpty() && shouldGrowFulgurite(seed, chunkX, chunkZ)) {
            SurfaceSite growthSite = selectFulguriteSite(scan.spireSites(), seed);
            placed |= placeFulguriteGrowth(level, growthSite, random);
        }

        placed |= applySurfaceDecisions(level, scan.decisions());
        return placed;
    }

    private static SurfaceScan scanSurfaces(WorldGenLevel level, int chunkX, int chunkZ, long seed,
                                            RandomSource random) {
        int chunkMinX = chunkX << 4;
        int chunkMinZ = chunkZ << 4;
        List<SurfaceSite> sites = new ArrayList<>(SITE_RESERVOIR);
        List<SurfaceSite> spireSites = new ArrayList<>(SPIRE_SITE_RESERVOIR);
        List<SurfaceSite> rimeSites = new ArrayList<>(RIME_SITE_RESERVOIR);
        List<SurfaceDecision> decisions = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int seenSites = 0;
        int seenSpireSites = 0;
        int seenRimeSites = 0;

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = chunkMinX + localX;
                int z = chunkMinZ + localZ;
                for (int y = MIN_Y; y <= MAX_Y; y++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (!isBaseTerrain(state) || !isExposed(level, cursor)) continue;
                    BlockPos position = cursor.immutable();
                    Direction face = selectExposedFace(level, position, seed);
                    SurfaceSite site = new SurfaceSite(position, face);
                    seenSites = addToReservoir(sites, site, SITE_RESERVOIR, seenSites, random);
                    if (Math.abs(position.getY() - FirmamentEnvironment.AMMONIA_SEA_Y) <= 12) {
                        seenRimeSites = addToReservoir(rimeSites, site, RIME_SITE_RESERVOIR, seenRimeSites, random);
                    }

                    Direction spireFace = selectSpireFace(level, position);
                    boolean spireSurface = spireFace != null && isStormSpireSurface(level, position);
                    if (spireSurface) {
                        seenSpireSites = addToReservoir(spireSites, new SurfaceSite(position, spireFace),
                                SPIRE_SITE_RESERVOIR, seenSpireSites, random);
                    }

                    SurfaceDecision decision = classifySurface(level, position, state, seed);
                    if (decision != null) decisions.add(decision);
                }
            }
        }
        return new SurfaceScan(sites, spireSites, rimeSites, decisions);
    }

    private static int addToReservoir(List<SurfaceSite> reservoir, SurfaceSite site, int capacity, int seen,
                                      RandomSource random) {
        int updatedSeen = seen + 1;
        if (reservoir.size() < capacity) {
            reservoir.add(site);
        } else {
            int replacement = random.nextInt(updatedSeen);
            if (replacement < capacity) reservoir.set(replacement, site);
        }
        return updatedSeen;
    }

    private static SurfaceDecision classifySurface(WorldGenLevel level, BlockPos position, BlockState state,
                                                   long seed) {
        if (state.is(CosmicBlocks.FIRMAMENT_SAPROLITE.get()) && hasOpenEastExposure(level, position) &&
                sunscaldPatch(seed, position)) {
            return new SurfaceDecision(position, CosmicBlocks.SUNSCALDED_SAPROLITE.getDefaultState(), Direction.WEST,
                    stratumInwardSteps(seed, position, SUNSCALD_SALT));
        }
        if (position.getY() >= UPPER_BAND_MIN_Y && level.isEmptyBlock(position.above()) &&
                umbralPatch(seed, position)) {
            return new SurfaceDecision(position, CosmicBlocks.UMBRAL_CRUST.getDefaultState(), Direction.DOWN,
                    stratumInwardSteps(seed, position, UMBRAL_SALT));
        }
        if (position.getY() <= LOWER_BAND_MAX_Y && level.isEmptyBlock(position.below()) &&
                umbralPatch(seed, position)) {
            return new SurfaceDecision(position, CosmicBlocks.UMBRAL_CRUST.getDefaultState(), Direction.UP,
                    stratumInwardSteps(seed, position, UMBRAL_SALT));
        }
        Direction faultFace = findFaultFace(level, position, seed);
        if (faultFace != null) {
            return new SurfaceDecision(position, CosmicBlocks.GRAVITIC_FAULTSTONE.getDefaultState(),
                    faultFace.getOpposite(), stratumInwardSteps(seed, position, FAULT_SALT));
        }
        return null;
    }

    private static boolean applySurfaceDecisions(WorldGenLevel level, List<SurfaceDecision> decisions) {
        boolean placed = false;
        for (SurfaceDecision decision : decisions) {
            if (!isBaseTerrain(level.getBlockState(decision.position()))) continue;
            level.setBlock(decision.position(), decision.replacement(), 2);
            placed = true;
            if (decision.inward() == null) continue;
            for (int depth = 1; depth <= decision.depth(); depth++) {
                BlockPos inside = decision.position().relative(decision.inward(), depth);
                if (!isBaseTerrain(level.getBlockState(inside))) break;
                level.setBlock(inside, decision.replacement(), 2);
            }
        }
        return placed;
    }

    private static Direction findFaultFace(WorldGenLevel level, BlockPos position, long seed) {
        if (position.getY() < UPPER_BAND_MIN_Y || level.isEmptyBlock(position.above())) return null;
        for (Direction face : FAULT_FACES) {
            if (level.isEmptyBlock(position.relative(face)) && faultVein(seed, position, face)) return face;
        }
        return null;
    }

    private static boolean faultVein(long seed, BlockPos position, Direction face) {
        double horizontal = face.getAxis() == Direction.Axis.Z ? position.getX() : position.getZ();
        double vertical = position.getY();
        long faceSeed = seed ^ FAULT_SALT ^ ((long) face.ordinal() * 0x9E3779B97F4A7C15L);
        double warp = valueNoise(faceSeed, horizontal / 48.0, vertical / 52.0);
        double region = valueNoise(faceSeed ^ SURFACE_SALT, horizontal / 92.0, vertical / 88.0);
        double primary = Math.sin(horizontal * 0.075 + vertical * 0.045 + warp * 3.2);
        double branch = Math.sin(horizontal * 0.055 - vertical * 0.08 + warp * 2.3);
        return region > -0.08 && (Math.abs(primary) < 0.035 || Math.abs(branch) < 0.018 && warp > 0.22);
    }

    private static boolean hasOpenEastExposure(WorldGenLevel level, BlockPos position) {
        for (int distance = 1; distance <= 8; distance++) {
            if (!level.isEmptyBlock(position.east(distance))) return false;
        }
        return true;
    }

    private static boolean sunscaldPatch(long seed, BlockPos position) {
        double broad = valueNoise(seed ^ SUNSCALD_SALT, position.getZ() / 31.0, position.getY() / 29.0);
        double detail = valueNoise(seed ^ SURFACE_SALT, position.getZ() / 13.0, position.getY() / 15.0);
        return broad * 0.72 + detail * 0.28 > 0.16;
    }

    private static boolean umbralPatch(long seed, BlockPos position) {
        double broad = valueNoise(seed ^ UMBRAL_SALT, position.getX() / 34.0, position.getZ() / 34.0);
        double detail = valueNoise(seed ^ LANDMARK_SALT, position.getX() / 15.0, position.getZ() / 15.0);
        return broad * 0.78 + detail * 0.22 > 0.34;
    }

    private static int stratumInwardSteps(long seed, BlockPos position, long salt) {
        BlockPos cell = new BlockPos(Math.floorDiv(position.getX(), 7), Math.floorDiv(position.getY(), 7),
                Math.floorDiv(position.getZ(), 7));
        return Math.min(2, (int) (unitHash(seed ^ salt, cell) * 3.0));
    }

    private static boolean placeStormglassShelf(WorldGenLevel level, long seed, int chunkX, int chunkZ) {
        if (!isStormglassShelfChunk(seed, chunkX, chunkZ)) return false;
        RandomSource random = RandomSource.create(FirmamentMiddleBandLayout.mix(seed ^ STORMGLASS_SALT, chunkX,
                chunkZ));
        int minimumX = chunkX << 4;
        int minimumZ = chunkZ << 4;
        int centerX = minimumX + 8;
        int centerZ = minimumZ + 8;
        double bestStrength = Double.NEGATIVE_INFINITY;
        for (int attempt = 0; attempt < 16; attempt++) {
            int candidateX = minimumX + random.nextInt(16);
            int candidateZ = minimumZ + random.nextInt(16);
            double strength = FirmamentMiddleBandLayout.sampleWind(candidateX + 0.5, candidateZ + 0.5).strength();
            if (strength <= bestStrength) continue;
            centerX = candidateX;
            centerZ = candidateZ;
            bestStrength = strength;
        }
        if (bestStrength < 0.18) return false;
        int centerY = 132 + random.nextInt(69);
        FirmamentMiddleBandLayout.WindCorridor wind = FirmamentMiddleBandLayout.sampleWind(centerX + 0.5,
                centerZ + 0.5);
        double axisX = wind.directionX();
        double axisZ = wind.directionZ();
        BlockState stormglass = CosmicBlocks.STORMGLASS.getDefaultState();
        boolean placed = false;
        int lobeCount = 3 + random.nextInt(3);
        for (int lobe = 0; lobe < lobeCount; lobe++) {
            double along = (random.nextDouble() - 0.5) * 15.0;
            double across = (random.nextDouble() - 0.5) * 9.0;
            int lobeX = centerX + (int) Math.round(axisX * along - axisZ * across);
            int lobeZ = centerZ + (int) Math.round(axisZ * along + axisX * across);
            int lobeY = centerY + random.nextInt(3) - 1;
            double longRadius = 8.0 + random.nextDouble() * 6.0;
            double shortRadius = 4.5 + random.nextDouble() * 4.0;
            double verticalRadius = 1.4 + random.nextDouble() * 1.4;
            placed |= placeStormglassLobe(level, new BlockPos(lobeX, lobeY, lobeZ), axisX, axisZ, longRadius,
                    shortRadius, verticalRadius, stormglass, seed ^ STORMGLASS_SALT);
        }
        return placed;
    }

    private static boolean placeStormglassLobe(WorldGenLevel level, BlockPos center, double axisX, double axisZ,
                                               double longRadius, double shortRadius, double verticalRadius,
                                               BlockState state, long seed) {
        int horizontalRange = (int) Math.ceil(longRadius + shortRadius * 0.35);
        int verticalRange = (int) Math.ceil(verticalRadius);
        boolean placed = false;
        for (int offsetX = -horizontalRange; offsetX <= horizontalRange; offsetX++) {
            for (int offsetY = -verticalRange; offsetY <= verticalRange; offsetY++) {
                for (int offsetZ = -horizontalRange; offsetZ <= horizontalRange; offsetZ++) {
                    double along = offsetX * axisX + offsetZ * axisZ;
                    double across = -offsetX * axisZ + offsetZ * axisX;
                    double shape = along * along / (longRadius * longRadius) +
                            across * across / (shortRadius * shortRadius) +
                            offsetY * offsetY / (verticalRadius * verticalRadius);
                    BlockPos position = center.offset(offsetX, offsetY, offsetZ);
                    double roughness = (unitHash(seed, position) - 0.5) * 0.24;
                    if (shape > 1.0 + roughness || !level.isEmptyBlock(position)) continue;
                    level.setBlock(position, state, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean isStormglassShelfChunk(long seed, int chunkX, int chunkZ) {
        double priority = stormglassChunkPriority(seed, chunkX, chunkZ);
        double strength = FirmamentMiddleBandLayout.sampleWind((chunkX << 4) + 8.5, (chunkZ << 4) + 8.5)
                .strength();
        if (strength < 0.18) return false;
        for (int offsetX = -2; offsetX <= 2; offsetX++) {
            for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
                if (offsetX == 0 && offsetZ == 0) continue;
                if (stormglassChunkPriority(seed, chunkX + offsetX, chunkZ + offsetZ) >= priority) return false;
            }
        }
        return true;
    }

    private static double stormglassChunkPriority(long seed, int chunkX, int chunkZ) {
        double strength = FirmamentMiddleBandLayout.sampleWind((chunkX << 4) + 8.5, (chunkZ << 4) + 8.5)
                .strength();
        return strength * 0.82 + chunkUnitHash(seed ^ STORMGLASS_SALT, chunkX, chunkZ) * 0.18;
    }

    private static Direction selectExposedFace(WorldGenLevel level, BlockPos position, long seed) {
        if (level.isEmptyBlock(position.east())) return Direction.EAST;
        Direction selected = Direction.UP;
        double selectedPriority = Double.NEGATIVE_INFINITY;
        for (Direction direction : Direction.values()) {
            if (!level.isEmptyBlock(position.relative(direction))) continue;
            double priority = unitHash(seed ^ ((long) direction.ordinal() * SURFACE_SALT), position);
            if (priority > selectedPriority) {
                selected = direction;
                selectedPriority = priority;
            }
        }
        return selected;
    }

    private static Direction selectSpireFace(WorldGenLevel level, BlockPos position) {
        FirmamentMiddleBandLayout.WindCorridor wind = FirmamentMiddleBandLayout.sampleWind(
                position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5);
        Direction selected = null;
        double selectedScore = Double.NEGATIVE_INFINITY;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!level.isEmptyBlock(position.relative(direction))) continue;
            double score = -(direction.getStepX() * wind.directionX() + direction.getStepZ() * wind.directionZ());
            if (score > selectedScore) {
                selected = direction;
                selectedScore = score;
            }
        }
        return selected;
    }

    private static boolean isStormSpireSurface(WorldGenLevel level, BlockPos position) {
        if (position.getY() < FirmamentMiddleBandLayout.WIND_FULL_MIN_Y ||
                position.getY() > FirmamentMiddleBandLayout.WIND_FULL_MAX_Y)
            return false;
        if (horizontallyExposed(level, position) < 1) return false;
        int verticalTerrain = 0;
        for (int offset = -4; offset <= 4; offset++) {
            if (isBaseTerrain(level.getBlockState(position.offset(0, offset, 0)))) verticalTerrain++;
        }
        return verticalTerrain >= 4;
    }

    private static boolean shouldGrowFulgurite(long seed, int chunkX, int chunkZ) {
        long mixed = FirmamentMiddleBandLayout.mix(seed ^ FULGURITE_SALT, chunkX, chunkZ);
        return (mixed >>> 11) * 0x1.0p-53 < 0.36;
    }

    private static SurfaceSite selectFulguriteSite(List<SurfaceSite> sites, long seed) {
        return selectSite(sites, seed ^ FULGURITE_SALT);
    }

    private static SurfaceSite selectSite(List<SurfaceSite> sites, long seed) {
        SurfaceSite selected = sites.getFirst();
        double selectedPriority = Double.NEGATIVE_INFINITY;
        for (SurfaceSite site : sites) {
            double priority = unitHash(seed, site.position());
            if (priority > selectedPriority) {
                selected = site;
                selectedPriority = priority;
            }
        }
        return selected;
    }

    private static boolean placeFulguriteGrowth(WorldGenLevel level, SurfaceSite site, RandomSource random) {
        if (!isBaseTerrain(level.getBlockState(site.position()))) return false;
        BlockState crystal = CosmicBlocks.TEMPEST_FULGURITE.getDefaultState();
        BlockState root = CosmicBlocks.STORMGLASS.getDefaultState();
        int hostThickness = measureHostThickness(level, site, 12);
        double mainRadius = Math.clamp(0.78 + hostThickness * 0.075, 1.0, 1.65);
        int mainLength = Math.clamp(3 + hostThickness / 2 + random.nextInt(3), 5, 9);
        boolean placed = placePatch(level, site, root, 2 + random.nextInt(2), 2, random.nextLong());
        Direction tangentA = firstTangent(site.face());
        Direction tangentB = secondTangent(site.face());
        placed |= placeTaperedSpike(level, site.position(), site.face(), crystal, mainLength, mainRadius, random);
        int satelliteCount = 2 + random.nextInt(3);
        for (int satellite = 0; satellite < satelliteCount; satellite++) {
            int offsetA = random.nextInt(5) - 2;
            int offsetB = random.nextInt(5) - 2;
            BlockPos rootPos = findNearbySurface(level,
                    site.position().relative(tangentA, offsetA).relative(tangentB, offsetB), site.face(), 2);
            if (rootPos == null) continue;
            placed |= placeTaperedSpike(level, rootPos, site.face(), crystal, 2 + random.nextInt(4),
                    0.72 + random.nextDouble() * 0.34, random);
        }
        return placed;
    }

    private static int measureHostThickness(WorldGenLevel level, SurfaceSite site, int limit) {
        Direction inward = site.face().getOpposite();
        int thickness = 0;
        for (int depth = 0; depth < limit; depth++) {
            if (!isFormationTerrain(level.getBlockState(site.position().relative(inward, depth)))) break;
            thickness++;
        }
        return Math.max(1, thickness);
    }

    private static boolean placeRimePad(WorldGenLevel level, SurfaceSite site, RandomSource random, long seed) {
        if (!isBaseTerrain(level.getBlockState(site.position()))) return false;
        BlockState rime = CosmicBlocks.AMMONIA_RIME.getDefaultState();
        boolean placed = placePatch(level, site, rime, 8, 3, seed ^ TIDE_SALT);
        Direction tangentA = firstTangent(site.face());
        Direction tangentB = secondTangent(site.face());
        placed |= placeTaperedSpike(level, site.position(), site.face(), rime, 9 + random.nextInt(7),
                2.35 + random.nextDouble() * 0.65, random);
        int spikeCount = 9 + random.nextInt(7);
        for (int spike = 0; spike < spikeCount; spike++) {
            int offsetA = random.nextInt(13) - 6;
            int offsetB = random.nextInt(13) - 6;
            if (offsetA * offsetA + offsetB * offsetB > 42) continue;
            BlockPos root = findNearbySurface(level,
                    site.position().relative(tangentA, offsetA).relative(tangentB, offsetB), site.face(), 4);
            if (root == null) continue;
            placed |= placeTaperedSpike(level, root, site.face(), rime, 4 + random.nextInt(8),
                    1.25 + random.nextDouble() * 1.0, random);
        }
        return placed;
    }

    private static boolean placeTaperedSpike(WorldGenLevel level, BlockPos root, Direction normal, BlockState state,
                                             int length, double baseRadius, RandomSource random) {
        Direction tangentA = firstTangent(normal);
        Direction tangentB = secondTangent(normal);
        double driftA = (random.nextDouble() - 0.5) * Math.min(2.4, length * 0.22);
        double driftB = (random.nextDouble() - 0.5) * Math.min(2.4, length * 0.22);
        boolean placed = false;
        BlockPos previousCenter = root;
        int samples = length * 2;
        for (int sample = 1; sample <= samples; sample++) {
            double distance = sample * 0.5;
            double progress = distance / length;
            double eased = progress * progress * (3.0 - 2.0 * progress);
            double radius = 0.55 + (baseRadius - 0.55) * (1.0 - eased);
            BlockPos center = root.relative(normal, (int) Math.round(distance))
                    .relative(tangentA, (int) Math.round(driftA * progress))
                    .relative(tangentB, (int) Math.round(driftB * progress));
            if (!isConnectableSegment(level, previousCenter, center, state)) break;
            placed |= placeConnectedSegment(level, previousCenter, center, state);
            placed |= placeAirSphere(level, center, radius, state);
            previousCenter = center;
        }
        return placed;
    }

    private static boolean isConnectableSegment(WorldGenLevel level, BlockPos start, BlockPos end, BlockState state) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        while (!cursor.equals(end)) {
            moveToward(cursor, end);
            BlockState existing = level.getBlockState(cursor);
            if (!existing.isAir() && !existing.is(state.getBlock())) return false;
        }
        return true;
    }

    private static boolean placeConnectedSegment(WorldGenLevel level, BlockPos start, BlockPos end, BlockState state) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        boolean placed = false;
        while (!cursor.equals(end)) {
            moveToward(cursor, end);
            if (!level.isEmptyBlock(cursor)) continue;
            level.setBlock(cursor, state, 2);
            placed = true;
        }
        return placed;
    }

    private static void moveToward(BlockPos.MutableBlockPos cursor, BlockPos end) {
        if (cursor.getX() != end.getX()) cursor.move(cursor.getX() < end.getX() ? Direction.EAST : Direction.WEST);
        else if (cursor.getY() != end.getY())
            cursor.move(cursor.getY() < end.getY() ? Direction.UP : Direction.DOWN);
        else if (cursor.getZ() != end.getZ())
            cursor.move(cursor.getZ() < end.getZ() ? Direction.SOUTH : Direction.NORTH);
    }

    private static boolean placeAirSphere(WorldGenLevel level, BlockPos center, double radius, BlockState state) {
        int range = (int) Math.ceil(radius);
        boolean placed = false;
        for (int offsetX = -range; offsetX <= range; offsetX++) {
            for (int offsetY = -range; offsetY <= range; offsetY++) {
                for (int offsetZ = -range; offsetZ <= range; offsetZ++) {
                    if (offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ > radius * radius) continue;
                    BlockPos position = center.offset(offsetX, offsetY, offsetZ);
                    if (!level.isEmptyBlock(position)) continue;
                    level.setBlock(position, state, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static BlockPos findNearbySurface(WorldGenLevel level, BlockPos center, Direction normal, int range) {
        for (int offset = range; offset >= -range; offset--) {
            BlockPos position = center.relative(normal, offset);
            if (!isFormationTerrain(level.getBlockState(position)) ||
                    !level.isEmptyBlock(position.relative(normal)))
                continue;
            return position;
        }
        return null;
    }

    private static boolean placeLandmark(WorldGenLevel level, SurfaceSite site, Landmark landmark,
                                         RandomSource random, long seed) {
        return switch (landmark) {
            case SOLAR_GLASS_FAN -> placeSolarGlassFan(level, site, random, seed);
            case STORM_KNOT -> placeStormKnot(level, site, random, seed);
            case AMMONIA_TIDE_CHIMNEY -> placeTideChimney(level, site, random, seed);
            case GRAVITY_LENS -> placeGravityLens(level, site, random, seed);
            case PRIMORDIAL_REMNANT -> placePrimordialRemnant(level, site, random, seed);
        };
    }

    private static boolean placeSolarGlassFan(WorldGenLevel level, SurfaceSite site, RandomSource random, long seed) {
        BlockState glass = CosmicBlocks.HELIOSTATIC_GLASS.getDefaultState();
        boolean placed = placePatch(level, site, glass, 6, 3,
                seed ^ LANDMARK_SALT);
        Direction tangentA = firstTangent(site.face());
        Direction tangentB = secondTangent(site.face());
        int bladeCount = 5 + random.nextInt(4);
        for (int blade = 0; blade < bladeCount; blade++) {
            int offsetA = random.nextInt(9) - 4;
            int offsetB = random.nextInt(7) - 3;
            BlockPos root = findNearbySurface(level,
                    site.position().relative(tangentA, offsetA).relative(tangentB, offsetB), site.face(), 3);
            if (root == null) continue;
            placed |= placeTaperedSpike(level, root, site.face(), glass, 5 + random.nextInt(7),
                    1.55 + random.nextDouble() * 0.8, random);
        }
        return placed;
    }

    private static boolean placeStormKnot(WorldGenLevel level, SurfaceSite site, RandomSource random, long seed) {
        BlockState stormglass = CosmicBlocks.STORMGLASS.getDefaultState();
        boolean placed = placePatch(level, site, stormglass, 5, 3,
                seed ^ LANDMARK_SALT);
        placed |= placeAttachedBulb(level, site, stormglass, 5, 4.2, 3.5, seed ^ STORMGLASS_SALT);
        if (site.position().getY() >= FirmamentMiddleBandLayout.WIND_FULL_MIN_Y &&
                site.position().getY() <= FirmamentMiddleBandLayout.WIND_FULL_MAX_Y) {
            placed |= placeTaperedSpike(level, site.position(), site.face(),
                    CosmicBlocks.TEMPEST_FULGURITE.getDefaultState(), 8 + random.nextInt(5), 2.15, random);
        }
        return placed;
    }

    private static boolean placeTideChimney(WorldGenLevel level, SurfaceSite site, RandomSource random, long seed) {
        return placeRimePad(level, site, random, seed ^ LANDMARK_SALT);
    }

    private static boolean placeGravityLens(WorldGenLevel level, SurfaceSite site, RandomSource random, long seed) {
        boolean placed = placePatch(level, site, CosmicBlocks.PRIMORDIAL_REMNANT.getDefaultState(), 1, 3,
                seed ^ SURFACE_SALT);
        placed |= placePatch(level, site, CosmicBlocks.GRAVITIC_FAULTSTONE.getDefaultState(), 4, 2,
                seed ^ LANDMARK_SALT);
        Direction tangentA = firstTangent(site.face());
        Direction tangentB = secondTangent(site.face());
        BlockPos center = site.position().relative(site.face(), 4);
        int phase = random.nextInt(2);
        for (int point = 0; point < 16; point++) {
            double angle = point / 16.0 * Math.PI * 2.0;
            int offsetA = (int) Math.round(Math.cos(angle) * 4.5);
            int offsetB = (int) Math.round(Math.sin(angle) * 2.5);
            BlockPos orbit = center.relative(tangentA, offsetA).relative(tangentB, offsetB);
            if (!level.isEmptyBlock(orbit)) continue;
            BlockState state = (point + phase) % 3 == 0 ? CosmicBlocks.PRIMORDIAL_REMNANT.getDefaultState() :
                    CosmicBlocks.GRAVITIC_FAULTSTONE.getDefaultState();
            placed |= placeAirBlob(level, orbit, state, 1.15, seed ^ point);
        }
        return placed;
    }

    private static boolean placePrimordialRemnant(WorldGenLevel level, SurfaceSite site, RandomSource random,
                                                  long seed) {
        if (!isBaseTerrain(level.getBlockState(site.position()))) return false;
        boolean placed = placePatch(level, site, CosmicBlocks.PRIMORDIAL_REMNANT.getDefaultState(), 2, 3,
                seed ^ SURFACE_SALT);
        Direction tangentA = firstTangent(site.face());
        Direction tangentB = secondTangent(site.face());
        for (int fragment = 0; fragment < 5; fragment++) {
            int outward = 3 + random.nextInt(4);
            int offsetA = random.nextInt(9) - 4;
            int offsetB = random.nextInt(9) - 4;
            BlockPos fragmentPos = site.position()
                    .relative(site.face(), outward)
                    .relative(tangentA, offsetA)
                    .relative(tangentB, offsetB);
            if (!level.isEmptyBlock(fragmentPos)) continue;
            BlockState fragmentState = random.nextBoolean() ? CosmicBlocks.PRIMORDIAL_REMNANT.getDefaultState() :
                    CosmicBlocks.GRAVITIC_FAULTSTONE.getDefaultState();
            placed |= placeAirBlob(level, fragmentPos, fragmentState, 1.25 + random.nextDouble() * 1.0,
                    seed ^ fragment);
        }
        return placed;
    }

    private static boolean placeAttachedBulb(WorldGenLevel level, SurfaceSite site, BlockState state,
                                             int outwardRadius, double radiusA, double radiusB, long seed) {
        Direction normal = site.face();
        Direction tangentA = firstTangent(normal);
        Direction tangentB = secondTangent(normal);
        boolean placed = false;
        for (int outward = 1; outward <= outwardRadius; outward++) {
            double normalCoordinate = (outward - 1.5) / outwardRadius;
            int rangeA = (int) Math.ceil(radiusA);
            int rangeB = (int) Math.ceil(radiusB);
            for (int offsetA = -rangeA; offsetA <= rangeA; offsetA++) {
                for (int offsetB = -rangeB; offsetB <= rangeB; offsetB++) {
                    double shape = normalCoordinate * normalCoordinate + offsetA * offsetA / (radiusA * radiusA) +
                            offsetB * offsetB / (radiusB * radiusB);
                    BlockPos position = site.position().relative(normal, outward)
                            .relative(tangentA, offsetA)
                            .relative(tangentB, offsetB);
                    double roughness = (unitHash(seed, position) - 0.5) * 0.2;
                    if (shape > 1.0 + roughness || !level.isEmptyBlock(position)) continue;
                    level.setBlock(position, state, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean placeAirBlob(WorldGenLevel level, BlockPos center, BlockState state, double radius,
                                        long seed) {
        int range = (int) Math.ceil(radius);
        boolean placed = false;
        for (int offsetX = -range; offsetX <= range; offsetX++) {
            for (int offsetY = -range; offsetY <= range; offsetY++) {
                for (int offsetZ = -range; offsetZ <= range; offsetZ++) {
                    BlockPos position = center.offset(offsetX, offsetY, offsetZ);
                    double shape = (offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ) /
                            (radius * radius);
                    double roughness = (unitHash(seed, position) - 0.5) * 0.24;
                    if (shape > 1.0 + roughness || !level.isEmptyBlock(position)) continue;
                    level.setBlock(position, state, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean placePatch(WorldGenLevel level, SurfaceSite site, BlockState replacement,
                                      int radius, int depth, long seed) {
        BlockPos center = site.position();
        Direction face = site.face();
        int normalX = face.getStepX();
        int normalY = face.getStepY();
        int normalZ = face.getStepZ();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placed = false;
        int range = radius + depth;
        for (int offsetX = -range; offsetX <= range; offsetX++) {
            for (int offsetY = -range; offsetY <= range; offsetY++) {
                for (int offsetZ = -range; offsetZ <= range; offsetZ++) {
                    double projection = offsetX * normalX + offsetY * normalY + offsetZ * normalZ;
                    if (projection > 0.25 || projection < -depth) continue;
                    double tangentSquared = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ -
                            projection * projection;
                    cursor.setWithOffset(center, offsetX, offsetY, offsetZ);
                    double roughness = (unitHash(seed ^ cursor.getY(), cursor) - 0.5) * 2.2;
                    if (tangentSquared > radius * radius + roughness) continue;
                    if (!isBaseTerrain(level.getBlockState(cursor))) continue;
                    level.setBlock(cursor, replacement, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static Landmark selectLandmark(long seed, int chunkX, int chunkZ) {
        long mixed = FirmamentMiddleBandLayout.mix(seed ^ SURFACE_SALT ^ LANDMARK_SALT, chunkX, chunkZ);
        return Landmark.values()[(int) Math.floorMod(mixed, Landmark.values().length)];
    }

    private static SurfaceSite selectLandmarkSite(WorldGenLevel level, List<SurfaceSite> sites, Landmark landmark,
                                                  long seed) {
        SurfaceSite best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (SurfaceSite site : sites) {
            double score = landmarkSiteScore(level, site, landmark, seed);
            if (score > bestScore) {
                best = site;
                bestScore = score;
            }
        }
        return Double.isFinite(bestScore) ? best : null;
    }

    private static double landmarkSiteScore(WorldGenLevel level, SurfaceSite site, Landmark landmark, long seed) {
        BlockPos position = site.position();
        return switch (landmark) {
            case SOLAR_GLASS_FAN -> site.face() == Direction.EAST ? unitHash(seed ^ SURFACE_SALT, position) :
                    Double.NEGATIVE_INFINITY;
            case STORM_KNOT -> {
                FirmamentMiddleBandLayout.WindCorridor wind = FirmamentMiddleBandLayout.sampleWind(
                        position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5);
                yield wind.strength() > 0.14 ? wind.strength() + unitHash(seed, position) * 0.1 :
                        Double.NEGATIVE_INFINITY;
            }
            case AMMONIA_TIDE_CHIMNEY -> {
                int distance = Math.abs(position.getY() - FirmamentEnvironment.AMMONIA_SEA_Y);
                yield distance <= 22 ? -distance + unitHash(seed, position) * 0.25 : Double.NEGATIVE_INFINITY;
            }
            case GRAVITY_LENS -> horizontallyExposed(level, position) + unitHash(seed, position);
            case PRIMORDIAL_REMNANT -> unitHash(seed ^ LANDMARK_SALT, position);
        };
    }

    private static boolean isLandmarkChunk(long seed, int chunkX, int chunkZ) {
        return isRegionalMaximum(seed ^ LANDMARK_SALT, chunkX, chunkZ, LANDMARK_RADIUS_CHUNKS);
    }

    private static boolean isRegionalMaximum(long seed, int chunkX, int chunkZ, int radius) {
        long priority = FirmamentMiddleBandLayout.mix(seed, chunkX, chunkZ);
        for (int offsetX = -radius; offsetX <= radius; offsetX++) {
            for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                if (offsetX == 0 && offsetZ == 0) continue;
                long other = FirmamentMiddleBandLayout.mix(seed, chunkX + offsetX, chunkZ + offsetZ);
                if (Long.compareUnsigned(other, priority) >= 0) return false;
            }
        }
        return true;
    }

    private static Direction firstTangent(Direction normal) {
        return normal.getAxis() == Direction.Axis.X ? Direction.UP : Direction.EAST;
    }

    private static Direction secondTangent(Direction normal) {
        return normal.getAxis() == Direction.Axis.Z ? Direction.UP : Direction.SOUTH;
    }

    private static int horizontallyExposed(WorldGenLevel level, BlockPos position) {
        int exposed = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.isEmptyBlock(position.relative(direction))) exposed++;
        }
        return exposed;
    }

    private static boolean isExposed(WorldGenLevel level, BlockPos position) {
        for (Direction direction : Direction.values()) {
            if (level.isEmptyBlock(position.relative(direction))) return true;
        }
        return false;
    }

    private static boolean isBaseTerrain(BlockState state) {
        return state.is(CosmicBlocks.FIRMAMENT_SAPROLITE.get()) ||
                state.is(CosmicBlocks.FIRMAMENT_SAPROLITE_SLAB.get()) ||
                state.is(CosmicBlocks.ASTRAL_REGOLITH.get()) || state.is(CosmicBlocks.STARDUST_TURF.get());
    }

    private static boolean isFormationTerrain(BlockState state) {
        return isBaseTerrain(state) || state.is(CosmicBlockTags.FIRMAMENT_RESOURCE_BLOCKS);
    }

    private static double valueNoise(long seed, double x, double z) {
        int floorX = (int) Math.floor(x);
        int floorZ = (int) Math.floor(z);
        double fractionX = smooth(x - floorX);
        double fractionZ = smooth(z - floorZ);
        double x0 = lerp(fractionX, signedHash(seed, floorX, floorZ), signedHash(seed, floorX + 1, floorZ));
        double x1 = lerp(fractionX, signedHash(seed, floorX, floorZ + 1), signedHash(seed, floorX + 1, floorZ + 1));
        return lerp(fractionZ, x0, x1);
    }

    private static double smooth(double value) {
        return value * value * (3.0 - 2.0 * value);
    }

    private static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    private static double signedHash(long seed, int x, int z) {
        long mixed = FirmamentMiddleBandLayout.mix(seed, x, z);
        return (mixed >>> 11) * 0x1.0p-52 - 1.0;
    }

    private static double unitHash(long seed, BlockPos position) {
        long mixed = FirmamentMiddleBandLayout.mix(seed ^ ((long) position.getY() * 0x9E3779B97F4A7C15L),
                position.getX(), position.getZ());
        return (mixed >>> 11) * 0x1.0p-53;
    }

    private static double chunkUnitHash(long seed, int chunkX, int chunkZ) {
        return (FirmamentMiddleBandLayout.mix(seed, chunkX, chunkZ) >>> 11) * 0x1.0p-53;
    }

    private enum Landmark {
        SOLAR_GLASS_FAN,
        STORM_KNOT,
        AMMONIA_TIDE_CHIMNEY,
        GRAVITY_LENS,
        PRIMORDIAL_REMNANT
    }

    private record SurfaceSite(BlockPos position, Direction face) {}

    private record SurfaceDecision(BlockPos position, BlockState replacement, Direction inward, int depth) {}

    private record SurfaceScan(List<SurfaceSite> sites, List<SurfaceSite> spireSites, List<SurfaceSite> rimeSites,
                               List<SurfaceDecision> decisions) {}
}
