package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public enum HPCAModifier {

    RED(CosmicCore.id("block/overlay/machine/hpca/indicator_red")),
    YELLOW(CosmicCore.id("block/overlay/machine/hpca/indicator_yellow")),
    GREEN(CosmicCore.id("block/overlay/machine/hpca/indicator_green"));

    public static final HPCAModifier[] VALUES = values();

    public final ResourceLocation overlay;

    HPCAModifier(ResourceLocation overlay) {
        this.overlay = overlay;
    }

    public static HPCAModifier getRandomModifier(RandomSource random) {
        return VALUES[random.nextInt(VALUES.length)];
    }
}
