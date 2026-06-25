package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.DreamersBasinMachine;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.EXPORT_SOUL;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.SUPERHEAVY_STEEL_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

/**
 * The Dreamer's Basin - A multithreaded processing machine.
 * <p>
 * This machine can run multiple unique recipes simultaneously using color-coded input buses.
 * Each thread requires a uniquely colored input bus/hatch pair.
 * Maximum threads is determined by energy hatch amperage (4A = 4 threads, 16A = 16 threads).
 * All threads share output buses/hatches.
 * <p>
 * Energy is split evenly among threads - each thread gets 1A worth of the input voltage.
 * Recipes can overclock within each thread's energy budget.
 */
public class DreamersBasin {

    public static final MultiblockMachineDefinition DREAMERS_BASIN = REGISTRATE
            .multiblock("dreamers_basin", DreamersBasinMachine::new)
            .langValue("Dreamer's Basin")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.MULTITHREADED_PROCESSOR)
            // CRITICAL: Disable default overclock modifier - we handle overclocking per-thread, under no circumstance
            // should anyone change this ~G
            .noRecipeModifier()
            .appearanceBlock(SUPERHEAVY_STEEL_CASING)
            .partAppearance((controller, part, side) -> SUPERHEAVY_STEEL_CASING.getDefaultState())
            .tooltips(
                    Component.translatable("cosmiccore.machine.dreamers_basin.tooltip.0"),
                    Component.translatable("cosmiccore.machine.dreamers_basin.tooltip.1"),
                    Component.translatable("cosmiccore.machine.dreamers_basin.tooltip.2"),
                    Component.translatable("cosmiccore.machine.dreamers_basin.tooltip.3"))
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("  AAA   AAA  ", "    A   A    ", "    A   A    ", "      B      ", "      B      ",
                            "     BBB     ", "   BBBBBBB   ", "    BBBBB    ", "     BBB     ", "             ",
                            "             ")
                    .slice(" ACCA   ACCA ", "             ", "    A B A    ", "    AB BA    ", "     B B     ",
                            "   BBBBBBB   ", "  BCCBBBCCB  ", "   CC   CC   ", "   CC   CC   ", "   C     C   ",
                            "             ")
                    .slice("ACCA     ACCA", "  C   B   C  ", "  C  B B  C  ", "    A B A    ", "    A B A    ",
                            "  B A C A B  ", " BCBBCCCBBCB ", "  CC     CC  ", "  C       C  ", "  C       C  ",
                            "  C       C  ")
                    .slice("ACA   B   ACA", "     B B     ", "      B      ", "   C     C   ", "   C CCC C   ",
                            " B CCC CCC B ", "BCBC     CBCB", " CC       CC ", " C         C ", " C         C ",
                            "             ")
                    .slice("AA    B    AA", "A    B B    A", "AA    B    AA", " AA       AA ", "  A       A  ",
                            " BAC     CAB ", "BCB       BCB", "BC         CB", " C         C ", "             ",
                            "             ")
                    .slice("     BBB     ", "   BBBBBBB   ", "  B   C   B  ", " B         B ", " B C     C B ",
                            "BB C     C BB", "BBC       CBB", "B           B", "B           B", "             ",
                            "             ")
                    .slice("   BBBBBBB   ", "  B  BBB  B  ", " B BBCCCBB B ", "B B       B B", "B BC     CB B",
                            "BBC       CBB", "BBC       CBB", "B           B", "B           B", "             ",
                            "             ")
                    .slice("     BBB     ", "   BBBBBBB   ", "  B   C   B  ", " B         B ", " B C     C B ",
                            "BB C     C BB", "BBC       CBB", "B           B", "B           B", "             ",
                            "             ")
                    .slice("AA    B    AA", "A    B B    A", "AA    B    AA", " AA       AA ", "  A       A  ",
                            " BAC     CAB ", "BCB       BCB", "BC         CB", " C         C ", "             ",
                            "             ")
                    .slice("ACA   B   ACA", "     B B     ", "      B      ", "   C     C   ", "   C CCC C   ",
                            " B CCC CCC B ", "BCBC     CBCB", " CC       CC ", " C         C ", " C         C ",
                            "             ")
                    .slice("ACCA     ACCA", "  C   B   C  ", "  C  B B  C  ", "    A B A    ", "    A B A    ",
                            "  B A C A B  ", " BCBBCCCBBCB ", "  CC     CC  ", "  C       C  ", "  C       C  ",
                            "  C       C  ")
                    .slice(" ACCA   ACCA ", "             ", "    A B A    ", "    AB BA    ", "     B B     ",
                            "   BBBBBBB   ", "  BCCBBBCCB  ", "   CC   CC   ", "   CC   CC   ", "   C     C   ",
                            "             ")
                    .slice("  AAA   AAA  ", "    A   A    ", "    A   A    ", "      B      ", "      B      ",
                            "     BBB     ", "   BBBBBBB   ", "    BBDBB    ", "     BBB     ", "             ",
                            "             ")

                    .where('D', controller(blocks(definition.getBlock())))
                    .where(' ', any())
                    .where('A', blocks(CosmicBlocks.SOUL_MUTED_CASING.get()))
                    .where('B', blocks(SUPERHEAVY_STEEL_CASING.get()).setMinGlobalLimited(200)
                            .or(autoAbilities(CosmicRecipeTypes.MULTITHREADED_PROCESSOR))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(abilities(EXPORT_SOUL).setExactLimit(1)))
                    .where('C', blocks(CosmicBlocks.SOMARUST_CASING.get()))
                    .build())
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/superheavy_steel_casing"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .register();

    public static void init() {}
}
