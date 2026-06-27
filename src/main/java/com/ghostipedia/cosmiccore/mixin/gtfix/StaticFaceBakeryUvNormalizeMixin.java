package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.client.model.quad.StaticFaceBakery;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = StaticFaceBakery.class, remap = false)
public class StaticFaceBakeryUvNormalizeMixin {

    @Redirect(
              method = "fillVertex",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;getU(F)F"),
              require = 1)
    private static float cosmiccore$normalizeU(TextureAtlasSprite sprite, float u) {
        return sprite.getU(u / 16.0F);
    }

    @Redirect(
              method = "fillVertex",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;getV(F)F"),
              require = 1)
    private static float cosmiccore$normalizeV(TextureAtlasSprite sprite, float v) {
        return sprite.getV(v / 16.0F);
    }
}
