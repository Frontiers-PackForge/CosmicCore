package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_HSSE_STURDY;

public class KryosynCrackingChamber {

    public final static MultiblockMachineDefinition KRYOSYN_CRACKING_CHAMBER = REGISTRATE
            .multiblock("kryosyn_cracking_chamber",
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
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" AAAAA ", " ABBBA ", " AAAAA ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "       ")
                    .aisle("ACCCCCA", "ACCCCCA", "ACCCCCA", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ")
                    .aisle("ACCCCCA", "BCCCCCB", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", " CCCCC ")
                    .aisle("ACCCCCA", "BCCCCCB", "ACCCCCA", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ")
                    .aisle("ACCCCCA", "BCCCCCB", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", "ACCCCCA", " CCCCC ")
                    .aisle("ACCCCCA", "ACCCCCA", "ACCCCCA", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ", " CCCCC ")
                    .aisle(" AAAAA ", " ABQBA ", " AAAAA ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "  A A  ", "       ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(CosmicBlocks.SOUL_STAINED_STEEL_ALU_CASING.get()))
                    .where('B', blocks(CASING_HSSE_STURDY.get())
                            .or(autoAbilities(CosmicRecipeTypes.MANA_DIGITIZER,CosmicRecipeTypes.MANA_FLUIDIZER))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.PARALLEL_HATCH).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('C', blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()))
                    .build())
            // spotless:on
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_sturdy_hsse"),
                    GTCEu.id("block/overlay/machine/calx_reactor"))
            .register();

    public static void init() {}
}
