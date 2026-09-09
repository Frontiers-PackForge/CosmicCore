package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.ghostipedia.cosmiccore.common.compat.gtceu.ae2.MEOutputUI;

import com.gregtechceu.gtceu.integration.ae2.machine.MEOutputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEOutputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = { MEOutputBusPartMachine.class, MEOutputHatchPartMachine.class }, remap = false)
public abstract class MEOutputUIFixMixin {

    @Shadow(remap = false)
    private KeyStorage internalBuffer;

    @Inject(method = "buildMainUI", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$buildLiveOutputList(ParentWidget<?> mainWidget, PosGuiData data,
                                                PanelSyncManager syncManager, UISettings settings, CallbackInfo ci) {
        MEOutputUI.build(mainWidget, syncManager, (IGridConnectedMachine) this, internalBuffer);
        ci.cancel();
    }
}
