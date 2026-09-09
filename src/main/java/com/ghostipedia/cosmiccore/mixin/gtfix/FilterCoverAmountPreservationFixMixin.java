package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.common.cover.FluidRegulatorCover;
import com.gregtechceu.gtceu.common.cover.RobotArmCover;
import com.gregtechceu.gtceu.common.cover.voiding.AdvancedFluidVoidingCover;
import com.gregtechceu.gtceu.common.cover.voiding.AdvancedItemVoidingCover;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = { RobotArmCover.class, FluidRegulatorCover.class,
        AdvancedItemVoidingCover.class, AdvancedFluidVoidingCover.class },
       remap = false)
public abstract class FilterCoverAmountPreservationFixMixin {

    @Inject(method = "configureFilter", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$preserveAmountsAcrossModes(CallbackInfo ci) {
        ci.cancel();
    }
}
