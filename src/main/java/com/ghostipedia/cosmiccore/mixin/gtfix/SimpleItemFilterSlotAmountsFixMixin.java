package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SimpleItemFilter.FilterItemStackHandler.class, remap = false)
public abstract class SimpleItemFilterSlotAmountsFixMixin {

    @Inject(method = "getStackLimit", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$allowConfiguredCounts(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(TransferMode.TRANSFER_EXACT.maxStackSize);
    }

    @Redirect(method = "setStackInSlot",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;",
                       remap = true))
    private ItemStack cosmiccore$preserveConfiguredCount(ItemStack stack, int ignored) {
        return stack.copy();
    }
}
