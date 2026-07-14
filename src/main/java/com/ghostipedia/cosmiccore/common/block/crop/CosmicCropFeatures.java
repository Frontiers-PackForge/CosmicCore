package com.ghostipedia.cosmiccore.common.block.crop;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CosmicCropFeatures {

    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE,
            CosmicCore.MOD_ID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SPOREBEAN_PATCH = FEATURES
            .register("sporebeans_patch", SporebeanPatchFeature::new);

    private CosmicCropFeatures() {}

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
