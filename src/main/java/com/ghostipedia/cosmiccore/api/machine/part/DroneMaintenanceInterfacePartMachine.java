package com.ghostipedia.cosmiccore.api.machine.part;

import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;
import com.ghostipedia.cosmiccore.api.misc.DroneStationConnection;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DroneMaintenanceInterfacePartMachine extends TieredPartMachine
                                                  implements IMaintenanceMachine {

    @Persisted
    protected int timeActive;
    @Persisted
    @DescSynced
    protected byte maintenanceProblems = startProblems();
    @Persisted
    private float durationMultiplier = 1f;
    @Nullable
    protected TickableSubscription maintenanceSubs;

    private DroneStationConnection connection;

    // Can't sync a DroneStationConnection so magic value it is
    // -1 = no connection, otherwise it's the Long packed BPos
    @DescSynced
    private long syncedConnectionPos;

    public DroneMaintenanceInterfacePartMachine(BlockEntityCreationInfo holder) {
        super(holder, GTValues.HV);
    }

    //////////////////////////////////////
    // ****** Initialization ******//
    //////////////////////////////////////

    @Override
    public byte startProblems() {
        return ALL_PROBLEMS;
    }

    //////////////////////////////////////
    // ********* Logic **********//
    //////////////////////////////////////
    @Override
    public int getTimeActive() {
        return timeActive;
    }

    @Override
    public void setTimeActive(int timeActive) {
        this.timeActive = timeActive;
    }

    @Override
    public byte getMaintenanceProblems() {
        return maintenanceProblems;
    }

    @Override
    public float getDurationMultiplier() {
        return durationMultiplier;
    }

    public DroneStationConnection getConnection() {
        return connection;
    }

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
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        if (hasConnection()) connection.machine = null;
    }

    public void update() {
        if (isRemote()) return;
        // Fix maintenance problems every second
        if (getOffsetTimer() % 20 == 0) {
            if (hasConnection()) {
                syncedConnectionPos = connection.droneStationPos.asLong();
                updateCleanroomStyle();
                if (hasMaintenanceProblems()) {
                    // See if we are allowed to fix maintenance issues + potentially consume a drone
                    if (connection.droneStation.fixMaintenanceIssue()) {
                        fixAllMaintenanceProblems();
                    }
                }
            } else {
                syncedConnectionPos = -1;
                // Find a new connection every 10 seconds
                if (getOffsetTimer() % 200 == 0) {
                    tryFindConnection();
                }
            }
        }
    }

    private void updateCleanroomStyle() {
        if (!hasConnection()) return;
        if (connection.droneStation.currentTier != DroneStationMachine.DroneTier.SANGUINE &&
                connection.droneStation.currentTier != DroneStationMachine.DroneTier.PLASMATIC)
            return;
        // TODO[GTCEu 8.0 port]: cleanroom-supplying disabled. The 1.20.1 API used here
        // (ICleanroomProvider / ICleanroomReceiver / DummyCleanroom.createForTypes) was removed.
        // 8.0 replaces it with the trait pair CleanroomProviderTrait / CleanroomReceiverTrait
        // (com.gregtechceu.gtceu.common.machine.trait). Reimplementing requires attaching a
        // CleanroomProviderTrait to this part and pushing it into each controller's
        // CleanroomReceiverTrait via CleanroomReceiverTrait#setCleanroomProvider; there is no
        // DummyCleanroom factory anymore, so a real provider trait must be constructed.
    }

    public boolean hasConnection() {
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
            long blockLimit = station.getBlockLimit();
            if (station.getBlockPos().distSqr(this.getBlockPos()) > blockLimit * blockLimit) continue;
            if (!station.isActive()) continue;
            connection = new DroneStationConnection(this, station);
            station.connections.add(connection);
            return;
        }
    }

    // TODO(8.0.0 MUI2): custom UI shelved; default UI used (orig in git).
    // attachTooltips(TooltipsPanel) used the removed api.gui.fancy IFancyTooltip/TooltipsPanel API.
    // It surfaced two GTECH-logo fancy tooltips driven by syncedConnectionPos:
    //   - when connected (syncedConnectionPos != -1): green "drone_maintenance_interface.connection_location"
    //     with BlockPos.of(syncedConnectionPos) X/Y/Z;
    //   - when disconnected (== -1): red "drone_maintenance_interface.no_connection".
    // Reinstate via GTMultiblockTextUtil rows in the controller's getWidgetsForDisplay when porting to MUI2.
    // The syncedConnectionPos field + connection logic above remain fully functional.

    public void fixAllMaintenanceProblems() {
        for (int i = 0; i < 6; i++) setMaintenanceFixed(i);
    }

    @Override
    public boolean isFullAuto() {
        return false;
    }

    @Override
    public void setTaped(boolean ignored) {}

    @Override
    public boolean isTaped() {
        return false;
    }
}
