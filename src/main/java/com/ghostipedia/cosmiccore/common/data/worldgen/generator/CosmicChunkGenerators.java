package com.ghostipedia.cosmiccore.common.data.worldgen.generator;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;

public final class CosmicChunkGenerators {

    private CosmicChunkGenerators() {}

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister
            .create(Registries.CHUNK_GENERATOR, CosmicCore.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<CosmicFloodedNoiseChunkGenerator>> FLOODED_NOISE = CHUNK_GENERATORS
            .register("flooded_noise", () -> CosmicFloodedNoiseChunkGenerator.CODEC);

    public static void register(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
    }
}
