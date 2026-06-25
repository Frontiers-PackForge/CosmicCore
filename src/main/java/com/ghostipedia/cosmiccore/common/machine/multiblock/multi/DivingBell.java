package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.DivingBellMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

public class DivingBell {

    public final static MultiblockMachineDefinition DIVING_BELL = REGISTRATE
            .multiblock("diving_bell", DivingBellMachine::new)
            .langValue("Diving Bell")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(REINFORCED_NAQUADRIA_CASING)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    // Front row (all vertical layers bottom to top)
                    .slice("CCC", "GGG", "GGG", "CCC", "CCC")
                    // Middle row (all vertical layers bottom to top)
                    .slice("CQC", "G G", "G G", "C C", "C C")
                    // Back row (all vertical layers bottom to top)
                    .slice("CCC", "GGG", "GGG", "CCC", "CCC")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('G', blocks(ZBLAN_REINFORCED_GLASS.get()))
                    .where('C', blocks(REINFORCED_NAQUADRIA_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/highly_flexible_reinforced_trinavine_casing"),
                    GTCEu.id("block/multiblock/generator/large_gas_turbine"))
            .register();

    public static void init() {}
}
