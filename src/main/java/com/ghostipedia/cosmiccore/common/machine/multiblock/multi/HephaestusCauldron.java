package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryPatternAirspace;
import com.ghostipedia.cosmiccore.common.machine.foundry.HephaestusCauldronStructure;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.HephaestusCauldronMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.BOLTED_HEAVY_FRAME_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CASING_HEAT_VENT;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.INDUSTRIAL_PARTWORK;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.REFRACTORY_STRUCTURAL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.SUPERHEAVY_STEEL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.VIBRANT_PIPE_FRAMEWORK;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.air;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.controller;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.HEAT_VENT;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES;

public final class HephaestusCauldron {

    public static final MultiblockMachineDefinition MACHINE = REGISTRATE
            .multiblock("hephaestus_cauldron", HephaestusCauldronMachine::new)
            .langValue("Hephaestus' Cauldron")
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .recipeType(DUMMY_RECIPES)
            .appearanceBlock(SUPERHEAVY_STEEL_CASING)
            .tooltips(
                    Component.translatable("cosmiccore.machine.hephaestus_cauldron.tooltip.0"),
                    Component.translatable("cosmiccore.machine.hephaestus_cauldron.tooltip.1"),
                    Component.translatable("cosmiccore.machine.hephaestus_cauldron.tooltip.2"))
            .pattern(definition -> pattern(definition, HephaestusCauldronStructure.TIER_1))
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/superheavy_steel_casing"),
                    GTCEu.id("block/multiblock/gcym/blast_alloy_smelter"))
            .register();

    private HephaestusCauldron() {}

    public static void init() {
        TieredMultiblockPatterns.register(MACHINE,
                () -> pattern(MACHINE, HephaestusCauldronStructure.TIER_2),
                () -> pattern(MACHINE, HephaestusCauldronStructure.TIER_3));
    }

    public static @NotNull IBlockPattern pattern(
                                                 MultiblockMachineDefinition definition,
                                                 HephaestusCauldronStructure.@NotNull TierPattern tier) {
        MultiblockPatternBuilder builder = MultiblockPatternBuilder.start(
                RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT);
        for (String[] slice : FoundryPatternAirspace.classify(tier.slices())) builder.slice(slice);
        builder.where(' ', air());
        builder.where(FoundryPatternAirspace.EXTERIOR_MARKER, any());
        switch (tier.tier()) {
            case TIER_1 -> builder
                    .where('A', blocks(BOLTED_HEAVY_FRAME_CASING.get()))
                    .where('B', coreHatchPositions())
                    .where('C', blocks(SUPERHEAVY_STEEL_CASING.get()))
                    .where('D', blocks(REFRACTORY_STRUCTURAL_CASING.get()))
                    .where('E', blocks(CASING_HEAT_VENT.get()))
                    .where('F', air())
                    .where('G', controller(blocks(definition.getBlock())))
                    .where('H', air());
            case TIER_2 -> builder
                    .where('A', blocks(REFRACTORY_STRUCTURAL_CASING.get()))
                    .where('B', blocks(BOLTED_HEAVY_FRAME_CASING.get()))
                    .where('C', coreHatchPositions())
                    .where('D', blocks(SUPERHEAVY_STEEL_CASING.get()))
                    .where('E', blocks(INDUSTRIAL_PARTWORK.get()))
                    .where('F', blocks(CASING_HEAT_VENT.get()))
                    .where('G', blocks(HEAT_VENT.get()))
                    .where('H', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where('I', air())
                    .where('J', controller(blocks(definition.getBlock())))
                    .where('K', air());
            case TIER_3 -> builder
                    .where('A', blocks(REFRACTORY_STRUCTURAL_CASING.get()))
                    .where('B', blocks(BOLTED_HEAVY_FRAME_CASING.get()))
                    .where('C', coreHatchPositions())
                    .where('D', blocks(SUPERHEAVY_STEEL_CASING.get()))
                    .where('E', blocks(INDUSTRIAL_PARTWORK.get()))
                    .where('F', blocks(CASING_HEAT_VENT.get()))
                    .where('G', blocks(VIBRANT_PIPE_FRAMEWORK.get()))
                    .where('H', blocks(HEAT_VENT.get()))
                    .where('I', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where('J', air())
                    .where('K', controller(blocks(definition.getBlock())))
                    .where('L', air());
        }
        return builder.build();
    }

    private static MultiPredicate coreHatchPositions() {
        return blocks(SUPERHEAVY_STEEL_CASING.get()).or(abilities(
                PartAbility.INPUT_ENERGY,
                PartAbility.IMPORT_ITEMS,
                PartAbility.IMPORT_FLUIDS));
    }
}
