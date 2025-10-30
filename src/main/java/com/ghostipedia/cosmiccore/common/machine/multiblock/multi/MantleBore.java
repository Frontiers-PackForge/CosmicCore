package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.AlchemicalFissionReactor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class MantleBore {

    public final static MultiblockMachineDefinition MANTLE_BORE = REGISTRATE
            .multiblock("mantle_bore", AlchemicalFissionReactor::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.ELECTROLYZER_RECIPES)
            .recipeModifier(ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
            .appearanceBlock(GTBlocks.STEEL_HULL)
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, BACK, UP)
                    .aisle(" A   A ", "A     A", "       ", "   D   ", "       ", "A     A", " A   A ")
                    .aisle(" A   A ", "A     A", "   D   ", "  DDD  ", "   D   ", "A     A", " A   A ")
                    .aisle("  BBB  ", " ACCCA ", "BCBBBCB", "BCBEBCB", "BCBBBCB", " ACCCA ", "  BBB  ")
                    .where(' ', any())
                    .where("E", controller(blocks(definition.getBlock())))
                    .where('C', blocks(CosmicBlocks.CASING_HEAT_VENT.get()))
                    .where('A', blocks(STEEL_HULL.get()))
                    .where('D', blocks(STEEL_HULL.get()))
                    .where('B', blocks(CASING_STEEL_SOLID.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setExactLimit(1)))
                    .build())
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .andThen(model -> model.addDynamicRenderer(CosmicDynamicRenderHelpers::getRenderTesterHelper)))
            .hasBER(true)
            .register();

    public static void init() {}
}
