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
        replace(provider,"cosmiccore.multiblock.fuel_star", "§a§lFuel Star Core");
        replace(provider,"cosmiccore.multiblock.send_orbit_data", "§a§lSend Research Payload");
        replace(provider,"cosmiccore.multiblock.iris.star_stage_empty", "§aStar Core Stage§f: §6Compressed Gas Cloud");
        replace(provider,"cosmiccore.multiblock.iris.star_stage_early_star", "§aStar Core Stage§f: §6Infant Star");
        replace(provider,"cosmiccore.multiblock.iris.star_stage_request", "§cStar Core Requires \n§r%s \n§cfor Next Stage.");
        replace(provider,"cosmiccore.multiblock.iris.star_stage_sustain", "§cStar Requires \n§r%s \n§cto avoid §lcataclysmic failure!");
        replace(provider,"cosmiccore.multiblock.iris.tooltip.0", "§cYour Mind Shatters Trying to Understand This");
        replace(provider,"cosmiccore.multiblock.iris.tooltip.1", "§c§lDANGER: DO NOT RENDER THE JEI PREVIEW");
        replace(provider,"cosmiccore.multiblock.iris.tooltip.2", "§c§lDANGER: YOU WILL LAG OR CRASH YOUR GAME");
        replace(provider,"cosmiccore.multiblock.iris.tooltip.3", "§aFuture Multiblock - JEI preview will be disabled/optimized");
        replace(provider,"cosmiccore.multiblock.advanced.star_ladder_tier", "§aVomahine StarLadder Tether Tier§f: §b%s \n §aMax Research Modules§f: §b%s");
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
        replace(provider,"item.cosmiccore.debug.structure_writer.structural_scale", "Structure size: X:%s Y:%s Z:%s");
        replace(provider,"item.cosmiccore.debug.structure_writer.export_order", "Pattern Export Order:\n §cC:%s§l§d/§aS:%s§l§d/§bA:%s");
        replace(provider,"item.cosmiccore.debug.structure_writer.export_to_log", "Print Aisles to Log");
        replace(provider,"item.cosmiccore.debug.structure_writer.rotate_along_x_axis", "Rotate X Axis");
        replace(provider,"item.cosmiccore.debug.structure_writer.rotate_along_y_axis", "Rotate Y Axis");
        replace(provider,"item.cosmiccore.debug.structure_writer.output_successful", "Output Successful! Check your log file!");
        replace(provider,"cosmiccore.lore.shard_small.0", "§6A shard from a past eternity");
        replace(provider,"cosmiccore.lore.shard_small.1", "§6it subtly echos to rewrite fate.");
        replace(provider,"cosmiccore.lore.shard_large.0", "§aA large fragment from a past eternity");
        replace(provider,"cosmiccore.lore.shard_large.1", "§ait echos to rewrite fate.");
        replace(provider,"cosmiccore.lore.shard_huge.0", "§3An abnormally massive cluster from past eternity.");
        replace(provider,"cosmiccore.lore.shard_huge.1", "§3it screams and wails at you to undo history.");
        replace(provider,"cosmiccore.lore.shard_huge.2", "§cYour mind shatters trying to understand this.");
        replace(provider,"cosmiccore.omnia_circuit.lv", "§6Works as any LV Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.mv", "§6Works as any MV Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.hv", "§6Works as any HV Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.ev", "§6Works as any EV Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.iv", "§6Works as any IV Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.luv", "§6Works as any LuV Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.zpm", "§6Works as any ZPM Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.uv", "§6Works as any UV Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.uhv", "§6Works as any UHV Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.uev", "§6Works as any UEV Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.uiv", "§6Works as any UIV Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.uxv", "§6Works as any UXV Circuit.");
        replace(provider,"cosmiccore.omnia_circuit.opv", "§6Works as any OPV Circuit.");
        replace(provider,"block.gtceu.steam_mixing_vessel", "§6Large Steam Mixing Vessel");
        replace(provider,"block.gtceu.industrial_primitive_blast_furnace", "Industrial Primitive Blast Furnace");
        replace(provider,"cosmiccore.multiblock.ipbf.tooltip.0", "§7§oTurn up the heat!");
        replace(provider,"cosmiccore.multiblock.ipbf.tooltip.1", "§fConsumes steam to power internal bellows");
        replace(provider,"cosmiccore.multiblock.ipbf.tooltip.2", "§fallowing the bulk smelting of steel.");
        replace(provider,"cosmiccore.multiblock.ipbf.tooltip.3", "§aParallel Amount§f: §b8x§r");

    }
}
