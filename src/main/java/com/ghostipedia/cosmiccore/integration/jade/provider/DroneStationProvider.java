package com.ghostipedia.cosmiccore.integration.jade.provider;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
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

public class DroneStationProvider extends CapabilityBlockProvider<DroneStationMachine> {

    public DroneStationProvider() {
        super(CosmicCore.id("drone_station"));
    }

    @Nullable
    @Override
    protected DroneStationMachine getCapability(Level level, BlockPos blockPos, @Nullable Direction direction) {
        if (MetaMachine.getMachine(level, blockPos) instanceof IMultiController controller) {
            if (controller instanceof DroneStationMachine maintenanceMachine) {
                return maintenanceMachine;
            }
        }
        return null;
    }

    @Override
    protected void write(CompoundTag compoundTag, DroneStationMachine droneStation) {
        compoundTag.putLong("connections", droneStation.connections.size());
    }

    @Override
    protected void addTooltip(CompoundTag compoundTag, ITooltip iTooltip, Player player, BlockAccessor blockAccessor,
                              BlockEntity blockEntity, IPluginConfig iPluginConfig) {
        if (compoundTag.contains("connections", Tag.TAG_LONG)) {
            long amount = compoundTag.getLong("connections");
            if (amount > 0) {
                iTooltip.add(Component.translatable("cosmiccore.multiblock.drone_station_machine.drone_amount", amount)
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));

            } else {
                iTooltip.add(Component.translatable("cosmiccore.multiblock.drone_station_machine.no_drones")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }
        }
    }
}
