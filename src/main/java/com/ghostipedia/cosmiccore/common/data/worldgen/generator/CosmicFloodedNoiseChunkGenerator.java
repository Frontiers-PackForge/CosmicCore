package com.ghostipedia.cosmiccore.common.data.worldgen.generator;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CosmicFloodedNoiseChunkGenerator extends NoiseBasedChunkGenerator {

    public static final MapCodec<CosmicFloodedNoiseChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.getBiomeSource()),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.settings))
                    .apply(instance, instance.stable(CosmicFloodedNoiseChunkGenerator::new)));

    private final Holder<NoiseGeneratorSettings> settings;

    public CosmicFloodedNoiseChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        this.settings = settings;
        this.globalFluidPicker = Suppliers.memoize(() -> floodedFluidPicker(settings.value()));
    }

    private static Aquifer.FluidPicker floodedFluidPicker(NoiseGeneratorSettings settings) {
        Aquifer.FluidStatus flooded = new Aquifer.FluidStatus(settings.seaLevel(), settings.defaultFluid());
        return (x, y, z) -> flooded;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
