package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.WEAR_RESISTANT_RURIDIT_CASING;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_STRESS_PROOF;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;

public class LargeSpoolingMachine {

    public final static MultiblockMachineDefinition LARGE_SPOOLING_MACHINE = REGISTRATE
            .multiblock("large_spooling_machine", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.SPOOLING_MACHINE)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK), GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(WEAR_RESISTANT_RURIDIT_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("  AAAA", "     F", "  DDDF", "     F", "     F", "     F", "     F", "  AAAA", "      ")
                    .aisle("  AAAA", "   C  ", "  DC  ", "   C  ", "   C  ", "   C  ", "   C  ", "  AAAA", "      ")
                    .aisle("AAAAAA", "A   A ", "A E A ", "A   A ", "A   A ", "A   A ", "A   A ", "AAAAAA", " AAA  ")
                    .aisle("AAAAA ", "B   B ", "B   B ", "B   B ", "B   B ", "B   B ", "B   B ", "A   A ", "AAAAA ")
                    .aisle("AACAA ", "B D B ", "B D B ", "B D B ", "B D B ", "B D B ", "B D B ", "B D B ", "AACAA ")
                    .aisle("AAAAA ", "B   B ", "B   B ", "B   B ", "B   B ", "B   B ", "B   B ", "A   A ", "AAAAA ")
                    .aisle("AAQAA ", "ABBBA ", "ABBBA ", "ABBBA ", "ABBBA ", "ABBBA ", "ABBBA ", "AABAA ", " AAA  ")
                    .where(' ', any())
                    .where("Q", controller(blocks(definition.getBlock())))
                    .where('A', blocks(WEAR_RESISTANT_RURIDIT_CASING.get()).setMinGlobalLimited(85, 90)
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1)))
                    .where('B', blocks(CASING_LAMINATED_GLASS.get()))
                    .where('C', blocks(CASING_TUNGSTENSTEEL_GEARBOX.get()))
                    .where('D', blocks(CASING_STRESS_PROOF.get()))
                    .where('E', blocks(CASING_STEEL_GEARBOX.get()))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Iridium)))

                    .build())
            .workableCasingModel(CosmicCore.id("block/casings/solid/ruridit_casing"),
                    GTCEu.id("block/multiblock/generator/large_gas_turbine"))
            .register();

    public static void init() {}
}
