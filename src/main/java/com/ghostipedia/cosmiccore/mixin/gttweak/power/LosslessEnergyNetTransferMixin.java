package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.gregtechceu.gtceu.common.pipelike.cable.EnergyNetHandler;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EnergyNetHandler.class, remap = false)
public abstract class LosslessEnergyNetTransferMixin {

    @ModifyExpressionValue(
                           method = "acceptEnergyFromNetwork(Lnet/minecraft/core/Direction;JJ)J",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lcom/gregtechceu/gtceu/common/pipelike/cable/EnergyRoutePath;getMaxLoss()J"),
                           require = 2,
                           expect = 2,
                           allow = 2)
    private long cosmiccore$ignoreCachedRouteLoss(long original) {
        return 0L;
    }

    @ModifyExpressionValue(
                           method = "acceptEnergyFromNetwork(Lnet/minecraft/core/Direction;JJ)J",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/WireProperties;getLossPerBlock()I"),
                           require = 1,
                           expect = 1,
                           allow = 1)
    private int cosmiccore$ignoreCableTelemetryLoss(int original) {
        return 0;
    }
}
