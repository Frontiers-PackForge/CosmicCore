package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates.autoAbilitiesNoEnergyIn;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_DARK_STEEL_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_STEEL_PIPE;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class IndustrialFlotationPlant {

    public static final MultiblockMachineDefinition INDUSTRIAL_FLOTATION_PLANT = REGISTRATE
            .multiblock("industrial_flotation_plant", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(LIGHTWEIGHT_DARK_STEEL_CASING)
            .recipeType(CosmicRecipeTypes.INDUSTRIAL_FLOTATION_PLANT)
            .recipeModifiers(CosmicRecipeModifiers.LOCKED_PARALLEL_8,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .pattern(definition -> MultiblockPatternBuilder.start(LEFT, UP, BACK)
                    .slice("AAAAA", "AAAAA", "  AAA")
                    .slice("AAAAA", "CBBBA", "  A A")
                    .slice("AAAAA", "AAAAA", "  AAA")
                    .slice("  AAA", "  ABA", "  A A")
                    .slice("  AAA", "  AAA", "  AAA")
                    .where('C', Predicates.controller(blocks(definition.getBlock())))
                    .where(' ', Predicates.any())
                    .where('A', blocks(LIGHTWEIGHT_DARK_STEEL_CASING.get())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.INDUSTRIAL_FLOTATION_PLANT))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(CASING_STEEL_PIPE.get()))
                    .build())
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/lightweight_dark_steel_casing"),
                    CosmicCore.id("block/multiblock/mixing_vessel")))
            .register();

    public static void init() {}
}
