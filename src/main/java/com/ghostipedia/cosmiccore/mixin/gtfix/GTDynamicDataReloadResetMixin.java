package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GTCraftingComponents.class, remap = false)
public abstract class GTDynamicDataReloadResetMixin {

    @Inject(method = "init", at = @At("HEAD"))
    private static void cosmiccore$clearGtDynamicData(CallbackInfo ci) {
        GTDynamicDataPack.clearServer();
    }
}
