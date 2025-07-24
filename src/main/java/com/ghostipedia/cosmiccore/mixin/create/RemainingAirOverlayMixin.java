package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.breath.OxygenHelper;

import net.minecraft.client.player.LocalPlayer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.equipment.armor.RemainingAirOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RemainingAirOverlay.class, remap = false)
public class RemainingAirOverlayMixin {

    @ModifyExpressionValue(method = "render",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z",
                                    remap = true))
    private boolean render(boolean isInFluid, @Local LocalPlayer player) {
        return isInFluid || OxygenHelper.airQualityActivatesHelmet(player);
    }
}
