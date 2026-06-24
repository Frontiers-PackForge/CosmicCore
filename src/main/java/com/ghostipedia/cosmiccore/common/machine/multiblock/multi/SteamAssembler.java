package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.steam.WeakSteamParallelMultiBlockMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.STEEL_PLATED_BRONZE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class SteamAssembler {

    public static final MultiblockMachineDefinition HIGH_PRESSURE_ASSEMBLER = REGISTRATE
            .multiblock("high_pressure_assembler", WeakSteamParallelMultiBlockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.ASSEMBLER_RECIPES)
            .recipeModifier(WeakSteamParallelMultiBlockMachine::recipeModifier, true)
            .appearanceBlock(STEEL_PLATED_BRONZE)
            .pattern(definition -> MultiblockPatternBuilder.start()
                    .slice("AAAAA", "BBBBB", "BBBBB")
                    .slice("AAAAA", "BDDDB", "BBBBB")
                    .slice("AAAAA", "BYBBB", "BBBBB")
                    .where('B', blocks(STEEL_PLATED_BRONZE.get())
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1)
                                    .setExactLimit(2))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1)
                                    .setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1).setExactLimit(1)))
                    .where('#', Predicates.air())
                    .where('Y', Predicates.controller(blocks(definition.getBlock())))
                    .where('A', blocks(FIREBOX_STEEL.get()).setMinGlobalLimited(11)
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('D', blocks(CASING_STEEL_GEARBOX.get()))
                    .build())
            .model(createWorkableCasingMachineModel(CosmicCore.id("block/casings/solid/steel_plated_bronze_casing"),
                    GTCEu.id("block/multiblock/steam_oven"))
                    .andThen(b -> b.addDynamicRenderer(
                            () -> DynamicRenderHelper.makeBoilerPartRender(
                                    BoilerFireboxType.STEEL_FIREBOX, STEEL_PLATED_BRONZE))))
            .tooltips(Component.translatable("cosmiccore.multiblock.hpsassem.tooltip.0"),
                    Component.translatable("cosmiccore.multiblock.hpsassem.tooltip.1"),
                    Component.translatable("cosmiccore.multiblock.hpsassem.tooltip.2"))
            .register();

    public static void init() {}
}
