package com.ghostipedia.cosmiccore.mixin.gttweak.ender;

import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.common.cover.ender.EnderFluidLinkCover;
import com.gregtechceu.gtceu.common.cover.ender.EnderItemLinkCover;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = { EnderItemLinkCover.class, EnderFluidLinkCover.class }, remap = false)
public abstract class DeprecatedEnderLinkMixin {

    @Inject(method = "transfer", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$disableTransfer(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "createVirtualEntryWidget", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$disableStorageInteraction(PanelSyncManager manager, VirtualEntry entry,
                                                      int width, int height, int index,
                                                      CallbackInfoReturnable<IWidget> cir) {
        cir.setReturnValue(new ParentWidget<>().size(width, height));
    }
}
