package com.ghostipedia.cosmiccore.integration.jade;

import com.ghostipedia.cosmiccore.integration.jade.provider.DroneMaintenanceInterfaceProvider;
import com.ghostipedia.cosmiccore.integration.jade.provider.DroneStationProvider;
import com.ghostipedia.cosmiccore.integration.jade.provider.PCBParallelProvider;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class CCJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new DroneStationProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new DroneMaintenanceInterfaceProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new PCBParallelProvider(), BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new DroneStationProvider(), Block.class);
        registration.registerBlockComponent(new DroneMaintenanceInterfaceProvider(), Block.class);
        registration.registerBlockComponent(new PCBParallelProvider(), Block.class);
    }
}
