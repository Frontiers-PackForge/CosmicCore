package com.ghostipedia.cosmiccore.api.machine.part;

import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;
import com.ghostipedia.cosmiccore.api.misc.DroneStationConnection;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.DummyCleanroom;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DroneMaintenanceInterfacePartMachine extends TieredPartMachine
                                                  implements IMachineLife, IMaintenanceMachine, IInteractedMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            DroneMaintenanceInterfacePartMachine.class,
            MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    private final ICleanroomProvider DUMMY_CLEANROOM;

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

    @Getter
    private DroneStationConnection connection;

    // Can't sync a DroneStationConnection so magic value it is
    // -1 = no connection, otherwise it's the Long packed BPos
    @DescSynced
    private long syncedConnectionPos;

    public DroneMaintenanceInterfacePartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.HV);
        DUMMY_CLEANROOM = DummyCleanroom.createForTypes(Collections.singletonList(CleanroomType.CLEANROOM));
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
        for (var controller : getControllers()) {
            if (!(controller instanceof ICleanroomReceiver cleanroomReceiver)) continue;
            cleanroomReceiver.setCleanroom(DUMMY_CLEANROOM);
        }
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
            if (station.getPos().distSqr(this.getPos()) > blockLimit * blockLimit) continue;
            if (!station.isActive()) continue;
            connection = new DroneStationConnection(this, station);
            station.connections.add(connection);
            return;
        }
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        super.attachTooltips(tooltipsPanel);
        tooltipsPanel.attachTooltips(new IFancyTooltip.Basic(
                () -> GuiTextures.GREGTECH_LOGO,
                () -> List.of(Component
                        .translatable("cosmiccore.multiblock.drone_maintenance_interface.connection_location",
                                BlockPos.of(this.syncedConnectionPos).getX(),
                                BlockPos.of(this.syncedConnectionPos).getY(),
                                BlockPos.of(this.syncedConnectionPos).getZ())
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))),
                () -> this.syncedConnectionPos != -1,
                () -> null));
        tooltipsPanel.attachTooltips(new IFancyTooltip.Basic(
                () -> GuiTextures.GREGTECH_LOGO,
                () -> List.of(Component.translatable("cosmiccore.multiblock.drone_maintenance_interface.no_connection")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))),
                (() -> this.syncedConnectionPos == -1),
                () -> null));
    }

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
