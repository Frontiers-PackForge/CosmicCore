package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.power.telemetry.CablePowerTelemetry;

import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CableBlockEntity.class, remap = false)
public abstract class CableBlockEntityTelemetryMixin implements CablePowerTelemetry {

    @Unique
    private int cosmiccore$overloadCause;

    @Inject(
            method = "incrementAmperage(JJ)Z",
            at = @At("RETURN"),
            require = 2,
            expect = 2,
            allow = 2)
    private void cosmiccore$recordOveramperage(long amps, long voltage, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            cosmiccore$markOveramperage();
        }
    }

    @Inject(
            method = "setTemperature(I)V",
            at = @At("RETURN"),
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$clearCooledCause(int temperature, CallbackInfo ci) {
        if (temperature <= CableBlockEntity.getDefaultTemp()) {
            cosmiccore$overloadCause = 0;
        }
    }

    @Override
    public int cosmiccore$getOverloadCause() {
        return cosmiccore$overloadCause;
    }

    @Override
    public void cosmiccore$markOveramperage() {
        cosmiccore$overloadCause |= OVERAMPERAGE;
    }

    @Override
    public void cosmiccore$markOvervoltage() {
        cosmiccore$overloadCause |= OVERVOLTAGE;
    }
}
