package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

public class AbyssalCultureVatMachine extends BloomwyrmUnitMachine {

    public AbyssalCultureVatMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public boolean supportsParallelControl() {
        return false;
    }

    @Override
    public boolean supportsSeasonalReserveMode() {
        return false;
    }

    @Override
    protected long getChargeOutputPerParallel(GTRecipe recipe, BloomwyrmSeason season) {
        long base = super.getChargeOutputPerParallel(recipe, season);
        return BloomwyrmRecipeKeys.favoredSeason(recipe.data) == season ? saturatingDouble(base) : base;
    }

    @Override
    protected long getSeasonalChargeOutputPerParallel(GTRecipe recipe, BloomwyrmSeason season) {
        long base = super.getSeasonalChargeOutputPerParallel(recipe, season);
        return BloomwyrmRecipeKeys.favoredSeason(recipe.data) == season ? saturatingDouble(base) : base;
    }

    private static long saturatingDouble(long value) {
        return value > Long.MAX_VALUE / 2 ? Long.MAX_VALUE : value * 2;
    }
}
