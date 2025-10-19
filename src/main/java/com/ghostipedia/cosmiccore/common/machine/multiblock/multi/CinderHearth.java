package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.RegressionPersistentWorkableElectricMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;

import com.rekindled.embers.RegistryManager;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.EXPORT_EMBER;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

public class CinderHearth {

    public final static MultiblockMachineDefinition CINDER_HEARTH = REGISTRATE
            .multiblock("cinder_hearth", RegressionPersistentWorkableElectricMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.CINDER_HEARTH)
            .appearanceBlock(RegistryManager.CAMINITE_TILES)
            .partAppearance((controller, part, side) -> RegistryManager.CAMINITE_TILES.get().defaultBlockState())
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAAAAAAAAA", "A   A    A", "A   A    A", "A   A    A", "AAAAAAAAAA", "          ",
                            "          ")
                    .aisle("ADDDAAAAAA", " D DCCCCC ", " D DCCCCC ", " D DCCCCC ", "AD DA    A", " D D      ",
                            " DDD      ")
                    .aisle("ADDDAAAAAA", "  F CCCCC ", "  F  CCCC ", "  F CCCCC ", "A F A    A", "  F       ",
                            " DDD      ")
                    .aisle("ADDDAAAAAA", " D DCBBBB ", " D DCCCCC ", " D DCBBBB ", "AD DA    A", " D D      ",
                            " DDD      ")
                    .aisle("AAQAA    A", "A   A    A", "A   A    A", "A   A    A", "AAAAA    A", "          ",
                            "          ")
                    .where(' ', any())
                    .where("Q", controller(blocks(definition.getBlock())))
                    .where('A', blocks(RegistryManager.ARCHAIC_LARGE_BRICKS.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(1)))
                    .where('B',
                            abilities(EXPORT_EMBER).setPreviewCount(8).or(blocks(RegistryManager.CAMINITE_TILES.get())))
                    .where('C', blocks(RegistryManager.CAMINITE_TILES.get()))
                    .where('D', blocks(CosmicBlocks.REINFORCED_DAWNSTONE_CASING.get()))
                    .where('F', blocks(RegistryManager.ARCHAIC_LIGHT.get()))
                    //
                    .build())
            .workableCasingModel(CosmicCore.id("block/embers/archaic_large_bricks"),
                    CosmicCore.id("block/multiblock/dawnforge"))
            .register();

    public static void init() {}
}
