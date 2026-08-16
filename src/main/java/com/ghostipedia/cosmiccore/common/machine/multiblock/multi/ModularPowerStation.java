package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ModularPowerStationMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.pattern.ModularPowerStationPatterns;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public final class ModularPowerStation {

    public static final MultiblockMachineDefinition MODULAR_POWER_STATION = REGISTRATE
            .multiblock("modular_power_station", ModularPowerStationMachine::new)
            .langValue("Modular Power Station")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(
                    CosmicRecipeTypes.TURBINE_POWER_STATION,
                    CosmicRecipeTypes.COMBUSTION_POWER_STATION)
            .regressWhenWaiting(false)
            .recipeModifier(ModularPowerStationMachine::recipeModifier, true)
            .generator(true)
            .appearanceBlock(CosmicBlocks.LIGHTWEIGHT_DARK_STEEL_CASING)
            .partAppearance((controller, part, side) -> CosmicBlocks.LIGHTWEIGHT_DARK_STEEL_CASING.getDefaultState())
            .pattern(ModularPowerStationPatterns::create)
            .tooltips(
                    Component.translatable("cosmiccore.multiblock.modular_power_station.tooltip.0"),
                    Component.translatable("cosmiccore.multiblock.modular_power_station.tooltip.1"),
                    Component.translatable("cosmiccore.multiblock.modular_power_station.tooltip.2"),
                    Component.translatable("cosmiccore.multiblock.modular_power_station.tooltip.3"))
            .workableCasingModel(CosmicCore.id("block/casings/solid/lightweight_dark_steel_casing"),
                    GTCEu.id("block/multiblock/generator/large_steam_turbine"))
            .register();

    private ModularPowerStation() {}

    public static void init() {}
}
