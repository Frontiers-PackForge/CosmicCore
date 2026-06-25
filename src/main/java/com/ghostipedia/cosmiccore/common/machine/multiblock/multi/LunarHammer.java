package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.TitanFusionReactorMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class LunarHammer {

    public final static MultiblockMachineDefinition LUNAR_HAMMER = REGISTRATE
            .multiblock("lunar_sheer_hammer",
                    TitanFusionReactorMachine::new)
            .langValue("§9Lunar Hammer")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.LUNAR_HAMMER)
            .appearanceBlock(CASING_HIGH_TEMPERATURE_SMELTING)
            .partAppearance((controller, part, side) -> CASING_HIGH_TEMPERATURE_SMELTING.getDefaultState())
            .recipeModifiers(CosmicRecipeModifiers::titanReactorParallel,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("    A     A    ", "    A  B  A    ", "      BBB      ", "      BBB      ", "       B       ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "       B       ", "      BBB      ", "      BBB      ", "    A  B  A    ", "    A     A    ")
                    .slice("   AA     AA   ", "   AA  C  AA   ", "   AA CDC AA   ", "    A CCC A    ", "       C       ", "               ", "               ", "     EEEEE     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "     EEEEE     ", "               ", "               ", "       C       ", "    A CCC A    ", "   AA CDC AA   ", "   AAFFCFFAA   ", "   AA     AA   ")
                    .slice("  ACCCCCCCCCA  ", "  AF   C   FA  ", "  AFCCCDCCCFA  ", "  AF  CCC  FA  ", "  AFEEECEEEFA  ", "  AF       FA  ", "   F       F   ", "   FE G G EF   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   FE G G EF   ", "   F       F   ", "  AF       FA  ", "  AFEEECEEEFA  ", "  AF  CCC  FA  ", "  AFCCCDCCCFA  ", "  AF   C   FA  ", "  ACCCCCCCCCA  ")
                    .slice(" ACC       CCA ", " AF HHHCHHH FA ", " AFCA CDC ACFA ", "  F AECCCEA F  ", "  FEA  C  AEF  ", "  F A     A F  ", "  F A     A F  ", "  FEA G G AEF  ", "  F A     A F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F A     A F  ", "  FEA G G AEF  ", "  F A     A F  ", "  F A     A F  ", "  FEA  C  AEF  ", "  F AECCCEA F  ", " AFCA CDC ACFA ", " AF HHHCHHH FA ", " ACC       CCA ")
                    .slice("AAC         CAA", "AA HH  C  HH AA", " ACA  CDC  ACA ", " A A ECCCE A A ", "  EA   C   AE  ", "   A       A   ", "   AA     AA   ", "  EAA G G AAE  ", "   AA     AA   ", "    A     A    ", "    A     A    ", "    A     A    ", "               ", "               ", "               ", "               ", "               ", "    A     A    ", "    A     A    ", "    A     A    ", "   AA     AA   ", "  EAA G G AAE  ", "   AA     AA   ", "   A       A   ", "  EA   C   AE  ", " A A ECCCE A A ", " ACA  CDC  ACA ", "AA HH  C  HH AA", "AAC         CAA")
                    .slice("  C         C  ", "   H   C   H   ", "  C  CCDCC  C  ", "   EEECCCEEE   ", "  E   CCC   E  ", "      CIC      ", "      CIC      ", " E    CIC    E ", "      CIC      ", "      CIC      ", "      CIC      ", "       C       ", "               ", "               ", "               ", "               ", "               ", "       C       ", "      CIC      ", "      CIC      ", "      CIC      ", " E    CIC    E ", "      CIC      ", "      CIC      ", "  E   CCC   E  ", "   EEECCCEEE   ", "  C  CCDCC  C  ", " F H   C   H F ", "  C         C  ")
                    .slice("  C         C  ", "   H  CCC  H   ", "BCCCCCCCCCCCCCB", "BCCCCCCCCCCCCCB", "  E  CD DC  E  ", "     CD DC     ", "     CD DC     ", " EGGGCD DCGGGE ", "     CD DC     ", "     CD DC     ", "     CD DC     ", "      EEE      ", "       E       ", "               ", "               ", "               ", "       E       ", "      EEE      ", "     CD DC     ", "     CD DC     ", "     CD DC     ", " EGGGCD DCGGGE ", "     CD DC     ", "     CD DC     ", "  E  CD DC  E  ", "BCCCCCCCCCCCCCB", "BCCCCCCCCCCCCCB", " F H  CCC  H F ", "  C         C  ")
                    .slice("  C         C  ", "BCCCCCCCCCCCCCB", "BDDDDDCCCDDDDDB", "BCCCCCCCCCCCCCB", "BCCCCC   CCCCCB", "     I   I     ", "     I   I     ", " E   I   I   E ", "     I   I     ", "     I   I     ", "     I   I     ", "     CE EC     ", "      E E      ", "               ", "               ", "               ", "      E E      ", "     CE EC     ", "     I   I     ", "     I   I     ", "     I   I     ", " E   I   I   E ", "     I   I     ", "     I   I     ", "BCCCCC   CCCCCB", "BCCCCCCCCCCCCCB", "BDDDDDCCCDDDDDB", "BCCCCCCCCCCCCCB", "  C         C  ")
                    .slice("  C         C  ", "   H  CCC  H   ", "BCCCCCCCCCCCCCB", "BCCCCCCCCCCCCCB", "  E  CD DC  E  ", "     CD DC     ", "     CD DC     ", " EGGGCD DCGGGE ", "     CD DC     ", "     CD DC     ", "     CD DC     ", "      EEE      ", "       E       ", "               ", "               ", "               ", "       E       ", "      EEE      ", "     CD DC     ", "     CD DC     ", "     CD DC     ", " EGGGCD DCGGGE ", "     CD DC     ", "     CD DC     ", "  E  CD DC  E  ", "BCCCCCCCCCCCCCB", "BCCCCCCCCCCCCCB", " F H  CCC  H F ", "  C         C  ")
                    .slice("  C         C  ", "   H   C   H   ", "  C  CCDCC  C  ", "   EEECCCEEE   ", "  E   CCC   E  ", "      CIC      ", "      CIC      ", " E    CIC    E ", "      CIC      ", "      CIC      ", "      CIC      ", "       C       ", "               ", "               ", "               ", "               ", "               ", "       C       ", "      CIC      ", "      CIC      ", "      CIC      ", " E    CIC    E ", "      CIC      ", "      CIC      ", "  E   CCC   E  ", "   EEECCCEEE   ", "  C  CCDCC  C  ", " F H   C   H F ", "  C         C  ")
                    .slice("AAC         CAA", "AA HH  C  HH AA", " ACA  CDC  ACA ", " A A ECCCE A A ", "  EA   C   AE  ", "   A       A   ", "   AA     AA   ", "  EAA G G AAE  ", "   AA     AA   ", "    A     A    ", "    A     A    ", "    A     A    ", "               ", "               ", "               ", "               ", "               ", "    A     A    ", "    A     A    ", "    A     A    ", "   AA     AA   ", "  EAA G G AAE  ", "   AA     AA   ", "   A       A   ", "  EA   C   AE  ", " A A ECCCE A A ", " ACA  CDC  ACA ", "AA HH  C  HH AA", "AAC         CAA")
                    .slice(" ACC       CCA ", " AF HHHCHHH FA ", " AFCA CDC ACFA ", "  F AECCCEA F  ", "  FEA  C  AEF  ", "  F A     A F  ", "  F A     A F  ", "  FEA G G AEF  ", "  F A     A F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F         F  ", "  F A     A F  ", "  FEA G G AEF  ", "  F A     A F  ", "  F A     A F  ", "  FEA  C  AEF  ", "  F AECCCEA F  ", " AFCA CDC ACFA ", " AF HHHCHHH FA ", " ACC       CCA ")
                    .slice("  ACCCCCCCCCA  ", "  AF   C   FA  ", "  AFCCCDCCCFA  ", "  AF  CCC  FA  ", "  AFEEECEEEFA  ", "  AF       FA  ", "   F       F   ", "   FE G G EF   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   F       F   ", "   FE G G EF   ", "   F       F   ", "  AF       FA  ", "  AFEEECEEEFA  ", "  AF  CCC  FA  ", "  AFCCCDCCCFA  ", "  AF   C   FA  ", "  ACCCCCCCCCA  ")
                    .slice("   AA     AA   ", "   AACCCCCAA   ", "   AA CDC AA   ", "    A CCC A    ", "       C       ", "               ", "               ", "     EEEEE     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "     EEEEE     ", "               ", "               ", "       C       ", "    A CCC A    ", "   AA CDC AA   ", "   AAFFCFFAA   ", "   AA     AA   ")
                    .slice("    A     A    ", "    ABBBBBA    ", "      BQB      ", "      BBB      ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "       B       ", "      BBB      ", "      BBB      ", "    A  B  A    ", "    A     A    ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(OSCILLATING_GILDED_PTHANTERUM_CASING.get())) //Part IO go here
                    .where('B', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get())
                            .or(autoAbilities())
                            .or(autoAbilities(CosmicRecipeTypes.HEAVY_ASSEMBLER))
                            .or(abilities(PartAbility.IMPORT_FLUIDS,PartAbility.IMPORT_FLUIDS_4X,PartAbility.IMPORT_FLUIDS_9X))
                            .or(abilities(PartAbility.INPUT_ENERGY,PartAbility.INPUT_LASER).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('C', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where('D', blocks(GTBlocks.FUSION_COIL.get()))
                    .where('F',  frames(GTMaterials.Neutronium))
                    .where('E', blocks(HIGH_TOLERANCE_RHENIUM_CASING.get()))
                    .where('G', frames(CosmicMaterials.Trinavine))
                    .where('H', blocks(ULTRA_POWERED_CASING.get()))
                    .where('I', blocks(GTBlocks.FUSION_CASING_MK3.get()))
                    .build())
            // spotless:on
            .model(
                    createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                            GTCEu.id("block/multiblock/fusion_reactor"))
                            .andThen(model -> model
                                    .addDynamicRenderer(CosmicDynamicRenderHelpers::getRenderTesterHelper)))
            .register();

    public static void init() {}
}
