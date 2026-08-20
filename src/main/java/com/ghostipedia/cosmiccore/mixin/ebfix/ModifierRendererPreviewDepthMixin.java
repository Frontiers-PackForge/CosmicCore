package com.ghostipedia.cosmiccore.mixin.ebfix;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import neoforge.nl.requios.effortlessbuilding.render.ModifierRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModifierRenderer.class)
public abstract class ModifierRendererPreviewDepthMixin {

    @Redirect(
              method = { "render", "renderMirrorPlanes", "renderRadialBoundary" },
              at = @At(
                       value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"),
              require = 3)
    private static RenderType cosmiccore$separateModifierPlaneBatch(ResourceLocation texture) {
        return RenderType.entityTranslucentEmissive(texture);
    }
}
