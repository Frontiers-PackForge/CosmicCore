package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

public enum BloomwyrmAllocationConstraint {

    NONE,
    NO_HEART,
    NO_RECIPE,
    LOCAL_IO,
    ENERGY,
    BIOPOWER,
    CHARGE,
    HEART_CAPACITY,
    STRUCTURE;

    public String translationKey() {
        return "cosmiccore.bloomwyrm.constraint." + name().toLowerCase();
    }

    public boolean isResourceLimit() {
        return this == LOCAL_IO || this == ENERGY || this == BIOPOWER || this == CHARGE || this == HEART_CAPACITY;
    }
}
