package com.ghostipedia.cosmiccore.client.renderer.utility;

import com.ghostipedia.cosmiccore.client.renderer.machine.*;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;

public class CosmicDynamicRenderHelpers {

    public static DynamicRender<?, ?> getHPCAIndicatorRender() {
        return HPCAIndicatorRender.INSTANCE;
    }

    public static DynamicRender<?, ?> getHemophagicTransfuserRender() {
        return HemophagicTransfuserRender.INSTANCE;
    }

    public static DynamicRender<?, ?> getStellarIrisRender() {
        return StellarIrisRender.INSTANCE;
    }

    public static DynamicRender<?, ?> getConceptIncinerator() {
        return ConceptIncineratorRender.INSTANCE;
    }

    public static DynamicRender<?, ?> getStarBallastRender() {
        return StarBallastRender.INSTANCE;
    }

    public static DynamicRender<?, ?> createHellfireFoundryPartRender() {
        return new HellFireFoundryPartRender(CosmicBlocks.HIGHLY_CONDUCTIVE_FISSION_CASING.getDefaultState());
    }

    public static DynamicRender<?, ?> getSufferingChamberRenderer() {
        return SufferingChamberRenderer.INSTANCE;
    }

    public static DynamicRender<?, ?> getWelderArmsRenderer() {
        return WelderArmRender.INSTANCE;
    }

    public static DynamicRender<?, ?> getBioVatFluidRender() {
        return BioVatRender.INSTANCE;
    }
}
