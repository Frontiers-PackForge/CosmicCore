package com.ghostipedia.cosmiccore.api.machine.part;

import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;
import com.ghostipedia.cosmiccore.api.misc.DroneStationConnection;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DroneMaintenanceInterfacePartMachine extends TieredPartMachine
                                                  implements IMachineLife, IMaintenanceMachine, IInteractedMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine.class,
            MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Setter
    @Persisted
    protected int timeActive;
    @Getter
    @Persisted
    @DescSynced
    protected byte maintenanceProblems = startProblems();
    @Getter
    @Persisted
    private float durationMultiplier = 1f;
    @Nullable
    protected TickableSubscription maintenanceSubs;

    private DroneStationConnection connection;

    public DroneMaintenanceInterfacePartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.HV);
    }

    //////////////////////////////////////
    // ****** Initialization ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public byte startProblems() {
        return ALL_PROBLEMS;
    }

    //////////////////////////////////////
    // ********* Logic **********//
    //////////////////////////////////////
    @Override
    public void setMaintenanceProblems(byte problems) {
        this.maintenanceProblems = problems;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            maintenanceSubs = subscribeServerTick(maintenanceSubs, this::update);
        }
    }

    @Override
    public void onUnload() {
        if (maintenanceSubs != null) {
            maintenanceSubs.unsubscribe();
            maintenanceSubs = null;
        }
    }

    @Override
    public void onMachineRemoved() {
        IMachineLife.super.onMachineRemoved();
        if (hasConnection()) connection.machine = null;
    }

    public void update() {
        if (isRemote()) return;
        // Fix maintenance problems every second
        if (getOffsetTimer() % 20 == 0) {
            if (hasConnection()) {
                // TODO: Should fixing maintenance actually take some sort of item or EU amount?
                if (hasMaintenanceProblems()) {
                    fixAllMaintenanceProblems();
                }
            } else {
                // Find a new connection every 10 seconds
                if (getOffsetTimer() % 200 == 0) {
                    tryFindConnection();
                }
            }
        }
    }

    private boolean hasConnection() {
        if (connection == null) return false;
        if (connection.isValid()) return true;
        return connection.reCheckConnection();
    }

    private void tryFindConnection() {
        ResourceLocation dimension = this.getLevel().dimension().location();
        if (!DroneStationMachine.droneStations.containsKey(dimension)) return;

        Set<DroneStationMachine> stations = DroneStationMachine.droneStations.get(dimension);
        for (DroneStationMachine station : stations) {
            // TODO: Do we want to take specifically the closest one (slower), or just take the first within range
            // (faster)?
            // the speed difference should be negligible :P
            if (station.getPos().distSqr(this.getPos()) > station.blockRangeLimit) continue;
            if (!station.isActive()) continue;
            connection = new DroneStationConnection(this, station);
            station.connections.add(connection);
            return;
        }
    }

    public void fixAllMaintenanceProblems() {
        for (int i = 0; i < 6; i++) setMaintenanceFixed(i);
    }

    @Override
    public boolean isFullAuto() {
        return false;
    }

    @Override
    public void setTaped(boolean isTaped) {}

    @Override
    public boolean isTaped() {
        return false;
    }
}
