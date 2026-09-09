package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEOutputBusPartMachine$ItemStackHandlerDelegate",
       remap = false)
public abstract class MEOutputBusRejectedInsertFixMixin {

    @Shadow
    private KeyStorage keyStorage;

    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$returnUnacceptedStack(int slot, ItemStack stack, boolean simulate,
                                                  CallbackInfoReturnable<ItemStack> cir) {
        if (stack.isEmpty() || keyStorage == null ||
                keyStorage.storage.getOrDefault(AEItemKey.of(stack), 0) == Long.MAX_VALUE) {
            cir.setReturnValue(stack);
        }
    }
}
