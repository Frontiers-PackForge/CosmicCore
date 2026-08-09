package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.ModularUIEmiScreenCacheLifecycle;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import brachy.modularui.screen.EmbedHandler;
import brachy.modularui.screen.ModularScreen;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModularUIEmiRecipe.class, remap = false)
public class ModularUIEmiScreenCacheMixin {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Redirect(method = "<clinit>",
              at = @At(value = "INVOKE",
                       target = "Lcom/google/common/cache/CacheBuilder;build(Lcom/google/common/cache/CacheLoader;)Lcom/google/common/cache/LoadingCache;"),
              remap = false)
    private static LoadingCache cosmiccore$buildCache(CacheBuilder builder, CacheLoader loader) {
        LoadingCache cache = builder
                .removalListener((RemovalListener<Object, ModularScreen>) notification -> {
                    ModularScreen screen = notification.getValue();
                    ModularUIEmiScreenCacheLifecycle.dispose(screen);
                })
                .build(loader);
        ModularUIEmiScreenCacheLifecycle.bind(cache);
        return cache;
    }

    @Redirect(method = "calculateSize",
              at = @At(value = "INVOKE",
                       target = "Lbrachy/modularui/screen/EmbedHandler;getEmbedHeight(Lbrachy/modularui/screen/ModularScreen;)I"),
              remap = false)
    private int cosmiccore$disposeMeasurementScreen(ModularScreen screen) {
        try {
            return EmbedHandler.getEmbedHeight(screen);
        } finally {
            ModularUIEmiScreenCacheLifecycle.dispose(screen);
        }
    }
}
