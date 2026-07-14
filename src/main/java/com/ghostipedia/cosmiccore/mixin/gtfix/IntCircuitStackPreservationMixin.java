package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = IntCircuitBehaviour.class, remap = false)
public class IntCircuitStackPreservationMixin {

    @Inject(method = "buildUI", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$preserveStackCount(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager,
                                               UISettings settings, CallbackInfoReturnable<ModularPanel<?>> cir) {
        cir.setReturnValue(GTMuiWidgets.createCircuitSlotPanel(configured -> {
            var current = data.getUsedItemStack();
            if (!current.isEmpty() && !configured.isEmpty()) {
                configured.setCount(current.getCount());
            }
            data.setUsedItemStack(configured);
        }, data::getUsedItemStack, syncManager));
    }
}
