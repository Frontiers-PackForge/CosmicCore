package com.ghostipedia.cosmiccore.client.util;

import com.gregtechceu.gtceu.client.util.SpriteFunctionWrapper;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Helper for {@link com.ghostipedia.cosmiccore.mixin.gtfix.ModelBakerImplCtmScraperFixMixin}.
 * <p>
 * Two reasons this lives outside the mixin:
 * <ol>
 * <li>The mixin must NOT reference GTCEu's {@link SpriteFunctionWrapper} directly in its added method body -
 * Mixin's preprocessor would try to resolve it while transforming {@code ModelBakerImpl} and throw
 * {@code ClassNotFoundException} at apply time. Hiding the {@code new SpriteFunctionWrapper(...)} here keeps it
 * out of mixin bytecode.</li>
 * <li>{@code SpriteFunctionWrapper} only exists in newer GTCEu builds. The pack may run an older GTCEu where it
 * is absent, so a direct call would {@code NoClassDefFoundError} at model-bake time. {@link #AVAILABLE} probes
 * for it once; if missing, {@link #wrap} returns the getter unwrapped (no CTM scrape, but no crash).</li>
 * </ol>
 */
public final class CtmTextureScraper {

    private static final boolean AVAILABLE = probe();

    private CtmTextureScraper() {}

    private static boolean probe() {
        try {
            Class.forName("com.gregtechceu.gtceu.client.util.SpriteFunctionWrapper", false,
                    CtmTextureScraper.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Wrap a model texture getter so GTCEu records the textures each model resolves (its CTM scrape). */
    public static Function<Material, TextureAtlasSprite> wrap(Function<Material, TextureAtlasSprite> getter,
                                                              ResourceLocation modelId) {
        if (!AVAILABLE) return getter;
        return new SpriteFunctionWrapper(getter, modelId);
    }
}
