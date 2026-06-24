package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IPBFMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.STEEL_PLATED_BRONZE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class IPBF {

    public static final MultiblockMachineDefinition INDUSTRIAL_PRIMITIVE_BLAST_FURNACE = REGISTRATE
            .multiblock("industrial_primitive_blast_furnace", IPBFMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(CosmicRecipeTypes.INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES)
            .recipeModifier(IPBFMachine::recipeModifier, true)
            .appearanceBlock(CASING_PRIMITIVE_BRICKS)
            .pattern(definition -> MultiblockPatternBuilder.start()
                    .slice("QQQ", "XXX", "XXX", "XXX", "XXX")
                    .slice("QQQ", "X#X", "X#X", "X#X", "X#X")
                    .slice("QQQ", "XYX", "XXX", "XXX", "XXX")
                    .where('X', blocks(CASING_PRIMITIVE_BRICKS.get()))
                    .where('#', Predicates.air()
                            .or(Predicates.custom(bws -> GTUtil.isBlockSnow(bws.getBlockState()) ?
                                    null : Predicates.PLACEHOLDER, null)))
                    .where('Y', Predicates.controller(blocks(definition.getBlock())))
                    .where('Q', blocks(FIREBOX_STEEL.get()).setMinGlobalLimited(6)
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1)
                                    .setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1)
                                    .setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1).setExactLimit(1)))
                    .build())
            .model(createWorkableCasingMachineModel(GTCEu.id("block/casings/solid/machine_primitive_bricks"),
                    GTCEu.id("block/multiblock/primitive_blast_furnace"))
                    .andThen(b -> b.addDynamicRenderer(
                            () -> DynamicRenderHelper.makeBoilerPartRender(
                                    BoilerFireboxType.STEEL_FIREBOX, STEEL_PLATED_BRONZE)))
                    .andThen(b -> b.addDynamicRenderer(DynamicRenderHelper::createPBFLavaRender)))
            .hasBER(true)
            .tooltips(
                    Component.translatable("cosmiccore.multiblock.ipbf.tooltip.0"),
                    Component.translatable("cosmiccore.multiblock.ipbf.tooltip.1"),
                    Component.translatable("cosmiccore.multiblock.ipbf.tooltip.2"),
                    Component.translatable("cosmiccore.multiblock.ipbf.tooltip.3"))
            .register();

    public static void init() {}
}
