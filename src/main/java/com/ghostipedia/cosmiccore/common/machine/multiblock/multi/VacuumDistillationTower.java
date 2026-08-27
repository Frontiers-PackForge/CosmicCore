package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import java.util.List;

import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CASING_HEAT_VENT;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_STAINLESS_STEEL_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.air;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.autoAbilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.controller;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.FRONT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.LEFT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.UP;

public final class VacuumDistillationTower {

    private VacuumDistillationTower() {}

    public static void init() {
        MultiblockMachineDefinition definition = GTMultiMachines.DISTILLATION_TOWER;
        definition.setRecipeTypes(new GTRecipeType[] {
                GTRecipeTypes.DISTILLATION_RECIPES,
                CosmicRecipeTypes.VACUUM_DISTILLATION
        });
        if (CosmicRecipeTypes.VACUUM_DISTILLATION.getIconSupplier() == null) {
            CosmicRecipeTypes.VACUUM_DISTILLATION.setIconSupplier(definition::asStack);
        }
        TieredMultiblockPatterns.registerConfigurations(definition, List.of(
                new TieredMultiblockPatterns.PatternLabel(
                        "cosmiccore.multiblock.configuration.atmospheric",
                        "cosmiccore.multiblock.configuration.atmospheric.short"),
                new TieredMultiblockPatterns.PatternLabel(
                        "cosmiccore.multiblock.configuration.vacuum",
                        "cosmiccore.multiblock.configuration.vacuum.short")),
                () -> vacuumPattern(definition));
    }

    private static IBlockPattern vacuumPattern(MultiblockMachineDefinition definition) {
        MultiPredicate exportPredicate = abilities(PartAbility.EXPORT_FLUIDS_1X);
        if (GTCEu.Mods.isAE2Loaded()) {
            exportPredicate = exportPredicate.or(blocks(GTAEMachines.FLUID_EXPORT_HATCH_ME.get()));
        }
        exportPredicate = exportPredicate.setMaxLayerLimited(1);
        MultiPredicate maintenance = autoAbilities(true, false, false).setMaxGlobalLimited(1);
        return MultiblockPatternBuilder.start(UP, FRONT, LEFT)
                .slice("FFFFAAA ", "FFFBAAAA", "FFFBAAAA", "FFFBAAAA", "FFFFASA ")
                .sliceRepeatable(3, 3, "FVF OOO ", "F  B###O", "F  B###O", "F FB###O", "FVF OOO ")
                .slice("FFFFOOO ", "FVFB###O", "FVFB###O", "FVFB###O", "FFFFOOO ")
                .sliceRepeatable(0, 8, "    OOO ", "   O###O", "   O###O", "   O###O", "    OOO ")
                .slice("    DDD ", "   DDDDD", "   DDMDD", "   DDDDD", "    DDD ")
                .where('A', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get())
                        .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                .setMaxGlobalLimited(2))
                        .or(abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                        .or(maintenance))
                .where('B', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get()))
                .where('S', controller(blocks(definition.getBlock())))
                .where('O', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get()).or(exportPredicate))
                .where('D', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get()).or(exportPredicate))
                .where('M', abilities(PartAbility.MUFFLER).setExactLimit(1))
                .where('F', blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()))
                .where('V', blocks(CASING_HEAT_VENT.get()))
                .where('#', air())
                .where(' ', any())
                .build();
    }
}
