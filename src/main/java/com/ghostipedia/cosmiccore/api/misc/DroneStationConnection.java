package com.ghostipedia.cosmiccore.api.misc;

import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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

    // TODO: Implement this.
    private MetaMachine getMetaMachineAt(BlockPos pos, Level level) {
        return null;
    }

    public boolean isValid() {
        return machine != null && !machine.isInValid() && droneStation != null && !droneStation.isInValid();
    }
}
