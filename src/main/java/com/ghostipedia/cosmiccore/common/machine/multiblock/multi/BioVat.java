package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.CosmicPartAbility;
import com.ghostipedia.cosmiccore.client.renderer.machine.CosmicDynamicRenderHelpers;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

public class BioVat {

    public final static MultiblockMachineDefinition BIOVAT = REGISTRATE
            .multiblock("biovat", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(CosmicRecipeTypes.BIOVAT)
            .appearanceBlock(REINFORCED_NAQUADRIA_CASING)
            .pattern(definition -> MultiblockPatternBuilder.start()
                    .slice("AAAAA", "CCCCC", "CCCCC", "AAAAA")
                    .slice("AAAAA", "C   C", "C   C", "ADDDA")
                    .slice("AAAAA", "C   C", "C   C", "ADDDA")
                    .slice("AAAAA", "C   C", "C   C", "ADDDA")
                    .slice("AAQAA", "CCCCC", "CCCCC", "AAAAA")
                    .where(' ', any())
                    .where('Q', controller(blocks(definition.getBlock())))
                    .where('C', blocks(ZBLAN_REINFORCED_GLASS.get()))
                    .where('D', blocks(RADIOACTIVE_FILTER_CASING.get()))
                    .where('A', blocks(REINFORCED_NAQUADRIA_CASING.get())
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS, PartAbility.IMPORT_ITEMS))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1, 1))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1, 1))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.abilities(CosmicPartAbility.STERILIZE_HATCH).setExactLimit(1)))
                    .build())
            .model(
                    createWorkableCasingMachineModel(
                            CosmicCore.id("block/casings/solid/reinforced_naquadria_casing"),
                            GTCEu.id("block/multiblock/generator/large_gas_turbine"))
                            .andThen(d -> d.addDynamicRenderer(CosmicDynamicRenderHelpers::getBioVatRenderer)))
            .hasBER(true)
            .register();

    public static void init() {}
}
