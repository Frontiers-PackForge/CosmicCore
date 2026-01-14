package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;

import dev.emi.emi.runtime.EmiPersistentData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiPersistentData.class, remap = false)
public class EmiPersistentDataMixin {

    @Inject(method = "load", at = @At("RETURN"))
    private static void cosmiccore$afterLoad(CallbackInfo ci) {
        CosmicBookmarkManager.getInstance().reload();
    }
}
