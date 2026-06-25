package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.steam.WeakSteamParallelMultiBlockMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_BRONZE_PIPE;

public class SteamCaster {

    public static final MultiblockMachineDefinition STEAM_CASTER = REGISTRATE
            .multiblock("steam_caster", WeakSteamParallelMultiBlockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(BRONZE_HULL)
            .recipeType(GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES)
            .recipeModifier(WeakSteamParallelMultiBlockMachine::recipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 1)
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AAAA", "ABBA", "AAAA")
                    .slice("AAAA", "BCCB", "AAAA")
                    .slice("AAAA", "ADBA", "AAAA")
                    .where('D', Predicates.controller(blocks(definition.getBlock())))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .where('A', blocks(CASING_BRONZE_BRICKS.get()))
                    .where('B', blocks(CASING_COKE_BRICKS.get())
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('C', blocks(CASING_BRONZE_PIPE.get()))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_coke_bricks"),
                    CosmicCore.id("block/multiblock/solidifier"))
            .register();

    public static void init() {}
}
