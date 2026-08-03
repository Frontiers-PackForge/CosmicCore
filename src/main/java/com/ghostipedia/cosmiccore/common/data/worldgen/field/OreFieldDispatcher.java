package com.ghostipedia.cosmiccore.common.data.worldgen.field;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldTerrainResolver.ResolvedOreField;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The worldgen bridge between {@link OreFieldPlacement} (which decides where fields go) and GTCEu's ore pipeline
 * (which paints blocks and feeds the prospector). For a chunk it computes every field member whose center lands in
 * that chunk, resolves each member's height through its bundle definition, registers it in GT's ServerCache so it
 * shows on the prospector, and returns the metadata for GT to paint. Invoked from {@code OreGeneratorMixin} at the
 * point GT builds its own per-chunk metadata, so our fields ride the rest of GT's pipeline for free.
 */
public final class OreFieldDispatcher {

    private OreFieldDispatcher() {}

    private static final Logger LOGGER = LoggerFactory.getLogger("CosmicOreFields");
    private static final int SEARCH_RADIUS = 140;
    private static final long Y_SALT = 0x5DEECE66DL;
    private static final int SURFACE_CLEARANCE = 40;

    public static List<GeneratedVeinMetadata> membersInChunk(WorldGenLevel level, ChunkGenerator generator,
                                                             ChunkPos chunkPos) {
        try {
            return collect(level, generator, chunkPos);
        } catch (Exception e) {
            LOGGER.error("Cosmic ore-field dispatch failed for chunk {}", chunkPos, e);
            return List.of();
        }
    }

    private static List<GeneratedVeinMetadata> collect(WorldGenLevel level, ChunkGenerator generator,
                                                       ChunkPos chunkPos) {
        long seed = level.getSeed();
        ResourceKey<Level> dimension = level.getLevel().dimension();
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        int chunkCenterX = (chunkPos.x << 4) + 8;
        int chunkCenterZ = (chunkPos.z << 4) + 8;

        List<OreFieldPlacement.OreField> rawFields = OreFieldPlacement.fieldsNear(
                seed, dimension, chunkCenterX, chunkCenterZ, SEARCH_RADIUS);
        if (rawFields.isEmpty()) return List.of();

        List<ResolvedOreField> fields = OreFieldTerrainResolver.resolveAll(
                level.getLevel(), generator, randomState, rawFields);
        if (fields.isEmpty()) return List.of();

        Registry<GTOreDefinition> registry = level.registryAccess().registryOrThrow(GTRegistries.Keys.ORE_VEIN);
        int gridSize = ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize;
        int gridX = Math.floorDiv(chunkPos.x, gridSize);
        int gridZ = Math.floorDiv(chunkPos.z, gridSize);

        List<GeneratedVeinMetadata> out = new ArrayList<>();
        PlacementContext placement = new PlacementContext(level, generator, Optional.empty());
        boolean surfaceDimension = dimension != Level.NETHER && dimension != Level.END;
        for (ResolvedOreField resolved : fields) {
            OreFieldPlacement.OreField field = resolved.field();
            ResourceLocation id = CosmicCore.id(field.bundle().getName());
            Optional<Holder.Reference<GTOreDefinition>> holder = registry
                    .getHolder(ResourceKey.create(GTRegistries.Keys.ORE_VEIN, id));
            if (holder.isEmpty()) continue;
            GTOreDefinition definition = holder.get().value();

            if (resolved.terrainAware()) {
                BlockPos representative = resolved.representative();
                if ((representative.getX() >> 4) == chunkPos.x &&
                        (representative.getZ() >> 4) == chunkPos.z) {
                    ServerCache.instance.addVein(dimension, gridX, gridZ,
                            new GeneratedVeinMetadata(chunkPos, representative, holder.get()));
                }
                for (BlockPos anchor : resolved.memberAnchors()) {
                    if ((anchor.getX() >> 4) != chunkPos.x || (anchor.getZ() >> 4) != chunkPos.z) continue;
                    out.add(new GeneratedVeinMetadata(chunkPos, anchor, holder.get()));
                }
                continue;
            }

            RandomSource coreRandom = new XoroshiroRandomSource(
                    seed ^ Y_SALT ^
                            ((long) field.core().getX() * 341873128712L + (long) field.core().getZ() * 132897987541L));
            BlockPos coreOrigin = definition.heightRange().getPositions(placement,
                    coreRandom, new BlockPos(field.core().getX(), 0, field.core().getZ())).findFirst().orElse(null);
            if (coreOrigin == null) continue;
            int minY = level.getMinBuildHeight() + 1;
            int maxY = level.getMaxBuildHeight() - 1;
            int coreY = coreOrigin.getY() + 5;
            if (surfaceDimension) {
                int surface = generator.getBaseHeight(field.core().getX(), field.core().getZ(),
                        Heightmap.Types.OCEAN_FLOOR_WG, level, randomState);
                coreY = Math.min(coreY, surface - SURFACE_CLEARANCE);
            }
            coreY = Math.max(minY, Math.min(maxY, coreY));

            if ((field.core().getX() >> 4) == chunkPos.x && (field.core().getZ() >> 4) == chunkPos.z) {
                BlockPos markerPos = new BlockPos(field.core().getX(), coreY, field.core().getZ());
                ServerCache.instance.addVein(dimension, gridX, gridZ,
                        new GeneratedVeinMetadata(chunkPos, markerPos, holder.get()));
            }

            for (OreFieldPlacement.FieldMember member : field.members()) {
                BlockPos center = member.center();
                if ((center.getX() >> 4) != chunkPos.x || (center.getZ() >> 4) != chunkPos.z) continue;

                RandomSource jitter = new XoroshiroRandomSource(
                        seed ^ ((long) center.getX() * 779L + (long) center.getZ() * 1087L));
                int py = Math.max(minY, Math.min(maxY, coreY + jitter.nextInt(21) - 10));
                BlockPos origin = new BlockPos(center.getX(), py, center.getZ());

                out.add(new GeneratedVeinMetadata(chunkPos, origin, holder.get()));
            }
        }
        return out;
    }
}
