package com.ghostipedia.cosmiccore.common.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangHandler.replace;

public class CosmicLangHandler {

    public static void init(RegistrateLangProvider provider) {
        replace(provider, "item.gtceu.tool.luv_meld_tool", "Trinavine Meld Multitool");
        replace(provider, "cosmiccore.multiblock.naqreactor.tooltip.0",
                "§cA massive reactor powered by explosions and reactive fuel");
        replace(provider, "cosmiccore.multiblock.naqreactor.tooltip.1",
                "§bWill always attempt to parallel to 16x output.");
        replace(provider, "cosmiccore.multiblock.naqreactor.tooltip.2", "§cOnly Accepts Laser hatches.");
        replace(provider, "gtceu.industrial_chemvat", "§aIndustrial Chemvat");
        replace(provider, "cosmiccore.multiblock.chemvat.tooltip.0", "§aA massive chemical plant capable of parallel");
        replace(provider, "cosmiccore.multiblock.chemvat.tooltip.1",
                "§fWhen parallelized, adds the cumulative time of all recipes together.");
        replace(provider, "cosmiccore.multiblock.chemvat.tooltip.2",
                "§fReduces total time of any recipe ran by 75% afterwards.");
        replace(provider, "cosmiccore.multiblock.chemvat.tooltip.3", "§6Accepts Laser hatches.");
        replace(provider, "cosmiccore.multiblock.chemvat.tooltip.4", "§6Accepts Cosmic Parallel Hatches.");

        replace(provider, "cosmiccore.gravpack.1", "§aNormalizes Gravity to Match Earth.");
        replace(provider, "cosmiccore.recipe.soulIn", "Soul Input: %s");
        replace(provider, "cosmiccore.recipe.soulOut", "Soul Output: %s");
        replace(provider, "cosmiccore.wire_coil.magnet_capacity", "  §fMax Field Strength: §f%s Tesla");
        replace(provider, "cosmiccore.wire_coil.magnet_regen", "  §5Field Regen Rate: %s Tesla/t");
        replace(provider, "cosmiccore.wire_coil.eu_multiplier", "  §aMagnet EU Cost: §c%s EU/t");
        replace(provider, "cosmiccore.wire_coil.magnet_stats", "§8Magnet Stats");
        replace(provider, "tooltip.cosmiccore.soul_hatch.input", "§cMax Recipe Input§f:§6 %s");
        replace(provider, "tooltip.cosmiccore.soul_hatch.output", "§cMax Soul Network Capacity§f:§6 %s");
        replace(provider, "gui.cosmiccore.soul_hatch.label.import", "Soul Input Hatch");
        replace(provider, "gui.cosmiccore.soul_hatch.label.export", "Soul Output Hatch");
        replace(provider, "gui.cosmiccore.thermia_hatch.label.export", "§6Thermia Output Vent");
        replace(provider, "gui.cosmiccore.thermia_hatch.label.import", "§6Thermia Input Socket");
        replace(provider, "gui.cosmiccore.soul_hatch.owner", "Network Owner: %d");
        replace(provider, "gui.cosmiccore.soul_hatch.lp", "LP Stored: %s");
        replace(provider, "tooltip.cosmiccore.soul_hatch.output", "§cMax Soul Network Capacity§f:§6 %s");
        replace(provider, "tooltip.cosmiccore.thermia_hatch_limit", "§cTemp. Limit: %sK");
        replace(provider, "gui.cosmiccore.thermia_hatch.hatch_limit", "§cTemp. Limit:");
        replace(provider, "gui.cosmiccore.thermia_hatch.stored_temp", "§6Current Temp:");
        replace(provider, "cosmiccore.multiblock.magnetic_field_strength", "§fMax Field Strength§f:§6 %s");
        replace(provider, "cosmiccore.multiblock.magnetic_regen", "§aField Recovery Rate§f:§6 %sT/t");
        replace(provider, "cosmiccore.multiblock.fuel_star", "§a§lFuel Star Core");
        replace(provider, "cosmiccore.multiblock.send_orbit_data", "§a§lSend Research Payload");
        replace(provider, "cosmiccore.multiblock.iris.star_stage_empty", "§aStar Core Stage§f: §6Compressed Gas Cloud");
        replace(provider, "cosmiccore.multiblock.iris.star_stage_early_star", "§aStar Core Stage§f: §6Infant Star");
        replace(provider, "cosmiccore.multiblock.iris.star_stage_request",
                "§cStar Core Requires \n§r%s \n§cfor Next Stage.");
        replace(provider, "cosmiccore.multiblock.iris.star_stage_sustain",
                "§cStar Requires \n§r%s \n§cto avoid §lcataclysmic failure!");
        replace(provider, "cosmiccore.multiblock.iris.tooltip.0", "§cYour Mind Shatters Trying to Understand This");
        replace(provider, "cosmiccore.multiblock.iris.tooltip.1", "§c§lDANGER: DO NOT RENDER THE JEI PREVIEW");
        replace(provider, "cosmiccore.multiblock.iris.tooltip.2", "§c§lDANGER: YOU WILL LAG OR CRASH YOUR GAME");
        replace(provider, "cosmiccore.multiblock.iris.tooltip.3",
                "§aFuture Multiblock - JEI preview will be disabled/optimized");
        replace(provider, "cosmiccore.multiblock.advanced.star_ladder_tier",
                "§aVomahine StarLadder Tether Tier§f: §b%s \n §aMax Research Modules§f: §b%s");
        replace(provider, "gtceu.naquahine_reactor", "§bNaquahine Reactor");
        replace(provider, "cosmiccore.multiblock.current_field_strength", "§fField Strength: %s");
        replace(provider, "cosmiccore.recipe.minField", "§fMin. Field Strength: %sT");
        replace(provider, "cosmiccore.recipe.fieldDecay", "§fField Decay: %sT/t");
        replace(provider, "cosmiccore.recipe.fieldSlam", "§fField Consumed: %sT");
        replace(provider, "tagprefix.leached_ore", "Leached %s Ore");
        replace(provider, "tagprefix.prisma_frothed_ore", "Prisma Frothed %s Ore");
        replace(provider, "block.gtceu.iv_naquahine_mini_reactor", "§3Micro Naquahine Reactor§r");
        replace(provider, "block.gtceu.luv_naquahine_mini_reactor", "§dAdvanced Micro Naquahine Reactor§r");
        replace(provider, "block.gtceu.zpm_naquahine_mini_reactor", "§cElite Micro Naquahine Reactor§r");
        replace(provider, "block.gtceu.uv_naquahine_mini_reactor", "§3Ultimate Micro Naquahine Reactor§r");
        replace(provider, "block.gtceu.uhv_naquahine_mini_reactor", "§4Epic Micro Naquahine Reactor§r");
        replace(provider, "item.cosmiccore.debug.structure_writer.structural_scale", "Structure size: X:%s Y:%s Z:%s");
        replace(provider, "item.cosmiccore.debug.structure_writer.export_order",
                "Pattern Export Order:\n §cC:%s§l§d/§aS:%s§l§d/§bA:%s");
        replace(provider, "item.cosmiccore.debug.structure_writer.export_to_log", "Print Aisles to Log");
        replace(provider, "item.cosmiccore.debug.structure_writer.rotate_along_x_axis", "Rotate X Axis");
        replace(provider, "item.cosmiccore.debug.structure_writer.rotate_along_y_axis", "Rotate Y Axis");
        replace(provider, "item.cosmiccore.debug.structure_writer.output_successful",
                "Output Successful! Check your log file!");
        replace(provider, "cosmiccore.lore.shard_small.0", "§6A shard from a past eternity");
        replace(provider, "cosmiccore.lore.shard_small.1", "§6it subtly echos to rewrite fate.");
        replace(provider, "cosmiccore.lore.shard_large.0", "§aA large fragment from a past eternity");
        replace(provider, "cosmiccore.lore.shard_large.1", "§ait echos to rewrite fate.");
        replace(provider, "cosmiccore.lore.shard_huge.0", "§3An abnormally massive cluster from past eternity.");
        replace(provider, "cosmiccore.lore.shard_huge.1", "§3it screams and wails at you to undo history.");
        replace(provider, "cosmiccore.lore.shard_huge.2", "§cYour mind shatters trying to understand this.");
        replace(provider, "cosmiccore.omnia_circuit.lv", "§6Works as any LV Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.mv", "§6Works as any MV Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.hv", "§6Works as any HV Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.ev", "§6Works as any EV Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.iv", "§6Works as any IV Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.luv", "§6Works as any LuV Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.zpm", "§6Works as any ZPM Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.uv", "§6Works as any UV Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.uhv", "§6Works as any UHV Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.uev", "§6Works as any UEV Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.uiv", "§6Works as any UIV Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.uxv", "§6Works as any UXV Circuit.");
        replace(provider, "cosmiccore.omnia_circuit.opv", "§6Works as any OPV Circuit.");
        replace(provider, "block.gtceu.steam_mixing_vessel", "§6Large Steam Mixing Vessel");
        replace(provider, "block.gtceu.industrial_primitive_blast_furnace", "Industrial Primitive Blast Furnace");
        replace(provider, "cosmiccore.multiblock.ipbf.tooltip.0", "§7§oTurn up the heat!");
        replace(provider, "cosmiccore.multiblock.ipbf.tooltip.1",
                "§fConsumes creosote with recipes to improve efficiency.");
        replace(provider, "cosmiccore.multiblock.ipbf.tooltip.2", "§aRecipes are §f25% §aFaster.");
        replace(provider, "cosmiccore.multiblock.ipbf.tooltip.3", "§aParallel Amount§f: §b8x§r");
        replace(provider, "block.gtceu.high_pressure_assembler", "High Pressure Assembler");
        replace(provider, "block.gtceu.large_combustion_engine_cc", "Large Combustion Engine");
        replace(provider, "block.gtceu.extreme_combustion_engine_cc", "Extreme Combustion Engine");
        replace(provider, "block.gtceu.ludicrous_combustion_engine_cc", "Ludicrous Combustion Engine");
        replace(provider, "block.gtceu.ultimate_combustion_engine_cc", "Ultimate Combustion Engine");
        replace(provider, "block.gtceu.steam_caster", "Steam Caster Solidifier");
        replace(provider, "cosmiccore.multiblock.hpsassem.tooltip.0", "§7§oLet's build an empire!");
        replace(provider, "cosmiccore.multiblock.hpsassem.tooltip.1", "§fA large but powerful assembler made of steel");
        replace(provider, "cosmiccore.multiblock.hpsassem.tooltip.2", "§aParallel Amount§f: §b4x§r");
        replace(provider, "block.gtceu.steam_fluid_output_hatch", "Bronze Output Hatch");
        replace(provider, "block.gtceu.steam_fluid_input_hatch", "Bronze Input Hatch");
        replace(provider, "gtceu.machine.steam_fluid_hatch_notice",
                "This hatch is for Fluid ingredients! Not to power with steam!");
        replace(provider, "block.gtceu.hp_steam_bender", "HP Steam Bender");
        replace(provider, "block.gtceu.lp_steam_bender", "I Don't Actually Exist");
        replace(provider, "block.gtceu.hp_steam_wiremill", "HP Steam Wiremill");
        replace(provider, "block.gtceu.lp_steam_wiremill", "I Don't Actually Exist");
        // Rune Lang
        replace(provider, "cosmiccore.rune_vague", "§7§oLatent emotions seem to be missing.");
        replace(provider, "cosmiccore.rune_emotion_weak.1", "§7§oAn incomplete ERA reaction is observed.");
        replace(provider, "cosmiccore.rune_emotion_weak.2",
                "§7§oStrong emotional and chemical reactions cause the slate to vibrate.");
        replace(provider, "cosmiccore.arklys.1", "§6Ark - Structure");
        replace(provider, "cosmiccore.arklys.2", "§6Lys - Release");

        replace(provider, "cosmiccore.tylomir.1", "§6Tylo - Formation");
        replace(provider, "cosmiccore.tylomir.2", "§6Mir - World");

        replace(provider, "cosmiccore.khoruth.1", "§6Khor - Space");
        replace(provider, "cosmiccore.khoruth.2", "§6Ruth - Foundation");

        replace(provider, "cosmiccore.zelothar.1", "§6Zelos - Zeal");
        replace(provider, "cosmiccore.zelothar.2", "§6Thar - Forge");

        replace(provider, "cosmiccore.tenura.1", "§6Ten - Control");
        replace(provider, "cosmiccore.tenura.2", "§6Ura - Flow");

        replace(provider, "cosmiccore.valdris.1", "§6Val - Overwhelm");
        replace(provider, "cosmiccore.valdris.2", "§6Dris - Connect");

        replace(provider, "cosmiccore.conjuct_kholys.1", "§6Khor - Space");
        replace(provider, "cosmiccore.conjuct_kholys.2", "§6Lys - Release");
        replace(provider, "cosmiccore.conjuct_kholys_emotion.1", "§bE.R.A - Confidence");

        replace(provider, "cosmiccore.conjuct_arklythar.1", "§6Ark - Structure");
        replace(provider, "cosmiccore.conjuct_arklythar.2", "§6Thar - Forge");
        replace(provider, "cosmiccore.conjuct_arklythar_emotion.1", "§bE.R.A -  Resolve");

        replace(provider, "cosmiccore.conjuct_valkruth.1", "§6Val - Overwhelming");
        replace(provider, "cosmiccore.conjuct_valkruth.2", "§6Ruth - Foundation");
        replace(provider, "cosmiccore.conjuct_valkruth_emotion.1", "§bE.R.A -  Convergence");
        replace(provider, "gtceu.hellfire_foundry", "§cHellfire Foundry");

        replace(provider, "item.cosmiccore.the_one_ring.tooltip.0",
                "§6§oOne Ring to rule them all, One Ring to find them, One Ring to bring them all and in the darkness bind them.§r");
        replace(provider, "item.cosmiccore.the_one_ring.tooltip.1", "You might find it hard to take off.");

        replace(provider, "item.cosmiccore.space_radio.tooltip", "§6Lets you hear sounds in space!");
        replace(provider, "cosmiccore.universal.tooltip.lube_info.0",
                "§aProviding Better Lubricants increases the total EU created");
        replace(provider, "cosmiccore.universal.tooltip.lube_info.1", "§eLubricant§f: §c1x §fEU total @ 1000mb/hr");
        replace(provider, "cosmiccore.universal.tooltip.lube_info.2",
                "§eAdv Lubricant§f: §c1.5x §fEU total @ 500mb/hr");
        replace(provider, "cosmiccore.universal.tooltip.lube_info.3",
                "§eTears of the Universe§f: §c2x §fEU total @ 250mb/hr");

        replace(provider, "cosmiccore.universal.boosting_agents.0",
                "§aCan consume various boosters to increase EU/t multiplier");
        replace(provider, "cosmiccore.errors.bad_fuel",
                "§aInsufficient Fuel Quality! \n Fuel Output Must be >720 EU total per unit");
        replace(provider, "cosmiccore.universal.boosting_agents.1", "§6Oxygen §ffor §a3x §fEu/t @ §b20mb/s ");
        replace(provider, "cosmiccore.universal.boosting_agents.2", "§6Liquid Oxygen §ffor §a6x §fEu/t @ §b80mb/s ");
        replace(provider, "cosmiccore.universal.boosting_agents.3", "§6Ichor §ffor §a9x §fEu/t @ §b10mb/s ");

        replace(provider, "cosmiccore.multiblock.booster_used", "Booster: %s");
        replace(provider, "cosmiccore.multiblock.lubricant_used", "Lubricant: %s");

        replace(provider, "behavior.wireless_data.owner.player", "§3Player Name: §r%s");
        replace(provider, "behavior.wireless_data.owner.team", "§3Team Name: §r");
        replace(provider, "cosmiccore.wireless_charger.mode.0", "Set charger mode: SUPERCHARGER [Range - %s blocks]");
        replace(provider, "cosmiccore.wireless_charger.mode.1", "Set charger mode: STANDARD [Range - %s blocks]");
        replace(provider, "cosmiccore.wireless_charger.range.single",
                "When in Supercharger mode, supplies 4A within %s blocks");
        replace(provider, "cosmiccore.wireless_charger.range.mixed",
                "When in Standard Charge mode, supplies 1A within %s blocks");
        replace(provider, "cosmiccore.wireless_charger.enter_range",
                "You have entered charging range [Range - %s blocks]");
        replace(provider, "cosmiccore.wireless_charger.left_range", "You have left charging range [Range - %s blocks]");
        replace(provider, "cosmiccore.circuit.lore.tier.max.0", "MAX Tier Circuit");
        replace(provider, "cosmiccore.circuit.lore.tier.max.1", "Not a processor- but an Obituary.");
        replace(provider, "cosmiccore.circuit.lore.tier.max.2", "Input: Existence.");
        replace(provider, "cosmiccore.circuit.lore.tier.max.3", "Output: A single conclusion.");

        replace(provider, "cosmiccore.lore.broken_virtue.0", "Perpetuity Shudders Softly");
        replace(provider, "cosmiccore.lore.broken_virtue.1", "Something has gone very wrong.");

        replace(provider, "coscore.pattern.multiply",
                "§b%dx§f");

        replace(provider, "coscore.pattern.div",
                "§c%d÷§f");

        replace(provider, "coscore.pattern.tooltip.div",
                "Divides contents by §b%d§f");

        replace(provider, "coscore.pattern.tooltip.multiply",
                "Multiplies contents by §c%d§f");

        replace(provider, "item.cosmiccore.the_one_ring.tooltip.0",
                "§6§oOne Ring to rule them all, One Ring to find them, One Ring to bring them all and in the darkness bind them.§r");
        replace(provider, "item.cosmiccore.the_one_ring.tooltip.1", "You might find it hard to take off.");

        replace(provider, "behavior.wireless_data.owner.player", "§3Player Name: §r%s");
        replace(provider, "behavior.wireless_data.owner.team", "§3Team Name: §r");

        // Dimensional Energy Storage
        provider.add("cosmic.multiblock.capacitor.info.tittle.global", "Global Network Info");
        provider.add("cosmic.multiblock.capacitor.info.tittle.local", "Local Buffer Info ");
        provider.add("cosmic.multiblock.capacitor.info.global", "Global");
        provider.add("cosmic.multiblock.capacitor.info.local", "Local");
        provider.add("cosmic.multiblock.capacitor.buffered", "§7Buffered: %s §7EU");
        provider.add("cosmic.multiblock.capacitor.duplicate.multiblock.1", "This multiblock is a duplicate");
        provider.add("cosmic.multiblock.capacitor.duplicate.multiblock.2", "Only one can exist");

        // Dimensional Energy Tooltip
        provider.add("gtceu.machine.dec.tooltip.0", "Stores power in an interdimensional pocket.");
        provider.add("gtceu.machine.dec.tooltip.1",
                "Can send/receive power to/from §bPower Substation Dimensional Interfaces");
        provider.add("gtceu.machine.dec.tooltip.2", "Can only create §cONE§r Power Substation per team/player.");
        provider.add("gtceu.machine.dec.tooltip.3", "§cDuplicates will not function.");
        provider.add("gtceu.machine.dec.tooltip.4",
                "§7Can Insert and Extract from your wireless networked linked to your §aPower Substation§r");

        provider.add("cosmiccore.machine.capacitor_array.tooltip.0",
                "§7Local Dense Power Storage§r");
        provider.add("cosmiccore.machine.capacitor_array.tooltip.1",
                "§7Can use any capacitor and be expanded vertically up to 18 times§r");
        provider.add("cosmiccore.machine.capacitor_array.tooltip.2",
                "§7Accepts §6Laser Hatches§r");
        // Wireless Energy Command
        provider.add("cosmic.command.wireless.energy.player", "§aPlayer:§a %s");
        provider.add("cosmic.command.wireless.energy.team", "§aTeam:§a %s");
        provider.add("cosmic.command.wireless.energy.header", "§eWireless Energy Network Info (§e %s §e)§e:");
        provider.add("cosmic.command.wireless.energy.capacity", "  §bCapacity:§b %s EU");
        provider.add("cosmic.command.wireless.energy.stored", "  §bStored:§b %s EU");
        provider.add("cosmic.command.wireless.energy.input", "  §bInput:§b %s EU/t");
        provider.add("cosmic.command.wireless.energy.output", "  §bOutput:§b %s EU/t");
        provider.add("cosmic.command.wireless.energy.buffered", "  §bBuffered:§b %s EU");
        provider.add("cosmic.command.wireless.energy.active", "  §bActive:§b %s");
        provider.add("cosmic.command.wireless.energy.location.format", "%s : x=%d y=%d z=%d");
        provider.add("cosmic.command.wireless.energy.no.capacitor", "No Formed Capacitor");
        provider.add("cosmic.command.wireless.energy.capacitor", "  §bCapacitor Location:§b ");

        // Wireless Energy Curio
        provider.add("cosmic.gui.wireless.energy.player", "§aPlayer:§a %s");
        provider.add("cosmic.gui.wireless.energy.team", "§aTeam:§a %s");
        provider.add("cosmic.gui.wireless.energy.header", "§eWireless Energy Network Info (§e %s §e)§e:");
        provider.add("cosmic.gui.wireless.energy.capacity", "  §bCapacity:§b %s EU");
        provider.add("cosmic.gui.wireless.energy.stored", "§eStorage §b%s §f%s/%s");
        provider.add("cosmic.gui.wireless.energy.net", "  §aEU NET: %s EU/t");
        provider.add("cosmic.gui.wireless.energy.input", "§aIN:§b %s EU/t");
        provider.add("cosmic.gui.wireless.energy.output", "§cOUTt:§b %s EU/t");
        provider.add("cosmic.gui.wireless.energy.buffered", "  §bBuffered:§b %s EU");
        provider.add("cosmic.gui.wireless.energy.active", "  §bActive:§b %s");
        provider.add("cosmic.gui.wireless.energy.location.format", "%s : x=%d y=%d z=%d");
        provider.add("cosmic.gui.wireless.energy.no.capacitor", "No Formed Capacitor");
        provider.add("cosmic.gui.wireless.energy.capacitor", "  §bCapacitor Location:§b ");
    }
}
