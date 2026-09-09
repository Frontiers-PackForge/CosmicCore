package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.ghostipedia.cosmiccore.common.compat.gtceu.ae2.MEStorageDisplayView;

import com.gregtechceu.gtceu.integration.ae2.gui.AEKeyStorageSyncHandler;

import appeng.api.stacks.GenericStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = AEKeyStorageSyncHandler.class, remap = false)
public abstract class AEKeyStorageDisplaySyncFixMixin {

    @Shadow
    private List<GenericStack> value;

    @Unique
    private final List<GenericStack> cosmiccore$liveView = new MEStorageDisplayView<>(() -> value);

    @Inject(method = "getValue()Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$keepDisplayAttached(CallbackInfoReturnable<List<GenericStack>> cir) {
        cir.setReturnValue(cosmiccore$liveView);
    }
}
