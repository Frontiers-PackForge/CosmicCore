package com.ghostipedia.cosmiccore.mixin.gtfix.accessor;

import com.gregtechceu.gtceu.api.cover.filter.CompositeFilter;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CompositeFilter.class, remap = false)
public interface CompositeFilterStateAccessor<T> {

    @Accessor("filterableType")
    Class<T> cosmiccore$getFilterableType();

    @Accessor("itemStacks")
    CustomItemStackHandler cosmiccore$getItemStacks();
}
