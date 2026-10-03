package com.ghostipedia.cosmiccore.common.compat.gtceu;

import com.ghostipedia.cosmiccore.mixin.gtfix.accessor.CompositeFilterStateAccessor;
import com.ghostipedia.cosmiccore.mixin.gtfix.accessor.SimpleFluidFilterConstructorInvoker;
import com.ghostipedia.cosmiccore.mixin.gtfix.accessor.TagFilterStateAccessor;

import com.gregtechceu.gtceu.api.cover.filter.CompositeFilter;
import com.gregtechceu.gtceu.api.cover.filter.Filter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.SmartItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.TagFilter;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.function.Function;

public final class FilterCopies {

    private FilterCopies() {}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Filter<?> detached(Filter<?> filter) {
        if (filter instanceof SimpleItemFilter simple) {
            return new SimpleItemFilter(
                    simple.isBlackList(), simple.isIgnoreNbt(),
                    Arrays.stream(simple.getMatches()).map(ItemStack::copy).toList());
        }
        if (filter instanceof SimpleFluidFilter simple) {
            return SimpleFluidFilterConstructorInvoker.cosmiccore$create(
                    simple.isBlackList(), simple.isIgnoreNbt(),
                    Arrays.stream(simple.getMatches()).map(FluidStack::copy).toList());
        }
        if (filter instanceof SmartItemFilter smart) {
            return new SmartItemFilter(smart.getFilterMode());
        }
        if (filter instanceof TagFilter tag) {
            TagFilterStateAccessor accessor = (TagFilterStateAccessor) tag;
            return new TagFilter(
                    tag.getFilterString(),
                    (Function) accessor.cosmiccore$getTagHolderObject(),
                    (Function) accessor.cosmiccore$getTagsSupplier());
        }
        if (filter instanceof CompositeFilter composite) {
            CompositeFilterStateAccessor accessor = (CompositeFilterStateAccessor) composite;
            return new CompositeFilter(
                    accessor.cosmiccore$getItemStacks().toList().stream().map(ItemStack::copy).toList(),
                    accessor.cosmiccore$getFilterableType());
        }
        return filter;
    }
}
