package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationArrayMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationArrayTuning;
import com.ghostipedia.nebulaeae2.compute.api.IComputeSource;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.core.Direction;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.events.GridPowerStorageStateChanged.PowerEventType;
import appeng.api.util.AECableType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public final class MEComputationUplinkPartMachine extends MultiblockPartMachine
                                                  implements IInWorldGridNodeHost, IComputeSource, IAEPowerStorage {

    private static final IGridNodeListener<MEComputationUplinkPartMachine> NODE_LISTENER = new IGridNodeListener<>() {

        @Override
        public void onSaveChanges(MEComputationUplinkPartMachine owner, IGridNode node) {
            owner.setChanged();
        }
    };

    @SaveField
    private UUID sourceId = UUID.randomUUID();
    @SaveField
    private final ManagedGridNodeState nodeState;
    @SaveField
    @SyncToClient
    private double aeBuffer;

    public MEComputationUplinkPartMachine(BlockEntityCreationInfo info) {
        super(info);
        nodeState = new ManagedGridNodeState(GridHelper.createManagedNode(this, NODE_LISTENER)
                .setVisualRepresentation(getDefinition().getItem())
                .setIdlePowerUsage(0)
                .setInWorldNode(true)
                .setExposedOnSides(EnumSet.of(getFrontFacing()))
                .setTagName("grid")
                .addService(IComputeSource.class, this)
                .addService(IAEPowerStorage.class, this));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        scheduleForNextServerTick(() -> nodeState.node().create(getLevel(), getBlockPos()));
    }

    @Override
    public void onUnload() {
        nodeState.node().destroy();
        super.onUnload();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        nodeState.node().setExposedOnSides(EnumSet.of(newFacing));
    }

    @Override
    public boolean canShared(MultiblockControllerMachine controller, String substructureName) {
        return false;
    }

    @Override
    public UUID sourceId() {
        return sourceId;
    }

    @Override
    public long availableCwut() {
        MEComputationArrayMachine controller = getComputationController();
        return controller == null ? 0 : controller.getAvailableCwut();
    }

    public double getRelayPowerDemandAe() {
        return Math.max(0, getAEMaxPower() - aeBuffer);
    }

    public boolean isGridOnline() {
        IGridNode node = nodeState.node().getNode();
        return node != null && node.isOnline();
    }

    public double acceptRelayPower(double amountAe) {
        if (amountAe <= 0) {
            return 0;
        }
        boolean wasEmpty = aeBuffer <= 0;
        double accepted = Math.min(amountAe, getRelayPowerDemandAe());
        if (accepted <= 0) {
            return 0;
        }
        aeBuffer += accepted;
        setChanged();
        getSyncDataHolder().markClientSyncFieldDirty("aeBuffer");
        if (wasEmpty) {
            nodeState.node().ifPresent(grid -> grid.postEvent(
                    new GridPowerStorageStateChanged(this, PowerEventType.PROVIDE_POWER)));
        }
        return accepted;
    }

    @Override
    public double injectAEPower(double amount, Actionable mode) {
        return amount;
    }

    @Override
    public double getAEMaxPower() {
        return MEComputationArrayTuning.uplinkBufferCapacityAe();
    }

    @Override
    public double getAECurrentPower() {
        return aeBuffer;
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.READ;
    }

    @Override
    public double extractAEPower(double amount, Actionable mode, PowerMultiplier multiplier) {
        double requested = multiplier.multiply(Math.max(0, amount));
        double extracted = Math.min(requested, aeBuffer);
        if (mode == Actionable.MODULATE && extracted > 0) {
            aeBuffer -= extracted;
            setChanged();
            getSyncDataHolder().markClientSyncFieldDirty("aeBuffer");
        }
        return multiplier.divide(extracted);
    }

    @Nullable
    @Override
    public IGridNode getGridNode(Direction direction) {
        return direction == getFrontFacing() ? nodeState.node().getNode() : null;
    }

    @Override
    public AECableType getCableConnectionType(Direction direction) {
        return AECableType.SMART;
    }

    @Nullable
    private MEComputationArrayMachine getComputationController() {
        for (MultiblockControllerMachine controller : getControllers()) {
            if (controller instanceof MEComputationArrayMachine computationArray) {
                return computationArray;
            }
        }
        return null;
    }
}
