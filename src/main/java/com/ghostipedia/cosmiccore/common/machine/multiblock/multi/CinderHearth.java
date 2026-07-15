package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.RegressionPersistentWorkableElectricMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.EXPORT_EMBER;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

public class CinderHearth {

    public final static MultiblockMachineDefinition CINDER_HEARTH = REGISTRATE
            .multiblock("cinder_hearth", RegressionPersistentWorkableElectricMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.CINDER_HEARTH)
            .regressWhenWaiting(false)
            .appearanceBlock(CosmicBlocks.LIGHT_DAWNSTONE_CASING)
            .partAppearance((controller, part, side) -> CosmicBlocks.LIGHT_DAWNSTONE_CASING.get().defaultBlockState())
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("AAAAAAAAAA", "A   A    A", "A   A    A", "A   A    A", "AAAAAAAAAA", "          ",
                            "          ")
                    .slice("ADDDAAAAAA", " D DCCCCC ", " D DCCCCC ", " D DCCCCC ", "AD DA    A", " D D      ",
                            " DDD      ")
                    .slice("ADDDAAAAAA", "  F CCCCC ", "  F  CCCC ", "  F CCCCC ", "A F A    A", "  F       ",
                            " DDD      ")
                    .slice("ADDDAAAAAA", " D DCBBBB ", " D DCCCCC ", " D DCBBBB ", "AD DA    A", " D D      ",
                            " DDD      ")
                    .slice("AAQAA    A", "A   A    A", "A   A    A", "A   A    A", "AAAAA    A", "          ",
                            "          ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(CosmicBlocks.REINFORCED_DAWNSTONE_CASING.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(1)))
                    .where('B',
                            abilities(EXPORT_EMBER).setPreviewCount(8)
                                    .or(blocks(CosmicBlocks.LIGHT_DAWNSTONE_CASING.get())))
                    .where('C', blocks(CosmicBlocks.LIGHT_DAWNSTONE_CASING.get()))
                    .where('D', blocks(CosmicBlocks.REINFORCED_DAWNSTONE_CASING.get()))
                    .where('F', blocks(CosmicBlocks.LIGHT_DAWNSTONE_CASING.get()))
                    //
                    .build())
            .workableCasingModel(CosmicCore.id("block/embers/archaic_large_bricks"),
                    CosmicCore.id("block/multiblock/dawnforge"))
            .register();

    public static void init() {}
}
