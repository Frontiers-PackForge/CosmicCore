package com.ghostipedia.cosmiccore.common.data.worldgen;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.tag.block.CosmicBlockTags;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.RegistryAccess;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.Set;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public class CosmicWorldGenLayers {

    public static SimpleWorldGenLayer OVERWORLD;

    public static void init() {
        OVERWORLD = new SimpleWorldGenLayer(
                "overworld",
                () -> new TagMatchTest(CosmicBlockTags.OVERWORLD_ORE_REPLACEABLES),
                Set.of(Level.OVERWORLD));

        WorldGenLayers.STONE.setLevels(Set.of());
        WorldGenLayers.DEEPSLATE.setLevels(Set.of());
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        // GTCEu 8.0 turned ore veins into a datapack registry (gtceu:ore_vein) that is only populated
        // once datapacks load. AddReloadListenerEvent is where GTCEu itself refreshes its frozen registry,
        // so it is the earliest point our vein post-processing can safely read gtceu:ore_vein.
        migrateOreVeinsToOverworldLayer(event.getRegistryAccess());
    }

    public static void migrateOreVeinsToOverworldLayer(RegistryAccess registryAccess) {
        // Initialize our vein definitions first
        CosmicOreVeins.init();

        var registry = registryAccess.registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY);
        for (var holder : registry.holders().toList()) {
            GTOreDefinition vein = holder.value();
            IWorldGenLayer currentLayer = vein.layer();
            if (currentLayer == WorldGenLayers.STONE || currentLayer == WorldGenLayers.DEEPSLATE) {
                vein.layer(OVERWORLD);
                vein.clusterSize(scaleIntProvider(vein.clusterSize()));
            }
        }

        // Apply our custom vein generators (replaces GT generators, disables veins we don't have)
        CosmicOreVeins.applyOverrides(registryAccess);
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
