package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ConversionComplexMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;

import java.util.List;

import static com.ghostipedia.cosmiccore.api.machine.multiblock.PatternMappedPartAppearance.of;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CASING_HEAT_VENT;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_MECHANICAL_PARTWORK;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_STAINLESS_STEEL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.REFRACTORY_CONTAINMENT_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.REFRACTORY_STRUCTURAL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.VIBRANT_PIPE_FRAMEWORK;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createConfiguredWorkableCasingMachineModel;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.frames;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.BACK;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.LEFT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.UP;

public final class ConversionComplex {

    public static final MultiblockMachineDefinition CONVERSION_COMPLEX = REGISTRATE
            .multiblock("conversion_complex", ConversionComplexMachine::new)
            .langValue("Conversion Complex")
            .rotationState(RotationState.ALL)
            .appearanceBlock(LIGHTWEIGHT_STAINLESS_STEEL_CASING)
            .partAppearance(of(LIGHTWEIGHT_STAINLESS_STEEL_CASING::getDefaultState))
            .recipeTypes(
                    CosmicRecipeTypes.FLUID_CATALYTIC_CRACKING,
                    CosmicRecipeTypes.HYDROTREATING,
                    CosmicRecipeTypes.HYDROCRACKING,
                    CosmicRecipeTypes.CATALYTIC_REFORMING,
                    CosmicRecipeTypes.DELAYED_COKING)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            .tooltips(
                    Component.translatable("cosmiccore.multiblock.conversion_complex.tooltip.0"),
                    Component.translatable("cosmiccore.multiblock.conversion_complex.tooltip.1"))
            .tooltipBuilder((stack, tooltip) -> {
                if (!GTUtil.isShiftDown()) {
                    tooltip.add(Component.translatable("gtceu.tooltip.hold_shift"));
                    return;
                }
                tooltip.add(Component.translatable("cosmiccore.multiblock.conversion_complex.tooltip.shift_header"));
                tooltip.add(Component.translatable("cosmiccore.multiblock.conversion_complex.tooltip.core"));
                tooltip.add(Component.translatable("cosmiccore.multiblock.conversion_complex.tooltip.fcc"));
                tooltip.add(Component.translatable("cosmiccore.multiblock.conversion_complex.tooltip.hydrotreating"));
                tooltip.add(Component.translatable("cosmiccore.multiblock.conversion_complex.tooltip.hydrocracking"));
                tooltip.add(Component.translatable("cosmiccore.multiblock.conversion_complex.tooltip.reforming"));
                tooltip.add(Component.translatable("cosmiccore.multiblock.conversion_complex.tooltip.coking"));
            })
            .modelProperty(ConversionComplexMachine.CONFIGURATION_PROPERTY, 0)
            .pattern(ConversionComplex::corePattern)
            .model(createConfiguredWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/lightweight_stainless_steel_casing"),
                    ConversionComplexMachine.CONFIGURATION_PROPERTY,
                    GTCEu.id("block/multiblock/multiblock_workable"),
                    GTCEu.id("block/machines/centrifuge"),
                    GTCEu.id("block/machines/chemical_reactor"),
                    GTCEu.id("block/machines/compressor"),
                    GTCEu.id("block/machines/distillery"),
                    CosmicCore.id("block/multiblock/conversion_complex/delayed_coking")))
            .register();

    private ConversionComplex() {}

    public static void init() {
        TieredMultiblockPatterns.registerConfigurations(CONVERSION_COMPLEX, List.of(
                new TieredMultiblockPatterns.PatternLabel("cosmiccore.multiblock.configuration.core",
                        "cosmiccore.multiblock.configuration.core.short"),
                new TieredMultiblockPatterns.PatternLabel("cosmiccore.multiblock.configuration.fcc",
                        "cosmiccore.multiblock.configuration.fcc.short"),
                new TieredMultiblockPatterns.PatternLabel("cosmiccore.multiblock.configuration.hydrotreater",
                        "cosmiccore.multiblock.configuration.hydrotreater.short"),
                new TieredMultiblockPatterns.PatternLabel("cosmiccore.multiblock.configuration.hydrocracker",
                        "cosmiccore.multiblock.configuration.hydrocracker.short"),
                new TieredMultiblockPatterns.PatternLabel("cosmiccore.multiblock.configuration.reformer",
                        "cosmiccore.multiblock.configuration.reformer.short"),
                new TieredMultiblockPatterns.PatternLabel("cosmiccore.multiblock.configuration.coker",
                        "cosmiccore.multiblock.configuration.coker.short")),
                () -> fccPattern(CONVERSION_COMPLEX),
                () -> hydrotreaterPattern(CONVERSION_COMPLEX),
                () -> hydrocrackerPattern(CONVERSION_COMPLEX),
                () -> reformerPattern(CONVERSION_COMPLEX),
                () -> cokerPattern(CONVERSION_COMPLEX));
    }

    private static IBlockPattern corePattern(MultiblockMachineDefinition definition) {
        return finishFull(MultiblockPatternBuilder.start(BACK, UP, LEFT)
                .slice("HHHHHHHHH", "H  HSH  H", "H  HHH  H")
                .slice("HHHHHHHHH", "H  H H  H", "H  HHH  H")
                .slice("HHHHHHHHH", "H  H H  H", "H  HHH  H")
                .slice("HHHHHHHHH", "H  H H  H", "H  HHH  H")
                .slice("HHHHHHHHH", "HHHHHHHHH", "HHHHHHHHH"), definition);
    }

    private static IBlockPattern fccPattern(MultiblockMachineDefinition definition) {
        return finishFull(MultiblockPatternBuilder.start(BACK, UP, LEFT)
                .slice("      AAA", "      AAA", "      AAA", "         ", "         ", "         ", "         ")
                .slice("      AAA", "      ACA", "      ACA", "       C ", "       C ", "         ", "         ")
                .slice("HHHHHHHHH", "H  HSHAAH", "HAAHHHAAH", "       V ", "         ", "         ", "         ")
                .slice("HHHHHHHHH", "HFFH HCCH", "HRRHHHCCH", " RR   CC ", "      CC ", "      CC ", "      CC ")
                .slice("HHHHHHHHH", "HRRH HCCH", "HRRHHHCCH", " RRVV CC ", "    V CC ", "    VVCC ", "      CC ")
                .slice("HHHHHHHHH", "HRRH HCCH", "HRRHHHCCH", " FF   CC ", "      CC ", "      CC ", "      CC ")
                .slice("HHHHHHHHH", "HHHHHHHHH", "HHHHHHHHH", "         ", "         ", "         ", "         "),
                definition);
    }

    private static IBlockPattern hydrotreaterPattern(MultiblockMachineDefinition definition) {
        return finishFull(MultiblockPatternBuilder.start(BACK, UP, LEFT)
                .slice("  HHHHHHHHH  ", "  H  HSH  H  ", "  HAAHHHAAH  ", "             ", "             ",
                        "             ")
                .slice("AAHHHHHHHHHAA", "A HFFH HFFH A", "A HCCHHHCCH A", "AAACCVVVCCAAA", "   CC   CC   ",
                        "   CC   CC   ")
                .slice("AAHHHHHHHHHAA", " VHCCH HCCHV ", " VHCCHHHCCHV ", " VVCC   CCVV ", "   CC   CC   ",
                        "   CC   CC   ")
                .slice("AAHHHHHHHHHAA", "A HCCH HCCH A", "A HCCHHHCCH A", "AAACCVVVCCAAA", "   CC   CC   ",
                        "   CC   CC   ")
                .slice("  HHHHHHHHH  ", "  HHHHHHHHH  ", "  HHHHHHHHH  ", "             ", "             ",
                        "             "),
                definition);
    }

    private static IBlockPattern hydrocrackerPattern(MultiblockMachineDefinition definition) {
        return finishFull(MultiblockPatternBuilder.start(BACK, UP, LEFT)
                .slice("  HHHHHHHHH  ", "  H  HSH  H  ", "  HAAHHHAAH  ", "             ", "             ",
                        "             ")
                .slice("AAHHHHHHHHHAA", "A HFFH HFFH A", "A HCCHHHCCH A", "AAACCVVVCCAAA", "   CC   CC   ",
                        "   CC   CC   ")
                .slice("AAHHHHHHHHHAA", " VHCCH HCCHV ", " VHCCHHHCCHV ", "AVVCC   CCVV ", "   CC   CC   ",
                        "   CC   CC   ")
                .slice("AAHHHHHHHHHAA", "A HCCH HCCH A", "A HCCHHHCCH A", "AAACCVVVCCAAA", "   CC   CC   ",
                        "   CC   CC   ")
                .slice("  HHHHHHHHH  ", "  HHHHHHHHH  ", "  HHHHHHHHH  ", "             ", "    V   V    ",
                        "             ")
                .slice("   RRRRRRR   ", "   RMMMMMR   ", "   RVRRRVR   ", "    V   V    ", "    V   V    ",
                        "             ")
                .slice("   RRRRRRR   ", "   RFRFRFR   ", "   RRRRRRR   ", "             ", "             ",
                        "             "),
                definition);
    }

    private static IBlockPattern reformerPattern(MultiblockMachineDefinition definition) {
        return finishFull(MultiblockPatternBuilder.start(BACK, UP, LEFT)
                .slice("AAAHHHHHHHHH", "AAAHAAHSH  H", "AAAHAAHHHAAH", "            ", "            ", "            ")
                .slice("AAAHHHHHHHHH", "ACCHCCH HFFH", "ACCHCCHHHAAH", " CC CC      ", " CC CC      ", " CC CC      ")
                .slice("AAAHHHHHHHHH", "ACCHCCH HRRH", "ACCHCCHHHRRH", " CC CCVVVRR ", " CCVCC   RR ", " CC CC      ")
                .slice("AAAHHHHHHHHH", "ACCHCCH HRRH", "ACCHCCHHHRRH", " CC CC   RR ", " CC CC   RR ", " CC CC      ")
                .slice("AAAHHHHHHHHH", "AAAHHHHHHHHH", "AAAHHHHHHHHH", "            ", "            ", "            "),
                definition);
    }

    private static IBlockPattern cokerPattern(MultiblockMachineDefinition definition) {
        return finishFull(MultiblockPatternBuilder.start(BACK, UP, LEFT)
                .slice(" HHHHHHHHH ", " H  HSH  H ", " HAAHHHAAH ", " T  T T  T ", " T  T T  T ", " T  T T  T ",
                        " T  T T  T ", " T  T T  T ", " T  T T  T ")
                .slice("RHHHHHHHHHR", "RHFFH HFFHR", "RHRRHHHRRHR", "  RR   RR  ", "           ", "  CCCCCCC  ",
                        "  CCCCCCC  ", "  CCCCCCC  ", "           ")
                .slice("RHHHHHHHHHR", "FHRRH HRRHF", "RHRRHHHRRHR", "  RR   RR  ", "   V   V   ", "  CCCCCCC  ",
                        "  C     C  ", "  CCCCCCC  ", "           ")
                .slice("RHHHHHHHHHR", "RHRRH HRRHR", "RHRRHHHRRHR", "  RR   RR  ", "           ", "  CCCCCCC  ",
                        "  CCCCCCC  ", "  CCCCCCC  ", "           ")
                .slice(" HHHHHHHHH ", " HHHHHHHHH ", " HHHHHHHHH ", " T  T T  T ", " T  T T  T ", " T  T T  T ",
                        " T  T T  T ", " T  T T  T ", " T  T T  T "),
                definition);
    }

    private static IBlockPattern fccModulePattern(MultiblockMachineDefinition definition) {
        return finishModule(MultiblockPatternBuilder.start(BACK, UP, LEFT)
                .slice("      AAA", "      AAA", "      AAA", "         ", "         ", "         ", "         ")
                .slice("      AAA", "      ACA", "      ACA", "       C ", "       C ", "         ", "         ")
                .slice("         ", "    S AA ", " AA   AA ", "       V ", "         ", "         ", "         ")
                .slice("         ", " FF   CC ", " RR   CC ", " RR   CC ", "      CC ", "      CC ", "      CC ")
                .slice("         ", " RR   CC ", " RR   CC ", " RRVV CC ", "    V CC ", "    VVCC ", "      CC ")
                .slice("         ", " RR   CC ", " RR   CC ", " FF   CC ", "      CC ", "      CC ", "      CC ")
                .slice("         ", "         ", "         ", "         ", "         ", "         ", "         "),
                definition);
    }

    private static IBlockPattern hydroprocessingModulePattern(MultiblockMachineDefinition definition) {
        return finishModule(MultiblockPatternBuilder.start(BACK, UP, LEFT)
                .slice("             ", "      S      ", "   AA   AA   ", "             ", "             ",
                        "             ")
                .slice("AA         AA", "A  FF   FF  A", "A  CC   CC  A", "AAACCVVVCCAAA", "   CC   CC   ",
                        "   CC   CC   ")
                .slice("AA         AA", " V CC   CC V ", " V CC   CC V ", " VVCC   CCVV ", "   CC   CC   ",
                        "   CC   CC   ")
                .slice("AA         AA", "A  CC   CC  A", "A  CC   CC  A", "AAACCVVVCCAAA", "   CC   CC   ",
                        "   CC   CC   ")
                .slice("             ", "             ", "             ", "             ", "             ",
                        "             "),
                definition);
    }

    private static IBlockPattern highPressureBoosterPattern(MultiblockMachineDefinition definition) {
        return finishModule(MultiblockPatternBuilder.start(BACK, UP, LEFT)
                .slice("             ", "      S      ", "             ", "             ", "             ",
                        "             ")
                .slice("             ", "             ", "             ", "             ", "             ",
                        "             ")
                .slice("             ", "             ", "             ", "A            ", "             ",
                        "             ")
                .slice("             ", "             ", "             ", "             ", "             ",
                        "             ")
                .slice("             ", "             ", "             ", "             ", "    V   V    ",
                        "             ")
                .slice("   RRRRRRR   ", "   RMMMMMR   ", "   RVRRRVR   ", "    V   V    ", "    V   V    ",
                        "             ")
                .slice("   RRRRRRR   ", "   RFRFRFR   ", "   RRRRRRR   ", "             ", "             ",
                        "             "),
                definition);
    }

    private static IBlockPattern reformerModulePattern(MultiblockMachineDefinition definition) {
        return finishModule(MultiblockPatternBuilder.start(BACK, UP, LEFT)
                .slice("AAA         ", "AAA AA S    ", "AAA AA   AA ", "            ", "            ", "            ")
                .slice("AAA         ", "ACC CC   FF ", "ACC CC   AA ", " CC CC      ", " CC CC      ", " CC CC      ")
                .slice("AAA         ", "ACC CC   RR ", "ACC CC   RR ", " CC CCVVVRR ", " CCVCC   RR ", " CC CC      ")
                .slice("AAA         ", "ACC CC   RR ", "ACC CC   RR ", " CC CC   RR ", " CC CC   RR ", " CC CC      ")
                .slice("AAA         ", "AAA         ", "AAA         ", "            ", "            ", "            "),
                definition);
    }

    private static IBlockPattern cokerModulePattern(MultiblockMachineDefinition definition) {
        return finishModule(MultiblockPatternBuilder.start(BACK, UP, LEFT)
                .slice("           ", "     S     ", "  AA   AA  ", " T  T T  T ", " T  T T  T ", " T  T T  T ",
                        " T  T T  T ", " T  T T  T ", " T  T T  T ")
                .slice("R         R", "R FF   FF R", "R RR   RR R", "  RR   RR  ", "           ", "  CCCCCCC  ",
                        "  CCCCCCC  ", "  CCCCCCC  ", "           ")
                .slice("R         R", "F RR   RR F", "R RR   RR R", "  RR   RR  ", "   V   V   ", "  CCCCCCC  ",
                        "  C     C  ", "  CCCCCCC  ", "           ")
                .slice("R         R", "R RR   RR R", "R RR   RR R", "  RR   RR  ", "           ", "  CCCCCCC  ",
                        "  CCCCCCC  ", "  CCCCCCC  ", "           ")
                .slice("           ", "           ", "           ", " T  T T  T ", " T  T T  T ", " T  T T  T ",
                        " T  T T  T ", " T  T T  T ", " T  T T  T "),
                definition);
    }

    private static IBlockPattern finishFull(MultiblockPatternBuilder builder,
                                            MultiblockMachineDefinition definition) {
        return finishModule(builder
                .where('H', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get()).setMinGlobalLimited(70)
                        .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                        .or(abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                        .or(abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                        .or(abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(4)
                                .setPreviewCount(3))
                        .or(abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(6)
                                .setPreviewCount(5))
                        .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))),
                definition);
    }

    private static IBlockPattern finishModule(MultiblockPatternBuilder builder,
                                              MultiblockMachineDefinition definition) {
        return builder
                .where('S', Predicates.controller(blocks(definition.getBlock())))
                .where('A', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get()))
                .where('C', blocks(REFRACTORY_CONTAINMENT_CASING.get()))
                .where('R', blocks(REFRACTORY_STRUCTURAL_CASING.get()))
                .where('V', blocks(VIBRANT_PIPE_FRAMEWORK.get()))
                .where('F', blocks(CASING_HEAT_VENT.get()))
                .where('M', blocks(LIGHTWEIGHT_MECHANICAL_PARTWORK.get()))
                .where('T', frames(GTMaterials.StainlessSteel))
                .where(' ', any())
                .build();
    }
}
