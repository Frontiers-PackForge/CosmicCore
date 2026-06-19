package com.ghostipedia.cosmiccore.integration.jade.provider;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.part.DroneMaintenanceInterfacePartMachine;
import com.ghostipedia.cosmiccore.api.misc.DroneStationConnection;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class DroneMaintenanceInterfaceProvider extends CapabilityBlockProvider<DroneMaintenanceInterfacePartMachine> {

    public DroneMaintenanceInterfaceProvider() {
        super(CosmicCore.id("drone_maintenance_interface"));
    }

    @Nullable
    @Override
    protected DroneMaintenanceInterfacePartMachine getCapability(Level level, BlockPos blockPos,
                                                                 @Nullable Direction direction) {
        if (MetaMachine.getMachine(level,
                blockPos) instanceof DroneMaintenanceInterfacePartMachine droneMaintenanceInterface) {
            return droneMaintenanceInterface;
        }
        return null;
    }

    @Override
    protected void write(CompoundTag compoundTag, DroneMaintenanceInterfacePartMachine droneInterface) {
        boolean isConnected = droneInterface.hasConnection();
        compoundTag.putBoolean("isConnected", isConnected);
        if (isConnected) {
            DroneStationConnection connection = droneInterface.getConnection();
            compoundTag.putLong("connectionX", connection.droneStationPos.getX());
            compoundTag.putLong("connectionY", connection.droneStationPos.getY());
            compoundTag.putLong("connectionZ", connection.droneStationPos.getZ());
        }
    }

    @Override
    protected void addTooltip(CompoundTag compoundTag, ITooltip iTooltip, Player player, BlockAccessor blockAccessor,
                              BlockEntity blockEntity, IPluginConfig iPluginConfig) {
        if (compoundTag.contains("isConnected", Tag.TAG_BYTE)) {
            if (compoundTag.getBoolean("isConnected")) {
                long xPos = compoundTag.getLong("connectionX");
                long yPos = compoundTag.getLong("connectionY");
                long zPos = compoundTag.getLong("connectionZ");
                iTooltip.add(Component
                        .translatable("cosmiccore.multiblock.drone_maintenance_interface.connection_location",
                                xPos,
                                yPos,
                                zPos)
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));

            } else {
                iTooltip.add(Component.translatable("cosmiccore.multiblock.drone_maintenance_interface.no_connection")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }
        }
    }
}
