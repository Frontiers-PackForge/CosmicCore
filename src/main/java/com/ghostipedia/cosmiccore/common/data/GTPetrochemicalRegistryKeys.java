package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public final class GTPetrochemicalRegistryKeys {

    public static final Map<String, ResourceLocation> REKEYS = Map.ofEntries(
            gtRekey("raw_oil", "multi_phase_oil"),
            gtRekey("sulfuric_gas", "sour_refinery_gas"),
            gtRekey("sulfuric_naphtha", "sour_naphtha"),
            gtRekey("sulfuric_light_fuel", "sour_middle_fraction_distillates"),
            gtRekey("sulfuric_heavy_fuel", "sour_gas_oils"),
            gtRekey("naphtha", "light_naphtha"),
            gtRekey("light_fuel", "middle_fraction_distillates"),
            gtRekey("heavy_fuel", "gas_oils"),
            gtRekey("coal_gas", "raw_coking_gas"),
            gtRekey("dimethylbenzene", "mixed_xylenes"),
            Map.entry("charcoal_byproducts", CosmicCore.id("hot_pyrolysis_vapors")));

    private GTPetrochemicalRegistryKeys() {}

    public static ResourceLocation canonicalId(String path) {
        return REKEYS.getOrDefault(path, gt(path));
    }

    public static ResourceLocation legacyTextureLocation(String namespace, String path) {
        String prefix = "block/fluids/fluid.";
        if (!path.startsWith(prefix)) return ResourceLocation.fromNamespaceAndPath(namespace, path);
        String name = path.substring(prefix.length());
        for (var entry : REKEYS.entrySet()) {
            if (!namespace.equals(entry.getValue().getNamespace())) continue;
            if (name.equals(entry.getValue().getPath())) return gt(prefix + entry.getKey());
            if (name.equals(entry.getValue().getPath() + "_flow")) return gt(prefix + entry.getKey() + "_flow");
        }
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private static Map.Entry<String, ResourceLocation> gtRekey(String from, String to) {
        return Map.entry(from, gt(to));
    }

    private static ResourceLocation gt(String path) {
        return ResourceLocation.fromNamespaceAndPath("gtceu", path);
    }
}
