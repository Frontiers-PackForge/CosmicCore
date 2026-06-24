package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_HSSE_STURDY;

public class CHITIN {

    public final static MultiblockMachineDefinition CHITIN = REGISTRATE
            .multiblock("contained_habitat_for_insectoid_tectonic_integration_and_neutralization",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§bKryosyn Cracking Chamber")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.LARGE_ROASTER)
            .appearanceBlock(CASING_HSSE_STURDY)
            .partAppearance((controller, part, side) -> CASING_HSSE_STURDY.getDefaultState())
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start()
                    .slice(" AAAAA ", " ABBBA ", " AAAAA ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "       ")
                    .slice("ACCCCCA", "ACCCCCA", "ACCCCCA", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ")
                    .slice("ACCCCCA", "BCCCCCB", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", " CCCCC ")
                    .slice("ACCCCCA", "BCCCCCB", "ACCCCCA", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ")
                    .slice("ACCCCCA", "BCCCCCB", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", " CCCCC ")
                    .slice("ACCCCCA", "ACCCCCA", "ACCCCCA", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ")
                    .slice(" AAAAA ", " ABQBA ", " AAAAA ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "       ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(CosmicBlocks.SOUL_STAINED_STEEL_ALU_CASING.get()))
                    .where('B', blocks(CASING_HSSE_STURDY.get())
                            .or(autoAbilities(CosmicRecipeTypes.MANA_DIGITIZER,CosmicRecipeTypes.MANA_FLUIDIZER))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.PARALLEL_HATCH).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('A', blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()))
                    .build())
            // spotless:on
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_sturdy_hsse"),
                    GTCEu.id("block/overlay/machine/calx_reactor"))
            .register();

    public static void init() {}
}
