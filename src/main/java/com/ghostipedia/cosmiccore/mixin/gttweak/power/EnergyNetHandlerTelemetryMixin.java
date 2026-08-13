package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.power.telemetry.CablePowerTelemetry;

import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.common.pipelike.cable.EnergyNetHandler;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EnergyNetHandler.class, remap = false)
public abstract class EnergyNetHandlerTelemetryMixin {

    @WrapOperation(
                   method = "acceptEnergyFromNetwork(Lnet/minecraft/core/Direction;JJ)J",
                   at = @At(
                            value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/common/blockentity/CableBlockEntity;applyHeat(I)V"),
                   require = 1,
                   expect = 1,
                   allow = 1)
    private void cosmiccore$recordOvervoltage(CableBlockEntity cable, int heat, Operation<Void> original) {
        ((CablePowerTelemetry) cable).cosmiccore$markOvervoltage();
        original.call(cable, heat);
    }
}
