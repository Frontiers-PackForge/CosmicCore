package com.ghostipedia.cosmiccore.common.data.worldgen;

import com.ghostipedia.cosmiccore.common.data.tag.block.CosmicBlockTags;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.Set;

public class CosmicWorldGenLayers {

    public static SimpleWorldGenLayer OVERWORLD;

    public static void init() {
        OVERWORLD = new SimpleWorldGenLayer(
                "overworld",
                () -> new TagMatchTest(CosmicBlockTags.OVERWORLD_ORE_REPLACEABLES),
                Set.of(Level.OVERWORLD.location()));

        WorldGenLayers.STONE.setLevels(Set.of());
        WorldGenLayers.DEEPSLATE.setLevels(Set.of());
    }

    public static void migrateOreVeinsToOverworldLayer() {
        // Initialize our vein definitions first
        CosmicOreVeins.init();

        // Migrate overworld veins to our unified layer and scale cluster sizes
        for (GTOreDefinition vein : GTRegistries.ORE_VEINS) {
            IWorldGenLayer currentLayer = vein.layer();
            if (currentLayer == WorldGenLayers.STONE || currentLayer == WorldGenLayers.DEEPSLATE) {
                vein.layer(OVERWORLD);
                vein.clusterSize(scaleIntProvider(vein.clusterSize()));
            }
        }

        // Apply our custom vein generators (replaces GT generators, disables veins we don't have)
        CosmicOreVeins.applyOverrides();
    }

    private static IntProvider scaleIntProvider(IntProvider provider) {
        int min = provider.getMinValue();
        int max = provider.getMaxValue();

        // Use a single multiplier based on the average size to avoid min > max issues
        int avgSize = (min + max) / 2;
        float multiplier = getClusterSizeMultiplier(avgSize);

        int scaledMin = Math.round(min * multiplier);
        int scaledMax = Math.round(max * multiplier);

        // Safety: ensure min <= max
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
        // Modest base multiplier - generators now handle their own style-specific sizing
        // This just provides a gentle boost, generators have minimums built in
        if (originalSize < 20) {
            return 2.0f;
        } else if (originalSize < 35) {
            return 1.75f;
        } else {
            return 1.5f;
        }
    }
}
