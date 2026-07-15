package com.ghostipedia.cosmiccore.mixin.gtfix.emi;

import com.ghostipedia.cosmiccore.integration.emi.MultiblockPreviewSchemaCache;

import com.gregtechceu.gtceu.integration.recipeviewer.emi.MultiblockInfoEmiCategory;

import dev.emi.emi.api.EmiRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MultiblockInfoEmiCategory.class, remap = false)
public class MultiblockInfoEmiCategorySchemaCacheMixin {

    @Inject(method = "registerDisplays", at = @At("HEAD"), remap = false)
    private static void cosmiccore$clearPreparedSchemas(EmiRegistry registry, CallbackInfo ci) {
        MultiblockPreviewSchemaCache.clear();
    }
}
