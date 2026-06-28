package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.integration.gtceu.GregtechUnloadRenderGuard;

import com.gregtechceu.gtceu.api.blockentity.IGregtechBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// NOTE : ALREADY FIXED UPSTREAM FOR COMPAT WITH C2ME
@Mixin(value = IGregtechBlockEntity.class, remap = false)
public interface IGregtechBlockEntityRemovedChunkReentryFixMixin {

    @Inject(method = "notifyBlockUpdate", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$guardNotifyBlockUpdate(CallbackInfo ci) {
        if (GregtechUnloadRenderGuard.wouldBlockOnChunk((IGregtechBlockEntity) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "scheduleNeighborShapeUpdate", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$guardNeighborShapeUpdate(CallbackInfo ci) {
        if (GregtechUnloadRenderGuard.wouldBlockOnChunk((IGregtechBlockEntity) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "scheduleRenderUpdate", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$guardRenderUpdate(CallbackInfo ci) {
        if (GregtechUnloadRenderGuard.wouldBlockOnChunk((IGregtechBlockEntity) this)) {
            ci.cancel();
        }
    }
}
