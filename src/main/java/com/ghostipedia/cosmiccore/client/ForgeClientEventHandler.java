package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.CosmicUtils;
import com.ghostipedia.cosmiccore.client.map.RevealedFieldStorage;
import com.ghostipedia.cosmiccore.client.map.RevealedFields;
import com.ghostipedia.cosmiccore.client.renderer.RingUpgradePreviewRenderer;
import com.ghostipedia.cosmiccore.client.renderer.StructureBoundingBox;

import net.minecraft.client.renderer.FogRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import com.mojang.blaze3d.shaders.FogShape;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ForgeClientEventHandler {

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        var stage = event.getStage();
        if (stage == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            StructureBoundingBox.renderStructureSelect(event.getPoseStack(), event.getCamera());
            RingUpgradePreviewRenderer.renderPreviews(event.getPoseStack(), event.getCamera());
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        // Clear all previews when world unloads to prevent stale data
        if (event.getLevel().isClientSide()) {
            RingUpgradePreviewRenderer.clearAllPreviews();
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        RevealedFields.INSTANCE.clearAll();
        RevealedFieldStorage.reset();
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

    // @SubscribeEvent
    // public static void onTooltipEvent(ItemTooltipEvent event) {
    // CosmicFluidTooltipAddon.appendFluidTooltip(event.getItemStack());
    // }
}
