package com.ghostipedia.cosmiccore.common.data.worldgen;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.tag.block.CosmicBlockTags;
import com.ghostipedia.cosmiccore.common.murkbloom.MurkbloomServerLogic;
import com.ghostipedia.cosmiccore.mixin.gtceu.SimpleWorldGenLayerLevelsAccessor;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.Set;

public class CosmicWorldGenLayers {

    public static SimpleWorldGenLayer OVERWORLD;
    public static SimpleWorldGenLayer HOLLOW;

    private static boolean initialized;

    public static void init() {
        if (initialized) return;
        initialized = true;

        OVERWORLD = new SimpleWorldGenLayer(
                CosmicCore.id("overworld"),
                () -> new TagMatchTest(CosmicBlockTags.OVERWORLD_ORE_REPLACEABLES),
                Set.of(Level.OVERWORLD));

        HOLLOW = new SimpleWorldGenLayer(
                CosmicCore.id("hollow"),
                () -> new TagMatchTest(CosmicBlockTags.HOLLOW_ORE_REPLACEABLES),
                Set.of(MurkbloomServerLogic.HOLLOW_DIM));

        ((SimpleWorldGenLayerLevelsAccessor) WorldGenLayers.STONE).cosmiccore$setLevels(Set.of());
        ((SimpleWorldGenLayerLevelsAccessor) WorldGenLayers.DEEPSLATE).cosmiccore$setLevels(Set.of());
    }

    public static SimpleWorldGenLayer hollow() {
        init();
        return HOLLOW;
    }

    public static void reassign(GTOreDefinition vein) {
        init();
        if (OVERWORLD == null) return;
        IWorldGenLayer currentLayer = vein.layer();
        if (currentLayer == WorldGenLayers.STONE || currentLayer == WorldGenLayers.DEEPSLATE) {
            vein.layer(OVERWORLD);
            vein.clusterSize(scaleIntProvider(vein.clusterSize()));
        }
    }

    private static IntProvider scaleIntProvider(IntProvider provider) {
        int min = provider.getMinValue();
        int max = provider.getMaxValue();

        int avgSize = (min + max) / 2;
        float multiplier = getClusterSizeMultiplier(avgSize);

        int scaledMin = Math.round(min * multiplier);
        int scaledMax = Math.round(max * multiplier);

        if (scaledMin > scaledMax) {
            int temp = scaledMin;
            scaledMin = scaledMax;
            scaledMax = temp;
        }

        if (scaledMin == scaledMax) {
            return ConstantInt.of(scaledMin);
        }
        return UniformInt.of(scaledMin, scaledMax);
    }

    private static float getClusterSizeMultiplier(int originalSize) {
        if (originalSize < 20) {
            return 2.0f;
        } else if (originalSize < 35) {
            return 1.75f;
        } else {
            return 1.5f;
        }
    }
}
