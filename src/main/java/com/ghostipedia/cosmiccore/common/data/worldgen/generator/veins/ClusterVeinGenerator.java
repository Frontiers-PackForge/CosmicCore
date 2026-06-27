package com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.VeinGeneratorUtil.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Accessors(fluent = true, chain = true)
public class ClusterVeinGenerator extends VeinGenerator {

    public static final MapCodec<ClusterVeinGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            OreBlockDef.CODEC.listOf().fieldOf("ore_blocks").forGetter(it -> it.oreBlocks),
            OreBlockDef.CODEC.listOf().fieldOf("rare_blocks").forGetter(it -> it.rareBlocks),
            Codec.INT.fieldOf("node_count").orElse(8).forGetter(it -> it.nodeCount),
            Codec.FLOAT.fieldOf("node_size_ratio").orElse(0.2f).forGetter(it -> it.nodeSizeRatio),
            Codec.FLOAT.fieldOf("channel_thickness_ratio").orElse(0.08f).forGetter(it -> it.channelThicknessRatio),
            Codec.FLOAT.fieldOf("scatter_amount").orElse(0.7f).forGetter(it -> it.scatterAmount),
            Codec.FLOAT.fieldOf("rare_block_chance").orElse(0.06f).forGetter(it -> it.rareBlockChance))
            .apply(instance, ClusterVeinGenerator::new));

    public List<OreBlockDef> oreBlocks = new ArrayList<>();
    public List<OreBlockDef> rareBlocks = new ArrayList<>();
    @Setter
    public int nodeCount = 10;
    @Setter
    public float nodeSizeRatio = 0.06f;
    @Setter
    public float channelThicknessRatio = 0.025f;
    @Setter
    public float scatterAmount = 0.9f;
    @Setter
    public float rareBlockChance = 0.06f;

    public ClusterVeinGenerator() {}

    public ClusterVeinGenerator(List<OreBlockDef> oreBlocks, List<OreBlockDef> rareBlocks,
                                int nodeCount, float nodeSizeRatio, float channelThicknessRatio,
                                float scatterAmount, float rareBlockChance) {
        this.oreBlocks = oreBlocks;
        this.rareBlocks = rareBlocks;
        this.nodeCount = nodeCount;
        this.nodeSizeRatio = nodeSizeRatio;
        this.channelThicknessRatio = channelThicknessRatio;
        this.scatterAmount = scatterAmount;
        this.rareBlockChance = rareBlockChance;
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        List<VeinEntry> entries = new ArrayList<>();
        for (var def : oreBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        for (var def : rareBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        return entries;
    }

    private static class Node {

        float x, y, z, radius, noiseSeed;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        int size = entry.clusterSize().sample(random);
        int radius = Math.round(1.75f * Math.max(14, Mth.ceil(size * 0.5f)));
        int actualNodeCount = nodeCount + random.nextInt(4) - 2;
        float baseNodeSize = Math.max(8.0f, Math.min(15.0f, radius * 0.12f));
        float channelThickness = Math.max(4.0f, Math.min(8.0f, radius * 0.06f));

        List<Node> nodes = new ArrayList<>();
        long noiseSeed = random.nextLong();

        Node centerNode = new Node();
        centerNode.x = (random.nextFloat() - 0.5f) * radius * 0.2f;
        centerNode.y = (random.nextFloat() - 0.5f) * radius * 0.2f;
        centerNode.z = (random.nextFloat() - 0.5f) * radius * 0.2f;
        centerNode.radius = baseNodeSize * (1.2f + random.nextFloat() * 0.3f);
        centerNode.noiseSeed = random.nextFloat() * 1000;
        nodes.add(centerNode);

        for (int i = 1; i < actualNodeCount; i++) {
            Node node = new Node();
            float scatter = scatterAmount * radius;
            float minSeparation = baseNodeSize * 4.0f;

            for (int attempts = 0; attempts < 10; attempts++) {
                float theta = random.nextFloat() * (float) Math.PI * 2;
                float phi = (random.nextFloat() - 0.5f) * (float) Math.PI;
                float dist = scatter * (0.4f + random.nextFloat() * 0.6f);

                node.x = (float) (Math.cos(theta) * Math.cos(phi) * dist);
                node.y = (float) (Math.sin(phi) * dist * 0.5f);
                node.z = (float) (Math.sin(theta) * Math.cos(phi) * dist);

                boolean valid = true;
                for (Node existing : nodes) {
                    float dx = node.x - existing.x;
                    float dy = node.y - existing.y;
                    float dz = node.z - existing.z;
                    if (Math.sqrt(dx * dx + dy * dy + dz * dz) < minSeparation) {
                        valid = false;
                        break;
                    }
                }
                if (valid) break;
            }

            node.radius = baseNodeSize * (0.7f + random.nextFloat() * 0.6f);
            node.noiseSeed = random.nextFloat() * 1000;
            nodes.add(node);
        }

        List<int[]> channels = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            int connections = 1 + random.nextInt(3);
            List<Integer> nearest = findNearestNodes(nodes, i, connections + 1);
            for (int j = 0; j < Math.min(connections, nearest.size()); j++) {
                int other = nearest.get(j);
                if (other != i) {
                    boolean exists = false;
                    for (int[] ch : channels) {
                        if ((ch[0] == i && ch[1] == other) || (ch[0] == other && ch[1] == i)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        channels.add(new int[] { i, other });
                    }
                }
            }
        }

        for (Node node : nodes) {
            int nodeRadius = Mth.ceil(node.radius * 1.4f);
            int nodeCenterX = origin.getX() + Mth.floor(node.x);
            int nodeCenterY = origin.getY() + Mth.floor(node.y);
            int nodeCenterZ = origin.getZ() + Mth.floor(node.z);

            for (int lx = -nodeRadius; lx <= nodeRadius; lx++) {
                for (int ly = -nodeRadius; ly <= nodeRadius; ly++) {
                    for (int lz = -nodeRadius; lz <= nodeRadius; lz++) {
                        float distToNode = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);

                        int px = nodeCenterX + lx;
                        int py = nodeCenterY + ly;
                        int pz = nodeCenterZ + lz;

                        float noise = noise3D(px + node.noiseSeed, py, pz, noiseSeed);
                        float noisyRadius = node.radius * (1.0f + noise * 0.3f);

                        if (distToNode > noisyRadius) continue;

                        BlockPos blockPos = new BlockPos(px, py, pz);
                        if (generatedBlocks.containsKey(blockPos)) continue;

                        if (random.nextFloat() > 0.95f * entry.density()) continue;

                        long randomSeed = random.nextLong();
                        generatedBlocks.put(blockPos,
                                (access, section) -> placeBlock(section, randomSeed, entry,
                                        new BlockPos.MutableBlockPos(px, py, pz), access, true));
                    }
                }
            }
        }

        int channelRadius = Mth.ceil(channelThickness * 1.3f);
        for (int[] channel : channels) {
            Node n1 = nodes.get(channel[0]);
            Node n2 = nodes.get(channel[1]);

            float cx1 = n1.x, cy1 = n1.y, cz1 = n1.z;
            float cx2 = n2.x, cy2 = n2.y, cz2 = n2.z;
            float length = (float) Math
                    .sqrt((cx2 - cx1) * (cx2 - cx1) + (cy2 - cy1) * (cy2 - cy1) + (cz2 - cz1) * (cz2 - cz1));

            int steps = Mth.ceil(length);
            for (int step = 0; step <= steps; step++) {
                float t = steps > 0 ? (float) step / steps : 0;
                float cx = cx1 + t * (cx2 - cx1);
                float cy = cy1 + t * (cy2 - cy1);
                float cz = cz1 + t * (cz2 - cz1);

                int centerX = origin.getX() + Mth.floor(cx);
                int centerY = origin.getY() + Mth.floor(cy);
                int centerZ = origin.getZ() + Mth.floor(cz);

                for (int lx = -channelRadius; lx <= channelRadius; lx++) {
                    for (int ly = -channelRadius; ly <= channelRadius; ly++) {
                        for (int lz = -channelRadius; lz <= channelRadius; lz++) {
                            float distSq = lx * lx + ly * ly + lz * lz;
                            if (distSq > channelThickness * channelThickness) continue;

                            int px = centerX + lx;
                            int py = centerY + ly;
                            int pz = centerZ + lz;

                            BlockPos blockPos = new BlockPos(px, py, pz);
                            if (generatedBlocks.containsKey(blockPos)) continue;

                            float noise = noise3D(px, py, pz, noiseSeed + 1);
                            float effectiveThickness = channelThickness * (0.8f + 0.4f * noise);
                            if (distSq > effectiveThickness * effectiveThickness) continue;

                            if (random.nextFloat() > 0.9f * entry.density()) continue;

                            long randomSeed = random.nextLong();
                            generatedBlocks.put(blockPos,
                                    (access, section) -> placeBlock(section, randomSeed, entry,
                                            new BlockPos.MutableBlockPos(px, py, pz), access, false));
                        }
                    }
                }
            }
        }

        return generatedBlocks;
    }

    private List<Integer> findNearestNodes(List<Node> nodes, int fromIndex, int count) {
        Node from = nodes.get(fromIndex);
        List<float[]> distances = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            if (i == fromIndex) continue;
            Node to = nodes.get(i);
            float dist = (from.x - to.x) * (from.x - to.x) +
                    (from.y - to.y) * (from.y - to.y) +
                    (from.z - to.z) * (from.z - to.z);
            distances.add(new float[] { i, dist });
        }
        distances.sort((a, b) -> Float.compare(a[1], b[1]));

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < Math.min(count, distances.size()); i++) {
            result.add((int) distances.get(i)[0]);
        }
        return result;
    }

    private float noise3D(float x, float y, float z, long seed) {
        long posHash = Float.floatToIntBits(x * 73856093) ^
                Float.floatToIntBits(y * 19349663) ^
                Float.floatToIntBits(z * 83492791);
        RandomSource noise = new XoroshiroRandomSource(seed ^ posHash);
        return noise.nextFloat() * 2.0f - 1.0f;
    }

    private void placeBlock(LevelChunkSection section, long randomSeed, GTOreDefinition entry,
                            BlockPos.MutableBlockPos pos, BulkSectionAccess access, boolean isNode) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        int sectionX = SectionPos.sectionRelative(pos.getX());
        int sectionY = SectionPos.sectionRelative(pos.getY());
        int sectionZ = SectionPos.sectionRelative(pos.getZ());

        BlockState current = section.getBlockState(sectionX, sectionY, sectionZ);

        float adjustedRareChance = isNode ? rareBlockChance * 2.5f : rareBlockChance;
        if (!rareBlocks.isEmpty() && random.nextFloat() < adjustedRareChance) {
            var ore = GTUtil.getRandomItem(random, rareBlocks);
            if (ore != null) placeOre(ore.block(), current, access, section, random, pos, entry);
        } else {
            var ore = GTUtil.getRandomItem(random, oreBlocks);
            if (ore != null) placeOre(ore.block(), current, access, section, random, pos, entry);
        }
    }

    @Override
    public VeinGenerator build() {
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new ClusterVeinGenerator(new ArrayList<>(oreBlocks), new ArrayList<>(rareBlocks),
                nodeCount, nodeSizeRatio, channelThicknessRatio, scatterAmount, rareBlockChance);
    }

    @Override
    public MapCodec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public ClusterVeinGenerator oreBlock(Material mat, int weight) {
        oreBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }

    public ClusterVeinGenerator rareBlock(Material mat, int weight) {
        rareBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }

    public ClusterVeinGenerator pocketCount(int count) {
        this.nodeCount = count;
        return this;
    }

    public ClusterVeinGenerator pocketRadius(float radius) {
        this.nodeSizeRatio = radius;
        return this;
    }

    public ClusterVeinGenerator channelRadius(float radius) {
        this.channelThicknessRatio = radius;
        return this;
    }

    public ClusterVeinGenerator pocketDensity(float density) {
        this.scatterAmount = density;
        return this;
    }
}
