package com.ghostipedia.cosmiccore.common.data.datagen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.common.registry.GTRegistration;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import com.tterrag.registrate.providers.DataGenContext;

public class MEInterfaceModels extends GTBlockstateProvider {

    public MEInterfaceModels(PackOutput output, ExistingFileHelper existingFiles) {
        super(GTRegistration.REGISTRATE, output, existingFiles);
    }

    @Override
    protected void registerStatesAndModels() {
        for (var definition : new MachineDefinition[] {
                GTAEMachines.ITEM_IMPORT_BUS_ME, GTAEMachines.FLUID_IMPORT_HATCH_ME,
                GTAEMachines.ITEM_EXPORT_BUS_ME, GTAEMachines.FLUID_EXPORT_HATCH_ME,
                GTAEMachines.STOCKING_IMPORT_BUS_ME, GTAEMachines.STOCKING_IMPORT_HATCH_ME }) {
            var context = new DataGenContext<Block, Block>(definition::getBlock, definition.getName(),
                    definition.getId());
            String overlay = definition.getName().replace("me_stocking_input_", "me_input_");
            GTMachineModels.createMachineModel(GTMachineModels.createColorOverlayTieredHullMachineModel(
                    GTCEu.id("block/overlay/appeng/" + overlay), null, null)).accept(context, this);
        }
    }

    @Override
    public String getName() {
        return "CosmicCore ME interface models";
    }
}
