package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.CosmicUtils;
import com.ghostipedia.cosmiccore.client.renderer.StructureBoundingBox;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.shaders.FogShape;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientEventHandler {

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        var stage = event.getStage();
        if (stage == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            StructureBoundingBox.renderStructureSelect(event.getPoseStack(), event.getCamera());
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (CosmicUtils.hasTheOneRing(event.getCamera().getEntity())) {
            event.setFogShape(FogShape.SPHERE);

            // Shrink the fog to be very close
            if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
                event.setFarPlaneDistance(16.0F);
                event.setNearPlaneDistance(0.0F);
            } else {
                event.setFarPlaneDistance(10.0F);
                event.setNearPlaneDistance(3.0F);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (CosmicUtils.hasTheOneRing(event.getCamera().getEntity())) {
            // and make the fog a blue mist.
            // #7CBADA
            event.setRed(0.671F);
            event.setGreen(0.792F);
            event.setBlue(0.855F);
        }
    }
}
