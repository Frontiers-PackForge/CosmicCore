package com.ghostipedia.cosmiccore.common.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangHandler.replace;

public class CosmicLangHandler {

    public static void init(RegistrateLangProvider provider) {
        replace(provider,"cosmiccore.recipe.soulIn","Soul Input: %s");
        replace(provider,"cosmiccore.recipe.soulOut", "Soul Output: %s");
        replace(provider,"cosmiccore.wire_coil.magnet_capacity", "  §fMax Field Strength: §f%s");
        replace(provider,"cosmiccore.wire_coil.magnet_regen", "  §5Field Regen Rate: %s");
        replace(provider,"cosmiccore.wire_coil.eu_multiplier", "  §aEnergy Usage Multiplier: §c%s x EU");
        replace(provider,"cosmiccore.wire_coil.magnet_stats", "§8Magnet Stats");
        replace(provider,"tooltip.cosmiccore.soul_hatch.input", "§cMax Recipe Input§f:§6 %s");
        replace(provider,"tooltip.cosmiccore.soul_hatch.output", "§cMax Soul Network Capacity§f:§6 %s");
    }
}
