package com.ghostipedia.cosmiccore.api.misc;

import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class DroneStationConnection {

    public MetaMachine machine;
    public BlockPos machinePos;
    public DroneStationMachine droneStation;
    public BlockPos droneStationPos;
    public Level world;

    public DroneStationConnection(MetaMachine machine, DroneStationMachine droneStation) {
        this.machine = machine;
        this.machinePos = machine.getPos();
        this.droneStation = droneStation;
        this.droneStationPos = droneStation.getPos();
        this.world = machine.getLevel();
    }

    public boolean reCheckConnection() {
        if (machine == null) this.machine = getMetaMachineAt(machinePos, world);
        if (droneStation == null) {
            MetaMachine droneStation = getMetaMachineAt(droneStationPos, world);
            if (!(droneStation instanceof DroneStationMachine droneStationMachine)) return false;
            this.droneStation = droneStationMachine;
        }

        if (machine != null && !droneStation.connections.contains(this))
            droneStation.connections.add(this);
        return isValid();
    }

    //gets a metamachine at a position
    private MetaMachine getMetaMachineAt(@NotNull BlockPos pos, Level level) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity == null) return null;
        if(blockEntity instanceof MetaMachineBlockEntity machineBlockEntity ) {
            return machineBlockEntity.getMetaMachine();
        }
        return null;
    }

    public boolean isValid() {
        return machine != null && !machine.isInValid() && droneStation != null && !droneStation.isInValid();
    }
}
