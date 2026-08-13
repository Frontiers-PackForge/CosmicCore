package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BatteryBufferMachine.class, remap = false)
public abstract class BatteryBufferTintFixMixin {

    @Inject(
            method = "tintColor(I)I",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$tintInputContacts(int index, CallbackInfoReturnable<Integer> cir) {
        if (index == 3) {
            BatteryBufferMachine buffer = (BatteryBufferMachine) (Object) this;
            cir.setReturnValue(GTValues.VC[buffer.getTier()]);
        }
    }
}
