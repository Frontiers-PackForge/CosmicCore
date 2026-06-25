package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MothCargoDropOffMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_STEEL_SOLID;

/**
 * Moth Cargo Drop Off - Receiver multiblock for the Cargo Moths system.
 * Receives items and fluids from linked Moth Cargo Stations.
 * Small, compact design for easy placement at outposts.
 */
public class MothCargoDropOff {

    public static final MultiblockMachineDefinition MOTH_CARGO_DROP_OFF = REGISTRATE
            .multiblock("moth_cargo_drop_off", MothCargoDropOffMachine::new)
            .langValue("Moth Cargo Drop Off")
            .tooltips(
                    Component.literal("Receives shipments from Moth Cargo Stations"),
                    Component.literal("Link to stations with a datastick"),
                    Component.literal("Small footprint for easy outpost placement"))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(CASING_STEEL_SOLID)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    // Compact 3x3x3 structure
                    .slice("CCC", "CCC", "C C")
                    .slice("CCC", "C C", "   ")
                    .slice("CCC", "CQC", "C C")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('C', blocks(CASING_STEEL_SOLID.get())
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(4))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(4)))
                    .build())
            // spotless:on
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static void init() {}
}
