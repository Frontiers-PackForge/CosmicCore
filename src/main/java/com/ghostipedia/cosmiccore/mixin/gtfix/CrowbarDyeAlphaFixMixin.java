package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.item.IGTTool;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IGTTool.class, remap = false)
public interface CrowbarDyeAlphaFixMixin {

    @ModifyExpressionValue(
                           method = "tintColor",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lnet/minecraft/world/item/component/DyedItemColor;rgb()I"))
    private static int cosmiccore$restoreOpaqueDyeAlpha(int rgb) {
        return 0xFF000000 | rgb;
    }
}
