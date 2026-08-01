package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

public final class MEComputationComponentPartMachine extends MultiblockPartMachine {

    private final Role role;
    @SyncToClient
    private boolean active;

    public MEComputationComponentPartMachine(BlockEntityCreationInfo info, Role role) {
        super(info);
        this.role = role;
    }

    public Role role() {
        return role;
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
