package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.client.firmament.FirmamentTwilightLighting;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class FirmamentClientLevelLightingMixin {

    @Inject(method = "getShade(Lnet/minecraft/core/Direction;Z)F", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$shadeFirmamentFace(Direction direction, boolean shade,
                                               CallbackInfoReturnable<Float> callback) {
        ClientLevel level = (ClientLevel) (Object) this;
        if (level.dimension().equals(FirmamentDimension.KEY)) {
            callback.setReturnValue(FirmamentTwilightLighting.shade(direction, shade));
        }
    }

    @Inject(method = "getShade(FFFZ)F", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$shadeFirmamentNormal(float normalX, float normalY, float normalZ, boolean shade,
                                                 CallbackInfoReturnable<Float> callback) {
        ClientLevel level = (ClientLevel) (Object) this;
        if (level.dimension().equals(FirmamentDimension.KEY)) {
            callback.setReturnValue(FirmamentTwilightLighting.shade(normalX, normalY, normalZ, shade));
        }
    }
}
