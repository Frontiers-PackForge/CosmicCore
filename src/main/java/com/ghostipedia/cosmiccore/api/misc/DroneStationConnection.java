package com.ghostipedia.cosmiccore.api.misc;

import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;
import com.ghostipedia.cosmiccore.api.machine.part.DroneMaintenanceInterfacePartMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public final class DroneStationConnection {

    private final BlockPos interfacePos;
    private final BlockPos stationPos;
    private WeakReference<DroneMaintenanceInterfacePartMachine> interfaceReference;
    private WeakReference<DroneStationMachine> stationReference;

    public DroneStationConnection(DroneMaintenanceInterfacePartMachine maintenanceInterface,
                                  DroneStationMachine station) {
        interfacePos = maintenanceInterface.getBlockPos().immutable();
        stationPos = station.getBlockPos().immutable();
        interfaceReference = new WeakReference<>(maintenanceInterface);
        stationReference = new WeakReference<>(station);
    }

    public BlockPos interfacePos() {
        return interfacePos;
    }

    public BlockPos stationPos() {
        return stationPos;
    }

    public @Nullable DroneMaintenanceInterfacePartMachine maintenanceInterface(Level level) {
        DroneMaintenanceInterfacePartMachine maintenanceInterface = interfaceReference.get();
        if (maintenanceInterface != null && !maintenanceInterface.isRemoved()) return maintenanceInterface;
        MetaMachine machine = MetaMachine.getMachine(level, interfacePos);
        if (!(machine instanceof DroneMaintenanceInterfacePartMachine resolved)) return null;
        interfaceReference = new WeakReference<>(resolved);
        return resolved;
    }

    public @Nullable DroneStationMachine station(Level level) {
        DroneStationMachine station = stationReference.get();
        if (station != null && !station.isRemoved()) return station;
        MetaMachine machine = MetaMachine.getMachine(level, stationPos);
        if (!(machine instanceof DroneStationMachine resolved)) return null;
        stationReference = new WeakReference<>(resolved);
        return resolved;
    }

    public boolean isValid(Level level) {
        DroneMaintenanceInterfacePartMachine maintenanceInterface = maintenanceInterface(level);
        DroneStationMachine station = station(level);
        return maintenanceInterface != null && station != null && station.canServe(interfacePos) &&
                maintenanceInterface.getConnection() == this;
    }
}
