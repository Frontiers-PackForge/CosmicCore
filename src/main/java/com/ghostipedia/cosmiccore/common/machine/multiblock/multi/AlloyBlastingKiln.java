package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.foundry.AlloyBlastingKilnStructure;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryPatternAirspace;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.AlloyBlastingKilnMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CASING_HEAT_VENT;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.REFRACTORY_STRUCTURAL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.SUPERHEAVY_STEEL_CASING;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes.ALLOY_BLASTING_KILN;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.air;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.controller;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING;

public final class AlloyBlastingKiln {

    public static final MultiblockMachineDefinition MACHINE = REGISTRATE
            .multiblock("alloy_blasting_kiln", AlloyBlastingKilnMachine::new)
            .langValue("Alloy Blasting Kiln")
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .recipeType(ALLOY_BLASTING_KILN)
            .recipeModifiers(AlloyBlastingKilnMachine::recipeModifier)
            .appearanceBlock(SUPERHEAVY_STEEL_CASING)
            .tooltips(
                    Component.translatable("cosmiccore.machine.alloy_blasting_kiln.tooltip.0"),
                    Component.translatable("cosmiccore.machine.alloy_blasting_kiln.tooltip.1"),
                    Component.translatable("cosmiccore.machine.alloy_blasting_kiln.tooltip.2"))
            .pattern(AlloyBlastingKiln::pattern)
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/superheavy_steel_casing"),
                    GTCEu.id("block/multiblock/gcym/blast_alloy_smelter"))
            .register();

    private AlloyBlastingKiln() {}

    public static void init() {}

    private static com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern pattern(
                                                                                      MultiblockMachineDefinition definition) {
        MultiblockPatternBuilder builder = MultiblockPatternBuilder.start(
                RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT);
        for (String[] slice : FoundryPatternAirspace.classify(AlloyBlastingKilnStructure.SLICES)) builder.slice(slice);
        return builder
                .where(' ', air())
                .where(FoundryPatternAirspace.EXTERIOR_MARKER, any())
                .where('A', blocks(REFRACTORY_STRUCTURAL_CASING.get()))
                .where('B', kilnHatchPositions())
                .where('C', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()))
                .where('D', blocks(SUPERHEAVY_STEEL_CASING.get()))
                .where('E', blocks(CASING_HEAT_VENT.get()))
                .where('F', air())
                .where('G', controller(blocks(definition.getBlock())))
                .where('H', air())
                .build();
    }

    private static MultiPredicate kilnHatchPositions() {
        return blocks(SUPERHEAVY_STEEL_CASING.get())
                .or(abilities(
                        PartAbility.IMPORT_ITEMS,
                        PartAbility.EXPORT_ITEMS,
                        PartAbility.IMPORT_FLUIDS,
                        PartAbility.EXPORT_FLUIDS));
    }
}
