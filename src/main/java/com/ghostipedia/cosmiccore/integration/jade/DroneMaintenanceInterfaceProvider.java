package com.ghostipedia.cosmiccore.integration.jade;

import com.ghostipedia.cosmiccore.api.machine.part.DroneMaintenanceInterfacePartMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum DroneMaintenanceInterfaceProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final String STATION = "CosmicCoreDroneMaintenanceStation";

    @Override
    public ResourceLocation getUid() {
        return CosmicCoreJadePlugin.DRONE_MAINTENANCE_INTERFACE;
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return getMachine(accessor) != null;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        DroneMaintenanceInterfacePartMachine maintenanceInterface = getMachine(accessor);
        if (maintenanceInterface != null) data.putLong(STATION, maintenanceInterface.getSyncedConnectionPos());
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (getMachine(accessor) == null) return;
        CompoundTag data = accessor.getServerData();
        if (!data.contains(STATION, Tag.TAG_LONG) || data.getLong(STATION) == -1) {
            tooltip.add(Component.translatable("cosmiccore.multiblock.drone_maintenance_interface.no_connection")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        BlockPos position = BlockPos.of(data.getLong(STATION));
        tooltip.add(Component.translatable("cosmiccore.multiblock.drone_maintenance_interface.connection_location",
                position.getX(), position.getY(), position.getZ()).withStyle(ChatFormatting.GREEN));
    }

    private static DroneMaintenanceInterfacePartMachine getMachine(BlockAccessor accessor) {
        MetaMachine machine = MetaMachine.getMachine(accessor.getLevel(), accessor.getPosition());
        return machine instanceof DroneMaintenanceInterfacePartMachine maintenanceInterface ?
                maintenanceInterface : null;
    }
}
