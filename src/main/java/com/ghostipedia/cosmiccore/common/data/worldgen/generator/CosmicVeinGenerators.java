package com.ghostipedia.cosmiccore.common.data.worldgen.generator;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.*;

import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

public class CosmicVeinGenerators {

    public static MapCodec<FractureVeinGenerator> FRACTURE;
    public static MapCodec<BranchingVeinGenerator> BRANCHING;
    public static MapCodec<LensVeinGenerator> LENS;
    public static MapCodec<ClusterVeinGenerator> CLUSTER;
    public static MapCodec<StringerVeinGenerator> STRINGER;
    public static MapCodec<ShellVeinGenerator> SHELL;

    public static void init() {
        FRACTURE = register("fracture", FractureVeinGenerator.CODEC, FractureVeinGenerator::new);
        BRANCHING = register("branching", BranchingVeinGenerator.CODEC, BranchingVeinGenerator::new);
        LENS = register("lens", LensVeinGenerator.CODEC, LensVeinGenerator::new);
        CLUSTER = register("cluster", ClusterVeinGenerator.CODEC, ClusterVeinGenerator::new);
        STRINGER = register("stringer", StringerVeinGenerator.CODEC, StringerVeinGenerator::new);
        SHELL = register("shell", ShellVeinGenerator.CODEC, ShellVeinGenerator::new);
    }

    private static <T extends VeinGenerator> MapCodec<T> register(
                                                                  String name,
                                                                  MapCodec<T> codec,
                                                                  Supplier<T> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, name);
        WorldGeneratorUtils.VEIN_GENERATORS.put(id, codec);
        WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.put(id, factory);
        return codec;
    }
}
