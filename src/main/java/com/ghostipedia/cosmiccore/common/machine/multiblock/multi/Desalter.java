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
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.machine.multiblock.PatternMappedPartAppearance.of;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_STAINLESS_STEEL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.REFRACTORY_STRUCTURAL_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.any;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.frames;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.BACK;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.DOWN;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.LEFT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.RIGHT;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.UP;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class Desalter {

    public static final MultiblockMachineDefinition DESALTER = REGISTRATE
            .multiblock("desalter", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(LIGHTWEIGHT_STAINLESS_STEEL_CASING)
            .partAppearance(of(LIGHTWEIGHT_STAINLESS_STEEL_CASING::getDefaultState))
            .recipeType(CosmicRecipeTypes.DESALTER)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .tooltips(
                    Component.translatable("cosmiccore.multiblock.desalter.tooltip.0"),
                    Component.translatable("cosmiccore.multiblock.desalter.tooltip.1"))
            .pattern(definition -> MultiblockPatternBuilder.start(BACK, UP, LEFT)
                    .startOffset(OriginOffset.of(DOWN, 4).move(RIGHT, 3))
                    .slice("AAAAAAA", "B     B", "B     B", "CCCCCCC", "CCCDCCC", "CCCCCCC", "       ")
                    .slice("AAAAAAA", " A A A ", "CCCCCCC", "C     C", "C     C", "C     C", "CCCCCCC")
                    .slice("AAAAAAA", "       ", "CCCCCCC", "C     C", "C     C", "C     C", "CCCCCCC")
                    .slice("AAAAAAA", " A A A ", "CCCCCCC", "C     C", "C     C", "C     C", "CCCCCCC")
                    .slice("AAAAAAA", "B     B", "B     B", "CCCCCCC", "CCCCCCC", "CCCCCCC", "       ")
                    .where('D', Predicates.controller(blocks(definition.getBlock())))
                    .where('A', blocks(REFRACTORY_STRUCTURAL_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', frames(GTMaterials.StainlessSteel))
                    .where('C', blocks(LIGHTWEIGHT_STAINLESS_STEEL_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(3))
                            .or(abilities(PartAbility.EXPORT_FLUIDS).setExactLimit(3)))
                    .where(' ', any())
                    .build())
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/lightweight_stainless_steel_casing"),
                    GTCEu.id("block/multiblock/gcym/large_electrolyzer")))
            .register();

    public static void init() {}
}
