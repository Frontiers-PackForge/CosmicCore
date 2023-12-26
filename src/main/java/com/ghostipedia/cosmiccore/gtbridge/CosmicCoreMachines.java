package com.ghostipedia.cosmiccore.gtbridge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.ghostipedia.cosmiccore.gtbridge.machine.kinetic.Alternator;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.autoAbilities;

@SuppressWarnings("unused")
public class CosmicCoreMachines {
    public static final MultiblockMachineDefinition ALTERNATOR_MACHINE = GTRegistries.REGISTRATE.multiblock("alternator", Alternator::new)
            .rotationState(RotationState.NON_Y_AXIS)
            //.tooltips(Component.translatable("gtceu.multiblock.alternator.tooltip1"))
            .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
            //.recipeModifier(Alternator::AlternatorRecipe)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("F###F", "ZWBWZ", "ZWBWZ", "ZWBWZ")
                    .aisle("#####", "ZWBWZ", "XOOOX", "ZWBWZ")
                    .aisle("F###F", "ZWBWZ", "ZWCWZ", "ZWBWZ")
                    .where('C', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('#', Predicates.any())
                    .where('W', Predicates.blocks(GTBlocks.COIL_CUPRONICKEL.get()))
                    .where('B', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('Z', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(abilities(PartAbility.OUTPUT_ENERGY).setMinGlobalLimited(1)))
                    .where('X', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(abilities(PartAbility.INPUT_KINETIC).setMinGlobalLimited(1)))
                    .where('O', Predicates.blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .where('F', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"), GTCEu.id("block/multiblock/generator/large_bronze_boiler"), false)
            .register();

    public static void init() {
    }
}