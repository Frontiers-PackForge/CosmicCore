package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.world.level.block.Blocks;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_DARK_STEEL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_INDUSTRIAL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_MECHANICAL_PARTWORK;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class DissolutionVat {

    public static final MultiblockMachineDefinition DISSOLUTION_VAT = REGISTRATE
            .multiblock("dissolution_vat", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(LIGHTWEIGHT_DARK_STEEL_CASING)
            .recipeType(CosmicRecipeTypes.DISSOLUTION_VAT)
            .recipeModifiers(CosmicRecipeModifiers.LOCKED_PARALLEL_8,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, DOWN, FRONT)
                    .aisle("CAAAAAC", "CAAAAAC", "CAAAAAC")
                    .aisle("AAAAAAA", "ADDDDDA", "AEEEEEA")
                    .aisle("CAAAAAC", "CAAAAAC", "CAAAAAC")
                    .aisle("  AAA  ", "  ABA  ", "       ")
                    .where('B', Predicates.controller(blocks(definition.getBlock())))
                    .where(' ', Predicates.air())
                    .where('A', blocks(LIGHTWEIGHT_DARK_STEEL_CASING.get())
                            .or(autoAbilities(CosmicRecipeTypes.DISSOLUTION_VAT))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('C', blocks(LIGHTWEIGHT_INDUSTRIAL_CASING.get()))
                    .where('D', blocks(LIGHTWEIGHT_MECHANICAL_PARTWORK.get()))
                    .where('E', blocks(Blocks.WATER))
                    .build())
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/lightweight_dark_steel_casing"),
                    CosmicCore.id("block/multiblock/vomahine_chemplant")))
            .register();

    public static void init() {}
}
