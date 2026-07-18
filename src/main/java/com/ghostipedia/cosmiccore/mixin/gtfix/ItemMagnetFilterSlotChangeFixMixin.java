package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter.FilterItemStackHandler;
import com.gregtechceu.gtceu.common.item.behavior.ItemMagnetBehavior;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemMagnetBehavior.class, remap = false)
public class ItemMagnetFilterSlotChangeFixMixin {

    @Inject(method = "lambda$createSimpleFilterPage$7", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$useNewFilterStack(
                                                     FilterItemStackHandler handler,
                                                     int slot,
                                                     ItemStack oldStack,
                                                     ItemStack newStack,
                                                     boolean client,
                                                     boolean init,
                                                     CallbackInfo ci) {
        handler.setStackInSlot(slot, newStack);
        ci.cancel();
    }
}
