package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;

import wayoftime.bloodmagic.BloodMagic;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_EMBER;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.HIGHLY_CONDUCTIVE_FISSION_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.SANGUINE_GLASS;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;
import static com.klikli_dev.occultism.registry.OccultismBlocks.IESNIUM_BLOCK;
import static wayoftime.bloodmagic.common.block.BloodMagicBlocks.BLANK_RUNE;
import static wayoftime.bloodmagic.common.fluid.BloodMagicFluids.LIFE_ESSENCE_FLUID;
import static wayoftime.bloodmagic.common.fluid.BloodMagicFluids.LIFE_ESSENCE_FLUID_FLOWING;

public class BloodHomogenizer {

    public static final MultiblockMachineDefinition BLOOD_HOMOGENIZER = REGISTRATE
            .multiblock("blood_homogenizer", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.BLOOD_HOMOGENIZER)
            .appearanceBlock(HIGHLY_CONDUCTIVE_FISSION_CASING)
            .partAppearance((controller, part, side) -> HIGHLY_CONDUCTIVE_FISSION_CASING.getDefaultState())
            .recipeModifiers(ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("       ", " AAAAA ", "  AAA  ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("  A A  ", "ABBBBBA", " BGGGB ", " CGGGC ", "  GGG  ", "       ", "       ", "       ")
                    .aisle(" AA AA ", "ABGGGBA", "AGLLLGA", " GLLLG ", " GLLLG ", "  GGG  ", "   G   ", "  GGG  ")
                    .aisle("       ", "ABGGGBA", "AGLLLGA", " GLLLG ", " GLLLG ", "  G G  ", "  G G  ", "  G G  ")
                    .aisle(" AA AA ", "ABGGGBA", "AGLLLGA", " GLLLG ", " GLLLG ", "  GGG  ", "   G   ", "  GGG  ")
                    .aisle("  A A  ", "ABBBBBA", " BGGGB ", " CGGGC ", "  GGG  ", "       ", "       ", "       ")
                    .aisle("       ", " AAAAA ", "  AQA  ", "       ", "       ", "       ", "       ", "       ")
                    .where('Q', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('A', blocks(BLANK_RUNE.get()))
                    .where('B', blocks(HIGHLY_CONDUCTIVE_FISSION_CASING.get())
                            .setMinGlobalLimited(10)
                            .or(autoAbilities(CosmicRecipeTypes.BLOOD_HOMOGENIZER))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(IMPORT_EMBER).setExactLimit(1)))
                    .where('C', blocks(IESNIUM_BLOCK.get()))
                    .where('G', blocks(SANGUINE_GLASS.get()))
                    .where('L', fluids(LIFE_ESSENCE_FLUID.get())
                            .or(fluids(LIFE_ESSENCE_FLUID_FLOWING.get())))
                    .where(' ', any())
                    .build())
            .workableCasingModel(BloodMagic.rl("block/blankrune"),
                    GTCEu.id("block/multiblock/power_substation"))
            .register();

    public static void init() {}
}
