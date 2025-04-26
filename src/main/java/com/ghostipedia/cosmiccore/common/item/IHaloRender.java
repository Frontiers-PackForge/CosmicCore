package com.ghostipedia.cosmiccore.common.item;

import com.ghostipedia.cosmiccore.client.renderer.item.CosmicItemRenderers;
import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface IHaloRender extends ICustomRenderer {

    ResourceLocation haloTexture();
    boolean shouldDrawHalo();
    int haloColor();
    int haloSize();
    boolean shouldDrawPulse();

    @Override
    default @NotNull IRenderer getRenderer() {
        return CosmicItemRenderers.HALO_ITEM_RENDER;
    }
}
