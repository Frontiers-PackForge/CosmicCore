package com.ghostipedia.cosmiccore.common.data.worldgen.field;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldPlacement.OreField;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class OreFieldTerrainResolver {

    public static final String ALGORITHM_FINGERPRINT = "firmament-terrain-v1:h1:b0-119+b120-239:d12:n8:r25";

    private static final int MIN_SOLID_DEPTH = 12;
    private static final int MIN_VALID_MEMBERS = 8;
    private static final int MIN_VALID_PERCENT = 25;
    private static final int MAX_CACHE_ENTRIES = 8192;
    private static final Map<ServerLevel, LevelCache> LEVEL_CACHES = Collections.synchronizedMap(new WeakHashMap<>());

    private OreFieldTerrainResolver() {}

    public enum TerrainBand {

        NONE(0, 0),
        LOWER(0, 120),
        UPPER(120, 240);

        private final int minY;
        private final int maxYExclusive;

        TerrainBand(int minY, int maxYExclusive) {
            this.minY = minY;
            this.maxYExclusive = maxYExclusive;
        }
    }

    public record ResolvedOreField(OreField field, TerrainBand band, BlockPos representative,
                                   List<BlockPos> memberAnchors) {

        public ResolvedOreField {
            memberAnchors = List.copyOf(memberAnchors);
        }

        public boolean terrainAware() {
            return band != TerrainBand.NONE;
        }
    }

    public static List<ResolvedOreField> resolveNear(ServerLevel level, int centerX, int centerZ, int radius) {
        List<OreField> fields = OreFieldPlacement.fieldsNear(
                level.getSeed(), level.dimension(), centerX, centerZ, radius);
        return resolveAll(level, level.getChunkSource().getGenerator(), level.getChunkSource().randomState(), fields);
    }

    public static List<ResolvedOreField> resolveAll(ServerLevel level, ChunkGenerator generator,
                                                    RandomState randomState, List<OreField> fields) {
        if (!level.dimension().equals(FirmamentDimension.KEY)) {
            return fields.stream()
                    .map(field -> new ResolvedOreField(field, TerrainBand.NONE, field.core(), List.of()))
                    .toList();
        }
        if (!(generator instanceof NoiseBasedChunkGenerator noiseGenerator)) {
            return List.of();
        }

        LevelCache cache = cacheFor(level, generator, randomState);
        List<ResolvedOreField> resolved = new ArrayList<>(fields.size());
        for (OreField field : fields) {
            Optional<ResolvedOreField> result = resolve(cache, level, noiseGenerator, randomState, field);
            result.ifPresent(resolved::add);
        }
        return List.copyOf(resolved);
    }

    public static String algorithmFingerprint(ResourceKey<Level> dimension) {
        return dimension.equals(FirmamentDimension.KEY) ? ALGORITHM_FINGERPRINT : "";
    }

    public static boolean isFirmament(ResourceKey<Level> dimension) {
        return dimension.equals(FirmamentDimension.KEY);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            synchronized (LEVEL_CACHES) {
                LEVEL_CACHES.remove(level);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        synchronized (LEVEL_CACHES) {
            LEVEL_CACHES.clear();
        }
    }

    private static LevelCache cacheFor(ServerLevel level, ChunkGenerator generator, RandomState randomState) {
        synchronized (LEVEL_CACHES) {
            LevelCache cache = LEVEL_CACHES.get(level);
            if (cache == null || cache.generator != generator || cache.randomState != randomState) {
                cache = new LevelCache(generator, randomState);
                LEVEL_CACHES.put(level, cache);
            }
            return cache;
        }
    }

    private static Optional<ResolvedOreField> resolve(LevelCache cache, ServerLevel level,
                                                      NoiseBasedChunkGenerator generator, RandomState randomState,
                                                      OreField field) {
        if (cache.fields.size() >= MAX_CACHE_ENTRIES) {
            cache.fields.clear();
        }
        FieldKey key = new FieldKey(field.core().getX(), field.core().getZ(),
                field.bundle().getResourceLocation());
        return cache.fields.computeIfAbsent(key, ignored -> resolveUncached(level, generator, randomState, field));
    }

    private static Optional<ResolvedOreField> resolveUncached(ServerLevel level, NoiseBasedChunkGenerator generator,
                                                              RandomState randomState, OreField field) {
        List<OreFieldPlacement.FieldMember> members = field.members();
        if (members.isEmpty()) return Optional.empty();

        BandAnchors bands = resolveBands(level, generator, randomState, members);
        List<BlockPos> lower = bands.lower();
        List<BlockPos> upper = bands.upper();
        TerrainBand band;
        List<BlockPos> anchors;
        if (upper.size() > lower.size() ||
                (upper.size() == lower.size() && upper.size() > 0 && preferUpper(field))) {
            band = TerrainBand.UPPER;
            anchors = upper;
        } else {
            band = TerrainBand.LOWER;
            anchors = lower;
        }

        int required = Math.max(MIN_VALID_MEMBERS,
                (members.size() * MIN_VALID_PERCENT + 99) / 100);
        if (anchors.size() < required) return Optional.empty();

        BlockPos representative = nearestToCore(field.core(), anchors);
        return Optional.of(new ResolvedOreField(field, band, representative, anchors));
    }

    private static BandAnchors resolveBands(ServerLevel level, NoiseBasedChunkGenerator generator,
                                            RandomState randomState,
                                            List<OreFieldPlacement.FieldMember> members) {
        List<BlockPos> lower = new ArrayList<>(members.size());
        List<BlockPos> upper = new ArrayList<>(members.size());
        for (OreFieldPlacement.FieldMember member : members) {
            BlockPos center = member.center();
            NoiseColumn column = generator.getBaseColumn(center.getX(), center.getZ(), level, randomState);
            findAnchor(level, column, center.getX(), center.getZ(), TerrainBand.LOWER).ifPresent(lower::add);
            findAnchor(level, column, center.getX(), center.getZ(), TerrainBand.UPPER).ifPresent(upper::add);
        }
        return new BandAnchors(List.copyOf(lower), List.copyOf(upper));
    }

    private static Optional<BlockPos> findAnchor(ServerLevel level, NoiseColumn column, int x, int z,
                                                 TerrainBand band) {
        int minY = Math.max(level.getMinBuildHeight(), band.minY);
        int maxY = Math.min(level.getMaxBuildHeight(), band.maxYExclusive) - 1;
        int bestStart = 0;
        int bestLength = 0;
        int runStart = minY;
        int runLength = 0;

        for (int y = minY; y <= maxY; y++) {
            var state = column.getBlock(y);
            if (!state.isAir() && state.getFluidState().isEmpty()) {
                if (runLength == 0) runStart = y;
                runLength++;
            } else {
                if (runLength > bestLength) {
                    bestStart = runStart;
                    bestLength = runLength;
                }
                runLength = 0;
            }
        }
        if (runLength > bestLength) {
            bestStart = runStart;
            bestLength = runLength;
        }
        if (bestLength < MIN_SOLID_DEPTH) return Optional.empty();
        return Optional.of(new BlockPos(x, bestStart + (bestLength - 1) / 2, z));
    }

    private static BlockPos nearestToCore(BlockPos core, List<BlockPos> anchors) {
        BlockPos nearest = anchors.getFirst();
        long nearestDistance = horizontalDistanceSquared(core, nearest);
        for (int i = 1; i < anchors.size(); i++) {
            BlockPos candidate = anchors.get(i);
            long distance = horizontalDistanceSquared(core, candidate);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static boolean preferUpper(OreField field) {
        long mixed = field.worldSeed() ^ (long) field.core().getX() * 341873128712L ^
                (long) field.core().getZ() * 132897987541L;
        return (mixed & 1L) != 0;
    }

    private static long horizontalDistanceSquared(BlockPos a, BlockPos b) {
        long dx = (long) a.getX() - b.getX();
        long dz = (long) a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }

    private record FieldKey(int coreX, int coreZ, ResourceLocation bundle) {}

    private record BandAnchors(List<BlockPos> lower, List<BlockPos> upper) {}

    private static final class LevelCache {

        private final ChunkGenerator generator;
        private final RandomState randomState;
        private final ConcurrentHashMap<FieldKey, Optional<ResolvedOreField>> fields = new ConcurrentHashMap<>();

        private LevelCache(ChunkGenerator generator, RandomState randomState) {
            this.generator = generator;
            this.randomState = randomState;
        }
    }
}
