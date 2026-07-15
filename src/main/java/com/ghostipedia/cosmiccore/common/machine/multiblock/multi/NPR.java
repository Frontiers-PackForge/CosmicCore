package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.MagneticFieldMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates.magnetCoils;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.FUSION_GLASS;

public class NPR {

    public final static MultiblockMachineDefinition NAQUAHINE_PRESSURE_REACTOR = REGISTRATE
            .multiblock("naquahine_pressure_reactor", MagneticFieldMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.NAQUAHINE_REACTOR)
            .regressWhenWaiting(false)
            .recipeModifier(CosmicRecipeModifiers::vomahineReactorOC)
            .appearanceBlock(CosmicBlocks.NAQUADAH_PRESSURE_RESISTANT_CASING)
            .generator(true)
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("##QQQ##", "##QQQ##", "###Q###", "#######", "#######", "#######", "#######", "#######",
                            "#######", "#######", "###Q###", "##QQQ##", "##QQQ##")
                    .slice("#QQQQQ#", "#QQSQQ#", "#FQQQF#", "#FQFQF#", "#F###F#", "#F###F#", "#F###F#", "#F###F#",
                            "#F###F#", "#FQFQF#", "#FQQQF#", "#QQSQQ#", "#QQQQQ#")
                    .slice("QQQQQQQ", "QQSSSQQ", "#QSSSQ#", "#QHGHQ#", "##HGH##", "##HGH##", "##HGH##", "##HGH##",
                            "##HGH##", "#QHGHQ#", "#QSSSQ#", "QQSSSQQ", "QQQQQQQ")
                    .slice("QQQQQQQ", "QSSSSSQ", "QQSSSQQ", "#FGSGF#", "##GSG##", "##GSG##", "##GSG##", "##GSG##",
                            "##GSG##", "#FGSGF#", "QQSSSQQ", "QSSSSSQ", "QQQQQQQ")
                    .slice("QQQQQQQ", "QQSSSQQ", "#QSSSQ#", "#QHGHQ#", "##HGH##", "##HGH##", "##HGH##", "##HGH##",
                            "##HGH##", "#QHGHQ#", "#QSSSQ#", "QQSSSQQ", "QQQQQQQ")
                    .slice("#QQQQQ#", "#QQSQQ#", "#FQQQF#", "#FQFQF#", "#F###F#", "#F###F#", "#F###F#", "#F###F#",
                            "#F###F#", "#FQFQF#", "#FQQQF#", "#QQSQQ#", "#QQQQQ#")
                    .slice("##QQQ##", "##QCQ##", "###Q###", "#######", "#######", "#######", "#######", "#######",
                            "#######", "#######", "###Q###", "##QQQ##", "##QQQ##")
                    .where('#', any())
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.NaquadahAlloy)))
                    .where('S', magnetCoils())
                    .where('H', blocks(CosmicBlocks.RESONANTLY_TUNED_VIRTUE_MELD_CASING.get()))
                    .where('G', blocks(FUSION_GLASS.get()))
                    .where('Q', blocks(CosmicBlocks.NAQUADAH_PRESSURE_RESISTANT_CASING.get()).setMinGlobalLimited(160)
                            .or(abilities(PartAbility.IMPORT_FLUIDS))
                            .or(abilities(PartAbility.IMPORT_ITEMS))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(abilities(PartAbility.OUTPUT_LASER).setExactLimit(1))
                            .or(abilities(PartAbility.INPUT_LASER).setExactLimit(1)))
                    .build())
            // Note, Never allow energy hatches, it breaks them pretty badly and i think this is the easier of the two
            // sacrifices for now - G
            .tooltips(Component.translatable("cosmiccore.multiblock.naqreactor.tooltip.0"),
                    Component.translatable("cosmiccore.multiblock.naqreactor.tooltip.1"),
                    Component.translatable("cosmiccore.multiblock.naqreactor.tooltip.2"))
            .workableCasingModel(CosmicCore.id("block/casings/solid/naquadah_pressure_resistant_casing"),
                    GTCEu.id("block/multiblock/hpca"))
            .register();

    public static void init() {}
}
