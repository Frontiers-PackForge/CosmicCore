package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import net.minecraft.nbt.CompoundTag;

public final class BloomwyrmRecipeKeys {

    public static final String BIOPOWER_INPUT = "bloomwyrm_biopower_input";
    public static final String BIOPOWER_OUTPUT = "bloomwyrm_biopower_output";
    public static final String CHARGE_INPUT = "bloomwyrm_charge_input";
    public static final String CHARGE_OUTPUT = "bloomwyrm_charge_output";
    public static final String SEASONAL_CHARGE_INPUT = "bloomwyrm_seasonal_essence_input";
    public static final String SEASONAL_CHARGE_OUTPUT = "bloomwyrm_seasonal_essence_output";
    public static final String FAVORED_SEASON = "bloomwyrm_favored_season";
    public static final String MAX_PARALLEL = "bloomwyrm_max_parallel";

    private BloomwyrmRecipeKeys() {}

    public static BloomwyrmSeason favoredSeason(CompoundTag data) {
        int season = data.getInt(FAVORED_SEASON);
        BloomwyrmSeason[] seasons = BloomwyrmSeason.values();
        return season >= 1 && season <= seasons.length ? seasons[season - 1] : null;
    }
}
