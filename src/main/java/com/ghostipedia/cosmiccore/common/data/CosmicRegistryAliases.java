package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
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

    private static final List<String> BUZZSAW_MATERIALS = List.of(
            "aluminium",
            "blue_steel",
            "bronze",
            "cobalt_brass",
            "damascus_steel",
            "diamond",
            "duranium",
            "hsse",
            "invar",
            "iron",
            "naquadah_alloy",
            "neutronium",
            "red_steel",
            "rose_gold",
            "stainless_steel",
            "steel",
            "sterling_silver",
            "titanium",
            "tungsten_carbide",
            "tungsten_steel",
            "ultimet",
            "vanadium_steel",
            "wrought_iron");

    private static final Map<ResourceLocation, ResourceLocation> ELEMENT_RENAMES = Map.ofEntries(
            gtRename("helium-_3", "helium-3"),
            gtRename("uranium-_235", "uranium-235"),
            gtRename("uranium-_238", "uranium-238"),
            gtRename("plutonium-_239", "plutonium-239"),
            gtRename("plutonium-_241", "plutonium-241"),
            gtRename("naquadah_enriched", "enriched_naquadah"));

    private CosmicRegistryAliases() {}

    public static void register(RegisterEvent event) {
        Registry<?> registry = event.getRegistry();
        registerPetrochemicalAliases(event, registry);
        if (event.getRegistryKey().equals(Registries.BLOCK) || event.getRegistryKey().equals(Registries.ITEM)) {
            ABYSS_RENAMES.forEach((from, to) -> addAlias(registry, from, to));
        }
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            BUZZSAW_MATERIALS.forEach(material -> addAlias(registry,
                    gt(material + "_buzz_saw_blade"), gt(material + "_buzzsaw_blade")));
        }
        if (event.getRegistryKey().equals(GTRegistries.Keys.TAG_PREFIX)) {
            addAlias(registry, gt("buzz_saw_blade"), gt("buzzsaw_blade"));
        }
        if (event.getRegistryKey().equals(GTRegistries.Keys.ELEMENT)) {
            ELEMENT_RENAMES.forEach((from, to) -> addAlias(registry, from, to));
        }
    }

    private static void registerPetrochemicalAliases(RegisterEvent event, Registry<?> registry) {
        if (event.getRegistryKey().equals(GTRegistries.Keys.MATERIAL)) {
            GTPetrochemicalRegistryKeys.REKEYS.forEach((from, to) -> addAlias(registry, gt(from), gt(to)));
            addAlias(registry, CosmicCore.id("multi_phase_oil"), gt("multi_phase_oil"));
        }
        if (event.getRegistryKey().equals(Registries.FLUID)) {
            GTPetrochemicalRegistryKeys.REKEYS.forEach((from, to) -> {
                addAlias(registry, gt(from), gt(to));
                addAlias(registry, gt("flowing_" + from), gt("flowing_" + to));
            });
            addAlias(registry, CosmicCore.id("multi_phase_oil"), gt("multi_phase_oil"));
            addAlias(registry, CosmicCore.id("flowing_multi_phase_oil"), gt("flowing_multi_phase_oil"));
        }
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            GTPetrochemicalRegistryKeys.REKEYS
                    .forEach((from, to) -> addAlias(registry, gt(from + "_bucket"), gt(to + "_bucket")));
            addAlias(registry, CosmicCore.id("multi_phase_oil_bucket"), gt("multi_phase_oil_bucket"));
        }
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            addAlias(registry, gt("raw_oil"), gt("multi_phase_oil"));
        }
        if (event.getRegistryKey().equals(NeoForgeRegistries.Keys.FLUID_TYPES)) {
            GTPetrochemicalRegistryKeys.REKEYS.forEach((from, to) -> addAlias(registry, gt(from), gt(to)));
            addAlias(registry, CosmicCore.id("multi_phase_oil"), gt("multi_phase_oil"));
        }
    }

    public static void validatePetrochemicalAliases() {
        GTPetrochemicalRegistryKeys.REKEYS.forEach((from, to) -> {
            validateAlias(GTRegistries.MATERIALS, gt(from), gt(to));
            validateAlias(BuiltInRegistries.FLUID, gt(from), gt(to));
            validateAlias(BuiltInRegistries.FLUID, gt("flowing_" + from), gt("flowing_" + to));
            validateAlias(BuiltInRegistries.ITEM, gt(from + "_bucket"), gt(to + "_bucket"));
            validateAlias(NeoForgeRegistries.FLUID_TYPES, gt(from), gt(to));
        });
        validateAlias(GTRegistries.MATERIALS, CosmicCore.id("multi_phase_oil"), gt("multi_phase_oil"));
        validateAlias(BuiltInRegistries.FLUID, CosmicCore.id("multi_phase_oil"), gt("multi_phase_oil"));
        validateAlias(BuiltInRegistries.FLUID, CosmicCore.id("flowing_multi_phase_oil"),
                gt("flowing_multi_phase_oil"));
        validateAlias(BuiltInRegistries.ITEM, CosmicCore.id("multi_phase_oil_bucket"),
                gt("multi_phase_oil_bucket"));
        validateAlias(NeoForgeRegistries.FLUID_TYPES, CosmicCore.id("multi_phase_oil"), gt("multi_phase_oil"));
        validateAlias(BuiltInRegistries.BLOCK, gt("raw_oil"), gt("multi_phase_oil"));
        CosmicCore.LOGGER.info("Validated petrochemical material, fluid, bucket, block, and fluid-type aliases.");
    }

    private static void validateAlias(Registry<?> registry, ResourceLocation from, ResourceLocation to) {
        ResourceLocation resolved = registry.resolve(from);
        if (!resolved.equals(to)) {
            throw new IllegalStateException(
                    "Registry alias " + from + " resolves to " + resolved + " instead of " + to);
        }
    }

    private static Map.Entry<ResourceLocation, ResourceLocation> rename(String from, String to) {
        return Map.entry(CosmicCore.id(from), CosmicCore.id(to));
    }

    private static Map.Entry<ResourceLocation, ResourceLocation> gtRename(String from, String to) {
        return Map.entry(gt(from), gt(to));
    }

    private static ResourceLocation gt(String path) {
        return ResourceLocation.fromNamespaceAndPath("gtceu", path);
    }

    private static void addAlias(Registry<?> registry, ResourceLocation from, ResourceLocation to) {
        if (registry.containsKey(from)) return;
        ResourceLocation resolved = registry.resolve(from);
        if (resolved.equals(to)) return;
        if (!resolved.equals(from)) {
            CosmicCore.LOGGER.warn("Skipping registry alias {} -> {} because {} already resolves to {}",
                    from, to, from, resolved);
            return;
        }
        registry.addAlias(from, to);
    }
}
