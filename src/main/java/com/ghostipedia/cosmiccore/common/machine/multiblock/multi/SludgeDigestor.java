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
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates.autoAbilitiesNoEnergyIn;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_DARK_STEEL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_INDUSTRIAL_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHTWEIGHT_MECHANICAL_PARTWORK;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class SludgeDigestor {

    public static final MultiblockMachineDefinition SLUDGE_DIGESTOR = REGISTRATE
            .multiblock("sludge_digestor", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(LIGHTWEIGHT_DARK_STEEL_CASING)
            .recipeType(CosmicRecipeTypes.SLUDGE_DIGESTOR)
            .recipeModifiers(CosmicRecipeModifiers.LOCKED_PARALLEL_8,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .pattern(definition -> MultiblockPatternBuilder.start(LEFT, UP, BACK)
                    .slice("AAAA", "A  A", "B  B", "B  B", "B  B", "A  A", "    ")
                    .slice("AAAA", " CC ", " CC ", " CC ", " CC ", "ACCA", "    ")
                    .slice("AAAA", " CC ", " EE ", " CC ", " EE ", "ACCA", " CC ")
                    .slice("AAAA", " CC ", " CC ", " EE ", " CC ", "ACCA", " CC ")
                    .slice("AAAA", " CC ", " DC ", " CC ", " CC ", "ACCA", "    ")
                    .slice("AAAA", "A  A", "B  B", "B  B", "B  B", "A  A", "    ")
                    .where('D', Predicates.controller(blocks(definition.getBlock())))
                    .where(' ', Predicates.any())
                    .where('A', blocks(LIGHTWEIGHT_DARK_STEEL_CASING.get())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.SLUDGE_DIGESTOR))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', frames(GTMaterials.StainlessSteel))
                    .where('C', blocks(LIGHTWEIGHT_INDUSTRIAL_CASING.get()))
                    .where('E', blocks(LIGHTWEIGHT_MECHANICAL_PARTWORK.get()))
                    .build())
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/lightweight_dark_steel_casing"),
                    CosmicCore.id("block/multiblock/dawnforge")))
            .register();

    public static void init() {}
}
