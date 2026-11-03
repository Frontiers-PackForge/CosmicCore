package com.ghostipedia.cosmiccore.mixin.support;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;
import java.util.Set;

public final class QualityFoodAcquisitionCanceller implements MixinCanceller {

    private static final Set<String> AUTOMATIC_TRANSFERS = Set.of(
            "de.cadentem.quality_food.mixin.CampFireBlockEntityMixin",
            "de.cadentem.quality_food.mixin.create.MillstoneBlockEntityMixin",
            "de.cadentem.quality_food.mixin.kaleidoscope_cookery.ChoppingBoardBlockEntityMixin",
            "de.cadentem.quality_food.mixin.kaleidoscope_cookery.MillstoneBlockEntityMixin",
            "de.cadentem.quality_food.mixin.kaleidoscope_cookery.ShawarmaSpitBlockEntityMixin",
            "de.cadentem.quality_food.mixin.kaleidoscope_cookery.SteamerBlockEntityMixin",
            "de.cadentem.quality_food.mixin.sophisticatedcore.RecipeHelperMixin");

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        return AUTOMATIC_TRANSFERS.contains(mixinClassName);
    }
}
