package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;

public class IndustChemVat {

    public final static MultiblockMachineDefinition INDUSTRIAL_CHEMPLANT = REGISTRATE
            .multiblock("industrial_chemical_vat", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeTypes(CosmicRecipeTypes.INDUSTRIAL_CHEMVAT, GTRecipeTypes.CRACKING_RECIPES)
            .recipeModifiers(CosmicRecipeModifiers::chemicalVatLogic, GTRecipeModifiers.PARALLEL_HATCH,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(CYCLOZINE_CHEMICALLY_REPELLING_CASING)
            // spotless:off
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("##QQQ##", "##QQQ##", "###Q###", "#######", "#######", "#######", "#######", "#######", "###Q###", "##QQQ##", "##QQQ##")
                    .slice("#QQQQQ#", "#QQSQQ#", "#FQQQF#", "#FQ#QF#", "#F###F#", "#F###F#", "#F###F#", "#FQ#QF#", "#FQQQF#", "#QQSQQ#", "#QQQQQ#")
                    .slice("QQQQQQQ", "QQSSSQQ", "#QSSSQ#", "##HGH##", "##HGH##", "##HGH##", "##HGH##", "#QHGHQ#", "#QSSSQ#", "QQSSSQQ", "QQQQQQQ")
                    .slice("QQQQQQQ", "QSSSSSQ", "QQSSSQQ", "##GSG##", "##GSG##", "##GSG##", "##GSG##", "##GSG##", "QQSSSQQ", "QSSSSSQ", "QQQQQQQ")
                    .slice("QQQQQQQ", "QQSSSQQ", "#QSSSQ#", "##HGH##", "##HGH##", "##HGH##", "##HGH##", "#QHGHQ#", "#QSSSQ#", "QQSSSQQ", "QQQQQQQ")
                    .slice("#QQQQQ#", "#QQSQQ#", "#FQQQF#", "#FQ#QF#", "#F###F#", "#F###F#", "#F###F#", "#FQ#QF#", "#FQQQF#", "#QQSQQ#", "#QQQQQ#")
                    .slice("##QQQ##", "##QCQ##", "###Q###", "#######", "#######", "#######", "#######", "#######", "###Q###", "##QQQ##", "##QQQ##")
                    .where('#', any())
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.NaquadahAlloy)))
                    .where('S', blocks(CosmicBlocks.COIL_RESONANT_VIRTUE_MELD.get()))
                    .where('H', blocks(CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_PIPE.get()))
                    .where('G', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where('Q', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS))
                            .or(abilities(PartAbility.EXPORT_FLUIDS))
                            .or(abilities(PartAbility.IMPORT_ITEMS))
                            .or(abilities(PartAbility.EXPORT_ITEMS))
                            .or(abilities(PartAbility.INPUT_ENERGY))
                            .or(abilities(PartAbility.MAINTENANCE))
                            .or(abilities(PartAbility.DATA_ACCESS))
                            .or(abilities(PartAbility.COMPUTATION_DATA_RECEPTION))
                            .or(abilities(PartAbility.OPTICAL_DATA_RECEPTION))
                            .or(abilities(PartAbility.PARALLEL_HATCH))
                            .or(abilities(CosmicPartAbility.COSMIC_PARALLEL_HATCH))
                            .or(abilities(PartAbility.INPUT_LASER))
                            .or(abilities(PartAbility.INPUT_ENERGY))
                            .or(Predicates.abilities(CosmicPartAbility.STERILIZE_HATCH)))
                    .build())
            // spotless:on
            .tooltips(Component.translatable("cosmiccore.multiblock.chemvat.tooltip.0"),
                    Component.translatable("cosmiccore.multiblock.chemvat.tooltip.1"),
                    Component.translatable("cosmiccore.multiblock.chemvat.tooltip.2"),
                    Component.translatable("cosmiccore.multiblock.chemvat.tooltip.3"),
                    Component.translatable("cosmiccore.multiblock.chemvat.tooltip.4"))
            .workableCasingModel(CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
                    CosmicCore.id("block/multiblock/vomahine_chemplant"))
            .register();

    public static void init() {}
}
