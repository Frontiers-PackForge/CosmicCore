package com.ghostipedia.cosmiccore.common.data.worldgen.generator;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.*;

import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator.VeinGeneratorType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

public class CosmicVeinGenerators {

    private static final DeferredRegister<VeinGeneratorType<?>> VEIN_GENERATORS = DeferredRegister
            .create(GTRegistries.Keys.VEIN_GENERATOR, CosmicCore.MOD_ID);

    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<FractureVeinGenerator>> FRACTURE = register(
            "fracture", FractureVeinGenerator.CODEC, FractureVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<BranchingVeinGenerator>> BRANCHING = register(
            "branching", BranchingVeinGenerator.CODEC, BranchingVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<LensVeinGenerator>> LENS = register(
            "lens", LensVeinGenerator.CODEC, LensVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<ClusterVeinGenerator>> CLUSTER = register(
            "cluster", ClusterVeinGenerator.CODEC, ClusterVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<StringerVeinGenerator>> STRINGER = register(
            "stringer", StringerVeinGenerator.CODEC, StringerVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<ShellVeinGenerator>> SHELL = register(
            "shell", ShellVeinGenerator.CODEC, ShellVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<PocketVeinGenerator>> POCKET = register(
            "pocket", PocketVeinGenerator.CODEC, PocketVeinGenerator::new);

    public static void init(IEventBus modBus) {
        VEIN_GENERATORS.register(modBus);
    }

    private static <T extends VeinGenerator> DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<T>> register(
                                                                                                                 String name,
                                                                                                                 MapCodec<T> codec,
                                                                                                                 Supplier<T> factory) {
        return VEIN_GENERATORS.register(name, () -> new VeinGeneratorType<>(codec, factory));
    }
}
