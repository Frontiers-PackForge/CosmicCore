package com.ghostipedia.cosmiccore.integration.jade;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(CosmicCore.MOD_ID)
public class CosmicCoreJadePlugin implements IWailaPlugin {

    public static final ResourceLocation EMBER_DETAILS = CosmicCore.id("ember_details");
    public static final ResourceLocation ME_COMPUTATION_ARRAY_DETAILS = CosmicCore.id("me_computation_array_details");
    public static final ResourceLocation POWER_GRID_TELEMETRY = CosmicCore.id("power_grid_telemetry");
    public static final ResourceLocation MODULAR_POWER_STATION_MODE = CosmicCore.id("modular_power_station_mode");
    public static final ResourceLocation DRONE_STATION = CosmicCore.id("drone_station");
    public static final ResourceLocation DRONE_MAINTENANCE_INTERFACE = CosmicCore.id("drone_maintenance_interface");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(RainSealantProvider.INSTANCE, Block.class);
        registration.registerBlockDataProvider(CosmicEmberProvider.INSTANCE, Block.class);
        registration.registerBlockDataProvider(MEComputationArrayProvider.INSTANCE, Block.class);
        registration.registerBlockDataProvider(ModularPowerStationModeProvider.INSTANCE, Block.class);
        registration.registerBlockDataProvider(PowerGridMachineProvider.INSTANCE, MetaMachine.class);
        registration.registerBlockDataProvider(DroneStationProvider.INSTANCE, MetaMachine.class);
        registration.registerBlockDataProvider(DroneMaintenanceInterfaceProvider.INSTANCE, MetaMachine.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(RainSealantProvider.INSTANCE, MetaMachineBlock.class);
        registration.registerBlockComponent(CosmicEmberProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(MEComputationArrayProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(ModularPowerStationModeProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(PowerGridMachineProvider.INSTANCE, MetaMachineBlock.class);
        registration.registerBlockComponent(DroneStationProvider.INSTANCE, MetaMachineBlock.class);
        registration.registerBlockComponent(DroneMaintenanceInterfaceProvider.INSTANCE, MetaMachineBlock.class);
    }
}
