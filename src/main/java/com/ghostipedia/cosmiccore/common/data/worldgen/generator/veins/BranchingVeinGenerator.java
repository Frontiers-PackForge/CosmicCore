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

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Accessors(fluent = true, chain = true)
public class BranchingVeinGenerator extends VeinGenerator {

    public static final Codec<BranchingVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OreBlockDef.CODEC.listOf().fieldOf("ore_blocks").forGetter(it -> it.oreBlocks),
            OreBlockDef.CODEC.listOf().fieldOf("rare_blocks").forGetter(it -> it.rareBlocks),
            Codec.INT.fieldOf("branch_count").orElse(4).forGetter(it -> it.branchCount),
            Codec.FLOAT.fieldOf("branch_thickness_ratio").orElse(0.15f).forGetter(it -> it.branchThicknessRatio),
            Codec.FLOAT.fieldOf("core_radius_ratio").orElse(0.15f).forGetter(it -> it.coreRadiusRatio),
            Codec.FLOAT.fieldOf("core_noise_intensity").orElse(0.4f).forGetter(it -> it.coreNoiseIntensity),
            Codec.FLOAT.fieldOf("sub_branch_chance").orElse(0.6f).forGetter(it -> it.subBranchChance),
            Codec.FLOAT.fieldOf("rare_block_chance").orElse(0.08f).forGetter(it -> it.rareBlockChance))
            .apply(instance, BranchingVeinGenerator::new));

    public List<OreBlockDef> oreBlocks = new ArrayList<>();
    public List<OreBlockDef> rareBlocks = new ArrayList<>();
    @Setter
    public int branchCount = 4;
    @Setter
    public float branchThicknessRatio = 0.15f;
    @Setter
    public float coreRadiusRatio = 0.15f;
    @Setter
    public float coreNoiseIntensity = 0.4f;
    @Setter
    public float subBranchChance = 0.6f;
    @Setter
    public float rareBlockChance = 0.08f;

    public BranchingVeinGenerator() {}

    public BranchingVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    public BranchingVeinGenerator(List<OreBlockDef> oreBlocks, List<OreBlockDef> rareBlocks,
                                  int branchCount, float branchThicknessRatio, float coreRadiusRatio,
                                  float coreNoiseIntensity, float subBranchChance, float rareBlockChance) {
        this.oreBlocks = oreBlocks;
        this.rareBlocks = rareBlocks;
        this.branchCount = branchCount;
        this.branchThicknessRatio = branchThicknessRatio;
        this.coreRadiusRatio = coreRadiusRatio;
        this.coreNoiseIntensity = coreNoiseIntensity;
        this.subBranchChance = subBranchChance;
        this.rareBlockChance = rareBlockChance;
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        List<VeinEntry> entries = new ArrayList<>();
        for (var def : oreBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        for (var def : rareBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        return entries;
    }

    private static class Segment {

        float x1, y1, z1, x2, y2, z2, thickness;
        int depth;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        int size = entry.clusterSize().sample(random);
        int baseRadius = Math.max(70, Mth.ceil(size * 0.9f));
        int actualBranchCount = branchCount + random.nextInt(3) - 1;
        long noiseSeed = random.nextLong();
        float nodeRadius = Math.max(6.0f, coreRadiusRatio * baseRadius);
        float mainThickness = Math.max(5.0f, branchThicknessRatio * baseRadius);

        List<Segment> allSegments = new ArrayList<>();

        for (int b = 0; b < actualBranchCount; b++) {
            double theta = (b * 2.0 * Math.PI / actualBranchCount) + (random.nextDouble() - 0.5) * 0.8;
            double phi = (random.nextDouble() - 0.5) * 0.6;

            float dirX = (float) (Math.cos(theta) * Math.cos(phi));
            float dirY = (float) (Math.sin(phi) * 0.4f);
            float dirZ = (float) (Math.sin(theta) * Math.cos(phi));

            generateMeanderingBranch(
                    allSegments, random,
                    0, 0, 0,
                    dirX, dirY, dirZ,
                    mainThickness,
                    baseRadius * (0.8f + random.nextFloat() * 0.4f),
                    0,
                    3,
                    subBranchChance);
        }

        float minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;
        for (Segment seg : allSegments) {
            minX = Math.min(minX, Math.min(seg.x1, seg.x2) - seg.thickness);
            maxX = Math.max(maxX, Math.max(seg.x1, seg.x2) + seg.thickness);
            minY = Math.min(minY, Math.min(seg.y1, seg.y2) - seg.thickness);
            maxY = Math.max(maxY, Math.max(seg.y1, seg.y2) + seg.thickness);
            minZ = Math.min(minZ, Math.min(seg.z1, seg.z2) - seg.thickness);
            maxZ = Math.max(maxZ, Math.max(seg.z1, seg.z2) + seg.thickness);
        }

        var posMin = origin.offset((int) minX - 2, (int) minY - 2, (int) minZ - 2);
        var posMax = origin.offset((int) maxX + 2, (int) maxY + 2, (int) maxZ + 2);

        for (BlockPos pos : BlockPos.betweenClosed(posMin, posMax)) {
            float dx = pos.getX() - origin.getX();
            float dy = pos.getY() - origin.getY();
            float dz = pos.getZ() - origin.getZ();

            float distFromOrigin = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

            float noise = noise3D(pos.getX() * 0.12f, pos.getY() * 0.12f, pos.getZ() * 0.12f, noiseSeed);
            float noisyNodeRadius = nodeRadius * (1.0f + noise * coreNoiseIntensity);
            boolean inNode = distFromOrigin <= noisyNodeRadius;

            boolean inSegment = false;
            int closestDepth = 0;
            if (!inNode) {
                float minDist = Float.MAX_VALUE;
                for (Segment seg : allSegments) {
                    float dist = distanceToLineSegment(dx, dy, dz, seg.x1, seg.y1, seg.z1, seg.x2, seg.y2, seg.z2);
                    if (dist <= seg.thickness && dist < minDist) {
                        minDist = dist;
                        inSegment = true;
                        closestDepth = seg.depth;
                    }
                }
            }

            if (!inNode && !inSegment) continue;

            if (random.nextFloat() > 0.15f) continue;

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
            long randomSeed = random.nextLong();
            boolean isMainBranch = inNode || closestDepth == 0;

            generatedBlocks.put(mutablePos.immutable(),
                    (access, section) -> placeBlock(section, randomSeed, entry, mutablePos, access, isMainBranch));
        }

        return generatedBlocks;
    }

    private void generateMeanderingBranch(List<Segment> segments, RandomSource random,
                                          float startX, float startY, float startZ,
                                          float dirX, float dirY, float dirZ,
                                          float thickness, float remainingLength,
                                          int depth, int maxDepth, float splitChance) {
        if (remainingLength <= 0 || thickness < 2.0f || depth > maxDepth) return;
        float segmentLength = Math.min(remainingLength, 8 + random.nextFloat() * 12);
        float endX = startX + dirX * segmentLength;
        float endY = startY + dirY * segmentLength;
        float endZ = startZ + dirZ * segmentLength;

        // Create segment and wander directions randomly
        Segment seg = new Segment();
        seg.x1 = startX;
        seg.y1 = startY;
        seg.z1 = startZ;
        seg.x2 = endX;
        seg.y2 = endY;
        seg.z2 = endZ;
        seg.thickness = thickness;
        seg.depth = depth;
        segments.add(seg);
        float newRemaining = remainingLength - segmentLength;
        float turnAmount = 0.3f + random.nextFloat() * 0.4f;
        float newDirX = dirX + (random.nextFloat() - 0.5f) * turnAmount * 2;
        float newDirY = dirY + (random.nextFloat() - 0.5f) * turnAmount * 0.5f;
        float newDirZ = dirZ + (random.nextFloat() - 0.5f) * turnAmount * 2;

        float len = (float) Math.sqrt(newDirX * newDirX + newDirY * newDirY + newDirZ * newDirZ);
        if (len > 0) {
            newDirX /= len;
            newDirY /= len;
            newDirZ /= len;
        }

        float newThickness = thickness * (0.92f + random.nextFloat() * 0.08f);
        if (depth < maxDepth && random.nextFloat() < splitChance && newRemaining > 20) {
            float splitAngle = (random.nextBoolean() ? 1 : -1) * (0.5f + random.nextFloat() * 0.7f);
            float splitDirX = (float) (newDirX * Math.cos(splitAngle) - newDirZ * Math.sin(splitAngle));
            float splitDirZ = (float) (newDirX * Math.sin(splitAngle) + newDirZ * Math.cos(splitAngle));
            float splitDirY = newDirY + (random.nextFloat() - 0.5f) * 0.3f;
            float splitLen = (float) Math.sqrt(splitDirX * splitDirX + splitDirY * splitDirY + splitDirZ * splitDirZ);
            if (splitLen > 0) {
                splitDirX /= splitLen;
                splitDirY /= splitLen;
                splitDirZ /= splitLen;
            }
            generateMeanderingBranch(
                    segments, random,
                    endX, endY, endZ,
                    splitDirX, splitDirY, splitDirZ,
                    newThickness * (0.5f + random.nextFloat() * 0.3f),
                    newRemaining * (0.4f + random.nextFloat() * 0.3f),
                    depth + 1, maxDepth,
                    splitChance * 0.7f);
        }
        if (newRemaining > 0) {
            generateMeanderingBranch(
                    segments, random,
                    endX, endY, endZ,
                    newDirX, newDirY, newDirZ,
                    newThickness,
                    newRemaining,
                    depth, maxDepth,
                    splitChance);
        }
    }

    private float distanceToLineSegment(float px, float py, float pz,
                                        float x1, float y1, float z1,
                                        float x2, float y2, float z2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float lengthSq = dx * dx + dy * dy + dz * dz;

        if (lengthSq == 0) {
            return (float) Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1) + (pz - z1) * (pz - z1));
        }

        float t = Math.max(0, Math.min(1, ((px - x1) * dx + (py - y1) * dy + (pz - z1) * dz) / lengthSq));

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
                            BlockPos.MutableBlockPos pos, BulkSectionAccess access, boolean isMainBranch) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        int sectionX = SectionPos.sectionRelative(pos.getX());
        int sectionY = SectionPos.sectionRelative(pos.getY());
        int sectionZ = SectionPos.sectionRelative(pos.getZ());

        BlockState current = section.getBlockState(sectionX, sectionY, sectionZ);

        float adjustedRareChance = isMainBranch ? rareBlockChance * 1.5f : rareBlockChance;
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
        return new BranchingVeinGenerator(new ArrayList<>(oreBlocks), new ArrayList<>(rareBlocks),
                branchCount, branchThicknessRatio, coreRadiusRatio, coreNoiseIntensity, subBranchChance,
                rareBlockChance);
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public BranchingVeinGenerator oreBlock(Material mat, int weight) {
        oreBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }

    public BranchingVeinGenerator rareBlock(Material mat, int weight) {
        rareBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }

    public BranchingVeinGenerator branchAngleVariance(float variance) {
        this.coreNoiseIntensity = variance;
        return this;
    }

    public BranchingVeinGenerator branchSplitChance(float chance) {
        this.subBranchChance = chance;
        return this;
    }

    public BranchingVeinGenerator widthDecay(float decay) {
        this.branchThicknessRatio = decay;
        return this;
    }

    public BranchingVeinGenerator lengthMultiplier(float multiplier) {
        this.coreRadiusRatio = multiplier;
        return this;
    }
}
