package com.ghostipedia.cosmiccore.common.worldgen.survey;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldPlacement;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class VeinSurveyUtil {

    public enum VeinConfidence {
        CONFIRMED,
        PREDICTED
    }

    public record VeinInfo(
                           BlockPos center,
                           ChunkPos originChunk,
                           GTOreDefinition definition,
                           ResourceLocation veinId,
                           int estimatedRadius,
                           VeinConfidence confidence) {

        public String getVeinName() {
            return veinId != null ? veinId.getPath() : "unknown";
        }

        public int horizontalDistanceFrom(BlockPos pos) {
            int dx = center.getX() - pos.getX();
            int dz = center.getZ() - pos.getZ();
            return (int) Math.sqrt(dx * dx + dz * dz);
        }

        public String directionFrom(BlockPos pos) {
            int dx = center.getX() - pos.getX();
            int dz = center.getZ() - pos.getZ();
            String ns = dz < -10 ? "N" : dz > 10 ? "S" : "";
            String ew = dx > 10 ? "E" : dx < -10 ? "W" : "";
            if (ns.isEmpty() && ew.isEmpty()) return "HERE";
            return ns + ew;
        }

        public boolean isConfirmed() {
            return confidence == VeinConfidence.CONFIRMED;
        }
    }

    public static List<VeinInfo> surveyVeins(ServerLevel level, BlockPos centerPos, int radiusBlocks,
                                             IWorldGenLayer targetLayer) {
        List<VeinInfo> results = new ArrayList<>();
        Set<ChunkPos> confirmedGridPositions = new HashSet<>();

        // Phase 1: Query GT's ServerCache for confirmed veins
        List<GeneratedVeinMetadata> cachedVeins = ServerCache.instance.getNearbyVeins(
                level.dimension(), centerPos, radiusBlocks);

        for (GeneratedVeinMetadata metadata : cachedVeins) {
            GTOreDefinition definition = metadata.definition().value();
            if (targetLayer != null && !definition.layer().equals(targetLayer)) {
                continue;
            }

            BlockPos veinCenter = metadata.center();
            if (distanceXZ(veinCenter, centerPos) > radiusBlocks) {
                continue;
            }

            confirmedGridPositions.add(metadata.originChunk());

            ResourceLocation veinId = metadata.definition().unwrapKey()
                    .map(ResourceKey::location)
                    .orElse(null);

            results.add(new VeinInfo(
                    veinCenter,
                    metadata.originChunk(),
                    definition,
                    veinId,
                    definition.clusterSize().getMaxValue(),
                    VeinConfidence.CONFIRMED));
        }

        // Phase 2: For grid positions not in cache, use prediction
        long worldSeed = level.getSeed();
        int gridSize = ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize;
        int randomOffset = ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinRandomOffset;

        int radiusChunks = (radiusBlocks / 16) + (randomOffset / 16) + 2;
        int centerChunkX = centerPos.getX() >> 4;
        int centerChunkZ = centerPos.getZ() >> 4;

        int gridStartX = Math.floorDiv(centerChunkX - radiusChunks, gridSize) * gridSize;
        int gridEndX = Math.floorDiv(centerChunkX + radiusChunks, gridSize) * gridSize;
        int gridStartZ = Math.floorDiv(centerChunkZ - radiusChunks, gridSize) * gridSize;
        int gridEndZ = Math.floorDiv(centerChunkZ + radiusChunks, gridSize) * gridSize;

        for (int gridX = gridStartX; gridX <= gridEndX; gridX += gridSize) {
            for (int gridZ = gridStartZ; gridZ <= gridEndZ; gridZ += gridSize) {
                ChunkPos chunkPos = new ChunkPos(gridX, gridZ);

                if (confirmedGridPositions.contains(chunkPos)) {
                    continue;
                }

                List<VeinInfo> predictedVeins = predictVeinsAtGridPos(level, worldSeed, chunkPos, targetLayer);

                for (VeinInfo info : predictedVeins) {
                    if (info.horizontalDistanceFrom(centerPos) <= radiusBlocks) {
                        results.add(info);
                    }
                }
            }
        }

        Set<Long> confirmedFieldCores = new HashSet<>();
        for (VeinInfo info : results) {
            if (info.isConfirmed()) {
                confirmedFieldCores.add(coreKey(info.center().getX(), info.center().getZ()));
            }
        }
        Registry<GTOreDefinition> veinRegistry = level.registryAccess().registryOrThrow(GTRegistries.Keys.ORE_VEIN);
        for (OreFieldPlacement.OreField field : OreFieldPlacement.fieldsNear(
                level.getSeed(), level.dimension(), centerPos.getX(), centerPos.getZ(), radiusBlocks)) {
            BlockPos fieldCenter = new BlockPos(field.core().getX(), 0, field.core().getZ());
            if (distanceXZ(fieldCenter, centerPos) > radiusBlocks) continue;
            if (confirmedFieldCores.contains(coreKey(fieldCenter.getX(), fieldCenter.getZ()))) continue;

            ResourceLocation veinId = CosmicCore.id(field.bundle().getName());
            var holder = veinRegistry.getHolder(ResourceKey.create(GTRegistries.Keys.ORE_VEIN, veinId));
            if (holder.isEmpty()) continue;
            GTOreDefinition definition = holder.get().value();
            if (targetLayer != null && !definition.layer().equals(targetLayer)) continue;

            OreFieldPlacement.FieldProfile profile = OreFieldPlacement.profileFor(field.bundle());
            int estimatedRadius = profile != null ? profile.fieldRadius() : 64;
            results.add(new VeinInfo(fieldCenter, new ChunkPos(fieldCenter), definition, veinId,
                    estimatedRadius, VeinConfidence.PREDICTED));
        }

        results.sort((a, b) -> Integer.compare(
                a.horizontalDistanceFrom(centerPos),
                b.horizontalDistanceFrom(centerPos)));

        return results;
    }

    private static int distanceXZ(BlockPos a, BlockPos b) {
        int dx = a.getX() - b.getX();
        int dz = a.getZ() - b.getZ();
        return (int) Math.sqrt(dx * dx + dz * dz);
    }

    private static long coreKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    /**
     * Prediction algorithm for ungenerated chunks.
     * Must match GT's OreGenerator.createConfigs() random sequence exactly.
     */
    private static List<VeinInfo> predictVeinsAtGridPos(
                                                        ServerLevel level, long worldSeed, ChunkPos chunkPos,
                                                        IWorldGenLayer targetLayer) {
        List<VeinInfo> results = new ArrayList<>();
        RandomSource random = new XoroshiroRandomSource(worldSeed ^ chunkPos.toLong());

        Optional<BlockPos> veinCenterOpt = OreVeinUtil.getVeinCenter(chunkPos, random);
        if (veinCenterOpt.isEmpty()) return results;

        BlockPos veinCenter = veinCenterOpt.get();
        Holder<Biome> biome = level.getBiome(veinCenter);

        var applicableLayers = level.registryAccess().registryOrThrow(GTRegistries.Keys.WORLD_GEN_LAYER).stream()
                .filter(l -> l.isApplicableForLevel(level.dimension()))
                .toList();

        for (IWorldGenLayer layer : applicableLayers) {
            var biomeVeins = WorldGeneratorUtils.getCachedBiomeVeins(level, biome).stream()
                    .filter(wv -> wv.vein().value().layer().equals(layer))
                    .toList();

            var selectedWeightedVein = GTUtil.getRandomItem(random, biomeVeins);
            if (selectedWeightedVein == null) continue;

            random.nextLong();

            if (targetLayer != null && !layer.equals(targetLayer)) continue;

            Holder<GTOreDefinition> selectedVein = selectedWeightedVein.vein();
            ResourceLocation veinId = selectedVein.unwrapKey()
                    .map(ResourceKey::location)
                    .orElse(null);
            BlockPos finalCenter = new BlockPos(veinCenter.getX(), 0, veinCenter.getZ());
            int estimatedRadius = selectedVein.value().clusterSize().getMaxValue();

            results.add(new VeinInfo(finalCenter, chunkPos, selectedVein.value(), veinId, estimatedRadius,
                    VeinConfidence.PREDICTED));
        }

        return results;
    }

    public static Optional<VeinInfo> findNearestVein(
                                                     ServerLevel level, BlockPos centerPos, int maxRadiusBlocks,
                                                     IWorldGenLayer layer, String veinNameFilter) {
        List<VeinInfo> veins = surveyVeins(level, centerPos, maxRadiusBlocks, layer);

        if (veinNameFilter != null && !veinNameFilter.isEmpty()) {
            String filterLower = veinNameFilter.toLowerCase();
            veins = veins.stream()
                    .filter(v -> v.getVeinName().toLowerCase().contains(filterLower))
                    .toList();
        }

        return veins.isEmpty() ? Optional.empty() : Optional.of(veins.get(0));
    }

    public static List<String> getAvailableVeinTypes(ServerLevel level, IWorldGenLayer layer) {
        List<String> types = new ArrayList<>();
        Registry<GTOreDefinition> registry = level.registryAccess()
                .registryOrThrow(GTRegistries.Keys.ORE_VEIN);
        for (Holder.Reference<GTOreDefinition> holder : registry.holders().toList()) {
            GTOreDefinition vein = holder.value();
            if ((layer == null || vein.layer() == layer) && vein.weight() > 0) {
                types.add(holder.key().location().getPath());
            }
        }
        return types;
    }
}
