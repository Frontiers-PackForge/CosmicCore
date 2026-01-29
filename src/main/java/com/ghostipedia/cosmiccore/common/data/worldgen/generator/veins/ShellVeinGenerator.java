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
public class ShellVeinGenerator extends VeinGenerator {

    public static final Codec<ShellVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OreBlockDef.CODEC.listOf().fieldOf("inner_blocks").forGetter(it -> it.innerBlocks),
            OreBlockDef.CODEC.listOf().fieldOf("outer_blocks").forGetter(it -> it.outerBlocks),
            OreBlockDef.CODEC.listOf().fieldOf("core_blocks").forGetter(it -> it.coreBlocks),
            Codec.FLOAT.fieldOf("inner_radius_ratio").orElse(0.4f).forGetter(it -> it.innerRadiusRatio),
            Codec.FLOAT.fieldOf("outer_radius_ratio").orElse(0.7f).forGetter(it -> it.outerRadiusRatio),
            Codec.FLOAT.fieldOf("shell_noise_intensity").orElse(0.25f).forGetter(it -> it.shellNoiseIntensity),
            Codec.FLOAT.fieldOf("ore_mixing_chance").orElse(0.15f).forGetter(it -> it.oreMixingChance))
            .apply(instance, ShellVeinGenerator::new));

    public List<OreBlockDef> innerBlocks = new ArrayList<>();
    public List<OreBlockDef> outerBlocks = new ArrayList<>();
    public List<OreBlockDef> coreBlocks = new ArrayList<>();
    @Setter
    public float innerRadiusRatio = 0.4f;
    @Setter
    public float outerRadiusRatio = 0.7f;
    @Setter
    public float shellNoiseIntensity = 0.25f;
    @Setter
    public float oreMixingChance = 0.15f;

    public ShellVeinGenerator() {}

    public ShellVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    public ShellVeinGenerator(List<OreBlockDef> innerBlocks, List<OreBlockDef> outerBlocks,
                              List<OreBlockDef> coreBlocks, float innerRadiusRatio, float outerRadiusRatio,
                              float shellNoiseIntensity, float oreMixingChance) {
        this.innerBlocks = innerBlocks;
        this.outerBlocks = outerBlocks;
        this.coreBlocks = coreBlocks;
        this.innerRadiusRatio = innerRadiusRatio;
        this.outerRadiusRatio = outerRadiusRatio;
        this.shellNoiseIntensity = shellNoiseIntensity;
        this.oreMixingChance = oreMixingChance;
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        List<VeinEntry> entries = new ArrayList<>();
        for (var def : coreBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        for (var def : innerBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        for (var def : outerBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        return entries;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        int size = entry.clusterSize().sample(random);
        int radius = Math.max(32, Mth.ceil(size * 0.5f));

        float innerRadius = radius * innerRadiusRatio;
        float outerRadius = radius * outerRadiusRatio;

        long noiseSeed = random.nextLong();

        var posMin = origin.offset(-radius, -radius, -radius);
        var posMax = origin.offset(+radius, +radius, +radius);

        for (BlockPos pos : BlockPos.betweenClosed(posMin, posMax)) {
            int dx = pos.getX() - origin.getX();
            int dy = pos.getY() - origin.getY();
            int dz = pos.getZ() - origin.getZ();
            float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist > radius) continue;

            float normalizedDist = dist / radius;
            float falloff = edgeFalloff(normalizedDist, 0.6f);
            if (random.nextFloat() > falloff * entry.density()) continue;

            float noise1 = noise3D(pos.getX() * 0.12f, pos.getY() * 0.12f, pos.getZ() * 0.12f, noiseSeed);
            float noise2 = noise3D(pos.getX() * 0.25f, pos.getY() * 0.25f, pos.getZ() * 0.25f, noiseSeed + 100);
            float combinedNoise = noise1 * 0.7f + noise2 * 0.3f;

            float noisyInnerRadius = innerRadius * (1.0f + combinedNoise * shellNoiseIntensity);
            float noisyOuterRadius = outerRadius * (1.0f + combinedNoise * shellNoiseIntensity * 0.8f);

            final Zone primaryZone;
            final boolean nearBoundary;

            if (dist <= noisyInnerRadius) {
                primaryZone = Zone.CORE;
                nearBoundary = dist > noisyInnerRadius * 0.75f;
            } else if (dist <= noisyOuterRadius) {
                primaryZone = Zone.INNER;
                nearBoundary = dist < noisyInnerRadius * 1.25f || dist > noisyOuterRadius * 0.85f;
            } else {
                primaryZone = Zone.OUTER;
                nearBoundary = dist < noisyOuterRadius * 1.15f;
            }

            if (random.nextFloat() > 0.45f) continue;

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
            long randomSeed = random.nextLong();

            generatedBlocks.put(mutablePos.immutable(),
                    (access, section) -> placeBlock(section, randomSeed, entry, mutablePos, access, primaryZone,
                            nearBoundary));
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

    private void placeBlock(LevelChunkSection section, long randomSeed, GTOreDefinition entry,
                            BlockPos.MutableBlockPos pos, BulkSectionAccess access, Zone zone, boolean nearBoundary) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        int sectionX = SectionPos.sectionRelative(pos.getX());
        int sectionY = SectionPos.sectionRelative(pos.getY());
        int sectionZ = SectionPos.sectionRelative(pos.getZ());

        BlockState current = section.getBlockState(sectionX, sectionY, sectionZ);

        List<OreBlockDef> primaryBlocks = switch (zone) {
            case CORE -> coreBlocks.isEmpty() ? innerBlocks : coreBlocks;
            case INNER -> innerBlocks.isEmpty() ? outerBlocks : innerBlocks;
            case OUTER -> outerBlocks.isEmpty() ? innerBlocks : outerBlocks;
        };

        List<OreBlockDef> adjacentBlocks = switch (zone) {
            case CORE -> innerBlocks.isEmpty() ? outerBlocks : innerBlocks;
            case INNER -> random.nextBoolean() ?
                    (coreBlocks.isEmpty() ? innerBlocks : coreBlocks) :
                    (outerBlocks.isEmpty() ? innerBlocks : outerBlocks);
            case OUTER -> innerBlocks.isEmpty() ? (coreBlocks.isEmpty() ? outerBlocks : coreBlocks) : innerBlocks;
        };

        if (primaryBlocks.isEmpty()) return;

        List<OreBlockDef> blocksToUse;
        if (nearBoundary && random.nextFloat() < oreMixingChance && !adjacentBlocks.isEmpty()) {
            blocksToUse = adjacentBlocks;
        } else {
            blocksToUse = primaryBlocks;
        }

        var ore = GTUtil.getRandomItem(random, blocksToUse);
        if (ore != null) placeOre(ore.block(), current, access, section, random, pos, entry);
    }

    private enum Zone {
        CORE,
        INNER,
        OUTER
    }

    @Override
    public VeinGenerator build() {
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new ShellVeinGenerator(new ArrayList<>(innerBlocks), new ArrayList<>(outerBlocks),
                new ArrayList<>(coreBlocks), innerRadiusRatio, outerRadiusRatio, shellNoiseIntensity, oreMixingChance);
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public ShellVeinGenerator coreBlock(Material mat, int weight) {
        coreBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }

    public ShellVeinGenerator innerBlock(Material mat, int weight) {
        innerBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }

    public ShellVeinGenerator outerBlock(Material mat, int weight) {
        outerBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }
}
