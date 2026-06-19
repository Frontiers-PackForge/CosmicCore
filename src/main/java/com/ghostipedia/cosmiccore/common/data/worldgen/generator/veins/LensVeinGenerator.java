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
public class LensVeinGenerator extends VeinGenerator {

    public static final MapCodec<LensVeinGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            OreBlockDef.CODEC.listOf().fieldOf("ore_blocks").forGetter(it -> it.oreBlocks),
            OreBlockDef.CODEC.listOf().fieldOf("rare_blocks").forGetter(it -> it.rareBlocks),
            Codec.FLOAT.fieldOf("vertical_scale").orElse(0.3f).forGetter(it -> it.verticalScale),
            Codec.FLOAT.fieldOf("rare_block_chance").orElse(0.05f).forGetter(it -> it.rareBlockChance))
            .apply(instance, LensVeinGenerator::new));

    public List<OreBlockDef> oreBlocks = new ArrayList<>();
    public List<OreBlockDef> rareBlocks = new ArrayList<>();
    @Setter
    public float verticalScale = 0.3f;
    @Setter
    public float rareBlockChance = 0.05f;

    public LensVeinGenerator() {}

    public LensVeinGenerator(List<OreBlockDef> oreBlocks, List<OreBlockDef> rareBlocks,
                             float verticalScale, float rareBlockChance) {
        this.oreBlocks = oreBlocks;
        this.rareBlocks = rareBlocks;
        this.verticalScale = verticalScale;
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
        int horizontalRadius = Mth.ceil(size / 2f);
        int verticalRadius = Math.max(2, Mth.ceil(horizontalRadius * verticalScale));

        float tiltX = (random.nextFloat() - 0.5f) * 0.2f;
        float tiltZ = (random.nextFloat() - 0.5f) * 0.2f;

        var posMin = origin.offset(-horizontalRadius, -verticalRadius, -horizontalRadius);
        var posMax = origin.offset(+horizontalRadius, +verticalRadius, +horizontalRadius);

        for (BlockPos pos : BlockPos.betweenClosed(posMin, posMax)) {
            int dx = pos.getX() - origin.getX();
            int dy = pos.getY() - origin.getY();
            int dz = pos.getZ() - origin.getZ();

            float adjustedDy = dy - dx * tiltX - dz * tiltZ;

            float normalizedX = dx / (float) horizontalRadius;
            float normalizedY = adjustedDy / (float) verticalRadius;
            float normalizedZ = dz / (float) horizontalRadius;
            float ellipsoidDist = normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ;

            if (ellipsoidDist > 1.0f) continue;

            float falloff = edgeFalloff((float) Math.sqrt(ellipsoidDist), 0.5f);
            if (random.nextFloat() > falloff * entry.density()) continue;

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
            long randomSeed = random.nextLong();

            generatedBlocks.put(mutablePos.immutable(),
                    (access, section) -> placeBlock(section, randomSeed, entry, mutablePos, access));
        }

        return generatedBlocks;
    }

    private void placeBlock(LevelChunkSection section, long randomSeed, GTOreDefinition entry,
                            BlockPos.MutableBlockPos pos, BulkSectionAccess access) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        int sectionX = SectionPos.sectionRelative(pos.getX());
        int sectionY = SectionPos.sectionRelative(pos.getY());
        int sectionZ = SectionPos.sectionRelative(pos.getZ());

        BlockState current = section.getBlockState(sectionX, sectionY, sectionZ);

        if (!rareBlocks.isEmpty() && random.nextFloat() < rareBlockChance) {
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
        return new LensVeinGenerator(new ArrayList<>(oreBlocks), new ArrayList<>(rareBlocks), verticalScale,
                rareBlockChance);
    }

    @Override
    public MapCodec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public LensVeinGenerator oreBlock(Material mat, int weight) {
        oreBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }

    public LensVeinGenerator rareBlock(Material mat, int weight) {
        rareBlocks.add(new OreBlockDef(Either.right(mat), weight));
        return this;
    }
}
