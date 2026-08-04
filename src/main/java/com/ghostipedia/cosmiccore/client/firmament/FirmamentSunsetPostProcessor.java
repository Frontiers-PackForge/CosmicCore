package com.ghostipedia.cosmiccore.client.firmament;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.compat.IrisCompat;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.post.PostProcessingManager;

public final class FirmamentSunsetPostProcessor {

    private static final ResourceLocation PIPELINE = CosmicCore.id("firmament_sunset");
    private static boolean active;

    private FirmamentSunsetPostProcessor() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FirmamentSunsetPostProcessor::onClientTick);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean shouldBeActive = minecraft.level != null &&
                minecraft.level.dimension().equals(FirmamentDimension.KEY) &&
                !IrisCompat.shadersActive();
        if (active == shouldBeActive) return;

        PostProcessingManager manager = VeilRenderSystem.renderer().getPostProcessingManager();
        if (shouldBeActive) {
            manager.add(1_500, PIPELINE);
        } else {
            manager.remove(PIPELINE);
        }
        active = shouldBeActive;
    }
}
