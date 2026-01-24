package com.ghostipedia.cosmiccore.common.worldgen.survey;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VeinSurveyUtil {

    public record VeinInfo(
                           BlockPos center,
                           ChunkPos originChunk,
                           GTOreDefinition definition,
                           ResourceLocation veinId,
                           int estimatedRadius) {

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
    }

    public static List<VeinInfo> surveyVeins(ServerLevel level, BlockPos centerPos, int radiusBlocks,
                                             IWorldGenLayer layer) {
        List<VeinInfo> results = new ArrayList<>();

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
                List<VeinInfo> veinsAtPos = calculateVeinsAtGridPos(level, worldSeed, chunkPos, layer);

                for (VeinInfo info : veinsAtPos) {
                    if (info.horizontalDistanceFrom(centerPos) <= radiusBlocks) {
                        results.add(info);
                    }
                }
            }
        }

        results.sort((a, b) -> Integer.compare(
                a.horizontalDistanceFrom(centerPos),
                b.horizontalDistanceFrom(centerPos)));

        return results;
    }

    // Must match GT's OreGenerator.createConfigs() random sequence:
    // 1. seed ^ chunkPos -> getVeinCenter() consumes 0-2 randoms
    // 2. For each layer: getRandomItem() consumes 1 random (if non-empty)
    // 3. If vein selected: computeVeinOrigin() consumes random.nextLong()
    private static List<VeinInfo> calculateVeinsAtGridPos(
                                                          ServerLevel level, long worldSeed, ChunkPos chunkPos,
                                                          IWorldGenLayer targetLayer) {
        List<VeinInfo> results = new ArrayList<>();
        RandomSource random = new XoroshiroRandomSource(worldSeed ^ chunkPos.toLong());

        Optional<BlockPos> veinCenterOpt = OreVeinUtil.getVeinCenter(chunkPos, random);
        if (veinCenterOpt.isEmpty()) return results;

        BlockPos veinCenter = veinCenterOpt.get();
        Holder<Biome> biome = level.getBiome(veinCenter);

        var applicableLayers = WorldGeneratorUtils.WORLD_GEN_LAYERS.values().stream()
                .filter(l -> l.isApplicableForLevel(level.dimension().location()))
                .toList();

        for (IWorldGenLayer layer : applicableLayers) {
            var biomeVeins = WorldGeneratorUtils.getCachedBiomeVeins(level, biome).stream()
                    .filter(wv -> wv.vein().layer().equals(layer))
                    .toList();

            var selectedWeightedVein = GTUtil.getRandomItem(random, biomeVeins);
            if (selectedWeightedVein == null) continue;

            // Match GT's computeVeinOrigin random consumption
            random.nextLong();

            if (targetLayer != null && !layer.equals(targetLayer)) continue;

            GTOreDefinition selectedVein = selectedWeightedVein.vein();
            ResourceLocation veinId = GTRegistries.ORE_VEINS.getKey(selectedVein);
            BlockPos finalCenter = new BlockPos(veinCenter.getX(), 0, veinCenter.getZ());
            int estimatedRadius = selectedVein.clusterSize().getMaxValue();

            results.add(new VeinInfo(finalCenter, chunkPos, selectedVein, veinId, estimatedRadius));
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

    public static List<String> getAvailableVeinTypes(IWorldGenLayer layer) {
        List<String> types = new ArrayList<>();
        for (GTOreDefinition vein : GTRegistries.ORE_VEINS) {
            if ((layer == null || vein.layer() == layer) && vein.weight() > 0) {
                ResourceLocation id = GTRegistries.ORE_VEINS.getKey(vein);
                if (id != null) {
                    types.add(id.getPath());
                }
            }
        }
        return types;
    }
}
