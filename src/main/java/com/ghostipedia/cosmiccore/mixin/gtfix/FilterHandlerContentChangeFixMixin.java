package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FilterHandler.class, remap = false)
public abstract class FilterHandlerContentChangeFixMixin {

    @Shadow
    public abstract CustomItemStackHandler getFilterSlot();

    @Shadow
    private void updateFilter() {
        throw new AssertionError();
    }

    @Inject(method = "getFilterSlot", at = @At("RETURN"), remap = false)
    private void cosmiccore$observeFilterSlot(CallbackInfoReturnable<CustomItemStackHandler> cir) {
        cir.getReturnValue().setOnContentsChanged(this::updateFilter);
    }

    @Inject(method = "setFilterItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$avoidDuplicateFilterUpdate(ItemStack item, CallbackInfo ci) {
        getFilterSlot().setStackInSlot(0, item);
        ci.cancel();
    }
}
