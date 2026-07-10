package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import static com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates.autoAbilitiesNoEnergyIn;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

// NOTE DO NOT ADD BERS/RENDERS TO THIS YET

public class VoidMiner {

    public final static MultiblockMachineDefinition VOID_MINER = REGISTRATE
            .multiblock("void_miner",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§cVoid Miner")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.VOID_MINER)
            .appearanceBlock(HIGH_TOLERANCE_RHENIUM_CASING)
            .partAppearance((controller, part, side) -> HIGH_TOLERANCE_RHENIUM_CASING.getDefaultState())
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("  A     A  ", "  A     A  ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .slice(" A B   B A ", " AAB   B A ", " AAB   BAA ", "  A     A  ", "  A     A  ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .slice("A         A", "AACCAAACC A", " AB     BA ", " AB     BA ", " AB     BA ", "  B     B  ", "  B     B  ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .slice(" B A   A B ", " BCA   ACB ", " B AAAAA B ", "    A A    ", "    A A    ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .slice("           ", "  A     A  ", "   ACCCA   ", "   ACDCA   ", "   ACDCA   ", "    CDC    ", "    CDC    ", "    CDC    ", "    A A    ", "           ", "           ", "           ", "           ")
                    .slice("           ", "  A     A  ", "   ACCCA   ", "    DBD    ", "    DBD    ", "    DBD    ", "    DBD    ", "    DBD    ", "     B     ", "     B     ", "     B     ", "     B     ", "     B     ")
                    .slice("           ", "  A     A  ", "   ACCCA   ", "   ACDCA   ", "   ACDCA   ", "    CDC    ", "    CDC    ", "    CDC    ", "    A A    ", "           ", "           ", "           ", "           ")
                    .slice(" B A   A B ", " BCA   ACB ", " B AAAAA B ", "    A A    ", "    A A    ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .slice("A         A", "AACCAQACCAA", " AB     BA ", " AB     BA ", " AB     BA ", "  B     B  ", "  B     B  ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .slice(" A B   B A ", " AAB   BAA ", " AAB   BAA ", "  A     A  ", "  A     A  ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .slice("  A     A  ", "  A     A  ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(HIGH_TOLERANCE_RHENIUM_CASING.get()).setMinGlobalLimited(105)
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.VOID_MINER))
                            .or(abilities(PartAbility.IMPORT_FLUIDS,PartAbility.IMPORT_FLUIDS_4X,PartAbility.IMPORT_FLUIDS_9X))
                            .or(abilities(PartAbility.EXPORT_ITEMS))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                    )
                    .where('B', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, CosmicMaterials.Trinavine)))   //.setMinGlobalLimited(28)
                    .where('C', blocks(HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING.get()))
                    .where('D', blocks(COMPUTER_CASING.get()))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/high_tolerance_rhenium_casing"),
                    GTCEu.id("block/multiblock/generator/large_gas_turbine"))
            .register();

    public static void init() {}
}
