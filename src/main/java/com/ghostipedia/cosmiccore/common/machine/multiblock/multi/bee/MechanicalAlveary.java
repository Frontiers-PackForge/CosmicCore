package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.bee;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.Map;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

public class MechanicalAlveary {

    private static Map<Block, Integer> coreBlockTiersCache;

    private static Map<Block, Integer> getCoreBlockTiers() {
        if (coreBlockTiersCache == null) {
            coreBlockTiersCache = Map.of(
                    GTBlocks.MACHINE_CASING_LV.get(), GTValues.LV,
                    GTBlocks.MACHINE_CASING_MV.get(), GTValues.MV,
                    GTBlocks.MACHINE_CASING_HV.get(), GTValues.HV,
                    GTBlocks.MACHINE_CASING_EV.get(), GTValues.EV);
        }
        return coreBlockTiersCache;
    }

    public static final MultiblockMachineDefinition MECHANICAL_ALVEARY = REGISTRATE
            .multiblock("mechanical_alveary", MechanicalAlvearyMachine::new)
            .langValue("Mechanical Alveary")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .recipeModifiers()
            .appearanceBlock(CosmicBlocks.ALVEARY_CASING)
            .tooltipBuilder((stack, list) -> {
                list.add(Component.literal("A GT-powered bee housing with climate control."));
                list.add(Component.literal("Internal core block determines tier and max queens."));
                list.add(Component.literal("LV=1, MV=2, HV=3, EV=4 queens."));
                list.add(Component.literal("Use colored buses to isolate queen I/O."));
                list.add(Component.literal("Wall casings can be swapped for modifier casings."));
            })
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAA", "AAA", "AAA", "BBB")
                    .aisle("AAA", "ACA", "AAA", "BBB")
                    .aisle("AAA", "ADA", "AAA", "BBB")
                    .where('D', controller(blocks(definition.getBlock())))
                    .where('C', tieredCorePredicate())
                    .where('A', alvearyWallPredicate())
                    .where('B', blocks(CosmicBlocks.IRON_PLATED_DEEPSLATE_BLOCK.slab().get()))
                    .build())
            // spotless:on
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/alveary_casing"),
                    CosmicCore.id("block/multiblock/dawnforge"))
            .register();

    /**
     * Predicate for the center core block. Accepts LV-EV GT machine casings.
     * Sets "AlvearyTier" in the match context for the machine to read on structure formation.
     */
    private static TraceabilityPredicate tieredCorePredicate() {
        return new TraceabilityPredicate(
                blockWorldState -> {
                    var blockState = blockWorldState.getBlockState();
                    Integer tier = getCoreBlockTiers().get(blockState.getBlock());
                    if (tier == null) {
                        blockWorldState.setError(
                                new PatternStringError("cosmiccore.mechanical_alveary.error.invalid_core"));
                        return false;
                    }
                    Object existing = blockWorldState.getMatchContext().getOrPut("AlvearyTier", tier);
                    if (!existing.equals(tier)) {
                        blockWorldState.setError(
                                new PatternStringError("cosmiccore.mechanical_alveary.error.mismatched_core"));
                        return false;
                    }
                    return true;
                },
                () -> getCoreBlockTiers().keySet().stream()
                        .map(block -> new BlockInfo(block.defaultBlockState(), null))
                        .toArray(BlockInfo[]::new));
    }

    private static TraceabilityPredicate alvearyWallPredicate() {
        return blocks(CosmicBlocks.ALVEARY_CASING.get())
                .or(abilities(CosmicPartAbility.ALVEARY_MODIFIER))
                .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                .or(abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1))
                .or(abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1))
                .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1));
    }

    public static void init() {}
}
