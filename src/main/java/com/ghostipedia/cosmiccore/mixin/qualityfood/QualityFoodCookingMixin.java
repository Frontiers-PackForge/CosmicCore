package com.ghostipedia.cosmiccore.mixin.qualityfood;

import de.cadentem.quality_food.core.attachments.BlockData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockData.class, remap = false)
public abstract class QualityFoodCookingMixin {

    @Inject(method = { "useQuality", "incrementQuality", "addQualityType" }, at = @At("HEAD"), cancellable = true)
    private void cosmiccore$disableCookingQuality(CallbackInfo ci) {
        ci.cancel();
    }
}
