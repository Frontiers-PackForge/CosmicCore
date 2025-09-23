package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.machine.multiblock.steam.WeakSteamParallelMultiBlockMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_EMBER;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHT_DAWNSTONE_CASING;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;
import static com.rekindled.embers.RegistryManager.DAWNSTONE_ANVIL;

public class DawnForge {

    public final static MultiblockMachineDefinition DAWN_FORGE = REGISTRATE
            .multiblock("dawn_forge", WeakSteamParallelMultiBlockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.DAWN_FORGE)
            .appearanceBlock(LIGHT_DAWNSTONE_CASING)
            .partAppearance((controller, part, side) -> LIGHT_DAWNSTONE_CASING.getDefaultState())
            .recipeModifier(ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("ABBBA", "A   A", "AA AA", " BBB ", "     ")
                    .aisle("BAAAB", "     ", "A   A", "BABAB", "  B  ")
                    .aisle("BAAAB", "  D  ", "     ", "BBABB", " BBB ")
                    .aisle("BAAAB", "     ", "A   A", "BABAB", "  B  ")
                    .aisle("ABQBA", "A   A", "AA AA", " BBB ", "     ")
                    .where(' ', any())
                    .where("Q", controller(blocks(definition.getBlock())))
                    .where('A', blocks(CosmicBlocks.REINFORCED_DAWNSTONE_CASING.get()))
                    .where('B', blocks(CosmicBlocks.LIGHT_DAWNSTONE_CASING.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(IMPORT_EMBER).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('D', blocks(DAWNSTONE_ANVIL.get()))
                    //
                    .build())
            .workableCasingModel(CosmicCore.id("block/casings/solid/light_dawnstone_casing"),
                    CosmicCore.id("block/multiblock/dawnforge"))
            .register();

    public static void init() {}
}
