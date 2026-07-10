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
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_STEEL_GEARBOX;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_STEEL_PIPE;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class IndustrialOreSorter {

    public static final MultiblockMachineDefinition INDUSTRIAL_ORE_SORTER = REGISTRATE
            .multiblock("industrial_ore_sorter", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(LIGHTWEIGHT_DARK_STEEL_CASING)
            .recipeType(CosmicRecipeTypes.INDUSTRIAL_ORE_SORTER)
            .recipeModifiers(CosmicRecipeModifiers.LOCKED_PARALLEL_8,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .pattern(definition -> MultiblockPatternBuilder.start(LEFT, UP, BACK)
                    .slice("AAA E ", "AAA E ", "    E ", "    E ")
                    .slice("ABAAA ", "D AAA ", "   AA ", "    E ")
                    .slice("AAAAAA", "AAABBA", "  A  A", "    C ")
                    .slice("  AAAA", "  ABBA", "  A  A", "      ")
                    .slice("   AA ", "   AA ", "   AA ", "      ")
                    .where('D', Predicates.controller(blocks(definition.getBlock())))
                    .where(' ', Predicates.any())
                    .where('A', blocks(LIGHTWEIGHT_DARK_STEEL_CASING.get())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.INDUSTRIAL_ORE_SORTER))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(CASING_STEEL_GEARBOX.get()))
                    .where('C', blocks(CASING_STEEL_PIPE.get()))
                    .where('E', frames(GTMaterials.Steel))
                    .build())
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/lightweight_dark_steel_casing"),
                    CosmicCore.id("block/multiblock/wireless_data_transmitter")))
            .register();

    public static void init() {}
}
