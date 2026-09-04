package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = PowerSubstationMachine.PowerStationEnergyBank.class, remap = false)
public abstract class PowerSubstationEnergyBankRebuildFixMixin {

    @Shadow
    private int index;

    @Inject(method = "setupBatteries", at = @At("TAIL"))
    private void cosmiccore$resetCursor(List<IBatteryData> batteries, CallbackInfo ci) {
        index = 0;
    }
}
