package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.client.renderer.item.CosmicItemRenderers;
import com.ghostipedia.cosmiccore.common.item.IHaloRender;
import com.lowdragmc.lowdraglib.Platform;
import net.minecraft.resources.ResourceLocation;

public record HaloItemBehavior(
        int haloSize,
        int haloColor,
        ResourceLocation haloTexture,
        boolean shouldDrawHalo,
        boolean shouldDrawPulse) implements IHaloRender {
    public HaloItemBehavior(
            int haloSize,
            int haloColor,
            ResourceLocation haloTexture,
            boolean shouldDrawHalo,
            boolean shouldDrawPulse) {
        this.haloSize = haloSize;
        this.haloColor = haloColor;
        this.haloTexture = haloTexture;
        this.shouldDrawHalo = shouldDrawHalo;
        this.shouldDrawPulse = shouldDrawPulse;
        if (Platform.isClient()) {
            CosmicItemRenderers.HALO_ITEM_RENDER.addTexture(haloTexture);
        }
    }
}
