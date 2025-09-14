package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;

public class ArcaneDistillery {

    public static final MultiblockMachineDefinition ARCANE_DISTILLERY = REGISTRATE
            .multiblock("arcane_distillery", WorkableElectricMultiblockMachine::new)
            .langValue("§6Arcane Distillery")
            .recipeTypes(CosmicRecipeTypes.ARCANE_DISTILLERY, CosmicRecipeTypes.ARCANE_FOLDING)
            .rotationState(RotationState.NON_Y_AXIS)
            .partAppearance((controller, part, side) -> OSCILLATING_GILDED_PTHANTERUM_CASING.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.BATCH_MODE,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK))
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("  A  AAA  A  ", "     AAA     ", "     AAA     ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "     AAA     ", "     AAA     ", "  A  AAA  A  ")
                    .aisle(" AAAAAAAAAAA ", "  ABB C BBA  ", "    B   B    ", "    B   B    ", "             ", "             ", "             ", "             ", "             ", "    B   B    ", "    B   B    ", "   BB C BBA  ", " AAAAAAAAAAA ")
                    .aisle("AAAAAAAAAAAAA", " AB   C   BA ", "  B       B  ", "  D       D  ", "  D       D  ", "  D       D  ", "  D       D  ", "  D       D  ", "  D       D  ", "  D       D  ", "  B       B  ", "  B   C   BA ", "AAAAAAAAAAAAA")
                    .aisle(" AAAAAAAAAAA ", " B EE C EE B ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", " B EE C EE B ", " AAAAAAAAAAA ")
                    .aisle(" AAAAAAAAAAA ", " B EE C EE B ", " B  E   E  B ", " B  EE EE  B ", "             ", "             ", "             ", "             ", "             ", " B  EE EE  B ", " B  E   E  B ", " B EE C EE B ", " AAAAAAAAAAA ")
                    .aisle("AAAAAAAAAAAAA", "A     C     A", "A           A", "    EE EE    ", "     E E     ", "       E     ", "             ", "     E       ", "     E E     ", "    EE EE    ", "A           A", "A     C     A", "AAAAAAAAAAAAA")
                    .aisle("AAAAAAAAAAAAA", "ACCCCCCCCCCCA", "A     C     A", "      C      ", "      C      ", "             ", "             ", "             ", "      C      ", "      C      ", "A     C     A", "ACCCCCCCCCCCA", "AAAAAAAAAAAAA")
                    .aisle("AAAAAAAAAAAAA", "A     C     A", "A           A", "    EE EE    ", "     E E     ", "     E       ", "             ", "       E     ", "    E  E     ", "    EE EE    ", "A           A", "A     C     A", "AAAAAAAAAAAAA")
                    .aisle(" AAAAAAAAAAA ", " B EE C EE B ", " B  E   E  B ", " B  EE EE  B ", "             ", "             ", "             ", "             ", "             ", " B  EE EE  B ", " B  E   E  B ", " B EE C EE B ", " AAAAAAAAAAA ")
                    .aisle(" AAAAAAAAAAA ", " B EE C EE B ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", " B EE C EE B ", " AAAAAAAAAAA ")
                    .aisle("AAAAAAAAAAAAA", " AB   C   BA ", "  B       B  ", "  D       D  ", "  D       D  ", "  D       D  ", "  D       D  ", "  D       D  ", "  D       D  ", "  D       D  ", "  B       B  ", " AB   C   BA ", "AAAAAAAAAAAAA")
                    .aisle(" AAAAAAAAAAA ", "  ABB C BBA  ", "    B   B    ", "    B   B    ", "             ", "             ", "             ", "             ", "             ", "    B   B    ", "    B   B    ", "  ABB C BBA  ", " AAAAAAAAAAA ")
                    .aisle("  A  AAA  A  ", "     AQA     ", "     AAA     ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "     AAA     ", "     AAA     ", "  A  AAA  A  ")
                    .where('Q', Predicates.controller(Predicates.blocks(definition.get())))
                    .where(' ', Predicates.any())
                    .where('A', blocks(OSCILLATING_GILDED_PTHANTERUM_CASING.get())
                            .or(autoAbilities(CosmicRecipeTypes.ARCANE_DISTILLERY))
                            .or(autoAbilities(true,false,false))
                            .or(abilities(PartAbility.INPUT_ENERGY, PartAbility.INPUT_LASER).setMaxGlobalLimited(2, 2)
                                    .setPreviewCount(1)))
                    .where('B', blocks(GILDED_PTHANTERUM_CASING.get()))
                    .where('C', blocks(VIBRANT_RUBIDIUM_CASING.get()))
                    .where('D', frames(CosmicMaterials.Neutronite))
                    .where('E', blocks(TRITANIUM_LINED_HEAVY_NEUTRONIUM_CASING.get()))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/oscillating_gilded_pthanterum_casings"),
                    CosmicCore.id("block/multiblock/vomahine_chemplant"))
            .register();

    public static void init() {}
}
