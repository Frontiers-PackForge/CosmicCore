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
        replace(provider,"cosmiccore.wire_coil.magnet_capacity", "  §fMax Field Strength: §f%s Tesla");
        replace(provider,"cosmiccore.wire_coil.magnet_regen", "  §5Field Regen Rate: %s Tesla/t");
        replace(provider,"cosmiccore.wire_coil.eu_multiplier", "  §aMagnet EU Cost: §c%s EU/t");
        replace(provider,"cosmiccore.wire_coil.magnet_stats", "§8Magnet Stats");
        replace(provider,"tooltip.cosmiccore.soul_hatch.input", "§cMax Recipe Input§f:§6 %s");
        replace(provider,"tooltip.cosmiccore.soul_hatch.output", "§cMax Soul Network Capacity§f:§6 %s");
        replace(provider,"gui.cosmiccore.soul_hatch.label.import", "Soul Input Hatch");
        replace(provider,"gui.cosmiccore.soul_hatch.label.export", "Soul Output Hatch");
        replace(provider,"gui.cosmiccore.thermia_hatch.label.export", "§6Thermia Output Vent");
        replace(provider,"gui.cosmiccore.thermia_hatch.label.import", "§6Thermia Input Socket");
        replace(provider,"gui.cosmiccore.soul_hatch.owner", "Network Owner: %d");
        replace(provider,"gui.cosmiccore.soul_hatch.lp", "LP Stored: %s");
        replace(provider,"tooltip.cosmiccore.soul_hatch.output", "§cMax Soul Network Capacity§f:§6 %s");
        replace(provider,"tooltip.cosmiccore.thermia_hatch_limit", "§cTemp. Limit: %sK");
        replace(provider,"gui.cosmiccore.thermia_hatch.hatch_limit", "§cTemp. Limit:");
        replace(provider,"gui.cosmiccore.thermia_hatch.stored_temp", "§6Current Temp:");
        replace(provider,"cosmiccore.multiblock.magnetic_field_strength", "§fMax Field Strength§f:§6 %s");
        replace(provider,"cosmiccore.multiblock.magnetic_regen", "§aField Recovery Rate§f:§6 %sT/t");
        replace(provider,"gtceu.naquahine_reactor", "§bNaquahine Reactor");
        replace(provider,"cosmiccore.multiblock.current_field_strength", "§fField Strength: %s");
        replace(provider,"cosmiccore.recipe.minField", "§fMin. Field Strength: %sT");
        replace(provider,"cosmiccore.recipe.fieldDecay", "§fField Decay: %sT/t");
        replace(provider,"cosmiccore.recipe.fieldSlam", "§fField Consumed: %sT");
        replace(provider,"tagprefix.leached_ore", "Leached %s Ore");
        replace(provider,"tagprefix.prisma_frothed_ore", "Prisma Frothed %s Ore");
        replace(provider,"block.gtceu.iv_naquahine_mini_reactor", "§3Micro Naquahine Reactor§r");
        replace(provider,"block.gtceu.luv_naquahine_mini_reactor", "§dAdvanced Micro Naquahine Reactor§r");
        replace(provider,"block.gtceu.zpm_naquahine_mini_reactor", "§cElite Micro Naquahine Reactor§r");
        replace(provider,"block.gtceu.uv_naquahine_mini_reactor", "§3Ultimate Micro Naquahine Reactor§r");
        replace(provider,"block.gtceu.uhv_naquahine_mini_reactor", "§4Epic Micro Naquahine Reactor§r");

    }
}
