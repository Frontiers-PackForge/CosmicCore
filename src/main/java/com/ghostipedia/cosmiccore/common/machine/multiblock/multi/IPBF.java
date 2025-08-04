package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IPBFMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.STEEL_PLATED_BRONZE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class IPBF {

    public static final MultiblockMachineDefinition INDUSTRIAL_PRIMITIVE_BLAST_FURNACE = REGISTRATE
            .multiblock("industrial_primitive_blast_furnace", IPBFMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(CosmicRecipeTypes.INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES)
            .recipeModifier(IPBFMachine::recipeModifier, true)
            .appearanceBlock(CASING_PRIMITIVE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("QQQ", "XXX", "XXX", "XXX", "XXX")
                    .aisle("QQQ", "X#X", "X#X", "X#X", "X#X")
                    .aisle("QQQ", "XYX", "XXX", "XXX", "XXX")
                    .where('X', blocks(CASING_PRIMITIVE_BRICKS.get()))
                    .where('#', Predicates.air())
                    .where('Y', Predicates.controller(blocks(definition.getBlock())))
                    .where('Q', blocks(FIREBOX_STEEL.get()).setMinGlobalLimited(6)
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1)
                                    .setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1)
                                    .setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1).setExactLimit(1)))
                    .build())
            .model(createWorkableCasingMachineModel(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/multiblock/steam_oven"))
                    .andThen(b -> b.addDynamicRenderer(
                            () -> DynamicRenderHelper.makeBoilerPartRender(
                                    BoilerFireboxType.STEEL_FIREBOX, STEEL_PLATED_BRONZE))))
            .tooltips(
                    Component.translatable("cosmiccore.multiblock.ipbf.tooltip.0"),
                    Component.translatable("cosmiccore.multiblock.ipbf.tooltip.1"),
                    Component.translatable("cosmiccore.multiblock.ipbf.tooltip.2"),
                    Component.translatable("cosmiccore.multiblock.ipbf.tooltip.3"))
            .register();

    public static void init() {}
}
