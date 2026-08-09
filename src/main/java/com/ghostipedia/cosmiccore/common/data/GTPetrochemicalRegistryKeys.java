package com.ghostipedia.cosmiccore.common.data;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public final class GTPetrochemicalRegistryKeys {

    public static final Map<String, String> REKEYS = Map.ofEntries(
            Map.entry("raw_oil", "multi_phase_oil"),
            Map.entry("sulfuric_gas", "sour_refinery_gas"),
            Map.entry("sulfuric_naphtha", "sour_naphtha"),
            Map.entry("sulfuric_light_fuel", "sour_middle_fraction_distillates"),
            Map.entry("sulfuric_heavy_fuel", "sour_gas_oils"),
            Map.entry("naphtha", "light_naphtha"),
            Map.entry("light_fuel", "middle_fraction_distillates"),
            Map.entry("heavy_fuel", "gas_oils"),
            Map.entry("coal_gas", "raw_coking_gas"),
            Map.entry("dimethylbenzene", "mixed_xylenes"));

    private GTPetrochemicalRegistryKeys() {}

    public static ResourceLocation canonicalGtId(String path) {
        return ResourceLocation.fromNamespaceAndPath("gtceu", REKEYS.getOrDefault(path, path));
    }

    public static String legacyTexturePath(String path) {
        String prefix = "block/fluids/fluid.";
        if (!path.startsWith(prefix)) return path;
        String name = path.substring(prefix.length());
        for (var entry : REKEYS.entrySet()) {
            if (name.equals(entry.getValue())) return prefix + entry.getKey();
            if (name.equals(entry.getValue() + "_flow")) return prefix + entry.getKey() + "_flow";
        }
        return path;
    }
}
