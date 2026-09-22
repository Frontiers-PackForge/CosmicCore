package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates.autoAbilitiesNoEnergyIn;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.SOUL_STAINED_STEEL_ALU_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class EssenceReactor {

    public static final MultiblockMachineDefinition ESSENCE_REACTOR = REGISTRATE
            .multiblock("essence_reactor", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(SOUL_STAINED_STEEL_ALU_CASING)
            .partAppearance((controller, part, side) -> SOUL_STAINED_STEEL_ALU_CASING.getDefaultState())
            .recipeType(CosmicRecipeTypes.ESSENCE_REACTOR)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AAAAA", " ADA ", "     ", "     ", "     ", "     ", " AAA ", "     ")
                    .slice("AAAAA", " AAA ", "     ", " BBB ", " BBB ", " BBB ", " AAA ", "     ")
                    .slice("AAAAA", " AAA ", "     ", " BBB ", " B B ", " BBB ", " AAA ", " AAA ")
                    .slice("AAAAA", " AAA ", "     ", " BBB ", " BBB ", " BBB ", "     ", " AAA ")
                    .slice("AAAAA", " AAA ", "     ", "     ", "     ", "     ", " CCC ", " AAA ")
                    .slice(" AAA ", " AAA ", " AAA ", "     ", "     ", "     ", " AAA ", " AAA ")
                    .slice("     ", "     ", " AAA ", " AAA ", " AAA ", " AAA ", " AAA ", "     ")
                    .where(' ', any())
                    .where('D', controller(blocks(definition.getBlock())))
                    .where('A', blocks(SOUL_STAINED_STEEL_ALU_CASING.get())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.ESSENCE_REACTOR))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1)))
                    .where('B', blocks(CosmicBlocks.ETHERSTEEL_PLATED_ASH_TILES.get()))
                    .where('C', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                            CosmicMaterials.EnergeticAluminium)))
                    .build())
            .model(createWorkableCasingMachineModel(
                    CosmicCore.id("block/casings/solid/soul_stained_steel_aluminium_plated_casing"),
                    CosmicCore.id("block/multiblock/dawnforge")))
            .register();

    public static void init() {}
}
