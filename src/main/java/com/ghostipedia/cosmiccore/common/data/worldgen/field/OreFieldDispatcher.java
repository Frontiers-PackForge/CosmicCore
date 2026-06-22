package com.ghostipedia.cosmiccore.common.data.worldgen.field;

import com.ghostipedia.cosmiccore.CosmicCore;

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
        int chunkCenterX = (chunkPos.x << 4) + 8;
        int chunkCenterZ = (chunkPos.z << 4) + 8;

        List<OreFieldPlacement.OreField> fields = OreFieldPlacement.fieldsNear(
                seed, dimension, chunkCenterX, chunkCenterZ, SEARCH_RADIUS);
        if (fields.isEmpty()) return List.of();

        Registry<GTOreDefinition> registry = level.registryAccess().registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY);
        int gridSize = ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize;
        int gridX = Math.floorDiv(chunkPos.x, gridSize);
        int gridZ = Math.floorDiv(chunkPos.z, gridSize);

        List<GeneratedVeinMetadata> out = new ArrayList<>();
        for (OreFieldPlacement.OreField field : fields) {
            ResourceLocation id = CosmicCore.id(field.bundle().getName());
            Optional<Holder.Reference<GTOreDefinition>> holder = registry
                    .getHolder(ResourceKey.create(GTRegistries.ORE_VEIN_REGISTRY, id));
            if (holder.isEmpty()) continue;
            GTOreDefinition definition = holder.get().value();

            RandomSource coreRandom = new XoroshiroRandomSource(
                    seed ^ Y_SALT ^
                            ((long) field.core().getX() * 341873128712L + (long) field.core().getZ() * 132897987541L));
            BlockPos coreOrigin = definition.heightRange().getPositions(
                    new PlacementContext(level, generator, Optional.empty()),
                    coreRandom, new BlockPos(field.core().getX(), 0, field.core().getZ())).findFirst().orElse(null);
            if (coreOrigin == null) continue;
            int coreY = coreOrigin.getY() + 5;
            int minY = level.getMinBuildHeight() + 1;
            int maxY = level.getMaxBuildHeight() - 1;

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
