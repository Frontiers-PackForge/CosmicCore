package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.modular.CoilWorkableElectricModularMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.CosmicMachine2;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular.orbitalForge.OrbitalForgeModularMultiblockMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;

//spotless: off
public class OrbitalForgeModularMachine {

    public final static MultiblockMachineDefinition ORBITAL_TEMPERING_FORGE = REGISTRATE.multiblock(
                    "orbital_tempering_forge", OrbitalForgeModularMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(CosmicRecipeTypes.ORBITAL_FORGE)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, CosmicRecipeModifiers::modularEbfOverclock)
            .appearanceBlock(CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("##QQQ##", "##QQQ##", "###Q###", "#######", "#######", "#######", "#######", "#######", "###Q###", "##QQQ##", "##QQQ##")
                    .aisle("#QQQQQ#", "#QQSQQ#", "#FQQQF#", "#FQ#QF#", "#F###F#", "#F###F#", "#F###F#", "#FQ#QF#", "#FQQQF#", "#QQSQQ#", "#QQQQQ#")
                    .aisle("QQQQQQQ", "QQSSSQQ", "#QSSSQ#", "##HGH##", "##HGH##", "##HGH##", "##HGH##", "#QHGHQ#", "#QSSSQ#", "QQSSSQQ", "QQQQQQQ")
                    .aisle("QQQQQQQ", "QSSSSSQ", "QQSSSQQ", "##GSG##", "##GSG##", "##GSG##", "##GSG##", "##GSG##", "QQSSSQQ", "QSSSSSQ", "QQQIQQQ")
                    .aisle("QQQQQQQ", "QQSSSQQ", "#QSSSQ#", "##HGH##", "##HGH##", "##HGH##", "##HGH##", "#QHGHQ#", "#QSSSQ#", "QQSSSQQ", "QQQQQQQ")
                    .aisle("#QQQQQ#", "#QQSQQ#", "#FQQQF#", "#FQ#QF#", "#F###F#", "#F###F#", "#F###F#", "#FQ#QF#", "#FQQQF#", "#QQSQQ#", "#QQQQQ#")
                    .aisle("##QQQ##", "##QCQ##", "###Q###", "#######", "#######", "#######", "#######", "#######", "###Q###", "##QQQ##", "##QQQ##")
                    .where('#', any())
                    .where("C", controller(blocks(definition.getBlock())))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.NaquadahAlloy)))
                    .where('S', heatingCoils())
                    .where('H', blocks(CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_PIPE.get()))
                    .where('G', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where('I',blocks(CosmicMachine2.MODULE_CONNECTOR.get()))
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
                            .or(abilities(PartAbility.INPUT_ENERGY)))
                    .build())
            .workableCasingRenderer(CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
                    CosmicCore.id("block/multiblock/vomahine_chemplant"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricModularMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                            Component
                                    .translatable(
                                            FormattingUtil
                                                    .formatNumbers(coilMachine.getCoilType().getCoilTemperature() +
                                                            100L * Math.max(0, coilMachine.getTier() - GTValues.MV)) +
                                                    "K")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
            })
            .register();
    public static void init() {}
}
