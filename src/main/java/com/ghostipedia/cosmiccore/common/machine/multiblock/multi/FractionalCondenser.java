package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.OriginOffset;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.machine.multiblock.PatternMappedPartAppearance.of;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_STAINLESS_STEEL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.REFRACTORY_STRUCTURAL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.VIBRANT_PIPE_FRAMEWORK;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.BACK;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.DOWN;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.LEFT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.RIGHT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.UP;

public class FractionalCondenser {

    public static final MultiblockMachineDefinition FRACTIONAL_CONDENSER = REGISTRATE
            .multiblock("fractional_condenser", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(REFRACTORY_STRUCTURAL_CASING)
            .partAppearance(of(REFRACTORY_STRUCTURAL_CASING::getDefaultState))
            .recipeType(CosmicRecipeTypes.FRACTIONAL_CONDENSER)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            .tooltips(
                    Component.translatable("cosmiccore.multiblock.fractional_condenser.tooltip.0"),
                    Component.translatable("cosmiccore.multiblock.fractional_condenser.tooltip.1"),
                    Component.translatable("cosmiccore.multiblock.fractional_condenser.tooltip.2"))
            .pattern(definition -> MultiblockPatternBuilder.start(BACK, UP, LEFT)
                    .startOffset(OriginOffset.of(DOWN, 1).move(RIGHT, 6))
                    .slice("AAAAAAAA", "    AABA", "        ", "        ", "        ", "        ", "        ",
                            "        ")
                    .slice("AAAAAAAA", " CC AAAA", " CC  DD ", " CCEEDD ", " CC  DD ", " CC  DD ", " CC  DD ",
                            " CC     ")
                    .slice("AAAAAAAA", " CC AAAA", " CC  DD ", " CC  DD ", " CC  DD ", " CCEEDD ", " CC  DD ",
                            " CC     ")
                    .slice("AAAAAAAA", "    AAAA", "        ", "        ", "        ", "        ", "        ",
                            "        ")
                    .where('B', Predicates.controller(blocks(definition.getBlock())))
                    .where('A', blocks(REFRACTORY_STRUCTURAL_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('C', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1)))
                    .where('D', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get())
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setExactLimit(5))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setExactLimit(1)))
                    .where('E', blocks(VIBRANT_PIPE_FRAMEWORK.get()))
                    .where(' ', any())
                    .build())
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/refractory_structural_casing"),
                    GTCEu.id("block/multiblock/distillation_tower"))
            .register();

    public static void init() {}
}
