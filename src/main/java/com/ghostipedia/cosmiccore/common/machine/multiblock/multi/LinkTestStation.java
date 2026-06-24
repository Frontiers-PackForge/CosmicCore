package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.LinkTestStationMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

/**
 * Simple test multiblock for verifying cross-dimensional linking.
 * Minimal 3x3x3 structure using steel casings.
 */
public class LinkTestStation {

    public final static MultiblockMachineDefinition LINK_TEST_STATION = REGISTRATE
            .multiblock("link_test_station", LinkTestStationMachine::new)
            .langValue("Link Test Station")
            .tooltips(
                    Component.literal("Test multiblock for cross-dimensional linking"),
                    Component.literal("Use datastick: Shift+click to copy, click to link"),
                    Component.literal("Some recipes require linked partners"))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.LINK_TEST_RECIPES)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> MultiblockPatternBuilder.start()
                    .slice("CCC", "CCC", "CCC")
                    .slice("CCC", "C C", "CCC")
                    .slice("CCC", "CQC", "CCC")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('C', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1)))
                    .build())
            .model(
                    createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                            GTCEu.id("block/multiblock/implosion_compressor")))
            .register();

    public static void init() {}
}
