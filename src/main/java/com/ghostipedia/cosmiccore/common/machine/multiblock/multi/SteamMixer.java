package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
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
import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class SteamMixer {

    public static final MultiblockMachineDefinition STEAM_MIXER = REGISTRATE
            .multiblock("steam_mixing_vessel", WeakSteamParallelMultiBlockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(BRONZE_BRICKS_HULL)
            .recipeType(GTRecipeTypes.MIXER_RECIPES)
            .recipeModifier(WeakSteamParallelMultiBlockMachine::recipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 1)
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AAA", "BCB", "BCB", " B ")
                    .slice("AAA", "CEC", "CEC", "BXB")
                    .slice("ADA", "BCB", "BCB", " B ")
                    .where('D', Predicates.controller(blocks(definition.getBlock())))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .where('A', blocks(BRONZE_BRICKS_HULL.get())
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('B', blocks(CASING_BRONZE_BRICKS.get()))
                    .where('C', blocks(BRONZE_HULL.get()))
                    .where('E', blocks(CASING_BRONZE_GEARBOX.get()))
                    .where('X',
                            blocks(CASING_BRONZE_BRICKS.get())
                                    .or(Predicates.abilities(CosmicPartAbility.IMPORT_EMBER).setPreviewCount(1)))
                    .build())
            .model(createWorkableCasingMachineModel(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    CosmicCore.id("block/multiblock/mixing_vessel")))
            .register();

    public static void init() {}
}
