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
public class StringerVeinGenerator extends VeinGenerator {

    public static final MapCodec<StringerVeinGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            OreBlockDef.CODEC.listOf().fieldOf("ore_blocks").forGetter(it -> it.oreBlocks),
            OreBlockDef.CODEC.listOf().fieldOf("rare_blocks").forGetter(it -> it.rareBlocks),
            Codec.INT.fieldOf("stringer_count").orElse(16).forGetter(it -> it.stringerCount),
            Codec.FLOAT.fieldOf("core_radius_ratio").orElse(0.25f).forGetter(it -> it.coreRadiusRatio),
            Codec.FLOAT.fieldOf("stringer_thickness_ratio").orElse(0.1f).forGetter(it -> it.stringerThicknessRatio),
            Codec.FLOAT.fieldOf("core_noise_intensity").orElse(0.6f).forGetter(it -> it.coreNoiseIntensity),
            Codec.FLOAT.fieldOf("ore_density").orElse(0.6f).forGetter(it -> it.oreDensity),
            Codec.FLOAT.fieldOf("rare_block_chance").orElse(0.04f).forGetter(it -> it.rareBlockChance))
            .apply(instance, StringerVeinGenerator::new));

    public List<OreBlockDef> oreBlocks = new ArrayList<>();
    public List<OreBlockDef> rareBlocks = new ArrayList<>();
    @Setter
    public int stringerCount = 16;
    @Setter
    public float coreRadiusRatio = 0.25f;
    @Setter
    public float stringerThicknessRatio = 0.1f;
    @Setter
    public float coreNoiseIntensity = 0.6f;
    @Setter
    public float oreDensity = 0.6f;
    @Setter
    public float rareBlockChance = 0.04f;

    public StringerVeinGenerator() {}

    public StringerVeinGenerator(List<OreBlockDef> oreBlocks, List<OreBlockDef> rareBlocks,
                                 int stringerCount, float coreRadiusRatio, float stringerThicknessRatio,
                                 float coreNoiseIntensity, float oreDensity, float rareBlockChance) {
        this.oreBlocks = oreBlocks;
        this.rareBlocks = rareBlocks;
        this.stringerCount = stringerCount;
        this.coreRadiusRatio = coreRadiusRatio;
        this.stringerThicknessRatio = stringerThicknessRatio;
        this.coreNoiseIntensity = coreNoiseIntensity;
        this.oreDensity = oreDensity;
        this.rareBlockChance = rareBlockChance;
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        List<VeinEntry> entries = new ArrayList<>();
        for (var def : oreBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        for (var def : rareBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        return entries;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        int size = entry.clusterSize().sample(random);
        int radius = Math.round(1.75f * Math.max(12, Mth.ceil(size * 0.4f)));
        float coreRadius = Math.max(5.0f, radius * coreRadiusRatio);
        float baseStringerThickness = Math.max(2.0f, radius * stringerThicknessRatio);
        int actualStringerCount = stringerCount + random.nextInt(6) - 3;

        long noiseSeed = random.nextLong();
        double[][] stringerDirs = new double[actualStringerCount][3];
        float[] stringerThickness = new float[actualStringerCount];
        float[] stringerLengths = new float[actualStringerCount];

        for (int i = 0; i < actualStringerCount; i++) {
            double theta = i * 2.399963 + random.nextDouble() * 0.5;
            double phi = (random.nextDouble() - 0.5) * 1.4;
            stringerDirs[i][0] = Math.cos(theta) * Math.cos(phi);
            stringerDirs[i][1] = Math.sin(phi) * 0.6;
            stringerDirs[i][2] = Math.sin(theta) * Math.cos(phi);
            stringerThickness[i] = baseStringerThickness * (0.5f + random.nextFloat() * 0.7f);
            stringerLengths[i] = 0.8f + random.nextFloat() * 0.4f;
        }

        var posMin = origin.offset(-radius, -radius, -radius);
        var posMax = origin.offset(+radius, +radius, +radius);

        for (BlockPos pos : BlockPos.betweenClosed(posMin, posMax)) {
            int dx = pos.getX() - origin.getX();
            int dy = pos.getY() - origin.getY();
            int dz = pos.getZ() - origin.getZ();
            float distFromOrigin = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distFromOrigin > radius) continue;

            float noise1 = noise3D(pos.getX() * 0.15f, pos.getY() * 0.15f, pos.getZ() * 0.15f, noiseSeed);
            float noise2 = noise3D(pos.getX() * 0.3f, pos.getY() * 0.3f, pos.getZ() * 0.3f, noiseSeed + 100);
            float combinedNoise = noise1 * 0.7f + noise2 * 0.3f;
            float noisyRadius = coreRadius * (1.0f + combinedNoise * coreNoiseIntensity);
            boolean inCore = distFromOrigin <= noisyRadius;

            boolean inBlob = false;
            if (!inCore && distFromOrigin <= coreRadius * 2.0f) {
                float blobNoise = noise3D(pos.getX() * 0.2f, pos.getY() * 0.2f, pos.getZ() * 0.2f, noiseSeed + 50);
                if (blobNoise > 0.4f) {
                    float blobRadius = coreRadius * 0.4f * blobNoise;
                    if (distFromOrigin <= coreRadius + blobRadius) {
                        inBlob = true;
                    }
                }
            }

            boolean inStringer = false;

            if (!inCore && !inBlob) {
                for (int i = 0; i < actualStringerCount; i++) {
                    float maxLen = radius * stringerLengths[i];
                    float distToStringer = distanceToRay(dx, dy, dz,
                            stringerDirs[i][0], stringerDirs[i][1], stringerDirs[i][2], (int) maxLen);

                    float progress = Math.max(0, distFromOrigin - coreRadius) / (maxLen - coreRadius);
                    float taperFactor = 1.0f - progress * 0.5f;

                    float effectiveThickness = stringerThickness[i] * taperFactor;

                    if (distToStringer <= effectiveThickness) {
                        inStringer = true;
                        break;
                    }
                }
            }

            if (!inCore && !inBlob && !inStringer) continue;

            if (random.nextFloat() > oreDensity) continue;

            float normalizedDist = distFromOrigin / radius;
            float falloff = edgeFalloff(normalizedDist, 0.75f);
            if (random.nextFloat() > falloff * entry.density()) continue;

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
            long randomSeed = random.nextLong();
            boolean isInCore = inCore || inBlob;

            generatedBlocks.put(mutablePos.immutable(),
                    (access, section) -> placeBlock(section, randomSeed, entry, mutablePos, access, isInCore));
        }

        return generatedBlocks;
    }

    private float noise3D(float x, float y, float z, long seed) {
        long posHash = Float.floatToIntBits(x * 73856093) ^
                Float.floatToIntBits(y * 19349663) ^
                Float.floatToIntBits(z * 83492791);
        RandomSource noise = new XoroshiroRandomSource(seed ^ posHash);
        return noise.nextFloat() * 2.0f - 1.0f;
    }

    private float distanceToRay(int px, int py, int pz, double dx, double dy, double dz, int maxLen) {
        double t = Math.max(0, Math.min(maxLen, px * dx + py * dy + pz * dz));
        double closestX = t * dx;
        double closestY = t * dy;
        double closestZ = t * dz;
        return (float) Math.sqrt(
                (px - closestX) * (px - closestX) +
                        (py - closestY) * (py - closestY) +
                        (pz - closestZ) * (pz - closestZ));
    }

    private void placeBlock(LevelChunkSection section, long randomSeed, GTOreDefinition entry,
                            BlockPos.MutableBlockPos pos, BulkSectionAccess access, boolean isCore) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        int sectionX = SectionPos.sectionRelative(pos.getX());
        int sectionY = SectionPos.sectionRelative(pos.getY());
        int sectionZ = SectionPos.sectionRelative(pos.getZ());

        BlockState current = section.getBlockState(sectionX, sectionY, sectionZ);

        float adjustedRareChance = isCore ? rareBlockChance * 2 : rareBlockChance;
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
        return new StringerVeinGenerator(new ArrayList<>(oreBlocks), new ArrayList<>(rareBlocks),
                stringerCount, coreRadiusRatio, stringerThicknessRatio, coreNoiseIntensity, oreDensity,
                rareBlockChance);
    }

    @Override
    public MapCodec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public StringerVeinGenerator oreBlock(Material mat, int weight) {
        oreBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }

    public StringerVeinGenerator rareBlock(Material mat, int weight) {
        rareBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }

    public StringerVeinGenerator tendrilCount(int count) {
        this.stringerCount = count;
        return this;
    }

    public StringerVeinGenerator coreRadius(float radius) {
        this.coreRadiusRatio = radius;
        return this;
    }

    public StringerVeinGenerator tendrilRadius(float radius) {
        this.stringerThicknessRatio = radius;
        return this;
    }

    public StringerVeinGenerator coreDensity(float density) {
        this.oreDensity = density;
        return this;
    }

    public StringerVeinGenerator tendrilCurvature(float curvature) {
        this.coreNoiseIntensity = curvature;
        return this;
    }
}
