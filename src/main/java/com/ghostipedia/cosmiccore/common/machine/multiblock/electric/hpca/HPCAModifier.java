package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca;

import com.ghostipedia.cosmiccore.CosmicCore;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

public enum HPCAModifier {
    RED(CosmicCore.id("block/overlay/machine/hpca/indicator_red")),
    YELLOW(CosmicCore.id("block/overlay/machine/hpca/indicator_yellow")),
    GREEN(CosmicCore.id("block/overlay/machine/hpca/indicator_green"));

    public final ResourceLocation overlay;

    HPCAModifier(ResourceLocation overlay) {
        this.overlay = overlay;
    }

    public static HPCAModifier getRandomModifier(Random rand) {
        var values = HPCAModifier.values();
        return values[rand.nextInt(values.length)];
    }
}
