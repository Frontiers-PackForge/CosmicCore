package com.ghostipedia.cosmiccore.client.renderer.item;

import com.ghostipedia.cosmiccore.api.item.component.HaloItemRenderData;
import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;
import com.ghostipedia.cosmiccore.api.item.component.LensItemRenderData;
import com.ghostipedia.cosmiccore.api.item.component.RadianceItemRenderData;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class CosmicItemRenderers {

    private CosmicItemRenderers() {}

    @Nullable
    public static CosmicItemRenderer resolve(ICustomRenderer renderData) {
        if (renderData instanceof HaloItemRenderData) {
            return HaloItemRenderer.INSTANCE;
        }
        if (renderData == RadianceItemRenderData.INSTANCE) {
            return RadianceItemRenderer.INSTANCE;
        }
        if (renderData == LensItemRenderData.INSTANCE) {
            return LensRender.INSTANCE;
        }
        return null;
    }
}
