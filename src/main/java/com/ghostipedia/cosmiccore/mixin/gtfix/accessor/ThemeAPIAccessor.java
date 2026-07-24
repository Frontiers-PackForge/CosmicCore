package com.ghostipedia.cosmiccore.mixin.gtfix.accessor;

import brachy.modularui.api.ITheme;
import brachy.modularui.theme.ThemeAPI;
import brachy.modularui.utils.serialization.json.JsonBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = ThemeAPI.class, remap = false)
public interface ThemeAPIAccessor {

    @Accessor("themes")
    Object2ObjectMap<String, ITheme> cosmiccore$getThemes();

    @Accessor("defaultThemes")
    Object2ObjectMap<String, List<JsonBuilder>> cosmiccore$getDefaultThemes();

    @Accessor("jsonScreenThemes")
    Object2ObjectOpenHashMap<String, String> cosmiccore$getJsonScreenThemes();
}
