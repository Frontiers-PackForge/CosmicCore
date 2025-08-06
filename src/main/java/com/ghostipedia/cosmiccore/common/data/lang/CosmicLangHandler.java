package com.ghostipedia.cosmiccore.common.data.lang;

import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class CosmicLangHandler extends LangHandler {

    public static void init(RegistrateLangProvider provider) {
        // items
        replace(provider, "item.gtceu.tool.luv_meld_tool", "%s Meld Multitool");
        provider.add("item.cosmiccore.portable_gravity_core.tooltip", "§aNormalizes Gravity to Match Earth.");

        // machine tooltips/names/etc
        provider.add("gtceu.naquahine_reactor", "§bNaquahine Reactor");

        provider.add("block.gtceu.steam_mixing_vessel", "§6Large Steam Mixing Vessel");
        provider.add("block.gtceu.large_combustion_engine_cc", "Large Combustion Engine");
        provider.add("block.gtceu.extreme_combustion_engine_cc", "Extreme Combustion Engine");
        provider.add("block.gtceu.ludicrous_combustion_engine_cc", "Ludicrous Combustion Engine");
        provider.add("block.gtceu.ultimate_combustion_engine_cc", "Ultimate Combustion Engine");

        provider.add("block.gtceu.steam_caster", "Steam Caster Solidifier");
        provider.add("block.gtceu.steam_fluid_output_hatch", "Bronze Output Hatch");
        provider.add("block.gtceu.steam_fluid_input_hatch", "Bronze Input Hatch");
        provider.add("gtceu.machine.steam_fluid_hatch_notice",
                "This hatch is for Fluid ingredients! Not to power with steam!");

        provider.add("block.gtceu.iv_naquahine_mini_reactor", "§3Micro Naquahine Reactor§r");
        provider.add("block.gtceu.luv_naquahine_mini_reactor", "§dAdvanced Micro Naquahine Reactor§r");
        provider.add("block.gtceu.zpm_naquahine_mini_reactor", "§cElite Micro Naquahine Reactor§r");
        provider.add("block.gtceu.uv_naquahine_mini_reactor", "§3Ultimate Micro Naquahine Reactor§r");
        provider.add("block.gtceu.uhv_naquahine_mini_reactor", "§4Epic Micro Naquahine Reactor§r");

        provider.add("block.gtceu.hp_steam_bender", "High Pressure Steam Bender");
        replace(provider, "block.gtceu.lp_steam_bender", "I Don't Actually Exist");
        provider.add("block.gtceu.hp_steam_wiremill", "High Pressure Steam Wiremill");
        replace(provider, "block.gtceu.lp_steam_wiremill", "I Don't Actually Exist");

        multiLang(provider, "cosmiccore.multiblock.naqreactor.tooltip",
                "§cA massive reactor powered by explosions and reactive fuel",
                "§bWill always attempt to parallel to 16x output.",
                "§cOnly Accepts Laser hatches.");

        provider.add("gtceu.industrial_chemvat", "§aIndustrial Chemvat");
        multiLang(provider, "cosmiccore.multiblock.chemvat.tooltip",
                "§aA massive chemical plant capable of parallel",
                "§fWhen parallelized, adds the cumulative time of all recipes together.",
                "§fReduces total time of any recipe ran by 75% afterwards.",
                "§6Accepts Laser hatches.",
                "§6Accepts Cosmic Parallel Hatches.");

        multiLang(provider, "cosmiccore.multiblock.iris.tooltip",
                "§cYour Mind Shatters Trying to Understand This",
                "§c§lDANGER: DO NOT RENDER THE JEI PREVIEW",
                "§c§lDANGER: YOU WILL LAG OR CRASH YOUR GAME",
                "§aFuture Multiblock - JEI preview will be disabled/optimized");

        provider.add("block.gtceu.industrial_primitive_blast_furnace", "Industrial Primitive Blast Furnace");
        multiLang(provider, "cosmiccore.multiblock.ipbf.tooltip",
                "§7§oTurn up the heat!",
                "§fConsumes creosote with recipes to improve efficiency.",
                "§aRecipes are §f25% §aFaster.",
                "§aParallel Amount§f: §b8x§r");

        provider.add("block.gtceu.high_pressure_assembler", "High Pressure Assembler");
        multiLang(provider, "cosmiccore.multiblock.hpsassem.tooltip",
                "§7§oLet's build an empire!",
                "§fA large but powerful assembler made of steel",
                "§aParallel Amount§f: §b4x§r");

        // Dimensional Energy Tooltip
        multiLang(provider, "gtceu.machine.dec.tooltip",
                "Stores power in an interdimensional pocket.",
                "Can send/receive power to/from §bPower Substation Dimensional Interfaces",
                "Can only create §cONE§r Power Substation per team/player.",
                "§cDuplicates will not function.",
                "§7Can Insert and Extract from your wireless networked linked to your §aPower Substation§r");

        multiLang(provider, "cosmiccore.machine.capacitor_array.tooltip",
                "§7Local Dense Power Storage§r",
                "§7Can use any capacitor and be expanded vertically up to 18 times§r",
                "§7Accepts §6Laser Hatches§r");

        // recipe stuff
        provider.add("cosmiccore.recipe.soulIn", "Soul Input: %s");
        provider.add("cosmiccore.recipe.soulOut", "Soul Output: %s");
        provider.add("cosmiccore.wire_coil.magnet_capacity", "  §fMax Field Strength: §f%s Tesla");
        provider.add("cosmiccore.wire_coil.magnet_regen", "  §5Field Regen Rate: %s Tesla/t");
        provider.add("cosmiccore.wire_coil.eu_multiplier", "  §aMagnet EU Cost: §c%s EU/t");
        provider.add("cosmiccore.wire_coil.magnet_stats", "§8Magnet Stats");
        provider.add("tooltip.cosmiccore.soul_hatch.input", "§cMax Recipe Input§f:§6 %s");
        provider.add("tooltip.cosmiccore.soul_hatch.output", "§cMax Soul Network Capacity§f:§6 %s");
        provider.add("tooltip.cosmiccore.thermia_hatch_limit", "§cTemp. Limit: %sK");
        provider.add("cosmiccore.multiblock.magnetic_field_strength", "§fMax Field Strength§f:§6 %s");
        provider.add("cosmiccore.multiblock.magnetic_regen", "§aField Recovery Rate§f:§6 %sT/t");

        // gui lines
        provider.add("gui.cosmiccore.soul_hatch.label.import", "Soul Input Hatch");
        provider.add("gui.cosmiccore.soul_hatch.label.export", "Soul Output Hatch");
        provider.add("gui.cosmiccore.thermia_hatch.label.export", "§6Thermia Output Vent");
        provider.add("gui.cosmiccore.thermia_hatch.label.import", "§6Thermia Input Socket");
        provider.add("gui.cosmiccore.soul_hatch.owner", "Network Owner: %d");
        provider.add("gui.cosmiccore.soul_hatch.lp", "LP Stored: %s");
        provider.add("gui.cosmiccore.thermia_hatch.hatch_limit", "§cTemp. Limit:");
        provider.add("gui.cosmiccore.thermia_hatch.stored_temp", "§6Current Temp:");
        provider.add("cosmiccore.multiblock.current_field_strength", "§fField Strength: %s");
        provider.add("cosmiccore.recipe.minField", "§fMin. Field Strength: %sT");
        provider.add("cosmiccore.recipe.fieldDecay", "§fField Decay: %sT/t");
        provider.add("cosmiccore.recipe.fieldSlam", "§fField Consumed: %sT");

        provider.add("cosmiccore.multiblock.fuel_star", "§a§lFuel Star Core");
        provider.add("cosmiccore.multiblock.send_orbit_data", "§a§lSend Research Payload");
        provider.add("cosmiccore.multiblock.iris.star_stage_empty", "§aStar Core Stage§f: §6Compressed Gas Cloud");
        provider.add("cosmiccore.multiblock.iris.star_stage_early_star", "§aStar Core Stage§f: §6Infant Star");
        provider.add("cosmiccore.multiblock.iris.star_stage_request",
                "§cStar Core Requires \n§r%s \n§cfor Next Stage.");
        provider.add("cosmiccore.multiblock.iris.star_stage_sustain",
                "§cStar Requires \n§r%s \n§cto avoid §lcataclysmic failure!");
        provider.add("cosmiccore.multiblock.advanced.star_ladder_tier",
                "§aVomahine StarLadder Tether Tier§f: §b%s \n §aMax Research Modules§f: §b%s");
        provider.add("tagprefix.leached_ore", "Leached %s Ore");
        provider.add("tagprefix.prisma_frothed_ore", "Prisma Frothed %s Ore");
        provider.add("tagprefix.ultradense_plate", "Ultradense %s Plate");
        provider.add("tagprefix.heavy_beam", "Heavy %s Beam");
        provider.add("tagprefix.modular_shelling", "%s Modular Shelling");
        provider.add("tagprefix.plasmites", "%s Plasmites");
        provider.add("tagprefix.wire_spool", "%s Wire Spool");

        provider.add("item.cosmiccore.debug.structure_writer.structural_scale", "Structure size: X:%s Y:%s Z:%s");
        provider.add("item.cosmiccore.debug.structure_writer.export_order",
                "Pattern Export Order:\n §cC:%s§l§d/§aS:%s§l§d/§bA:%s");
        provider.add("item.cosmiccore.debug.structure_writer.export_to_log", "Print Aisles to Log");
        provider.add("item.cosmiccore.debug.structure_writer.rotate_along_x_axis", "Rotate X Axis");
        provider.add("item.cosmiccore.debug.structure_writer.rotate_along_y_axis", "Rotate Y Axis");
        provider.add("item.cosmiccore.debug.structure_writer.output_successful",
                "Output Successful! Check your log file!");

        // item tooltips
        // TODO reorganize, use multiLang where applicable
        provider.add("cosmiccore.lore.shard_small.0", "§6A shard from a past eternity");
        provider.add("cosmiccore.lore.shard_small.1", "§6it subtly echos to rewrite fate.");
        provider.add("cosmiccore.lore.shard_large.0", "§aA large fragment from a past eternity");
        provider.add("cosmiccore.lore.shard_large.1", "§ait echos to rewrite fate.");
        provider.add("cosmiccore.lore.shard_huge.0", "§3An abnormally massive cluster from past eternity.");
        provider.add("cosmiccore.lore.shard_huge.1", "§3it screams and wails at you to undo history.");
        provider.add("cosmiccore.lore.shard_huge.2", "§cYour mind shatters trying to understand this.");

        provider.add("cosmiccore.omnia_circuit.lv", "§6Works as any LV Circuit.");
        provider.add("cosmiccore.omnia_circuit.mv", "§6Works as any MV Circuit.");
        provider.add("cosmiccore.omnia_circuit.hv", "§6Works as any HV Circuit.");
        provider.add("cosmiccore.omnia_circuit.ev", "§6Works as any EV Circuit.");
        provider.add("cosmiccore.omnia_circuit.iv", "§6Works as any IV Circuit.");
        provider.add("cosmiccore.omnia_circuit.luv", "§6Works as any LuV Circuit.");
        provider.add("cosmiccore.omnia_circuit.zpm", "§6Works as any ZPM Circuit.");
        provider.add("cosmiccore.omnia_circuit.uv", "§6Works as any UV Circuit.");
        provider.add("cosmiccore.omnia_circuit.uhv", "§6Works as any UHV Circuit.");
        provider.add("cosmiccore.omnia_circuit.uev", "§6Works as any UEV Circuit.");
        provider.add("cosmiccore.omnia_circuit.uiv", "§6Works as any UIV Circuit.");
        provider.add("cosmiccore.omnia_circuit.uxv", "§6Works as any UXV Circuit.");
        provider.add("cosmiccore.omnia_circuit.opv", "§6Works as any OPV Circuit.");

        // Rune Lang
        provider.add("cosmiccore.rune_vague", "§7§oLatent emotions seem to be missing.");
        provider.add("cosmiccore.rune_emotion_weak.1", "§7§oAn incomplete ERA reaction is observed.");
        provider.add("cosmiccore.rune_emotion_weak.2",
                "§7§oStrong emotional and chemical reactions cause the slate to vibrate.");
        provider.add("cosmiccore.arklys.1", "§6Ark - Structure");
        provider.add("cosmiccore.arklys.2", "§6Lys - Release");

        provider.add("cosmiccore.tylomir.1", "§6Tylo - Formation");
        provider.add("cosmiccore.tylomir.2", "§6Mir - World");

        provider.add("cosmiccore.khoruth.1", "§6Khor - Space");
        provider.add("cosmiccore.khoruth.2", "§6Ruth - Foundation");

        provider.add("cosmiccore.zelothar.1", "§6Zelos - Zeal");
        provider.add("cosmiccore.zelothar.2", "§6Thar - Forge");

        provider.add("cosmiccore.tenura.1", "§6Ten - Control");
        provider.add("cosmiccore.tenura.2", "§6Ura - Flow");

        provider.add("cosmiccore.valdris.1", "§6Val - Overwhelm");
        provider.add("cosmiccore.valdris.2", "§6Dris - Connect");

        provider.add("cosmiccore.conjuct_kholys.1", "§6Khor - Space");
        provider.add("cosmiccore.conjuct_kholys.2", "§6Lys - Release");
        provider.add("cosmiccore.conjuct_kholys_emotion.1", "§bE.R.A - Confidence");

        provider.add("cosmiccore.conjuct_arklythar.1", "§6Ark - Structure");
        provider.add("cosmiccore.conjuct_arklythar.2", "§6Thar - Forge");
        provider.add("cosmiccore.conjuct_arklythar_emotion.1", "§bE.R.A -  Resolve");

        provider.add("cosmiccore.conjuct_valkruth.1", "§6Val - Overwhelming");
        provider.add("cosmiccore.conjuct_valkruth.2", "§6Ruth - Foundation");
        provider.add("cosmiccore.conjuct_valkruth_emotion.1", "§bE.R.A -  Convergence");
        provider.add("gtceu.hellfire_foundry", "§cHellfire Foundry");

        multiLang(provider, "item.cosmiccore.the_one_ring.tooltip",
                "§6§oOne Ring to rule them all, One Ring to find them, One Ring to bring them all and in the darkness bind them.§r",
                "You might find it hard to take off.");

        // generic machine tooltips
        provider.add("item.cosmiccore.space_radio.tooltip", "§6Lets you hear sounds in space!");
        provider.add("cosmiccore.universal.tooltip.lube_info.0",
                "§aProviding Better Lubricants increases the total EU created");
        provider.add("cosmiccore.universal.tooltip.lube_info.1", "§eLubricant§f: §c1x §fEU total @ 1000mb/hr");
        provider.add("cosmiccore.universal.tooltip.lube_info.2",
                "§eAdv Lubricant§f: §c1.5x §fEU total @ 500mb/hr");
        provider.add("cosmiccore.universal.tooltip.lube_info.3",
                "§eTears of the Universe§f: §c2x §fEU total @ 250mb/hr");

        provider.add("cosmiccore.universal.boosting_agents.0",
                "§aCan consume various boosters to increase EU/t multiplier");
        provider.add("cosmiccore.errors.bad_fuel",
                "§aInsufficient Fuel Quality! \n Fuel Output Must be >720 EU total per unit");
        provider.add("cosmiccore.universal.boosting_agents.1", "§6Oxygen §ffor §a3x §fEu/t @ §b20mb/s ");
        provider.add("cosmiccore.universal.boosting_agents.2", "§6Liquid Oxygen §ffor §a6x §fEu/t @ §b80mb/s ");
        provider.add("cosmiccore.universal.boosting_agents.3", "§6Ichor §ffor §a9x §fEu/t @ §b10mb/s ");

        provider.add("cosmiccore.multiblock.booster_used", "Booster: %s");
        provider.add("cosmiccore.multiblock.lubricant_used", "Lubricant: %s");

        provider.add("behavior.wireless_data.owner.player", "§3Player Name: §r");
        provider.add("behavior.wireless_data.owner.network", "§3Network Owner: §r");
        provider.add("behavior.wireless_data.owner.team", "§3Team Name: §r");
        provider.add("cosmiccore.wireless_charger.mode.0", "Set charger mode: SUPERCHARGER [Range - %s blocks]");
        provider.add("cosmiccore.wireless_charger.mode.1", "Set charger mode: STANDARD [Range - %s blocks]");
        provider.add("cosmiccore.wireless_charger.range.single",
                "When in Supercharger mode, supplies 4A within %s blocks");
        provider.add("cosmiccore.wireless_charger.range.mixed",
                "When in Standard Charge mode, supplies 1A within %s blocks");
        provider.add("cosmiccore.wireless_charger.enter_range",
                "You have entered charging range [Range - %s blocks]");
        provider.add("cosmiccore.wireless_charger.left_range", "You have left charging range [Range - %s blocks]");
        provider.add("cosmiccore.circuit.lore.tier.max.0", "MAX Tier Circuit");
        provider.add("cosmiccore.circuit.lore.tier.max.1", "Not a processor- but an Obituary.");
        provider.add("cosmiccore.circuit.lore.tier.max.2", "Input: Existence.");
        provider.add("cosmiccore.circuit.lore.tier.max.3", "Output: A single conclusion.");

        provider.add("cosmiccore.lore.broken_virtue.0", "Perpetuity Shudders Softly");
        provider.add("cosmiccore.lore.broken_virtue.1", "Something has gone very wrong.");

        provider.add("coscore.pattern.multiply", "§b%dx§f");
        provider.add("coscore.pattern.div", "§c%d÷§f");
        provider.add("coscore.pattern.tooltip.div", "Divides contents by §b%d§f");
        provider.add("coscore.pattern.tooltip.multiply", "Multiplies contents by §c%d§f");

        // Sanguine Warptech
        provider.add("cosmiccore.armor.sanguinewarptech.hud.LP", "§4Life Force: §c%s");
        provider.add("cosmiccore.armor.sanguinewarptech.hud.shieldstate", "Sanguine Shield: %s");
        provider.add("cosmiccore.armor.sanguinewarptech.message.death_defiance",
                "Your sanguine armor protected you from death!");

        // Dimensional Energy Storage
        provider.add("cosmic.multiblock.capacitor.info.tittle.global", "Global Network Info");
        provider.add("cosmic.multiblock.capacitor.info.tittle.local", "Local Buffer Info ");
        provider.add("cosmic.multiblock.capacitor.info.global", "Global");
        provider.add("cosmic.multiblock.capacitor.info.local", "Local");
        provider.add("cosmic.multiblock.capacitor.buffered", "§7Buffered: %s §7EU");
        provider.add("cosmic.multiblock.capacitor.duplicate.multiblock.1", "This multiblock is a duplicate");
        provider.add("cosmic.multiblock.capacitor.duplicate.multiblock.2", "Only one can exist");
        provider.add("cosmic.multiblock.capacitor.owner.null", "Owner not found");

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

        replace(provider, "item.cosmiccore.infinite_spray_can", "§lInfinite Spray Can");

        // AE2 EU Display Mixin
        provider.add("gui.ae2.units.eu", "EU");

        // HPCA
        provider.add("cosmiccore.multiblock.hpca.incomplete-array", "Incomplete Array will not generate");

        provider.add("debug.owner.uuid", "§aOwner UUID:§a %s");
        provider.add("debug.team.uuid", "§aTeam UUID:§a %s");

        provider.add("cosmiccore.item.spraycan.tooltip.lclick", "§4Left Click: §8Cycle color");
        provider.add("cosmiccore.item.spraycan.tooltip.lclick_sneak", "§4Left Click + Sneak: §8Cycle color");
        provider.add("cosmiccore.item.spraycan.tooltip.rclick", "§4Right Click: §8Paint block");
        provider.add("cosmiccore.item.spraycan.tooltip.rclick_sneak", "§5Right Click + Sneak: §8Open UI");
        provider.add("cosmiccore.item.spraycan.tooltip.rclick_offhand", "§5Right Click in Offhand: §8Place & paint");
        provider.add("cosmiccore.item.spraycan.tooltip.locked", "Spraycan is locked");
        provider.add("cosmiccore.item.spraycan.tooltip.current_color", "Current Color: %s");
        provider.add("cosmiccore.item.spraycan.tooltip.solvent_mode", "Spraycan in SOLVENT mode");
    }
}
