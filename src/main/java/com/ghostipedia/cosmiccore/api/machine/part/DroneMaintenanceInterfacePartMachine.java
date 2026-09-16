package com.ghostipedia.cosmiccore.api.machine.part;

import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;
import com.ghostipedia.cosmiccore.api.misc.DroneStationConnection;
import com.ghostipedia.cosmiccore.api.misc.DroneStationNetwork;
import com.ghostipedia.cosmiccore.api.misc.DroneStationServiceLogic;
import com.ghostipedia.cosmiccore.api.misc.DroneStationSpace;
import com.ghostipedia.cosmiccore.mixin.accessor.CleanroomReceiverTraitAccessor;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomProviderTrait;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomReceiverTrait;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DroneMaintenanceInterfacePartMachine extends MaintenanceHatchPartMachine {

    private DroneStationConnection connection;
    private final CleanroomProviderTrait cleanroomProvider;

    @SyncToClient
    private long syncedConnectionPos = -1;

    public DroneMaintenanceInterfacePartMachine(BlockEntityCreationInfo holder) {
        super(holder, DroneStationServiceLogic.INTERFACE_TIER, false);
        cleanroomProvider = attachTrait(new CleanroomProviderTrait(Set.of(CleanroomType.CLEANROOM)));
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        if (context.getItemInHand().is(GTItems.DUCT_TAPE.get())) return InteractionResult.PASS;
        return super.onUseWithItem(context);
    }

    @Override
    public byte startProblems() {
        return ALL_PROBLEMS;
    }

    public @Nullable DroneStationConnection getConnection() {
        return connection;
    }

    public @Nullable MultiblockControllerMachine getController() {
        return getControllers().isEmpty() ? null : getControllers().first();
    }

    @Override
    protected void updateMaintenanceSubscription() {
        if (!isRemote()) maintenanceSubs = subscribeServerTick(maintenanceSubs, this::update);
    }

    @Override
    public void onUnload() {
        revokeCleanroom();
        disconnect();
        if (maintenanceSubs != null) {
            maintenanceSubs.unsubscribe();
            maintenanceSubs = null;
        }
        super.onUnload();
    }

    @Override
    public void onMachineDestroyed() {
        revokeCleanroom();
        disconnect();
        super.onMachineDestroyed();
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        revokeCleanroom(controller);
        super.removedFromController(controller);
        if (getControllers().isEmpty()) disconnect();
    }

    @Override
    public void update() {
        if (isRemote() || getOffsetTimer() % 20 != 0) return;
        if (getController() == null) {
            disconnect();
            return;
        }
        if (!hasConnection()) tryFindConnection();
        if (!hasConnection()) {
            revokeCleanroom();
            setSyncedConnectionPos(-1);
            return;
        }
        DroneStationMachine station = connection.station(getLevel());
        if (station == null) {
            disconnect();
            return;
        }
        setSyncedConnectionPos(DroneStationSpace.worldPosition(getLevel(), station.getBlockPos()).asLong());
        updateCleanroom(station);
        int missingProblem = DroneStationServiceLogic.firstMissingProblem(getMaintenanceProblems());
        if (missingProblem >= 0 && station.consumeServiceUse(getBlockPos())) setMaintenanceFixed(missingProblem);
    }

    public boolean hasConnection() {
        if (connection == null) return false;
        if (connection.isValid(getLevel())) return true;
        disconnect();
        return false;
    }

    private void tryFindConnection() {
        if (!(getLevel() instanceof ServerLevel level)) return;
        DroneStationMachine station = DroneStationNetwork.nearestActive(level, getBlockPos());
        if (station == null) return;
        connection = station.connect(this);
        setSyncedConnectionPos(DroneStationSpace.worldPosition(getLevel(), station.getBlockPos()).asLong());
    }

    private void disconnect() {
        revokeCleanroom();
        DroneStationConnection oldConnection = connection;
        connection = null;
        setSyncedConnectionPos(-1);
        if (oldConnection == null || getLevel() == null) return;
        DroneStationMachine station = oldConnection.station(getLevel());
        if (station != null) station.disconnect(getBlockPos());
    }

    public void disconnectFrom(DroneStationMachine station) {
        if (connection == null || !connection.stationPos().equals(station.getBlockPos())) return;
        revokeCleanroom();
        connection = null;
        setSyncedConnectionPos(-1);
        station.disconnect(getBlockPos());
    }

    public void refreshCleanroomService(DroneStationMachine station) {
        if (connection == null || !connection.stationPos().equals(station.getBlockPos())) return;
        updateCleanroom(station);
    }

    private void updateCleanroom(DroneStationMachine station) {
        if (!DroneStationServiceLogic.providesCleanroom(station.getActiveTier(), station.isOnline())) {
            revokeCleanroom();
            return;
        }
        MultiblockControllerMachine controller = getController();
        if (controller == null) {
            revokeCleanroom();
            return;
        }
        controller.getTraitOptional(CleanroomReceiverTrait.class).ifPresent(receiver -> {
            CleanroomProviderTrait current = ((CleanroomReceiverTraitAccessor) receiver)
                    .cosmiccore$getCleanroomProvider();
            if (current == null || current == cleanroomProvider) {
                cleanroomProvider.setActive(true);
                receiver.setCleanroomProvider(cleanroomProvider);
            }
        });
    }

    private void revokeCleanroom() {
        cleanroomProvider.setActive(false);
        for (MultiblockControllerMachine controller : Set.copyOf(getControllers())) revokeCleanroom(controller);
    }

    private void revokeCleanroom(MultiblockControllerMachine controller) {
        cleanroomProvider.setActive(false);
        controller.getTraitOptional(CleanroomReceiverTrait.class).ifPresent(receiver -> {
            if (((CleanroomReceiverTraitAccessor) receiver).cosmiccore$getCleanroomProvider() == cleanroomProvider) {
                receiver.removeCleanroom();
            }
        });
    }

    private void setSyncedConnectionPos(long position) {
        if (syncedConnectionPos == position) return;
        syncedConnectionPos = position;
        getSyncDataHolder().markClientSyncFieldDirty("syncedConnectionPos");
    }

    public long getSyncedConnectionPos() {
        return syncedConnectionPos;
    }

    @Override
    public void setTaped(boolean ignored) {}

    @Override
    public boolean isTaped() {
        return false;
    }
}
