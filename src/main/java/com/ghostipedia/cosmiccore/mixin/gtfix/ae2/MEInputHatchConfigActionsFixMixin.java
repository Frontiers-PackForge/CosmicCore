package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.ghostipedia.cosmiccore.common.compat.gtceu.ae2.MEInputConfigActions;

import com.gregtechceu.gtceu.integration.ae2.machine.MEInputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;

import brachy.modularui.value.sync.PanelSyncManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MEInputHatchPartMachine.class, remap = false)
public abstract class MEInputHatchConfigActionsFixMixin {

    @Shadow
    protected ExportOnlyAEFluidList aeFluidHandler;

    @Inject(method = "registerConfigActions", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$registerValidatedActions(PanelSyncManager syncManager, CallbackInfo ci) {
        MEInputConfigActions.register(this, aeFluidHandler, true, syncManager);
        ci.cancel();
    }
}
