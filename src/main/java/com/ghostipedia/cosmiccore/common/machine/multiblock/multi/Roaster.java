package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.ULTRA_POWERED_CASING;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_INVAR_HEATPROOF;

public class Roaster {

    public final static MultiblockMachineDefinition LARGE_ROASTER = REGISTRATE
            .multiblock("large_roaster",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§cLarge Roaster")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.LARGE_ROASTER)
            .appearanceBlock(CASING_INVAR_HEATPROOF)
            .partAppearance((controller, part, side) -> CASING_INVAR_HEATPROOF.getDefaultState())
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("A A", "ABA", "BBB", "BBB", " B ")
                    .aisle(" B ", "BBB", "B B", "B B", "B B")
                    .aisle("A A", "ABA", "BQB", "BBB", " B ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', frames(GTMaterials.BlackSteel))
                    .where('B', blocks(CASING_INVAR_HEATPROOF.get())
                            .or(autoAbilities(CosmicRecipeTypes.LARGE_ROASTER))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2,2))
                            .or(abilities(CosmicPartAbility.IMPORT_EMBER).setMaxGlobalLimited(1,1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .build())
            // spotless:on
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_heatproof"),
                    GTCEu.id("block/overlay/machine/roaster"))
            .register();

    public static void init() {}
}
