package com.ghostipedia.cosmiccore.integration.emi;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.PanelManager;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.systems.RenderSystem;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ModularUIEmiScreenCacheLifecycle {

    private static volatile LoadingCache<ModularUIEmiRecipe, ModularScreen> screenCache;
    private static int cleanupTicks;

    private ModularUIEmiScreenCacheLifecycle() {}

    public static void bind(LoadingCache<ModularUIEmiRecipe, ModularScreen> cache) {
        screenCache = cache;
    }

    public static void dispose(ModularScreen screen) {
        if (screen == null) return;
        Runnable disposal = () -> {
            PanelManager manager = screen.getPanelManager();
            if (manager.isDisposed()) return;
            if (manager.isOpen()) {
                manager.closeAll();
            }
            if (manager.isClosed() && !manager.isDisposed()) {
                manager.dispose();
            }
        };
        if (RenderSystem.isOnRenderThread()) {
            disposal.run();
        } else {
            RenderSystem.recordRenderCall(disposal::run);
        }
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        invalidate();
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        invalidate();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        invalidate();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LoadingCache<ModularUIEmiRecipe, ModularScreen> cache = screenCache;
        if (cache == null || cache.size() == 0L) {
            cleanupTicks = 0;
            return;
        }
        if (++cleanupTicks < 20) return;
        cleanupTicks = 0;
        cache.cleanUp();
    }

    private static void invalidate() {
        GTEmiRecipeBounds.clearCache();
        LoadingCache<ModularUIEmiRecipe, ModularScreen> cache = screenCache;
        if (cache == null) return;
        cache.invalidateAll();
        cache.cleanUp();
    }
}
