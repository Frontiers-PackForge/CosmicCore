package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.*;

public class StellarSmeltingModule {

    public static final MultiblockMachineDefinition STELLAR_SMELTING_MODULE = REGISTRATE
            .multiblock("stellar_smelting_module", StellarBaseModule::new)
            .langValue("Godsbane Hyper-tensor Platform")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.STELLAR_SMELTING)
            .appearanceBlock(CASING_HIGH_TEMPERATURE_SMELTING)
            .recipeModifiers(CosmicRecipeModifiers.STELLAR_MODULE_OVERCLOCK)
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, BACK, UP)
                    // The module has a compact structure that extends from the Iris ring
                    // 'A' = CASING_HIGH_TEMPERATURE_SMELTING (shared with Iris ring structure)
                    // 'B' = MULTIPURPOSE_INTERSTELLAR_GRADE_CASING
                    // 'C' = Controller
                    .aisle("  AAAAA  ", " ACCCCCA ", "AFCFFFCFA", "AFCFFFCFA", "AFCFFFCFA", " ACCCCCA ", "  AAAAA  ")
                    .aisle("  B   B  ", "DDDDDDDDD", "DDDDDDDDD", "GGGGGGGGG", "DDDDDDDDD", "DDDDDDDDD", "  B   B  ")
                    .aisle("  B   B  ", "BCCCCCCCB", "GGGGGGGGG", "GGGGGGGGG", "GGGGGGGGG", "BCC   CCB", "  B   B  ")
                    .aisle("  B   B  ", "BCC   CCB", "GGGBBBGGG", "GGGBBBGGG", "GGGBBBGGG", "BCC   CCB", "  B   B  ")
                    .aisle(" BBBBBBB ", "BCC B CCB", "GGGBBBGGG", "GGGBBBGGG", "GGGBBBGGG", "BCC B CCB", " BBBBBBB ")
                    .aisle("  B   B  ", "BCC   CCB", "GGGBBBGGG", "GGGBBBGGG", "GGGBBBGGG", "BCC   CCB", "  B   B  ")
                    .aisle("  B   B  ", "BCCCCCCCB", "GGGGGGGGG", "GGGGGGGGG", "GGGGGGGGG", "BCC   CCB", "  B   B  ")
                    .aisle("  B   B  ", "DDDDDDDDD", "DDDDDDDDD", "GGGGGGGGG", "DDDDDDDDD", "DDDDDDDDD", "  B   B  ")
                    .aisle("  AAAAA  ", " AEEEEEA ", "AFEFFFEFA", "AFEFQFEFA", "AFEFFFEFA", " AEEEEEA ", "  AAAAA  ")

                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('B', blocks(ROYAL_ICHORIUM_CASING.get()))  // Shared ring blocks
                    .where('C', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()))// Shared ring blocks
                    .where('D', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()))  // Shared ring blocks
                    .where('E', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
                    .where('F', blocks(ULTRA_POWERED_CASING.get()))
                    .where('G', blocks(MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get()))
                    .where('A', blocks(SOMARUST_CASING.get()))
                    .build())
            // spotless:on
            .workableCasingModel(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/overlay/machine/alloy_blast_smelter"))
            .register();

    public static void init() {}
}
