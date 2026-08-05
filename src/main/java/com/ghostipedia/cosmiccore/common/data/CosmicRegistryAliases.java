package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Map;

public final class CosmicRegistryAliases {

    private static final Map<ResourceLocation, ResourceLocation> ABYSS_RENAMES = Map.ofEntries(
            rename("large_strange_crystal", "large_arcanite_cluster"),
            rename("strange_crystal", "arcanite_cluster"),
            rename("droop_strand", "ripe_abyss_vine"),
            rename("blighted_growth", "bloomrot_growth"),
            rename("blightroot", "bloomrot_tendrils"),
            rename("clinging_blight", "anchored_bloomrot"),
            rename("ditchbulb", "lantern_bulb"));

    private CosmicRegistryAliases() {}

    public static void register(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.BLOCK) && !event.getRegistryKey().equals(Registries.ITEM)) {
            return;
        }
        Registry<?> registry = event.getRegistry();
        ABYSS_RENAMES.forEach(registry::addAlias);
    }

    private static Map.Entry<ResourceLocation, ResourceLocation> rename(String from, String to) {
        return Map.entry(CosmicCore.id(from), CosmicCore.id(to));
    }
}
