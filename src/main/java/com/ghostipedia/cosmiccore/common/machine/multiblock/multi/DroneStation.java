package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.STEEL_PLATED_BRONZE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;

public class DroneStation {

    public final static MultiblockMachineDefinition DRONE_STATION = REGISTRATE
            .multiblock("drone_station",
                    DroneStationMachine::new)
            .langValue("Drone Station")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .partAppearance((controller, part, side) -> STEEL_PLATED_BRONZE.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAA", "AAA", "AAA")
                    .aisle("AAA", "A A", "AAA")
                    .aisle("AAA", "AQA", "AAA")
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(STEEL_PLATED_BRONZE.get())
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1)
                                    .setPreviewCount(1)))
                    .build())
            .workableCasingModel(CosmicCore.id("block/casings/solid/tritanium_lined_heavy_bolted_neutronium_casing"),
                    CosmicCore.id("block/casings/solid/tritanium_lined_heavy_bolted_neutronium_casing"))
            .register();

    public static void init() {}
}
