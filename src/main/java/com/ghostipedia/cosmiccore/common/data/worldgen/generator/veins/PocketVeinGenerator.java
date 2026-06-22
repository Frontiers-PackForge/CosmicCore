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

/**
 * A single small ore blob - the fragment-pocket of an ore field. The field's shape comes from how many of these are
 * placed and where (see {@code OreFieldPlacement}); each pocket itself is just a compact noisy sphere whose radius
 * tracks clusterSize directly, with NO minimum-size floor, so pockets can stay genuinely small.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Accessors(fluent = true, chain = true)
public class PocketVeinGenerator extends VeinGenerator {

    public static final MapCodec<PocketVeinGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            OreBlockDef.CODEC.listOf().fieldOf("ore_blocks").forGetter(it -> it.oreBlocks),
            Codec.FLOAT.fieldOf("radius_jitter").orElse(0.3f).forGetter(it -> it.radiusJitter),
            Codec.FLOAT.fieldOf("falloff_start").orElse(0.55f).forGetter(it -> it.falloffStart))
            .apply(instance, PocketVeinGenerator::new));

    public List<OreBlockDef> oreBlocks = new ArrayList<>();
    @Setter
    public float radiusJitter = 0.3f;
    @Setter
    public float falloffStart = 0.55f;

    public PocketVeinGenerator() {}

    public PocketVeinGenerator(List<OreBlockDef> oreBlocks, float radiusJitter, float falloffStart) {
        this.oreBlocks = oreBlocks;
        this.radiusJitter = radiusJitter;
        this.falloffStart = falloffStart;
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        List<VeinEntry> entries = new ArrayList<>();
        for (var def : oreBlocks) VeinGenerator.mapTarget(def.block(), def.weight()).forEach(entries::add);
        return entries;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> out = new Object2ObjectOpenHashMap<>();

        int size = entry.clusterSize().sample(random);
        float sizeRoll = random.nextFloat();
        float sizeMul;
        if (sizeRoll < 0.15f) {
            sizeMul = 1.35f + random.nextFloat() * 0.45f;
        } else if (sizeRoll < 0.42f) {
            sizeMul = 0.5f + random.nextFloat() * 0.3f;
        } else {
            sizeMul = 0.85f + random.nextFloat() * 0.4f;
        }
        float baseR = Math.max(2.0f, size * sizeMul);
        long noiseSeed = random.nextLong();
        int variant = random.nextInt(4);

        switch (variant) {
            case 1 -> {
                double a = random.nextDouble() * Math.PI * 2;
                int beads = 3 + random.nextInt(2);
                float spacing = baseR * 0.7f;
                float br = baseR * 0.42f;
                for (int i = 0; i < beads; i++) {
                    float t = i - (beads - 1) / 2.0f;
                    paintSub(out, random, entry, origin,
                            Math.cos(a) * spacing * t, (random.nextFloat() - 0.5f) * baseR * 0.25f,
                            Math.sin(a) * spacing * t, br, br, br, 0.3f, noiseSeed + i);
                }
            }
            case 2 -> {
                paintSub(out, random, entry, origin, 0, 0, 0,
                        baseR * 0.7f, baseR * 0.7f, baseR * 0.7f, 0.35f, noiseSeed);
                int arms = 3 + random.nextInt(2);
                for (int i = 0; i < arms; i++) {
                    double a = (double) i / arms * Math.PI * 2 + (random.nextDouble() - 0.5) * 0.6;
                    double d = baseR * (0.6 + random.nextDouble() * 0.3);
                    float ar = baseR * 0.4f;
                    paintSub(out, random, entry, origin,
                            Math.cos(a) * d, (random.nextFloat() - 0.5f) * baseR * 0.3f, Math.sin(a) * d,
                            ar, ar, ar, 0.35f, noiseSeed + i + 1);
                }
            }
            case 3 -> {
                paintSub(out, random, entry, origin, 0, 0, 0,
                        baseR * 0.8f, baseR * 0.8f, baseR * 0.8f, 0.7f, noiseSeed);
                int spikes = 2 + random.nextInt(2);
                for (int i = 0; i < spikes; i++) {
                    double a = random.nextDouble() * Math.PI * 2;
                    double d = baseR * 0.9;
                    float sr = baseR * 0.3f;
                    paintSub(out, random, entry, origin,
                            Math.cos(a) * d, (random.nextFloat() - 0.5f) * baseR * 0.5f, Math.sin(a) * d,
                            sr, sr, sr, 0.5f, noiseSeed + i + 1);
                }
            }
            default -> paintSub(out, random, entry, origin, 0, 0, 0, baseR, baseR, baseR, 0.45f, noiseSeed);
        }
        return out;
    }

    private void paintSub(Map<BlockPos, OreBlockPlacer> out, RandomSource random, GTOreDefinition entry,
                          BlockPos origin, double offX, double offY, double offZ,
                          float rx, float ry, float rz, float warp, long noiseSeed) {
        int cx = origin.getX() + (int) Math.round(offX);
        int cy = origin.getY() + (int) Math.round(offY);
        int cz = origin.getZ() + (int) Math.round(offZ);
        int rxi = Mth.ceil(rx * (1.0f + warp));
        int ryi = Mth.ceil(ry * (1.0f + warp));
        int rzi = Mth.ceil(rz * (1.0f + warp));

        for (int lx = -rxi; lx <= rxi; lx++) {
            for (int ly = -ryi; ly <= ryi; ly++) {
                for (int lz = -rzi; lz <= rzi; lz++) {
                    float nx = lx / rx;
                    float ny = ly / ry;
                    float nz = lz / rz;
                    float dist = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);

                    int px = cx + lx;
                    int py = cy + ly;
                    int pz = cz + lz;

                    float coarse = noise3D(Math.round(px / 2.5f), Math.round(py / 2.5f), Math.round(pz / 2.5f),
                            noiseSeed);
                    if (dist > 1.0f + coarse * warp) continue;

                    BlockPos blockPos = new BlockPos(px, py, pz);
                    if (out.containsKey(blockPos)) continue;

                    float fine = noise3D(px, py, pz, noiseSeed + 777);
                    float keep = (0.9f + fine * 0.1f) * entry.density();
                    if (random.nextFloat() > keep) continue;

                    long randomSeed = random.nextLong();
                    out.put(blockPos,
                            (access, section) -> placeBlock(section, randomSeed, entry,
                                    new BlockPos.MutableBlockPos(px, py, pz), access));
                }
            }
        }
    }

    private void placeBlock(LevelChunkSection section, long randomSeed, GTOreDefinition entry,
                            BlockPos.MutableBlockPos pos, BulkSectionAccess access) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        int sectionX = SectionPos.sectionRelative(pos.getX());
        int sectionY = SectionPos.sectionRelative(pos.getY());
        int sectionZ = SectionPos.sectionRelative(pos.getZ());
        BlockState current = section.getBlockState(sectionX, sectionY, sectionZ);
        var ore = GTUtil.getRandomItem(random, oreBlocks);
        if (ore != null) placeOre(ore.block(), current, access, section, random, pos, entry);
    }

    private float noise3D(float x, float y, float z, long seed) {
        long posHash = Float.floatToIntBits(x * 73856093) ^
                Float.floatToIntBits(y * 19349663) ^
                Float.floatToIntBits(z * 83492791);
        RandomSource noise = new XoroshiroRandomSource(seed ^ posHash);
        return noise.nextFloat() * 2.0f - 1.0f;
    }

    @Override
    public VeinGenerator build() {
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new PocketVeinGenerator(new ArrayList<>(oreBlocks), radiusJitter, falloffStart);
    }

    @Override
    public MapCodec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public PocketVeinGenerator oreBlock(Material mat, int weight) {
        oreBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }
}
