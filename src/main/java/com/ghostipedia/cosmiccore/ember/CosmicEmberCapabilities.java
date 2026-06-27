package com.ghostipedia.cosmiccore.ember;

import com.ghostipedia.cosmiccore.common.data.CosmicBlockEntities;
import com.ghostipedia.cosmiccore.common.data.CosmicMachines;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.EmberHatchPartMachine;
import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberEmitterBlockEntity;
import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberReceptorBlockEntity;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import com.rekindled.embers.api.capabilities.EmbersCapabilities;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class CosmicEmberCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {
        for (BlockEntityEntry<CosmicEmberEmitterBlockEntity> entry : CosmicBlockEntities.COSMIC_EMBER_EMITTER_BE
                .values()) {
            event.registerBlockEntity(EmbersCapabilities.EMBER_BLOCK_CAPABILITY, entry.get(),
                    (be, dir) -> be.capability);
        }
        for (BlockEntityEntry<CosmicEmberReceptorBlockEntity> entry : CosmicBlockEntities.COSMIC_EMBER_RECEIVER_BE
                .values()) {
            event.registerBlockEntity(EmbersCapabilities.EMBER_BLOCK_CAPABILITY, entry.get(),
                    (be, dir) -> be.capability);
        }
        registerHatches(event, CosmicMachines.EMBER_IMPORT_HATCH);
        registerHatches(event, CosmicMachines.EMBER_EXPORT_HATCH);
    }

    private static void registerHatches(RegisterCapabilitiesEvent event, MachineDefinition[] definitions) {
        for (MachineDefinition definition : definitions) {
            if (definition == null) continue;
            event.registerBlock(EmbersCapabilities.EMBER_BLOCK_CAPABILITY, (level, pos, state, blockEntity, side) -> {
                if (MetaMachine.getMachine(level, pos) instanceof EmberHatchPartMachine hatch) {
                    return new IOGatedEmberCapability(hatch.emberContainer.capability,
                            hatch.emberContainer.getHandlerIO());
                }
                return null;
            }, definition.getBlock());
        }
    }
}
