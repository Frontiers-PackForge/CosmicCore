package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
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
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.world.level.block.Blocks;

import static com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility.IMPORT_SOUL;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.abilities;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;

public class DrygmyGrove {

    public final static MultiblockMachineDefinition DRYGMY_GROVE = REGISTRATE
            .multiblock("drygmy_grove", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.GROVE_RECIPES)
            .recipeModifiers(CosmicRecipeModifiers::groveMulti,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK), GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("##QQQ##", "##QQQ##", "#######", "#######", "#######", "##QQQ##", "##QQQ##")
                    .slice("#QQQQQ#", "#QMMMQ#", "#FLBBF#", "#F#B#F#", "#F###F#", "#QGGGQ#", "#QQQQQ#")
                    .slice("QQQQQQQ", "QMMMMMQ", "#B#####", "#B#####", "#######", "QGP#PGQ", "QQQQQQQ")
                    .slice("QQQQQQQ", "QMMMMMQ", "#B###B#", "#######", "#######", "QG###GQ", "QQQQQQQ")
                    .slice("QQQQQQQ", "QMMMMMQ", "####LB#", "#####B#", "#######", "QGP#PGQ", "QQQQQQQ")
                    .slice("#QQQQQ#", "#QMMMQ#", "#F#BBF#", "#F##BF#", "#F###F#", "#QGGGQ#", "#QQQQQ#")
                    .slice("##QQQ##", "##QCQ##", "#######", "#######", "#######", "##QQQ##", "##QQQ##")
                    .where('#', any())
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                    .where('M', blocks(Blocks.MOSS_BLOCK))
                    .where('B', blocks(Blocks.AZALEA_LEAVES)
                            .or(blocks(Blocks.FLOWERING_AZALEA_LEAVES)))
                    .where('L', blocks(Blocks.FLOWERING_AZALEA)
                            .or(blocks(Blocks.AZALEA)))
                    .where('P', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                    .where('G', blocks(Blocks.SEA_LANTERN)) // WHAT THE HELL IS A LAMP BRO - HALP
                    .where('Q', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS))
                            .or(abilities(PartAbility.EXPORT_FLUIDS))
                            .or(abilities(PartAbility.IMPORT_ITEMS))
                            .or(abilities(PartAbility.EXPORT_ITEMS))
                            .or(abilities(PartAbility.INPUT_ENERGY))
                            .or(abilities(PartAbility.MAINTENANCE))
                            .or(abilities(IMPORT_SOUL)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/data_bank"))
            .register();

    public static void init() {}
}
