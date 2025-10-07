package com.ghostipedia.cosmiccore.common.ascension;

import com.ghostipedia.cosmiccore.CosmicCore;
import net.minecraft.resources.ResourceLocation;

public enum AscensionConsumables {

    SOUL(CosmicCore.id("soul_soul")),
    HEART(CosmicCore.id("soul_heart")),
    WRATH(CosmicCore.id("soul_wrath")),
    PRIDE(CosmicCore.id("soul_pride")),
    LUST(CosmicCore.id("soul_lust"));

    public final ResourceLocation id;
    AscensionConsumables(ResourceLocation id){
        this.id = id;
    }


}
