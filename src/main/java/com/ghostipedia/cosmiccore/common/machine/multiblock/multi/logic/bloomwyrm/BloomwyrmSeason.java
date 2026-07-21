package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

public enum BloomwyrmSeason {

    GERMINATION,
    PROLIFERATION,
    BLOOM,
    SENESCENCE;

    public BloomwyrmSeason next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public String translationKey() {
        return "cosmiccore.bloomwyrm.season." + name().toLowerCase();
    }

    public String essenceTranslationKey() {
        return "cosmiccore.bloomwyrm.essence." + name().toLowerCase();
    }

    public static BloomwyrmSeason fromOrdinal(int ordinal) {
        return values()[Math.floorMod(ordinal, values().length)];
    }
}
