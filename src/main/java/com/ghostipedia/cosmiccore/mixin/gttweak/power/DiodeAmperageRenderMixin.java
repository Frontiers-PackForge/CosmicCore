package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.gregtechceu.gtceu.common.machine.multiblock.part.DiodePartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DiodePartMachine.AmpMode.class, remap = false)
public abstract class DiodeAmperageRenderMixin {

    @Inject(method = "getByValue", at = @At("HEAD"), cancellable = true, require = 1)
    private static void cosmiccore$useLargestExistingOverlay(int amps,
                                                             CallbackInfoReturnable<DiodePartMachine.AmpMode> cir) {
        if (amps > 16) cir.setReturnValue(DiodePartMachine.AmpMode.MODE_16A);
    }
}
