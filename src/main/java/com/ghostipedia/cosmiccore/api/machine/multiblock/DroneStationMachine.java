package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.misc.DroneStationConnection;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.HashMultimap;

import java.util.ArrayList;
import java.util.List;

public class DroneStationMachine extends WorkableElectricMultiblockMachine {

    // A MultiMap from Dimension -> DroneStation, such that all Drone Maintenance Interfaces can
    // find their closest DroneStation in their world
    public static final HashMultimap<ResourceLocation, DroneStationMachine> droneStations = HashMultimap.create();

    private TickableSubscription tickSubscription;

    public final List<DroneStationConnection> connections = new ArrayList<>();

    // TODO: Make this configurable? Maybe per voltage you give it?
    public long blockRangeLimit = 4096;

    public DroneStationMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!isRemote()) {
            droneStations.put(this.getLevel().dimension().location(), this);
            tickSubscription = this.subscribeServerTick(this::updateDroneHatches);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (!isRemote()) {
            droneStations.remove(this.getLevel().dimension().location(), this);
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    public void updateDroneHatches() {
        // TODO: Make this machine take EU
        if (getOffsetTimer() % 20 == 0) {
            connections.removeIf(connection -> !connection.isValid());
        }
    }

    // TODO: Add functions for UI to disable/enable/read status/etc machines remotely
}
