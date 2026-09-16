package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.cover.filter.Filter;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FilterHandler.class, remap = false)
public abstract class FilterHandlerReloadFixMixin<T> {

    @Shadow
    private @Nullable Filter<T> filter;

    @Shadow
    public abstract ItemStack getFilterItem();

    @Invoker("updateFilter")
    protected abstract void cosmiccore$invokeUpdateFilter();

    @Inject(method = { "isFilterPresent", "getFilter" }, at = @At("HEAD"), require = 2)
    private void cosmiccore$restoreRuntimeFilter(CallbackInfoReturnable<?> cir) {
        cosmiccore$ensureRuntimeFilter();
    }

    @Unique
    private void cosmiccore$ensureRuntimeFilter() {
        if (filter == null && !getFilterItem().isEmpty()) {
            cosmiccore$invokeUpdateFilter();
        }
    }
}
