package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

public enum BloomwyrmReserveMode {

    CONSERVE,
    STABILIZE,
    OVERDRIVE;

    public String translationKey() {
        return "cosmiccore.bloomwyrm.reserve_mode." + name().toLowerCase();
    }

    public static BloomwyrmReserveMode fromOrdinal(int ordinal) {
        BloomwyrmReserveMode[] modes = values();
        return modes[Math.floorMod(ordinal, modes.length)];
    }
}
