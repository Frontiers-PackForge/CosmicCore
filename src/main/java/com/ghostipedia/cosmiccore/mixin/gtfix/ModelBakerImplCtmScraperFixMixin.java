package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.client.util.CtmTextureScraper;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(targets = { "net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl" }, remap = false)
public abstract class ModelBakerImplCtmScraperFixMixin {

    @Mutable
    @Shadow
    @Final
    private Function<Material, TextureAtlasSprite> modelTextureGetter;

    @Inject(method = "<init>(Lnet/minecraft/client/resources/model/ModelBakery;Lnet/minecraft/client/resources/model/ModelBakery$TextureGetter;Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
            at = @At("TAIL"),
            remap = false,
            require = 0)
    private void cosmiccore$scrapeTopLevelBakeTextures(ModelBakery outer,
                                                       ModelBakery.TextureGetter textureGetter,
                                                       ModelResourceLocation modelLocation,
                                                       CallbackInfo ci) {
        this.modelTextureGetter = CtmTextureScraper.wrap(this.modelTextureGetter, modelLocation.id());
    }
}
