package com.ghostipedia.cosmiccore.client.aether;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import com.mojang.blaze3d.shaders.FogShape;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class AetherCurrentMedium {

    private static final ResourceLocation AETHER = ResourceLocation.fromNamespaceAndPath("aether", "the_aether");
    private static final float ABOVE_FULL = 6.0f;
    private static final float ABOVE_EDGE = 22.0f;
    private static final float BELOW_FULL = 10.0f;
    private static final float BELOW_EDGE = 28.0f;
    private static final float FOG_DISTANCE_BLEND = 0.55f;
    private static final float FOG_COLOR_BLEND = 0.35f;
    private static final float FOG_FAR = 96.0f;
    private static final float FOG_NEAR = 4.0f;
    private static final float FOG_RED = 0.025f;
    private static final float FOG_GREEN = 0.17f;
    private static final float FOG_BLUE = 0.32f;

    private AetherCurrentMedium() {}

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float medium = mediumFactor(event);
        if (medium <= 0.0f) return;

        float blend = medium * FOG_DISTANCE_BLEND;
        float currentNear = (float) event.getNearPlaneDistance();
        float currentFar = (float) event.getFarPlaneDistance();
        event.setNearPlaneDistance(Mth.lerp(blend, currentNear, Math.min(currentNear, FOG_NEAR)));
        event.setFarPlaneDistance(Mth.lerp(blend, currentFar, Math.min(currentFar, FOG_FAR)));
        event.setFogShape(FogShape.CYLINDER);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float medium = mediumFactor(event);
        if (medium <= 0.0f) return;

        float blend = medium * FOG_COLOR_BLEND;
        event.setRed(Mth.lerp(blend, (float) event.getRed(), FOG_RED));
        event.setGreen(Mth.lerp(blend, (float) event.getGreen(), FOG_GREEN));
        event.setBlue(Mth.lerp(blend, (float) event.getBlue(), FOG_BLUE));
    }

    private static float mediumFactor(ViewportEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !minecraft.level.dimension().location().equals(AETHER)) return 0.0f;
        if (event.getCamera().getFluidInCamera() != FogType.NONE) return 0.0f;

        double signedHeight = event.getCamera().getPosition().y - AetherSightWallRenderer.SEA_Y;
        if (signedHeight >= 0.0) {
            return descendingSmoothstep((float) signedHeight, ABOVE_FULL, ABOVE_EDGE);
        }
        return descendingSmoothstep((float) -signedHeight, BELOW_FULL, BELOW_EDGE);
    }

    private static float descendingSmoothstep(float value, float full, float edge) {
        float progress = Mth.clamp((value - full) / (edge - full), 0.0f, 1.0f);
        return 1.0f - progress * progress * (3.0f - 2.0f * progress);
    }
}
