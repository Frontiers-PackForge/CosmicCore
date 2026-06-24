package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_EMBER;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIVING_ROCK_TILES;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;

public class ManaEtchingFactory {

    public final static MultiblockMachineDefinition MANA_ETCHING_FACTORY = REGISTRATE
            .multiblock("mana_etching_factory", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.MANA_ETCHING_FACTORY)
            .appearanceBlock(LIVING_ROCK_TILES)
            .partAppearance((controller, part, side) -> LIVING_ROCK_TILES.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK), GTRecipeModifiers.BATCH_MODE)
            .pattern(definition -> MultiblockPatternBuilder.start()
                    .slice(" AAAAA ", " CCCCC ", " CCCCC ", " DDDDD ")
                    .slice("AADDDAA", "D     D", "D     D", "DDDDDDD")
                    .slice("AADDDAA", "A     A", "A     A", "AADADAA")
                    .slice("AADDDAA", "D     D", "D     D", "DDDDDDD")
                    .slice(" AAQAA ", " CCCCC ", " CCCCC ", " DDDDD ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(LIVING_ROCK_TILES.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(IMPORT_EMBER).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(1)))
                    .where('C', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('D', blocks(CosmicBlocks.SOUL_STAINED_STEEL_ALU_CASING.get()))
                    .build())
            .workableCasingModel(CosmicCore.id("block/casings/solid/livingrock_tiles"),
                    CosmicCore.id("block/multiblock/dawnforge"))
            .register();

    public static void init() {}
}
