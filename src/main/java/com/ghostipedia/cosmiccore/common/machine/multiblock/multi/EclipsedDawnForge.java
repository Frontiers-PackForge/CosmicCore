package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import com.rekindled.embers.RegistryManager;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_EMBER;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.ETHERSTEEL_PLATED_ASH_TILES;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.IMPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;
import static com.rekindled.embers.RegistryManager.DAWNSTONE_ANVIL;

public class EclipsedDawnForge {

    public final static MultiblockMachineDefinition ECLIPSED_DAWNFORGE = REGISTRATE
            .multiblock("dawnforge_eclipsed", WorkableElectricMultiblockMachine::new)
            .langValue("Dawnforge [Eclipsed]")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.DAWNFORGE_ECLIPSED)
            .appearanceBlock(ETHERSTEEL_PLATED_ASH_TILES)
            .partAppearance((controller, part, side) -> ETHERSTEEL_PLATED_ASH_TILES.getDefaultState())
            .recipeModifiers(CosmicRecipeModifiers::innateParallel4x,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
            .pattern(definition -> FactoryBlockPattern.start()
                    // spotless:off
                    .aisle("  AAAAA  ", "   AAA   ", "         ", "         ", "         ", "         ", "   AAA   ", "         ")
                    .aisle(" AAAAAAA ", " AACCCAA ", " AA   AA ", " A     A ", "         ", "         ", " AAAAAAA ", "   AAA   ")
                    .aisle("AAAAAAAAA", " ACCCCCA ", " AD   DA ", "  D   D  ", "  D   D  ", "  D   D  ", " ADA ADA ", "  AAAAA  ")
                    .aisle("AAAAAAAAA", "ACCCCCCCA", "   CCC   ", "         ", "         ", "         ", "AAA   AAA", " AAAEAAA ")
                    .aisle("AAAAAAAAA", "ACCCCCCCA", "   CCC   ", "    F    ", "         ", "         ", "AA     AA", " AAEEEAA ")
                    .aisle("AAAAAAAAA", "ACCCCCCCA", "   CCC   ", "         ", "         ", "         ", "AAA   AAA", " AAAEAAA ")
                    .aisle("AAAAAAAAA", " ACCCCCA ", " AD   DA ", "  D   D  ", "  D   D  ", "  D   D  ", " ADA ADA ", "  AAAAA  ")
                    .aisle(" AAAAAAA ", " AACCCAA ", " AA   AA ", " A     A ", "         ", "         ", " AAAAAAA ", "   AAA   ")
                    .aisle("  AAAAA  ", "   AQA   ", "         ", "         ", "         ", "         ", "   AAA   ", "         ")
                    // spotless:on
                    .where(' ', any())
                    .where("Q", controller(blocks(definition.getBlock())))
                    .where('A', blocks(ETHERSTEEL_PLATED_ASH_TILES.get())
                            .or(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(EXPORT_FLUIDS).setPreviewCount(1))
                            .or(abilities(IMPORT_EMBER).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(1)))
                    .where('C', blocks(RegistryManager.CAMINITE_TILES.get()))
                    .where('D', blocks(GTBlocks.CASING_PTFE_INERT.get()))
                    .where('E', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where('F', blocks(DAWNSTONE_ANVIL.get()))

                    .build())
            .workableCasingModel(CosmicCore.id("block/casings/solid/ethersteel_plated_ash_tiles"),
                    CosmicCore.id("block/multiblock/dawnforge"))
            .register();

    public static void init() {}
}
