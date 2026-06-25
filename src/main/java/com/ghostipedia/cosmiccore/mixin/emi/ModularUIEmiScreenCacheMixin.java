package com.ghostipedia.cosmiccore.mixin.emi;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import brachy.modularui.screen.ModularScreen;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.time.Duration;

@Mixin(value = ModularUIEmiRecipe.class, remap = false)
public class ModularUIEmiScreenCacheMixin {

    @SuppressWarnings("rawtypes")
    @Redirect(method = "<clinit>",
              at = @At(value = "INVOKE",
                       target = "Lcom/google/common/cache/CacheBuilder;expireAfterAccess(Ljava/time/Duration;)Lcom/google/common/cache/CacheBuilder;"),
              remap = false)
    private static CacheBuilder cosmiccore$expire(CacheBuilder builder, Duration original) {
        return builder.expireAfterAccess(Duration.ofSeconds(30L));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Redirect(method = "<clinit>",
              at = @At(value = "INVOKE",
                       target = "Lcom/google/common/cache/CacheBuilder;maximumSize(J)Lcom/google/common/cache/CacheBuilder;"),
              remap = false)
    private static CacheBuilder cosmiccore$retune(CacheBuilder builder, long original) {
        return builder.maximumSize(64L)
                .removalListener((RemovalListener<Object, ModularScreen>) notification -> {
                    ModularScreen screen = notification.getValue();
                    if (screen == null) return;
                    if (RenderSystem.isOnRenderThread()) {
                        if (screen.getPanelManager().closeAll()) {
                            screen.getPanelManager().dispose();
                        }
                    } else {
                        RenderSystem.recordRenderCall(() -> {
                            if (screen.getPanelManager().closeAll()) {
                                screen.getPanelManager().dispose();
                            }
                        });
                    }
                });
    }
}
