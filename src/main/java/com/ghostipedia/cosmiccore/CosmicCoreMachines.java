package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.autoAbilities;

@SuppressWarnings("unused")
public class CosmicCoreMachines {

    public static final MachineDefinition[] PRESSURE_HATCH = AirHatchPartMachine("pressure_hatch", (holder, tier) ->
            new AirHatchPartMachine(holder, tier, IO.BOTH, 20,1),
            (tier, builder) -> builder.abilities(PRESSURE_CONTAINER)
                    .rotationState(Rotationstate.ALL)
                    .overlayTieredHullRenderer("pressure_hatch")
                    .register()
    );

    public static final MultiblockMachineDefinition AIR_MACHINE = GTRegistries.REGISTRATE.multiblock("airmachine", BasicAirMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            //.tooltips(Component.translatable("gtceu.multiblock.alternator.tooltip1"))
            .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
            //.recipeModifier(BasicSteamTurbineMachine::recipeModifier)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("F###F", "ZWBWZ", "ZWBWZ", "ZWBWZ")
                    .aisle("#####", "ZWBWZ", "XOOOX", "ZWBWZ")
                    .aisle("F###F", "ZWBWZ", "ZWCWZ", "ZWBWZ")
                    .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('#', Predicates.any())
                    .where('W', Predicates.blocks(CosmicBlocks.ALTERNATOR_FLUX_COILING.get()))
                    .where('B', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('Z', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(abilities(PartAbility.).setExactLimit(1)))
                    .where('X', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1)))
                    .where('O', Predicates.blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .where('F', Predicates.frames(GTMaterials.Steel))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"), GTCEu.id("block/multiblock/generator/large_bronze_boiler"), false)
            .register();

    public static void init() {
    }
}