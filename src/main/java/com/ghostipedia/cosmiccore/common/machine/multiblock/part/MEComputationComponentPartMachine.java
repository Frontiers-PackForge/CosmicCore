package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationComponentTier;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

public final class MEComputationComponentPartMachine extends MultiblockPartMachine {

    private final Role role;
    private final MEComputationComponentTier componentTier;
    @SyncToClient
    private boolean active;

    public MEComputationComponentPartMachine(BlockEntityCreationInfo info, Role role,
                                             MEComputationComponentTier componentTier) {
        super(info);
        this.role = role;
        this.componentTier = componentTier;
    }

    public Role role() {
        return role;
    }

    public MEComputationComponentTier componentTier() {
        return componentTier;
    }

    public long euPerTick() {
        return role == Role.COMPUTATION_CORE ? componentTier.coreEuPerTick() : componentTier.relayEuPerTick();
    }

    public long cwutPerTick() {
        return role == Role.COMPUTATION_CORE ? componentTier.coreCwutPerTick() : 0;
    }

    public long standbyEuPerTick() {
        return role == Role.COMPUTATION_CORE ? componentTier.coreStandbyEuPerTick() : 0;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        getSyncDataHolder().markClientSyncFieldDirty("active");
    }

    @Override
    public boolean canShared(MultiblockControllerMachine controller, String substructureName) {
        return false;
    }

    public enum Role {
        COMPUTATION_CORE,
        POWER_RELAY
    }
}
