package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.modules;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.CosmicMachine2;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicRecipeModifiers;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular.orbitalForge.OrbitalForgeModularMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular.orbitalForge.OrbitalForgeModule;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.HEAT_VENT;

public class OrbitalForgeEBFModule {

    public final static MultiblockMachineDefinition ORBITAL_TEMPERING_FORGE_EBF_MODULE = REGISTRATE.multiblock(
            "orbital_tempering_forge_ebf_module", OrbitalForgeModule::new)
            .rotationState(RotationState.ALL)
            .recipeType(CosmicRecipeTypes.ORBITAL_FORGE)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                    CosmicRecipeModifiers::ebfModuleOverclock)
            .appearanceBlock(CosmicBlocks.CYCLOZINE_CHEMICALLY_REPELLING_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAA", "BBB")
                    .aisle("ACA", "BDB")
                    .aisle("AAA", "BBB")
                    .where("D", controller(blocks(definition.getBlock())))
                    .where('C', blocks(CosmicMachine2.MODULE_CONNECTOR.get()))
                    .where('A', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get()))
                    .where('B', blocks(CYCLOZINE_CHEMICALLY_REPELLING_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS))
                            .or(abilities(PartAbility.EXPORT_FLUIDS))
                            .or(abilities(PartAbility.IMPORT_ITEMS))
                            .or(abilities(PartAbility.EXPORT_ITEMS))
                            .or(abilities(PartAbility.INPUT_ENERGY))
                            .or(abilities(PartAbility.COMPUTATION_DATA_RECEPTION).setMaxGlobalLimited(1, 1))
                            .or(abilities(PartAbility.DATA_ACCESS, PartAbility.OPTICAL_DATA_RECEPTION)
                                    .setMaxGlobalLimited(1, 1))
                            .or(abilities(PartAbility.PARALLEL_HATCH, CosmicPartAbility.COSMIC_PARALLEL_HATCH)
                                    .setExactLimit(1)))
                    .build())
            .workableCasingRenderer(CosmicCore.id("block/casings/solid/vomahine_certified_chemically_resistant_casing"),
                    CosmicCore.id("block/multiblock/vomahine_chemplant"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof OrbitalForgeModule coilMachine && controller.isFormed()) {
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
