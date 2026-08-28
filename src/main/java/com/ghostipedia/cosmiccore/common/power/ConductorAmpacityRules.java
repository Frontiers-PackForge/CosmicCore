package com.ghostipedia.cosmiccore.common.power;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public final class ConductorAmpacityRules {

    private static final int BASE_TIER_CAP = 32;
    private static final Map<String, Integer> BASE_AMPERAGE_BY_MATERIAL = Map.ofEntries(
            Map.entry("cosmiccore:chronon", 64),
            Map.entry("cosmiccore:infinity", 16),
            Map.entry("cosmiccore:living_igniclad", 4),
            Map.entry("cosmiccore:naquadric_superalloy", 6),
            Map.entry("cosmiccore:neutronite", 12),
            Map.entry("cosmiccore:nevramite", 4),
            Map.entry("cosmiccore:prismatic_tungstensteel", 4),
            Map.entry("cosmiccore:resonant_virtue_meld", 6),
            Map.entry("cosmiccore:sol_steel", 4),
            Map.entry("cosmiccore:starmetal", 4),
            Map.entry("cosmiccore:temmerite", 64),
            Map.entry("cosmiccore:trinavine", 6),
            Map.entry("gtceu:aluminium", 1),
            Map.entry("gtceu:annealed_copper", 1),
            Map.entry("gtceu:black_steel", 3),
            Map.entry("gtceu:blue_alloy", 2),
            Map.entry("gtceu:chrysanthium", 1),
            Map.entry("gtceu:cobalt", 2),
            Map.entry("gtceu:copper", 1),
            Map.entry("gtceu:cupronickel", 1),
            Map.entry("gtceu:dark_steel", 1),
            Map.entry("gtceu:electrum", 3),
            Map.entry("gtceu:elementium", 3),
            Map.entry("gtceu:enriched_naquadah_trinium_europium_duranide", 16),
            Map.entry("gtceu:europium", 2),
            Map.entry("cosmiccore:galvanized_ethersteel", 6),
            Map.entry("gtceu:gold", 1),
            Map.entry("gtceu:graphene", 4),
            Map.entry("gtceu:hssg", 4),
            Map.entry("gtceu:indium_tin_barium_titanium_cuprate", 7),
            Map.entry("gtceu:kanthal", 4),
            Map.entry("gtceu:lead", 1),
            Map.entry("gtceu:magnesium_diboride", 4),
            Map.entry("gtceu:manasteel", 2),
            Map.entry("gtceu:mercury_barium_calcium_cuprate", 4),
            Map.entry("gtceu:naquadah", 2),
            Map.entry("gtceu:naquadah_alloy", 2),
            Map.entry("gtceu:nichrome", 4),
            Map.entry("gtceu:niobium_nitride", 1),
            Map.entry("gtceu:niobium_titanium", 3),
            Map.entry("gtceu:osmium", 5),
            Map.entry("gtceu:platinum", 2),
            Map.entry("gtceu:psi_superconductor_alpha", 8),
            Map.entry("gtceu:psi_superconductor_beta", 8),
            Map.entry("gtceu:psi_superconductor_eterna", 2048),
            Map.entry("gtceu:psi_superconductor_primordia", 8),
            Map.entry("gtceu:red_alloy", 1),
            Map.entry("gtceu:ruthenium_trinium_americium_neutronate", 24),
            Map.entry("gtceu:samarium_iron_arsenic_oxide", 6),
            Map.entry("gtceu:silver", 2),
            Map.entry("gtceu:terrasteel", 9),
            Map.entry("gtceu:tin", 1),
            Map.entry("gtceu:trinium", 5),
            Map.entry("gtceu:tritanium", 1),
            Map.entry("gtceu:tungsten", 2),
            Map.entry("gtceu:tungsten_steel", 3),
            Map.entry("gtceu:uranium_rhodium_dinaquadide", 8),
            Map.entry("gtceu:uranium_triplatinum", 2),
            Map.entry("gtceu:vanadium_gallium", 4),
            Map.entry("gtceu:virtue_meld", 3),
            Map.entry("gtceu:yttrium_barium_cuprate", 4));

    private ConductorAmpacityRules() {}

    public static int applyMaterialBaseAmperages() {
        int applied = 0;
        for (var entry : BASE_AMPERAGE_BY_MATERIAL.entrySet()) {
            Material material = GTRegistries.MATERIALS.get(ResourceLocation.parse(entry.getKey()));
            if (material == null || material == GTMaterials.NULL || !material.hasProperty(PropertyKey.WIRE)) {
                continue;
            }
            material.getProperty(PropertyKey.WIRE).setAmperage(entry.getValue());
            applied++;
        }
        return applied;
    }

    public static Map<String, Integer> baseAmperageByMaterial() {
        return BASE_AMPERAGE_BY_MATERIAL;
    }

    public static int effectiveAmperage(WireProperties properties, boolean insulated) {
        if (!insulated && !properties.isSuperconductor()) {
            return properties.getAmperage();
        }
        long doubledAmperage = Math.min((long) properties.getAmperage() * 2L, Integer.MAX_VALUE);
        return (int) Math.min(doubledAmperage,
                maximumAmperageForTier(GTUtil.getTierByVoltage(properties.getVoltage())));
    }

    public static int maximumAmperageForTier(int tier) {
        if (tier <= 0) {
            return BASE_TIER_CAP;
        }
        if (tier >= 26) {
            return Integer.MAX_VALUE;
        }
        return BASE_TIER_CAP << tier;
    }
}
