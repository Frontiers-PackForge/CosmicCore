package com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public final class VeinGeneratorUtil {

    public static final Codec<Either<List<TargetBlockState>, Material>> BLOCK_ENTRY_CODEC = Codec
            .either(TargetBlockState.CODEC.listOf(), GTRegistries.MATERIALS.byNameCodec());

    private VeinGeneratorUtil() {}

    public static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.max(0, Math.min(1, (x - edge0) / (edge1 - edge0)));
        return t * t * (3 - 2 * t);
    }

    public static float edgeFalloff(float normalizedDistance, float falloffStart) {
        if (normalizedDistance <= falloffStart) return 1.0f;
        return 1.0f - smoothstep(falloffStart, 1.0f, normalizedDistance);
    }

    public static void placeOre(
                                Either<List<TargetBlockState>, Material> block,
                                BlockState current,
                                BulkSectionAccess level,
                                LevelChunkSection section,
                                RandomSource random,
                                BlockPos pos,
                                GTOreDefinition entry) {
        int x = SectionPos.sectionRelative(pos.getX());
        int y = SectionPos.sectionRelative(pos.getY());
        int z = SectionPos.sectionRelative(pos.getZ());

        block.ifLeft(blockStates -> {
            for (TargetBlockState targetState : blockStates) {
                if (!OreVeinUtil.canPlaceOre(current, level::getBlockState, random, entry, targetState, pos))
                    continue;
                if (targetState.state.isAir())
                    continue;
                section.setBlockState(x, y, z, targetState.state, false);
                break;
            }
        }).ifRight(material -> {
            if (!OreVeinUtil.canPlaceOre(current, level::getBlockState, random, entry, pos))
                return;
            BlockState currentState = level.getBlockState(pos);
            var prefix = ChemicalHelper.getOrePrefix(currentState);
            if (prefix.isEmpty()) return;
            Block toPlace = ChemicalHelper.getBlock(prefix.get(), material);
            if (toPlace == null || toPlace.defaultBlockState().isAir())
                return;
            section.setBlockState(x, y, z, toPlace.defaultBlockState(), false);
        });
    }

    public record OreBlockDef(Either<List<TargetBlockState>, Material> block, int weight)
            implements com.gregtechceu.gtceu.utils.WeightedEntry {

        public static final Codec<OreBlockDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BLOCK_ENTRY_CODEC.fieldOf("block").forGetter(OreBlockDef::block),
                Codec.INT.fieldOf("weight").forGetter(OreBlockDef::weight)).apply(instance, OreBlockDef::new));
    }
}
