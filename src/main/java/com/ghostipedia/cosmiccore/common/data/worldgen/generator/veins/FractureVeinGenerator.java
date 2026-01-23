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
 * Fracture vein generator - creates a "Shattered Geode" pattern.
 * Central hollow geode shell with crystal spikes pointing inward,
 * surrounded by radiating cracks/fractures extending outward like
 * shattered glass from an impact point.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Accessors(fluent = true, chain = true)
public class FractureVeinGenerator extends VeinGenerator {

    public static final Codec<FractureVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OreBlockDef.CODEC.listOf().fieldOf("ore_blocks").forGetter(it -> it.oreBlocks),
            OreBlockDef.CODEC.listOf().fieldOf("rare_blocks").forGetter(it -> it.rareBlocks),
            Codec.FLOAT.fieldOf("geode_radius").orElse(12.0f).forGetter(it -> it.geodeRadius),
            Codec.FLOAT.fieldOf("rare_block_chance").orElse(0.08f).forGetter(it -> it.rareBlockChance),
            Codec.INT.fieldOf("crack_count").orElse(8).forGetter(it -> it.crackCount),
            Codec.FLOAT.fieldOf("spike_length").orElse(6.0f).forGetter(it -> it.spikeLength))
            .apply(instance, FractureVeinGenerator::new));

    public List<OreBlockDef> oreBlocks = new ArrayList<>();
    public List<OreBlockDef> rareBlocks = new ArrayList<>();
    @Setter
    public float geodeRadius = 12.0f; // Radius of the central geode shell
    @Setter
    public float rareBlockChance = 0.08f; // Higher chance for rare blocks in geode center
    @Setter
    public int crackCount = 8; // Number of radiating cracks
    @Setter
    public float spikeLength = 6.0f; // Length of crystal spikes pointing inward

    public FractureVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    public FractureVeinGenerator(List<OreBlockDef> oreBlocks, List<OreBlockDef> rareBlocks, float geodeRadius,
                                 float rareBlockChance, int crackCount, float spikeLength) {
        this.oreBlocks = oreBlocks;
        this.rareBlocks = rareBlocks;
        this.geodeRadius = geodeRadius;
        this.rareBlockChance = rareBlockChance;
        this.crackCount = crackCount;
        this.spikeLength = spikeLength;
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        List<VeinEntry> entries = new ArrayList<>();
        for (var def : oreBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        for (var def : rareBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        return entries;
    }

    // Radiating crack data
    private static class Crack {

        float dirX, dirY, dirZ; // Direction from center
        float length; // How far the crack extends
        float thickness; // Thickness of the crack
        float noiseSeed;
    }

    // Crystal spike data
    private static class Spike {

        float dirX, dirY, dirZ; // Direction pointing inward
        float baseX, baseY, baseZ; // Starting point on geode shell
        float length;
        float thickness;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        int size = entry.clusterSize().sample(random);
        long noiseSeed = random.nextLong();

        // Scale geode based on vein size
        float actualGeodeRadius = geodeRadius * (0.8f + (size / 100.0f) * 0.4f);
        float shellThickness = 3.0f + random.nextFloat() * 2.0f; // 3-5 block thick shell
        float innerRadius = actualGeodeRadius - shellThickness;

        // === PART 1: GEODE SHELL ===
        // Hollow sphere with ore on the shell
        int geodeRadiusInt = Mth.ceil(actualGeodeRadius * 1.2f);
        for (int lx = -geodeRadiusInt; lx <= geodeRadiusInt; lx++) {
            for (int ly = -geodeRadiusInt; ly <= geodeRadiusInt; ly++) {
                for (int lz = -geodeRadiusInt; lz <= geodeRadiusInt; lz++) {
                    float dist = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);

                    // Noise for irregular geode shape
                    float noise = noise3D(lx * 0.2f, ly * 0.2f, lz * 0.2f, noiseSeed);
                    float noisyOuterRadius = actualGeodeRadius * (1.0f + noise * 0.15f);
                    float noisyInnerRadius = innerRadius * (1.0f + noise * 0.2f);

                    // Only place on shell (between inner and outer radius)
                    if (dist < noisyInnerRadius || dist > noisyOuterRadius) continue;

                    int px = origin.getX() + lx;
                    int py = origin.getY() + ly;
                    int pz = origin.getZ() + lz;
                    BlockPos blockPos = new BlockPos(px, py, pz);

                    if (generatedBlocks.containsKey(blockPos)) continue;
                    if (random.nextFloat() > 0.92f * entry.density()) continue;

                    long randomSeed = random.nextLong();
                    // Rare blocks more common on inner surface of geode
                    boolean isInnerSurface = dist < (noisyInnerRadius + noisyOuterRadius) / 2;
                    generatedBlocks.put(blockPos,
                            (access, section) -> placeBlock(section, randomSeed, entry,
                                    new BlockPos.MutableBlockPos(px, py, pz), access, isInnerSurface));
                }
            }
        }

        // === PART 2: CRYSTAL SPIKES POINTING INWARD ===
        // Generate spikes on the inner surface pointing toward center
        int actualSpikeCount = 12 + random.nextInt(8); // 12-20 spikes
        List<Spike> spikes = new ArrayList<>();

        for (int i = 0; i < actualSpikeCount; i++) {
            Spike spike = new Spike();

            // Random point on sphere surface using spherical coordinates
            float theta = random.nextFloat() * (float) (2 * Math.PI);
            float phi = (float) Math.acos(2 * random.nextFloat() - 1);

            spike.dirX = (float) (Math.sin(phi) * Math.cos(theta));
            spike.dirY = (float) Math.cos(phi);
            spike.dirZ = (float) (Math.sin(phi) * Math.sin(theta));

            // Base is on inner surface of geode
            float baseNoise = noise3D(spike.dirX * 10, spike.dirY * 10, spike.dirZ * 10, noiseSeed + 100);
            float baseRadius = innerRadius * (1.0f + baseNoise * 0.15f);
            spike.baseX = spike.dirX * baseRadius;
            spike.baseY = spike.dirY * baseRadius;
            spike.baseZ = spike.dirZ * baseRadius;

            // Spike points inward (negative direction)
            spike.length = spikeLength * (0.5f + random.nextFloat() * 1.0f); // 50-150% of base length
            spike.thickness = 1.5f + random.nextFloat() * 1.5f; // 1.5-3 blocks thick at base

            spikes.add(spike);
        }

        // Generate spike blocks
        for (Spike spike : spikes) {
            int steps = Mth.ceil(spike.length);
            for (int step = 0; step <= steps; step++) {
                float t = steps > 0 ? (float) step / steps : 0;

                // Position along spike (moving inward toward center)
                float cx = spike.baseX - spike.dirX * spike.length * t;
                float cy = spike.baseY - spike.dirY * spike.length * t;
                float cz = spike.baseZ - spike.dirZ * spike.length * t;

                // Spike tapers toward tip
                float currentThickness = spike.thickness * (1.0f - t * 0.7f);
                int thicknessInt = Math.max(1, Mth.ceil(currentThickness));

                // Fill cross-section
                for (int lx = -thicknessInt; lx <= thicknessInt; lx++) {
                    for (int ly = -thicknessInt; ly <= thicknessInt; ly++) {
                        for (int lz = -thicknessInt; lz <= thicknessInt; lz++) {
                            float distSq = lx * lx + ly * ly + lz * lz;
                            if (distSq > currentThickness * currentThickness) continue;

                            int px = origin.getX() + Mth.floor(cx) + lx;
                            int py = origin.getY() + Mth.floor(cy) + ly;
                            int pz = origin.getZ() + Mth.floor(cz) + lz;

                            BlockPos blockPos = new BlockPos(px, py, pz);
                            if (generatedBlocks.containsKey(blockPos)) continue;
                            if (random.nextFloat() > 0.95f * entry.density()) continue;

                            long randomSeed = random.nextLong();
                            // Tips of spikes have higher rare chance
                            boolean isTip = t > 0.7f;
                            generatedBlocks.put(blockPos,
                                    (access, section) -> placeBlock(section, randomSeed, entry,
                                            new BlockPos.MutableBlockPos(px, py, pz), access, isTip));
                        }
                    }
                }
            }
        }

        // === PART 3: RADIATING CRACKS EXTENDING OUTWARD ===
        // Cracks originate from geode surface and extend outward
        int actualCrackCount = crackCount + random.nextInt(5); // 8-12 cracks
        List<Crack> cracks = new ArrayList<>();

        for (int i = 0; i < actualCrackCount; i++) {
            Crack crack = new Crack();

            // Random direction outward from center
            float theta = random.nextFloat() * (float) (2 * Math.PI);
            float phi = (float) Math.acos(2 * random.nextFloat() - 1);

            crack.dirX = (float) (Math.sin(phi) * Math.cos(theta));
            crack.dirY = (float) Math.cos(phi) * 0.6f; // Bias toward horizontal
            crack.dirZ = (float) (Math.sin(phi) * Math.sin(theta));

            // Normalize
            float len = (float) Math.sqrt(crack.dirX * crack.dirX + crack.dirY * crack.dirY + crack.dirZ * crack.dirZ);
            crack.dirX /= len;
            crack.dirY /= len;
            crack.dirZ /= len;

            crack.length = actualGeodeRadius * (1.5f + random.nextFloat() * 2.0f); // 1.5-3.5x geode radius
            crack.thickness = 2.0f + random.nextFloat() * 2.0f; // 2-4 blocks thick
            crack.noiseSeed = random.nextFloat() * 1000;

            cracks.add(crack);
        }

        // Generate crack blocks
        for (Crack crack : cracks) {
            int steps = Mth.ceil(crack.length);
            float startDist = actualGeodeRadius; // Start at geode surface

            for (int step = 0; step <= steps; step++) {
                float t = steps > 0 ? (float) step / steps : 0;
                float dist = startDist + crack.length * t;

                // Add wobble to crack path
                float wobbleX = noise3D(t * 5 + crack.noiseSeed, 0, 0, noiseSeed) * 3.0f;
                float wobbleY = noise3D(0, t * 5 + crack.noiseSeed, 0, noiseSeed) * 2.0f;
                float wobbleZ = noise3D(0, 0, t * 5 + crack.noiseSeed, noiseSeed) * 3.0f;

                float cx = crack.dirX * dist + wobbleX;
                float cy = crack.dirY * dist + wobbleY;
                float cz = crack.dirZ * dist + wobbleZ;

                // Crack tapers and becomes more sparse toward end
                float currentThickness = crack.thickness * (1.0f - t * 0.6f);
                int thicknessInt = Math.max(1, Mth.ceil(currentThickness));

                // Cracks become more sparse toward tips
                float placementChance = 0.9f - t * 0.4f;

                for (int lx = -thicknessInt; lx <= thicknessInt; lx++) {
                    for (int ly = -thicknessInt; ly <= thicknessInt; ly++) {
                        for (int lz = -thicknessInt; lz <= thicknessInt; lz++) {
                            float distSq = lx * lx + ly * ly + lz * lz;
                            if (distSq > currentThickness * currentThickness) continue;

                            int px = origin.getX() + Mth.floor(cx) + lx;
                            int py = origin.getY() + Mth.floor(cy) + ly;
                            int pz = origin.getZ() + Mth.floor(cz) + lz;

                            BlockPos blockPos = new BlockPos(px, py, pz);
                            if (generatedBlocks.containsKey(blockPos)) continue;
                            if (random.nextFloat() > placementChance * entry.density()) continue;

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

    // Simple 3D noise function
    private float noise3D(float x, float y, float z, long seed) {
        long posHash = Float.floatToIntBits(x * 73856093) ^
                Float.floatToIntBits(y * 19349663) ^
                Float.floatToIntBits(z * 83492791);
        RandomSource noise = new XoroshiroRandomSource(seed ^ posHash);
        return noise.nextFloat() * 2.0f - 1.0f;
    }

    private void placeBlock(LevelChunkSection section, long randomSeed, GTOreDefinition entry,
                            BlockPos.MutableBlockPos pos, net.minecraft.world.level.chunk.BulkSectionAccess access,
                            boolean atIntersection) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        int sectionX = SectionPos.sectionRelative(pos.getX());
        int sectionY = SectionPos.sectionRelative(pos.getY());
        int sectionZ = SectionPos.sectionRelative(pos.getZ());

        BlockState current = section.getBlockState(sectionX, sectionY, sectionZ);

        // Higher rare chance at fracture intersections
        float adjustedRareChance = atIntersection ? rareBlockChance * 3.0f : rareBlockChance;
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
        return new FractureVeinGenerator(new ArrayList<>(oreBlocks), new ArrayList<>(rareBlocks),
                geodeRadius, rareBlockChance, crackCount, spikeLength);
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public FractureVeinGenerator oreBlock(Material mat, int weight) {
        oreBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }

    public FractureVeinGenerator rareBlock(Material mat, int weight) {
        rareBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }
}
