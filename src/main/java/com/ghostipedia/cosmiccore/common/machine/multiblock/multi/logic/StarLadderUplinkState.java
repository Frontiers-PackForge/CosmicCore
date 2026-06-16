package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

public enum StarLadderUplinkState {

    IDLE,
    INTERRUPTED,
    AWAITING_CONFIRMATION,
    ACTIVE_PHASE_1,
    ACTIVE_PHASE_2,
    ACTIVE_PHASE_3,
    COMPLETED,
    FAILED;

    public boolean isActive() {
        return this == ACTIVE_PHASE_1 || this == ACTIVE_PHASE_2 || this == ACTIVE_PHASE_3;
    }

    public boolean isFightState() {
        return isActive() || this == INTERRUPTED || this == AWAITING_CONFIRMATION;
    }
}
