package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.RegressionPersistentWorkableElectricMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;

import com.rekindled.embers.RegistryManager;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_EMBER;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.LIGHT_DAWNSTONE_CASING;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.IMPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;

public class PyrothermicRefinery {

    public final static MultiblockMachineDefinition PYROTHERMIC_REFINDERY = REGISTRATE
            .multiblock("pyrothermic_refinery", RegressionPersistentWorkableElectricMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.PYROTHERMIC_REFINERY)
            .appearanceBlock(LIGHT_DAWNSTONE_CASING)
            .partAppearance((controller, part, side) -> LIGHT_DAWNSTONE_CASING.getDefaultState())
            .recipeModifiers(CosmicRecipeModifiers::innateParallel4x,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
            .pattern(definition -> FactoryBlockPattern.start()
                    // spotless:off
                    .aisle("     AAAAA     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                    .aisle("   AABBBBBAA   ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                    .aisle(" AABBBBBBBBBAA ", "      DDD      ", "      DDD      ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                    .aisle(" ABBBBBBBBBBBA ", "     D   D     ", "     D   D     ", "     DDDDD     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                    .aisle("ABBBBBBBBBBBBBA", "   AD     DA   ", "   AD     DA   ", "   A DEEED A   ", "   AAA E AAA   ", "     A E A     ", "     A E A     ", "     A E A     ", "     A E A     ", "     A E A     ", "     A E A     ", "     A E A     ", "     AAAAA     ")
                    .aisle("ABBBBBBBBBBBBBA", " C  D     D  C ", " C  D     D  C ", " C   DEEED   C ", " C    FEF    C ", " C    FEF    C ", " C    FEFCCCCC ", " C    FEF      ", " C    FEF      ", " C    FEF      ", " CCCCCFEF      ", "      FEF      ", "     A   A     ")
                    .aisle("ABBBBBBBBBBBBBA", "   AD     DA   ", "   AD     DA   ", "   A DEEED A   ", "   AAA E AAA   ", "     A E A     ", "     A E A     ", "     A E A     ", "     A E A     ", "     A E A     ", "     A E A     ", "     A E A     ", "     AAAAA     ")
                    .aisle(" ABBBBBBBBBBBA ", "     D   D     ", "     D   D     ", "     DDDDD     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                    .aisle(" AABBBBBBBBBAA ", "      DQD      ", "      DDD      ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                    .aisle("   AABBBBBAA   ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                    .aisle("     AAAAA     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                    // spotless:on
                    .where(' ', any())
                    .where("Q", controller(blocks(definition.getBlock())))
                    .where('A', blocks(RegistryManager.ARCHAIC_LARGE_BRICKS.get()))
                    .where('B', blocks(RegistryManager.CAMINITE_TILES.get()))
                    .where('C', blocks(CosmicBlocks.STEEL_PLATED_BRONZE.get()))
                    .where('D', blocks(CosmicBlocks.LIGHT_DAWNSTONE_CASING.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(IMPORT_EMBER).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(1)))
                    .where('E', blocks(CosmicBlocks.LIGHT_DAWNSTONE_CASING.get()))
                    .where('F', abilities(IMPORT_FLUIDS))

                    .build())
            .workableCasingModel(CosmicCore.id("block/casings/solid/light_dawnstone_casing"),
                    CosmicCore.id("block/multiblock/dawnforge"))
            .register();

    public static void init() {}
}
