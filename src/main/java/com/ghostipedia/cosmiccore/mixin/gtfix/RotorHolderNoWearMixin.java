package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.compat.gtceu.TurbineRotorImmortality;

import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RotorHolderPartMachine.class, remap = false)
public abstract class RotorHolderNoWearMixin {

    @Shadow
    public abstract ItemStack getRotorStack();

    @Inject(method = "damageRotor", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$preventRotorWear(int damageAmount, CallbackInfo ci) {
        TurbineRotorImmortality.normalize(getRotorStack());
        ci.cancel();
    }

    @Inject(method = "getRotorDurabilityPercent", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$reportFullRotorDurability(CallbackInfoReturnable<Integer> cir) {
        TurbineRotorImmortality.normalize(getRotorStack());
        cir.setReturnValue(100);
    }
}
