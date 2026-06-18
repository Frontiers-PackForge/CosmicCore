package com.ghostipedia.cosmiccore.common.data.worldgen.generator;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.*;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;

import java.util.function.Function;

public class CosmicVeinGenerators {

    public static Codec<FractureVeinGenerator> FRACTURE;
    public static Codec<BranchingVeinGenerator> BRANCHING;
    public static Codec<LensVeinGenerator> LENS;
    public static Codec<ClusterVeinGenerator> CLUSTER;
    public static Codec<StringerVeinGenerator> STRINGER;
    public static Codec<ShellVeinGenerator> SHELL;

    public static void init() {
        FRACTURE = register("fracture", FractureVeinGenerator.CODEC, FractureVeinGenerator::new);
        BRANCHING = register("branching", BranchingVeinGenerator.CODEC, BranchingVeinGenerator::new);
        LENS = register("lens", LensVeinGenerator.CODEC, LensVeinGenerator::new);
        CLUSTER = register("cluster", ClusterVeinGenerator.CODEC, ClusterVeinGenerator::new);
        STRINGER = register("stringer", StringerVeinGenerator.CODEC, StringerVeinGenerator::new);
        SHELL = register("shell", ShellVeinGenerator.CODEC, ShellVeinGenerator::new);
    }

    private static <T extends VeinGenerator> Codec<T> register(
                                                               String name,
                                                               Codec<T> codec,
                                                               Function<GTOreDefinition, T> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, name);
        WorldGeneratorUtils.VEIN_GENERATORS.put(id, codec);
        WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.put(id, factory);
        return codec;
    }
}
