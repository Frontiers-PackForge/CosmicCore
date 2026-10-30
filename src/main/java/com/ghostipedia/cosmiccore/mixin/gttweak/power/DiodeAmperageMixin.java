package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.gregtechceu.gtceu.common.machine.multiblock.part.DiodePartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DiodePartMachine.class, remap = false)
public abstract class DiodeAmperageMixin {

    @Shadow
    public static int MAX_AMPS;

    @Inject(method = "<clinit>", at = @At("TAIL"), require = 1)
    private static void cosmiccore$extendAmperage(CallbackInfo ci) {
        MAX_AMPS = 64;
    }
}
