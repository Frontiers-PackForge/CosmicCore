package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.world.level.block.Blocks;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_CORROSION_PROOF;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_WATERTIGHT;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;

public class ChromaticFlotationPlant {

    public final static MultiblockMachineDefinition CHROMATIC_FLOTATION_PLANT = REGISTRATE
            .multiblock("chromatic_flotation_plant", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.CHROMATIC_FLOTATION_PLANT)
            .recipeModifiers(ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK),
                    GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GCYMBlocks.CASING_WATERTIGHT)
            .pattern(definition -> MultiblockPatternBuilder.start()
                    .slice("AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA")
                    .slice("AAAAAAA", "ABCBCBA", "ABBBBBA", "ABBBBBA", "ABBBBBA")
                    .slice("AAAAAAA", "ABCBCBA", "ABBBBBA", "ABBBBBA", "ABBBBBA")
                    .slice("AAAAAAA", "ABCBCBA", "ABBBBBA", "ABBBBBA", "ABBBBBA")
                    .slice("AAAAAAA", "ABCBCBA", "ABBBBBA", "ABBBBBA", "ABBBBBA")
                    .slice("AAAAAAA", "AACACAA", "AAAAAAA", "AAAAAAA", "AAAAAAA")
                    .slice("       ", "  C C  ", "       ", "       ", "       ")
                    .slice(" DDDDD ", " DCDCD ", " DDDDD ", "       ", "       ")
                    .slice(" DDDDD ", " DEEED ", " DDDDD ", "       ", "       ")
                    .slice(" DDDDD ", " DEEED ", " DDDDD ", "       ", "       ")
                    .slice(" DDDDD ", " DDFDD ", " DDDDD ", "       ", "       ")
                    .slice("       ", "       ", "       ", "       ", "       ")
                    .where(' ', any())
                    .where('F', controller(blocks(definition.getBlock())))
                    .where('C', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('A', blocks(CASING_CORROSION_PROOF.get()))
                    .where('E', blocks(CASING_STEEL_SOLID.get()))
                    .where('B', blocks(Blocks.WATER))
                    .where('D', blocks(CASING_WATERTIGHT.get())
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setExactLimit(1)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/generator/large_gas_turbine"))
            .register();

    public static void init() {}
}
