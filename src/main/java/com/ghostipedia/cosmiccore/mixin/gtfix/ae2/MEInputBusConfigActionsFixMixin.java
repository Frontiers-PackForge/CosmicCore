package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.ghostipedia.cosmiccore.common.compat.gtceu.ae2.MEInputConfigActions;

import com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;

import brachy.modularui.value.sync.PanelSyncManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MEInputBusPartMachine.class, remap = false)
public abstract class MEInputBusConfigActionsFixMixin {

    @Shadow
    protected ExportOnlyAEItemList aeItemHandler;

    @Inject(method = "registerConfigActions", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$registerValidatedActions(PanelSyncManager syncManager, CallbackInfo ci) {
        MEInputConfigActions.register(this, aeItemHandler, false, syncManager);
        ci.cancel();
    }
}
