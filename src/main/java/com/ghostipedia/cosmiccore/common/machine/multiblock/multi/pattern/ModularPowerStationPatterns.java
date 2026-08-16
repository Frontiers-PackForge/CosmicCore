package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.pattern;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.BasicSliceStrategy;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.world.level.block.Block;

import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.air;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.controller;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.BACK;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.LEFT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.UP;

public final class ModularPowerStationPatterns {

    private static final String[][] CORE_SOURCE = {
            { "     ", "AAAAA", "AABAA", "AAAAA", "     " },
            { "AAAAA", "ACCCA", " D D ", "ACCCA", "AAAAA" },
            { "AAAAA", " D D ", "EEED ", " D D ", "AAAAA" },
            { "AAAAA", "ACCCA", " D D ", "ACCCA", "AAAAA" },
            { "     ", "AAAAA", "AAAAA", "AAAAA", "     " }
    };
    private static final String[][] INITIAL_BOUNDARY_SOURCE = {
            { "A", "A", "A", "A", " " },
            { "A", "B", "B", "A", "A" },
            { "A", "B", "C", "B", "A" },
            { "A", "B", "B", "A", "A" },
            { "A", "A", "A", "A", " " }
    };
    private static final String[][] STAGE_SOURCE = {
            { "AA", "A ", "A ", "A ", "  " },
            { "AX", "CB", "CB", "AB", "A " },
            { "AX", "CB", "DD", "CB", "A " },
            { "AX", "CB", "CB", "AB", "A " },
            { "AA", "A ", "A ", "A ", "  " }
    };
    private static final String[][] OUTPUT_SOURCE = {
            { "AAAAAAA", "ABBBBBB", "ABCCCCB", "ABBBBBB", "       " },
            { "AAAAAAA", "ADDDDDD", "ADDDDDD", "ADDDDDD", "ABBBBBB" },
            { "AAAAAAA", "ADDDDDD", "FDEEEEE", "ADDDDDD", "ABCCCCB" },
            { "AAAAAAA", "ADDDDDD", "ADDDDDD", "ADDDDDD", "ABBBBBB" },
            { "AAAAAAA", "ABBBBBB", "ABCCCCB", "ABBBBBB", "       " }
    };

    private ModularPowerStationPatterns() {}

    public static IBlockPattern create(MultiblockMachineDefinition definition) {
        MultiblockPatternBuilder builder = MultiblockPatternBuilder.start(LEFT, UP, BACK);
        appendReversedTransposed(builder, CORE_SOURCE, "ABCDE", "KSPLG");
        appendReversedTransposed(builder, INITIAL_BOUNDARY_SOURCE, "ABC", "HWG");
        appendReversedTransposed(builder, STAGE_SOURCE, "ABCDX", "HIWGX");
        appendReversedTransposed(builder, OUTPUT_SOURCE, "ABCDEF", "HVWOGE");
        return builder
                .sliceStrategy(new BasicSliceStrategy().multiSlice(1, 4, 6, 8))
                .where('S', controller(blocks(definition.getBlock())))
                .where('K', coreCasing())
                .where('H', blocks(CosmicBlocks.LIGHTWEIGHT_DARK_STEEL_CASING.get()))
                .where('P', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                .where('L', blocks(CosmicBlocks.LIGHTWEIGHT_INDUSTRIAL_CASING.get()))
                .where('G', blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                .where('W', blocks(CosmicBlocks.INDUSTRIAL_PARTWORK.get()))
                .where('I', blocks(CosmicBlocks.STEAM_GAS_TURBINE_INTEGRAL_COMPONENTS.get(),
                        CosmicBlocks.COMBUSTION_INTEGRAL_COMPONENTS.get()))
                .where('X', blocks(CosmicBlocks.LIGHTWEIGHT_DARK_STEEL_CASING.get()).or(air()))
                .where('V', blocks(CosmicBlocks.INDUSTRIAL_CONVERTER_SHELL.get()))
                .where('O', blocks(CosmicBlocks.LOW_VOLTAGE_STATOR_HOUSING.get(),
                        CosmicBlocks.MEDIUM_VOLTAGE_STATOR_HOUSING.get(),
                        CosmicBlocks.HIGH_VOLTAGE_STATOR_HOUSING.get(),
                        CosmicBlocks.EXTREME_VOLTAGE_STATOR_HOUSING.get()))
                .where('E', abilities(PartAbility.OUTPUT_ENERGY).setExactLimit(1).setPreviewCount(1))
                .where(' ', any())
                .build();
    }

    public static int stageBodyLeftOffset(int stage) {
        return 4 + stage * 2;
    }

    public static int outputStartLeftOffset(int stages) {
        return 4 + stages * 2;
    }

    public static boolean isIntegral(Block block) {
        return block == CosmicBlocks.STEAM_GAS_TURBINE_INTEGRAL_COMPONENTS.get() ||
                block == CosmicBlocks.COMBUSTION_INTEGRAL_COMPONENTS.get();
    }

    public static int statorTier(Block block) {
        if (block == CosmicBlocks.LOW_VOLTAGE_STATOR_HOUSING.get()) return GTValues.LV;
        if (block == CosmicBlocks.MEDIUM_VOLTAGE_STATOR_HOUSING.get()) return GTValues.MV;
        if (block == CosmicBlocks.HIGH_VOLTAGE_STATOR_HOUSING.get()) return GTValues.HV;
        if (block == CosmicBlocks.EXTREME_VOLTAGE_STATOR_HOUSING.get()) return GTValues.EV;
        return -1;
    }

    private static com.gregtechceu.gtceu.api.multiblock.PatternPredicate coreCasing() {
        return blocks(CosmicBlocks.LIGHTWEIGHT_DARK_STEEL_CASING.get())
                .or(abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(4)
                        .setPreviewCount(1))
                .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(4).setPreviewCount(1))
                .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1));
    }

    private static void appendReversedTransposed(MultiblockPatternBuilder builder, String[][] source,
                                                 String symbols, String replacements) {
        int width = source[0][0].length();
        for (int sourceColumn = width - 1; sourceColumn >= 0; sourceColumn--) {
            String[] transposed = new String[source[0].length];
            for (int row = 0; row < transposed.length; row++) {
                StringBuilder line = new StringBuilder(source.length);
                for (String[] sourceSlice : source) {
                    char symbol = sourceSlice[row].charAt(sourceColumn);
                    int replacement = symbols.indexOf(symbol);
                    line.append(replacement < 0 ? symbol : replacements.charAt(replacement));
                }
                transposed[row] = line.toString();
            }
            builder.slice(transposed);
        }
    }
}
