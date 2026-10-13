package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.compat.gtceu.FilterCopies;

import com.gregtechceu.gtceu.api.cover.filter.Filter;
import com.gregtechceu.gtceu.api.cover.filter.Filters;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Filters.class, remap = false)
public abstract class FilterDataComponentIsolationFixMixin {

    @ModifyExpressionValue(
                           method = "loadFilter",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lcom/gregtechceu/gtceu/api/cover/filter/Filter;createCopy()Lcom/gregtechceu/gtceu/api/cover/filter/Filter;"))
    private static Filter<?> cosmiccore$detachMutableFilterComponent(Filter<?> filter) {
        return FilterCopies.detached(filter);
    }
}
