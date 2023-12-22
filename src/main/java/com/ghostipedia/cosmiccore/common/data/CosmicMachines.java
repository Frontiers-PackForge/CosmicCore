package com.ghostipedia.cosmiccore.common.data;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.GTMachines;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.autoAbilities;

public class CosmicMachines {
    //Time for some shenanigans

    public static final MultiblockMachineDefinition ALTERNATOR_MACHINE = GTRegistries.REGISTRATE.multiblock("alternator_machine", Alternator::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(CosmicMachineRecipeLogic.ALTERNATOR_RECIPES)
            .recipeModifier(CosmicRecipeModifiers::AlternatorModifiers)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("F###F", "ZWBWZ", "ZWBWZ", "ZWBWZ")
                    .aisle("####", "ZWBWZ", "XOOOX", "XWBWZ")
                    .aisle("F###F", "ZWBWZ", "ZWCWZ", "ZWBWZ")
                    .where("C", Predicates.blocks(definition.get()))
                    .where("#", Predicates.any())
                    .where("W", Predicates.blocks(GTBlocks.COIL_CUPRONICKEL.get()))
                    .where("B", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where("Z", Predicates.abilities(PartAbility.OUTPUT_ENERGY).setMinGlobalLimited(1)
                            .or(Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    )
                    .where("X", Predicates.abilities(PartAbility.INPUT_KINETIC).setMinGlobalLimited(1)
                            .or(Predicates.blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    )
                    .where("O", Predicates.blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .where("F", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .build())
            .workableCasingRenderer(
                    GTCEu.id("gtceu:block/machine_casing_alternator"), false)
            .register();





}
