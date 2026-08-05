package com.ghostipedia.cosmiccore.common.data.worldgen.firmament;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CosmicFirmamentFeatures {

    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE,
            CosmicCore.MOD_ID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> MIDDLE_BAND_BRIDGE = FEATURES
            .register("firmament_middle_band_bridge", FirmamentMiddleBandFeature::new);
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> ECOLOGY = FEATURES
            .register("firmament_ecology", FirmamentEcologyFeature::new);

    private CosmicFirmamentFeatures() {}

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
