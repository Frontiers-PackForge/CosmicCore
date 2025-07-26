package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.misc.DroneStationConnection;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.Setter;
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
        setWorkingEnabled(false);
        super.onStructureInvalid();
        if (!isRemote()) {
            droneStations.remove(this.getLevel().dimension().location(), this);
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    public void updateDroneHatches() {
        // TODO: Make this machine take EU
       if(energyContainer != null) {
           drainEnergy(false);
       }
       if(energyContainer.getEnergyStored() != 0) {
           if (getOffsetTimer() % 20 == 0) {
               connections.removeIf(connection -> !connection.isValid());
           }
       }
    }


    public boolean drainEnergy(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - 200;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(200);
            setWorkingEnabled(true);
            getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
            return true;
        }
        return false;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
    }
    // TODO: Add functions for UI to disable/enable/read status/etc machines remotely
}
