package com.ghostipedia.cosmiccore.mixin.qualityfood;

import de.cadentem.quality_food.util.QualityUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QualityUtils.class, remap = false)
public abstract class QualityFoodAcquisitionMixin {

    @Inject(method = {
            "applyQuality(Lnet/minecraft/world/item/ItemStack;Ljava/util/Collection;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/RegistryAccess;)V",
            "applyQuality(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Lde/cadentem/quality_food/core/codecs/Quality;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/RegistryAccess;)V",
            "applyQuality(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/RegistryAccess;)V",
            "handleConversion"
    }, at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$disableAutomaticQuality(CallbackInfo ci) {
        ci.cancel();
    }
}
