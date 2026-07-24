package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.mixin.gtfix.accessor.ThemeAPIAccessor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import brachy.modularui.ModularUI;
import brachy.modularui.api.ITheme;
import brachy.modularui.theme.ThemeAPI;
import brachy.modularui.theme.ThemeManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Map;

@Mixin(value = ThemeManager.class, remap = false)
public abstract class MUIThemeReloadLifecycleFixMixin {

    @Unique
    private Object2ObjectOpenHashMap<String, String> cosmiccore$preparedScreenThemes;

    @Redirect(
              method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;",
              at = @At(
                       value = "INVOKE",
                       target = "Lbrachy/modularui/theme/ThemeAPI;onReload()V"))
    private void cosmiccore$beginThemePreparation(ThemeAPI api) {
        this.cosmiccore$preparedScreenThemes = new Object2ObjectOpenHashMap<>();
    }

    @Redirect(
              method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;",
              at = @At(
                       value = "INVOKE",
                       target = "Lbrachy/modularui/theme/ThemeManager;loadScreenThemes(Lcom/google/gson/JsonObject;)V"))
    private void cosmiccore$stageScreenThemes(JsonObject json) {
        if (this.cosmiccore$preparedScreenThemes == null) {
            this.cosmiccore$preparedScreenThemes = new Object2ObjectOpenHashMap<>();
        }
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getValue().isJsonPrimitive()) {
                this.cosmiccore$preparedScreenThemes.put(entry.getKey(), entry.getValue().getAsString());
            } else {
                ModularUI.LOGGER.error("Theme screen definitions must be strings!");
            }
        }
    }

    @WrapMethod(
                method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
    private void cosmiccore$applyThemesTransactionally(Map<String, List<ResourceLocation>> preparedThemes,
                                                       ResourceManager resourceManager, ProfilerFiller profiler,
                                                       Operation<Void> original) {
        ThemeAPIAccessor api = (ThemeAPIAccessor) (Object) ThemeAPI.INSTANCE;
        Object2ObjectMap<String, ITheme> liveThemes = api.cosmiccore$getThemes();
        Object2ObjectOpenHashMap<String, String> liveScreenThemes = api.cosmiccore$getJsonScreenThemes();
        Object2ObjectOpenHashMap<String, ITheme> previousThemes = new Object2ObjectOpenHashMap<>(liveThemes);
        Object2ObjectOpenHashMap<String, String> previousScreenThemes = new Object2ObjectOpenHashMap<>(
                liveScreenThemes);

        liveThemes.clear();
        liveThemes.put(ThemeAPI.DEFAULT_ID, ThemeAPI.DEFAULT_THEME);
        if (this.cosmiccore$preparedScreenThemes != null) {
            liveScreenThemes.clear();
            liveScreenThemes.putAll(this.cosmiccore$preparedScreenThemes);
        }

        try {
            original.call(preparedThemes, resourceManager, profiler);
            String lostRegisteredTheme = api.cosmiccore$getDefaultThemes().keySet().stream()
                    .filter(id -> previousThemes.containsKey(id) && !liveThemes.containsKey(id))
                    .findFirst()
                    .orElse(null);
            if (lostRegisteredTheme != null) {
                cosmiccore$restoreThemes(liveThemes, previousThemes, liveScreenThemes, previousScreenThemes);
                ModularUI.LOGGER.warn(
                        "Theme reload did not rebuild Java-registered theme '{}'; restored the previous theme snapshot",
                        lostRegisteredTheme);
            }
        } catch (RuntimeException | Error throwable) {
            cosmiccore$restoreThemes(liveThemes, previousThemes, liveScreenThemes, previousScreenThemes);
            throw throwable;
        } finally {
            this.cosmiccore$preparedScreenThemes = null;
        }
    }

    @Unique
    private static void cosmiccore$restoreThemes(Object2ObjectMap<String, ITheme> liveThemes,
                                                 Object2ObjectMap<String, ITheme> previousThemes,
                                                 Object2ObjectOpenHashMap<String, String> liveScreenThemes,
                                                 Object2ObjectOpenHashMap<String, String> previousScreenThemes) {
        liveThemes.clear();
        liveThemes.putAll(previousThemes);
        liveScreenThemes.clear();
        liveScreenThemes.putAll(previousScreenThemes);
    }
}
