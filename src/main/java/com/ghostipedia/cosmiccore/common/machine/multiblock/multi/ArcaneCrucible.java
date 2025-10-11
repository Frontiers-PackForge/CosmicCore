package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_EMBER;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.SOUL_STAINED_STEEL_ALU_CASING;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;

public class ArcaneCrucible {

    public final static MultiblockMachineDefinition ARCANE_CRUCIBLE = REGISTRATE
            .multiblock("arcane_crucible", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.ARCANE_CRUCIBLE)
            .appearanceBlock(SOUL_STAINED_STEEL_ALU_CASING)
            .partAppearance((controller, part, side) -> SOUL_STAINED_STEEL_ALU_CASING.getDefaultState())
            .recipeModifier(ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" AAA ", " ABA ", " BBB ", " BBB ", " ABA ", " AAA ")
                    .aisle("AAAAA", "A   A", "B   B", "B   B", "A   A", "AAAAA")
                    .aisle("AAAAA", "B   B", "B   B", "B   B", "B   B", "AAAAA")
                    .aisle("AAAAA", "A   A", "B   B", "B   B", "A   A", "AAAAA")
                    .aisle(" AQA ", " ABA ", " BBB ", " BBB ", " ABA ", " AAA ")
                    .where(' ', any())
                    .where("Q", controller(blocks(definition.getBlock())))
                    .where('A', blocks(SOUL_STAINED_STEEL_ALU_CASING.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(IMPORT_EMBER).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(1)))
                    .where('B', blocks(CosmicBlocks.STEEL_ROSE_LIGHT.block().get()))
                    //
                    .build())
            .workableCasingModel(CosmicCore.id("block/casings/solid/soul_stained_steel_aluminium_plated_casing"),
                    CosmicCore.id("block/multiblock/dawnforge"))
            .register();

    public static void init() {}
}
