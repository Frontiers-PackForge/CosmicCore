package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;

public class CosmicDynamicRenderHelpers {

    public static DynamicRender<?, ?> getHPCAIndicatorRender() {
        return HPCAIndicatorRender.INSTANCE;
    }

    public static DynamicRender<?, ?> getHellfireFoundryPartRender() {
        return new HellFireFoundryPartRender(CosmicBlocks.HIGHLY_CONDUCTIVE_FISSION_CASING.getDefaultState());
    }
}
