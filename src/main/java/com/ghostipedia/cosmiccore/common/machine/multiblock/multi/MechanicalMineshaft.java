package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates.autoAbilitiesNoEnergyIn;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.ETHERSTEEL_PLATED_ASH_TILES;
import static com.ghostipedia.cosmiccore.common.data.datagen.CosmicMachineModels.createSeparateControllerCasingMachineModel;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

public class MechanicalMineshaft {

    public static final MultiblockMachineDefinition MECHANICAL_MINESHAFT = REGISTRATE
            .multiblock("mechanical_mineshaft", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .partAppearance((controller, part, side) -> GTBlocks.CASING_STAINLESS_CLEAN.getDefaultState())
            .recipeType(CosmicRecipeTypes.MECHANICAL_MINESHAFT)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AA AA", "AA AA", " A A ", "     ", "     ")
                    .slice("ABBBA", "AACAA", "AA AA", " A A ", " A A ")
                    .slice(" BBB ", " BBB ", "  B  ", "     ", "     ")
                    .slice("ABBBA", "AABAA", "AA AA", " A A ", " A A ")
                    .slice("AA AA", "AA AA", " A A ", "     ", "     ")
                    .where(' ', any())
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('A', blocks(ETHERSTEEL_PLATED_ASH_TILES.get()))
                    .where('B', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.MECHANICAL_MINESHAFT))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .build())
            .model(createSeparateControllerCasingMachineModel(
                    GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/large_miner")))
            .register();

    public static void init() {}
}
