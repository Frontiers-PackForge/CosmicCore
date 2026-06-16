package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.EXPORT_SOUL;
import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_SOUL;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;

public class OneiricSieve {

    public static final MultiblockMachineDefinition ONEIRIC_SIEVE = REGISTRATE
            .multiblock("oneiric_sieve", WorkableElectricMultiblockMachine::new)
            .langValue("Oneiric Sieve")
            .recipeType(CosmicRecipeTypes.ONEIRIC_SIEVE)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeModifiers(GTRecipeModifiers.BATCH_MODE,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
            .appearanceBlock(SUPERHEAVY_STEEL_CASING)
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" AABBBBBAA ", "           ", "           ", "           ", "           ", "           ")
                    .aisle(" A BAAAB A ", "           ", "           ", "  BBCCCBB  ", "    CAC    ", " BBBBBBBBB ")
                    .aisle("CACBAAABCAC", "CCC     CCC", "  C     C  ", "  CBDDDBC  ", "CCCCC CCCCC", "CB       BC")
                    .aisle(" A BAAAB A ", "           ", "           ", "   BDDDB   ", "  AA   AA  ", " B       B ")
                    .aisle("CACBAAABCAC", "CCC     CCC", "  C     C  ", "  CBDDDBC  ", "CCCCC CCCCC", "CB       BC")
                    .aisle(" A BAAAB A ", "           ", "           ", "  BBCECBB  ", "    CAC    ", " BBBBBBBBB ")
                    .aisle(" AABBBBBAA ", "           ", "           ", "           ", "           ", "           ")
                    .where('E', Predicates.controller(Predicates.blocks(definition.get())))
                    .where(' ', Predicates.any())
                    .where('A', blocks(SOUL_MUTED_CASING.get()))
                    .where('B', blocks(SOMARUST_CASING.get()))
                    .where('C', blocks(SUPERHEAVY_STEEL_CASING.get())
                            .or(autoAbilities(CosmicRecipeTypes.ONEIRIC_SIEVE))
                            .or(autoAbilities(true, false, false))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(abilities(IMPORT_SOUL))
                            .or(abilities(EXPORT_SOUL))
                    )
                    .where('D', blocks(GTBlocks.CASING_GRATE.get()))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/superheavy_steel_casing"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static void init() {}
}
