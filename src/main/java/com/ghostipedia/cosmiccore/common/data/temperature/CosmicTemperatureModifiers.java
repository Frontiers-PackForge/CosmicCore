package com.ghostipedia.cosmiccore.common.data.temperature;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import sfiomn.legendarysurvivaloverhaul.api.temperature.ModifierBase;
import sfiomn.legendarysurvivaloverhaul.registry.TemperatureModifierRegistry;

public final class CosmicTemperatureModifiers {

    private CosmicTemperatureModifiers() {}

    public static final DeferredRegister<ModifierBase> MODIFIERS = DeferredRegister
            .create(TemperatureModifierRegistry.MODIFIERS_KEY, CosmicCore.MOD_ID);

    public static final DeferredHolder<ModifierBase, GregtechMachineHeatModifier> GREGTECH_MACHINE_HEAT = MODIFIERS
            .register("gtceu_machine_heat", GregtechMachineHeatModifier::new);

    public static final DeferredHolder<ModifierBase, FirmamentTemperatureModifier> FIRMAMENT = MODIFIERS
            .register("firmament", FirmamentTemperatureModifier::new);

    public static void register(IEventBus modBus) {
        MODIFIERS.register(modBus);
    }
}
