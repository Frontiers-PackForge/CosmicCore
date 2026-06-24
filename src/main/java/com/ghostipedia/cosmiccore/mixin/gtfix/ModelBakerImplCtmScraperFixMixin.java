package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.client.util.SpriteFunctionWrapper;

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

/**
 * TODO(gtm-upstream): GTCEu 8.0 connected textures (CTM) never form - GTCEu casings/blocks show the plain base
 * texture because no model is ever wrapped in {@code CTMBakedModel}.
 * <p>
 * Root cause is in GTCEu's own {@code core.mixins.client.ModelBakerImplMixin}. That mixin installs GTCEu's
 * {@link SpriteFunctionWrapper} (which records each texture a model resolves into
 * {@code ModelEventHelper.SCRAPED_TEXTURES}) via a {@code @ModifyVariable} on
 * {@code ModelBakery$ModelBakerImpl.bake(ResourceLocation, ModelState, Function)}. In 1.21.1 NeoForge that 3-arg
 * {@code bake} is NOT on the top-level block-baking path: {@code ModelBakery.bakeModels} calls
 * {@code ModelBakerImpl.bakeUncached(UnbakedModel, ModelState)}, which reads the un-wrapped
 * {@code this.modelTextureGetter} field directly and forwards to the 3-arg {@code bakeUncached} - bypassing
 * {@code bake} entirely. So {@code SCRAPED_TEXTURES} is empty for every standard block model,
 * {@code ModelEventHelper}'s bake listener computes {@code shouldWrap == false}, and no {@code CTMBakedModel} is
 * ever created -> CTM is completely dead.
 * <p>
 * Fix: install the scraper wrapper on the field itself at the end of the {@code ModelBakerImpl} constructor, so
 * every bake path ({@code bakeUncached} and {@code bake}) scrapes textures. {@link SpriteFunctionWrapper}
 * unwraps an already-wrapped function, so GTCEu's existing {@code bake} mixin can double-apply harmlessly.
 */
@Mixin(targets = { "net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl" }, remap = false)
public abstract class ModelBakerImplCtmScraperFixMixin {

    @Mutable
    @Shadow
    @Final
    private Function<Material, TextureAtlasSprite> modelTextureGetter;

    @Inject(method = "<init>(Lnet/minecraft/client/resources/model/ModelBakery$TextureGetter;Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
            at = @At("TAIL"),
            remap = false)
    private void cosmiccore$scrapeTopLevelBakeTextures(ModelBakery.TextureGetter textureGetter,
                                                       ModelResourceLocation modelLocation,
                                                       CallbackInfo ci) {
        this.modelTextureGetter = new SpriteFunctionWrapper(this.modelTextureGetter, modelLocation.id());
    }
}
