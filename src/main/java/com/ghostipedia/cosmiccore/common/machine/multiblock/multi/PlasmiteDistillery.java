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

public class PlasmiteDistillery {

    public final static MultiblockMachineDefinition PLASMITE_DISTILLERY = REGISTRATE
            .multiblock("plasmite_distillery",
                    WorkableElectricMultiblockMachine::new)
            .langValue("§9Plasmite Distillery")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.PLASMITE_FORGE)
            .appearanceBlock(HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING)
            .partAppearance((controller, part, side) -> HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING.getDefaultState())
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice(" AAAEEEEEEEEEEE", " AAAEFFFFFFFFFE", " AAAEFFFFFFFFFE", " AAAEFFFFFFFFFE", " AAAEEEEEEEEEEE", " AAA           ", " AAA           ", " AAA           ", " AAA           ", " AAA           ", " AAA           ", "               ")
                    .slice("AAAAAEEEEEEEEEA", "AABAA         A", "AABAAGGGGGGGGGA", "AABAA         A", "AABAAEFFFFFFFFA", "AABAAHHHHHHHHHA", "AABAA          ", "AABAA          ", "AABAA          ", "AABAA          ", "AABAA          ", " AAA           ")
                    .slice("AAAAAEEEEEEEEEA", "ABCBAGGGGGGGGGA", "ABCBACCCCCCCCCA", "ABCBAGGGGGGGGGA", "ABCBAEFFFFFFFFA", "ABCBA         A", "ABCBA          ", "ABCBA          ", "ABCBA          ", "ABCBA          ", "ABCBA          ", " AAA           ")
                    .slice("AAAAAEEEEEEEEEA", "AABAA         A", "AABAAGGGGGGGGGA", "AABAA         A", "AABAAEFFFFFFFFA", "AABAAHHHHHHHHHA", "AABAA          ", "AABAA          ", "AABAA          ", "AABAA          ", "AABAA          ", " AAA           ")
                    .slice(" AAAEEEEEEEEEEE", " AAAEFFFFFFFFFE", " AQAEFFFFFFFFFE", " AAAEFFFFFFFFFE", " AAAEEEEEEEEEEE", " AAA           ", " AAA           ", " AAA           ", " AAA           ", " AAA           ", " AAA           ", "               ")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('A', blocks(HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING.get())
                            .or(autoAbilitiesNoEnergyIn(CosmicRecipeTypes.PLASMITE_FORGE))
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(abilities(PartAbility.PARALLEL_HATCH).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('C', blocks(CASING_GRATE.get()))
                    .where('F', blocks(ZBLAN_REINFORCED_GLASS.get()))
                    .where('E', blocks(NAQUADAH_PRESSURE_RESISTANT_CASING.get()))
                    .where('G', blocks(FUSION_CASING_MK2.get()))
                    .where('H', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, CosmicMaterials.Trinavine)))
                    .where('D', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()).setMinGlobalLimited(28))
                    .build())
            // spotless:on
            .workableCasingModel(CosmicCore.id("block/casings/solid/highly_flexible_reinforced_trinavine_casing"),
                    GTCEu.id("block/multiblock/generator/large_gas_turbine"))
            .register();

    public static void init() {}
}
