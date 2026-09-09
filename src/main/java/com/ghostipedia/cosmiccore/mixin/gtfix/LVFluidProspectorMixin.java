package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;
import com.gregtechceu.gtceu.common.item.behavior.ProspectorScannerBehavior;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ProspectorScannerBehavior.class, remap = false)
public abstract class LVFluidProspectorMixin {

    @Shadow
    @Final
    @Mutable
    private ProspectorMode<?>[] modes;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cosmiccore$enableFluidMode(int radius, long cost, ProspectorMode<?>[] modes, CallbackInfo ci) {
        if (radius == 2 && cost == GTValues.V[GTValues.LV] / 16L && this.modes.length == 1 &&
                this.modes[0] == ProspectorMode.ORE) {
            this.modes = new ProspectorMode<?>[] { ProspectorMode.ORE, ProspectorMode.FLUID };
        }
    }
}
