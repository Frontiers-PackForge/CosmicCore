package com.ghostipedia.cosmiccore.common.data.lang;

import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import static com.gregtechceu.gtceu.data.lang.LangHandler.replace;

public class CosmicLangHandler {

    public static void init(RegistrateLangProvider provider) {
        replace(provider,"cosmiccore.recipe.soulIn","Soul Input: %s");
        replace(provider,"cosmiccore.recipe.soulOut", "Soul Output: %s");
        replace(provider,"cosmiccore.wire_coil.magnet_capacity", "  §fMax Field Strength: §f%s");
        replace(provider,"cosmiccore.wire_coil.magnet_regen", "  §5Field Regen Rate: %s");
        replace(provider,"cosmiccore.wire_coil.eu_multiplier", "  §aMagnet EU Cost: §c%s/t");
        replace(provider,"cosmiccore.wire_coil.magnet_stats", "§8Magnet Stats");
        replace(provider,"tooltip.cosmiccore.soul_hatch.input", "§cMax Recipe Input§f:§6 %s");
        replace(provider,"tooltip.cosmiccore.soul_hatch.output", "§cMax Soul Network Capacity§f:§6 %s");
        replace(provider,"cosmiccore.multiblock.magnetic_field_strength", "§fMax Field Strength§f:§6 %s");
        replace(provider,"cosmiccore.multiblock.magnetic_regen", "§aField Recovery Rate§f:§6 %s");
        replace(provider,"gtceu.naquahine_reactor", "§bNaquahine Reactor");
        replace(provider,"cosmiccore.multiblock.current_field_strength", "§fCurrent Field Strength: %s");

    }
}
