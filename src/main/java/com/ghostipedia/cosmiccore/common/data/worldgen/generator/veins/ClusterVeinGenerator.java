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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.VeinGeneratorUtil.*;

/**
 * Cluster vein generator - creates multiple ore-rich nodes/pockets scattered
 * throughout the vein volume, connected by mineral channels.
 * Like finding a system of geodes or ore pockets in a mineral formation.
 * Replaces the boring flat disk/lens shape with something worth exploring.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Accessors(fluent = true, chain = true)
public class ClusterVeinGenerator extends VeinGenerator {

    public static final Codec<ClusterVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
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
    public int nodeCount = 10; // Distinct node pockets spread throughout large volume
    @Setter
    public float nodeSizeRatio = 0.06f; // Small nodes relative to large radius
    @Setter
    public float channelThicknessRatio = 0.025f; // Thin channels connecting nodes
    @Setter
    public float scatterAmount = 0.9f; // Push nodes far apart
    @Setter
    public float rareBlockChance = 0.06f;

    public ClusterVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

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

    // Node data structure
    private static class Node {

        float x, y, z;
        float radius;
        float noiseSeed;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        int size = entry.clusterSize().sample(random);
        // Cluster veins: moderate spread with distinct nodes
        // 1.5x size, minimum 80 blocks radius - balanced for performance and visibility
        int radius = Math.max(80, Mth.ceil(size * 1.5f));
        int actualNodeCount = nodeCount + random.nextInt(4) - 2; // 10-14 nodes

        // Node sizes scale with radius for visibility (8-15 blocks)
        float baseNodeSize = Math.max(8.0f, Math.min(15.0f, radius * 0.12f));
        // Channel thickness (4-8 blocks)
        float channelThickness = Math.max(4.0f, Math.min(8.0f, radius * 0.06f));

        // Generate node positions scattered throughout the volume
        List<Node> nodes = new ArrayList<>();
        long noiseSeed = random.nextLong();

        // First node is near center - slightly larger as the "main" pocket
        Node centerNode = new Node();
        centerNode.x = (random.nextFloat() - 0.5f) * radius * 0.2f;
        centerNode.y = (random.nextFloat() - 0.5f) * radius * 0.2f;
        centerNode.z = (random.nextFloat() - 0.5f) * radius * 0.2f;
        centerNode.radius = baseNodeSize * (1.2f + random.nextFloat() * 0.3f); // Central node slightly larger
        centerNode.noiseSeed = random.nextFloat() * 1000;
        nodes.add(centerNode);

        // Generate scattered nodes - ensure minimum spacing between nodes
        for (int i = 1; i < actualNodeCount; i++) {
            Node node = new Node();
            // Scatter nodes throughout the volume with minimum separation
            float scatter = scatterAmount * radius;

            // Try to place node with minimum distance from others
            int attempts = 0;
            boolean validPlacement = false;
            while (!validPlacement && attempts < 10) {
                float theta = random.nextFloat() * (float) Math.PI * 2;
                float phi = (random.nextFloat() - 0.5f) * (float) Math.PI;
                // Push nodes toward outer regions for better spacing
                float dist = scatter * (0.4f + random.nextFloat() * 0.6f);

                node.x = (float) (Math.cos(theta) * Math.cos(phi) * dist);
                node.y = (float) (Math.sin(phi) * dist * 0.5f); // Vertical compression
                node.z = (float) (Math.sin(theta) * Math.cos(phi) * dist);

                // Check minimum distance from existing nodes (at least 4x node size for clear separation)
                validPlacement = true;
                float minSeparation = baseNodeSize * 4.0f;
                for (Node existing : nodes) {
                    float dx = node.x - existing.x;
                    float dy = node.y - existing.y;
                    float dz = node.z - existing.z;
                    float distToOther = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distToOther < minSeparation) {
                        validPlacement = false;
                        break;
                    }
                }
                attempts++;
            }

            node.radius = baseNodeSize * (0.7f + random.nextFloat() * 0.6f);
            node.noiseSeed = random.nextFloat() * 1000;
            nodes.add(node);
        }

        // Pre-compute channel connections (connect nearby nodes)
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

        // OPTIMIZED: Instead of iterating 300^3 positions, iterate only around nodes and channels
        // This reduces iterations from ~27 million to ~50-100k

        // Generate blocks for each node (spherical iteration)
        for (Node node : nodes) {
            int nodeRadius = Mth.ceil(node.radius * 1.4f); // Slightly larger for noise
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

        // Generate blocks for each channel (cylinder iteration along line segment)
        int channelRadius = Mth.ceil(channelThickness * 1.3f);
        for (int[] channel : channels) {
            Node n1 = nodes.get(channel[0]);
            Node n2 = nodes.get(channel[1]);

            float cx1 = n1.x, cy1 = n1.y, cz1 = n1.z;
            float cx2 = n2.x, cy2 = n2.y, cz2 = n2.z;
            float length = (float) Math
                    .sqrt((cx2 - cx1) * (cx2 - cx1) + (cy2 - cy1) * (cy2 - cy1) + (cz2 - cz1) * (cz2 - cz1));

            // Step along channel
            int steps = Mth.ceil(length);
            for (int step = 0; step <= steps; step++) {
                float t = steps > 0 ? (float) step / steps : 0;
                float cx = cx1 + t * (cx2 - cx1);
                float cy = cy1 + t * (cy2 - cy1);
                float cz = cz1 + t * (cz2 - cz1);

                int centerX = origin.getX() + Mth.floor(cx);
                int centerY = origin.getY() + Mth.floor(cy);
                int centerZ = origin.getZ() + Mth.floor(cz);

                // Fill cylinder cross-section at this point
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
        List<Integer> result = new ArrayList<>();
        Node from = nodes.get(fromIndex);

        // Simple O(n) nearest neighbor search
        List<float[]> distances = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            if (i == fromIndex) continue;
            Node to = nodes.get(i);
            float dist = (from.x - to.x) * (from.x - to.x) +
                    (from.y - to.y) * (from.y - to.y) +
                    (from.z - to.z) * (from.z - to.z);
            distances.add(new float[] { i, dist });
        }

        // Sort by distance
        distances.sort((a, b) -> Float.compare(a[1], b[1]));

        for (int i = 0; i < Math.min(count, distances.size()); i++) {
            result.add((int) distances.get(i)[0]);
        }
        return result;
    }

    private float distanceToLineSegment(float px, float py, float pz,
                                        float x1, float y1, float z1,
                                        float x2, float y2, float z2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float lengthSq = dx * dx + dy * dy + dz * dz;

        if (lengthSq == 0) {
            // Degenerate segment
            return (float) Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1) + (pz - z1) * (pz - z1));
        }

        float t = Math.max(0, Math.min(1,
                ((px - x1) * dx + (py - y1) * dy + (pz - z1) * dz) / lengthSq));

        float closestX = x1 + t * dx;
        float closestY = y1 + t * dy;
        float closestZ = z1 + t * dz;

        return (float) Math.sqrt(
                (px - closestX) * (px - closestX) +
                        (py - closestY) * (py - closestY) +
                        (pz - closestZ) * (pz - closestZ));
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

        // Higher rare chance in nodes (the ore pockets)
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
    public Codec<? extends VeinGenerator> codec() {
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
}
