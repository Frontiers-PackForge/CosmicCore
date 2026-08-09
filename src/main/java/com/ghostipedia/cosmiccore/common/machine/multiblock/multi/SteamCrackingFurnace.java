package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.OriginOffset;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.machine.multiblock.PatternMappedPartAppearance.of;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.REFRACTORY_CONTAINMENT_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.REFRACTORY_STRUCTURAL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.STEEL_PLATED_BRONZE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.BACK;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.DOWN;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.LEFT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.RIGHT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.UP;

public class SteamCrackingFurnace {

    public static final MultiblockMachineDefinition STEAM_CRACKING_FURNACE = REGISTRATE
            .multiblock("steam_cracking_furnace", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(REFRACTORY_STRUCTURAL_CASING)
            .partAppearance(of(REFRACTORY_STRUCTURAL_CASING::getDefaultState))
            .recipeType(CosmicRecipeTypes.STEAM_CRACKING_FURNACE)
            .recipeModifiers(GTRecipeModifiers::crackerOverclock, GTRecipeModifiers.BATCH_MODE)
            .tooltips(
                    Component.translatable("cosmiccore.multiblock.steam_cracking_furnace.tooltip.0"),
                    Component.translatable("cosmiccore.multiblock.steam_cracking_furnace.tooltip.1"),
                    Component.translatable("cosmiccore.multiblock.steam_cracking_furnace.tooltip.2"),
                    Component.translatable("cosmiccore.multiblock.steam_cracking_furnace.tooltip.3"))
            .pattern(definition -> MultiblockPatternBuilder.start(BACK, UP, LEFT)
                    .startOffset(OriginOffset.of(DOWN, 1).move(RIGHT, 2))
                    .slice("AAAAA", "AABAA", "AAAAA", " AAA ", "     ", "     ", "     ", "     ", "     ",
                            "     ")
                    .slice("AAAAA", "A   A", "A   A", "ACCCA", " HHH ", " CCC ", " CCC ", " CCC ", " CCC ",
                            " CCC ")
                    .slice("AAAAA", "D   D", "D   D", "ACCCA", " H H ", " C C ", " C C ", " C C ", " C C ",
                            " CCC ")
                    .slice("AAAAA", "A   A", "A   A", "ACCCA", " HHH ", " CCC ", " CCC ", " CCC ", " CCC ",
                            " CCC ")
                    .slice("AAAAA", "AADAA", "AADAA", " AAA ", "     ", "     ", "     ", "     ", "     ",
                            "     ")
                    .where('B', Predicates.controller(blocks(definition.getBlock())))
                    .where('A', blocks(REFRACTORY_STRUCTURAL_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('C', blocks(REFRACTORY_CONTAINMENT_CASING.get())
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setExactLimit(1)))
                    .where('D', blocks(STEEL_PLATED_BRONZE.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(2))
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(0)))
                    .where('H', Predicates.heatingCoils())
                    .where(' ', any())
                    .build())
            .workableCasingModel(
                    CosmicCore.id("block/casings/solid/refractory_structural_casing"),
                    GTCEu.id("block/multiblock/cracking_unit"))
            .register();

    public static void init() {}
}
