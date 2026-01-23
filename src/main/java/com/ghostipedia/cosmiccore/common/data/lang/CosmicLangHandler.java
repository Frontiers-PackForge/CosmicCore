package com.ghostipedia.cosmiccore.common.data.lang;

import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class CosmicLangHandler extends LangHandler {

    public static void init(RegistrateLangProvider provider) {
        // Vein Survey Scanner
        provider.add("cosmiccore.survey.mode.radial", "Mode: Radial Scan (360°)");
        provider.add("cosmiccore.survey.mode.directional", "Mode: Directional Cone (90°)");
        provider.add("cosmiccore.survey.mode.nearest", "Mode: Nearest Vein");
        provider.add("cosmiccore.survey.no_energy", "Not enough energy to scan!");
        provider.add("cosmiccore.survey.tooltip.radius", "Scan Radius: %d blocks");
        provider.add("cosmiccore.survey.tooltip.mode", "Current Mode: ");
        provider.add("cosmiccore.survey.tooltip.filter", "Vein Filter: ");
        provider.add("cosmiccore.survey.tooltip.use", "§7Use: Scan for veins");
        provider.add("cosmiccore.survey.tooltip.shift", "§7Shift+Use: Change mode");
        provider.add("cosmiccore.survey.header", "═══ Vein Survey ═══");
        provider.add("cosmiccore.survey.found", "Found %d veins within %dm");
        provider.add("cosmiccore.survey.found.directional", " (directional)");
        provider.add("cosmiccore.survey.types", "Types: ");
        provider.add("cosmiccore.survey.nearest", "Nearest:");
        provider.add("cosmiccore.survey.nearest_vein", "Nearest vein:");
        provider.add("cosmiccore.survey.nearest_vein.filtered", "Nearest vein (%s):");
        provider.add("cosmiccore.survey.more", "  ... and %d more");
        provider.add("cosmiccore.survey.no_veins", "No veins found");
        provider.add("cosmiccore.survey.no_veins.filtered", "No veins found matching '%s'");
        provider.add("cosmiccore.survey.no_veins.directional", "No veins found in that direction");
        provider.add("cosmiccore.survey.click_tp", "Click to teleport");
        provider.add("cosmiccore.survey.command.scanning", "Surveying veins within %d blocks...");
        provider.add("cosmiccore.survey.command.results", "=== Vein Survey Results ===");
        provider.add("cosmiccore.survey.command.vein_types", "Vein types: ");
        provider.add("cosmiccore.survey.command.nearest_veins", "Nearest veins:");
        provider.add("cosmiccore.survey.command.more", "... and %d more veins");
        provider.add("cosmiccore.survey.command.no_veins_dimension", "No vein types registered for this dimension.");
        provider.add("cosmiccore.survey.command.available_types", "Available vein types (%d):");
        provider.add("cosmiccore.survey.command.player_only", "This command must be run by a player.");

        // items
        replace(provider, "item.gtceu.tool.luv_meld_tool", "%s Meld Multitool");
        provider.add("item.cosmiccore.portable_gravity_core.tooltip", "§aNormalizes Gravity to Match Earth.");

        // machine tooltips/names/etc
        provider.add("gtceu.naquahine_reactor", "§bNaquahine Reactor");

        provider.add("tooltip.gt_scythe.no_energy", "§cNot enough energy.");
        provider.add("tooltip.gt_scythe.energy", "Energy: %s / %s EU");
        provider.add("tooltip.gt_scythe.per_hit", "Cost: %s EU / hit");

        multiLang(provider, "cosmiccore.machine.fluid_drilling_rig.description",
                "§bDrills infinite fluid from",
                "§bliquid pockets suspended throughout the void.");

        provider.add("cosmiccore.universal.tooltip.energy_usage",
                "§eConsumes 1 ZPM Amp while operating.");
        provider.add("cosmiccore.machine.fluid_drilling_rig.production",
                "§eProduction Multiplier: 256x");
        provider.add("cosmiccore.machine.fluid_drilling_rig.depletion", "§bDepletion Rate: 0%");

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

        multiLang(provider, "cosmiccore.multiblock.star_ladder.tooltip",
                "§cThe peaks of creation reach out into the stars",
                "§c§lDANGER: DATA LOSS PRESENT",
                "§c§lDANGER: RECOVERY IS POSSIBLE",
                "§aPinacle Multiblock : The Final Goal of ACT1 (Steam to IV)");

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

        provider.add("emi.category.cosmiccore.asteroid_mining", "Asteroid Mining Operations");

        // recipe stuff
        provider.add("cosmiccore.recipe.soul_in", "Soul Input: %s");
        provider.add("cosmiccore.recipe.soul_out", "Soul Output: %s");
        provider.add("cosmiccore.recipe.sterile_in", "Sterilizer: %s %s");
        provider.add("cosmiccore.recipe.sterile_out", "ERROR?");
        provider.add("cosmiccore.recipe.ember_in", "Ember Input: %s");
        provider.add("cosmiccore.recipe.ember_out", "Ember Output: %s");
        provider.add("cosmiccore.wire_coil.magnet_capacity", "  §fMax Field Strength: §f%s Tesla");
        provider.add("cosmiccore.wire_coil.magnet_regen", "  §5Field Regen Rate: %s Tesla/t");
        provider.add("cosmiccore.wire_coil.eu_multiplier", "  §aMagnet EU Cost: §c%s EU/t");
        provider.add("cosmiccore.wire_coil.magnet_stats", "§8Magnet Stats");
        provider.add("tooltip.cosmiccore.soul_hatch.input", "§cMax Recipe Input§f:§6 %s");
        provider.add("tooltip.cosmiccore.soul_hatch.output", "§cMax Soul Network Capacity§f:§6 %s");
        provider.add("tooltip.cosmiccore.ember_hatch.consumption", "§cMax Ember Consumption§f:§6 %s");
        provider.add("tooltip.cosmiccore.ember_hatch.capacity", "§cMax Ember capacity§f:§6 %s");
        provider.add("tooltip.cosmiccore.thermia_hatch_limit", "§cTemp. Limit: %sK");
        provider.add("cosmiccore.multiblock.magnetic_field_strength", "§fMax Field Strength§f:§6 %s");
        provider.add("cosmiccore.multiblock.magnetic_regen", "§aField Recovery Rate§f:§6 %sT/t");
        provider.add("gtceu.titan_fusion", "Titan Fusion Reactor"); // recipe type lang

        // SB Flavor Texts
        provider.add("cosmiccore.vaccum_bubbler.desc", "Floatation Station Machination");
        provider.add("cosmiccore.thermomagnitizer.desc", "Heating and Magnets, what could go wrong");
        provider.add("cosmiccore.calx_reactor.desc", "Working wonders with the Arcane");
        provider.add("cosmiccore.mana_leaching_tub.desc", "Mana Soaker 9000");
        provider.add("cosmiccore.roaster.desc", "Marshmallows not included");

        provider.add("tooltip.cosmiccore.asteroid_chip.unprogrammed", "Unprogrammed — no target data");
        provider.add("tooltip.cosmiccore.asteroid_chip.type", "Type: %s");
        provider.add("tooltip.cosmiccore.asteroid_chip.target", "Target ID: %s");
        provider.add("tooltip.cosmiccore.asteroid_chip.tier", "Acquisition Tier: %s");
        provider.add("tooltip.cosmiccore.asteroid_chip.lock", "Lock Strength: %s%%");
        provider.add("tooltip.cosmiccore.asteroid_chip.sector", "Sector: %s");
        provider.add("tooltip.cosmiccore.asteroid_chip.mode", "Mode: %s");

        provider.add("tooltip.cosmiccore.asteroid.tiny", "Asteroid Size: Unknown");
        provider.add("tooltip.cosmiccore.asteroid.tier", "Asteroid Size: %s Kilotons");
        provider.add("cosmiccore.recipe.asteroid_weight_greater_1", "Greater Yields\nfrom Larger Asteroids");

        // gui lines
        provider.add("gui.cosmiccore.soul_hatch.label.import", "Soul Input Hatch");
        provider.add("gui.cosmiccore.soul_hatch.label.export", "Soul Output Hatch");
        provider.add("gui.cosmiccore.ember_hatch.label.import", "Ember Input Hatch");
        provider.add("gui.cosmiccore.ember_hatch.label.export", "Ember Output Hatch");
        provider.add("gui.cosmiccore.thermia_hatch.label.export", "§6Thermia Output Vent");
        provider.add("gui.cosmiccore.thermia_hatch.label.import", "§6Thermia Input Socket");
        provider.add("gui.cosmiccore.soul_hatch.owner", "Network Owner: %d");
        provider.add("gui.cosmiccore.soul_hatch.lp", "LP Stored: %s");
        provider.add("gui.cosmiccore.ember_hatch.ember", "Ember Stored: %s");
        provider.add("gui.cosmiccore.thermia_hatch.hatch_limit", "§cTemp. Limit:");
        provider.add("gui.cosmiccore.thermia_hatch.stored_temp", "§6Current Temp:");
        provider.add("gui.cosmiccore.sterilization_hatch", "Sterilization Hatch");
        provider.add("cosmiccore.multiblock.current_field_strength", "§fField Strength: %s");
        provider.add("cosmiccore.recipe.minField", "§fMin. Field Strength: %sT");
        provider.add("cosmiccore.recipe.fieldDecay", "§fField Decay: %sT/t");
        provider.add("cosmiccore.recipe.fieldSlam", "§fField Consumed: %sT");
        provider.add("cosmiccore.recipe.condition.titan.tooltip", "Requires Titan Reactor Tier: %s");

        // Linked Partner Condition
        provider.add("cosmiccore.recipe.condition.linked_partner.tooltip", "Requires %s linked partner(s)");
        provider.add("cosmiccore.recipe.condition.linked_partner.formed",
                "Requires %s linked partner(s) with valid structure");
        provider.add("cosmiccore.recipe.condition.linked_partner.working",
                "Requires %s linked partner(s) actively working");
        provider.add("cosmiccore.recipe.condition.linked_partner_dimension.tooltip", "Requires linked partner in %s");
        provider.add("cosmiccore.recipe.condition.linked_partner_dimension_item.tooltip",
                "Requires %sx %s in partner in %s");
        provider.add("cosmiccore.recipe.condition.linked_partner_dimension_fluid.tooltip",
                "Requires %smB %s in partner in %s");

        provider.add("cosmiccore.multiblock.heat_value", "§6Current Heat: %s");
        provider.add("cosmiccore.multiblock.heat_capacity", "§cMax Heat: %s");

        provider.add("cosmiccore.multiblock.current_contagion", "§6Contagion Strength: %s");
        provider.add("cosmiccore.multiblock.contagion_rate", "§cContagion Rate: %s/t");
        provider.add("cosmiccore.multiblock.cleaning_status", "§aCleaning Status: %s");
        provider.add("cosmiccore.multiblock.cleaning_status.error", "§cCleaning Status: §4No Cleaning Agent!");

        provider.add("cosmic.multiblock.parallel",
                "Overloading Parallels by 4x" + "\nMax Parallel: %d" + "\nOriginal Parallel: %d");
        provider.add("cosmic.multiblock.parallel.exact", "Performing %d Recipes in Parallel");

        provider.add("cosmic.multiblock.parallel_fixed_64", "Max Parallel: %d");
        provider.add("cosmic.multiblock.parallel_fixed_64.exact", "Performing %d Recipes in Parallel");
        provider.add("cosmic.multiblock.orvex_tier", "§fReactor Tier§7: §6%s");
        provider.add("cosmic.multiblock.orvex_count", "§fOrvex Residue Extracted§7: %s");
        provider.add("cosmic.multiblock.orvex_upgrade_requires", "§fUpgrade Requires§7: %s");
        provider.add("cosmic.multiblock.orvex_upgrade_check", "§fUpgrade Status: §a%s");

        provider.add("cosmiccore.multiblock.fuel_star", "§a§lFuel Star Core");
        provider.add("cosmiccore.multiblock.send_orbit_data", "§a§lSend Research Payload");
        provider.add("cosmiccore.multiblock.iris.star_stage_empty", "§aStar Core Stage§f: §6Compressed Gas Cloud");
        provider.add("cosmiccore.multiblock.iris.star_stage_early_star", "§aStar Core Stage§f: §6Infant Star");
        provider.add("cosmiccore.multiblock.iris.star_stage_request",
                "§cStar Core Requires \n§r%s \n§cfor Next Stage.");
        provider.add("cosmiccore.multiblock.iris.star_stage_sustain",
                "§cStar Requires \n§r%s \n§cto avoid §lcataclysmic failure!");
        provider.add("cosmiccore.multiblock.advanced.star_ladder_tier",
                "§aVomahine StarLadderOld Tether Tier§f: §b%s \n §aMax Research Modules§f: §b%s");
        provider.add("tagprefix.leached_ore", "Leached %s Ore");
        provider.add("tagprefix.prisma_frothed_ore", "Prisma Frothed %s Ore");
        provider.add("tagprefix.ultradense_plate", "Ultradense %s Plate");
        provider.add("tagprefix.heavy_beam", "Heavy %s Beam");
        provider.add("tagprefix.modular_shelling", "%s Modular Shelling");
        provider.add("tagprefix.plasmites", "%s Plasmites");
        provider.add("tagprefix.wire_spool", "%s Wire Spool");
        provider.add("tagprefix.shape_memory_foil", "%s Shaping Memory Foil");
        provider.add("tagprefix.alve_foil_insulator", "%s Alve Insulator");
        provider.add("tagprefix.raw_ore_cubic", "Cubic %s Ore");

        provider.add("cosmiccore.multiblock.reboot_powergrid", "§aReboot All Connected Machines");
        provider.add("cosmiccore.multiblock.sleep_powergrid", "§cSuspend All Connected Machines");

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

        // embers lang
        provider.add("cosmiccore.ember.capacity", "§cEmber Capacity:§6 %s");
        provider.add("cosmiccore.ember.transfer", "§cEmber Transfer Rate:§6 %s");

        multiLang(provider, "item.cosmiccore.the_one_ring.tooltip",
                "§6§oOne Ring to rule them all, One Ring to find them, One Ring to bring them all and in the darkness bind them.§r",
                "You might find it hard to take off.");

        // generic machine tooltips
        provider.add("item.cosmiccore.space_radio.tooltip", "§6Lets you hear sounds in space!");
        provider.add("item.cosmiccore.simple_rebreather.tooltip",
                "§7Reduces oxygen drain in §bThin Air§7 environments.");
        provider.add("item.cosmiccore.pressurized_rebreather.tooltip",
                "§6Enables oxygen tank usage. Works in §cNo Air§6 environments.");
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

        // Drone Station
        provider.add("cosmiccore.multiblock.drone_station_machine.drone_amount", "Currently serving %s drones");
        provider.add("cosmiccore.multiblock.drone_station_machine.no_drones", "No drones connected");
        provider.add("cosmiccore.multiblock.drone_station_machine.current_tier", "Current tier: %s");

        provider.add("cosmiccore.multiblock.drone_maintenance_interface.connection_location",
                "Currently connected to (%s, %s, %s)");
        provider.add("cosmiccore.multiblock.drone_maintenance_interface.no_connection", "Not connected");

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

        provider.add("cosmiccore.item.linked_terminal.boundTo", "Bound to %s");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_oxygen", "Lofty Oxygen");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_hydrogen", "Lofty Hydrogen");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_nitrogen", "Lofty Nitrogen");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_argon", "Lofty Argon");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_rose_polymer", "Rose Polymer");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_citrus_polymer", "Citrus Polymer");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_waxy_polymer", "Waxy Polymer");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_pale", "Pale");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_soul", "Soul");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_runic", "Runic");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_ambrosic", "Ambrosic");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_biohazard", "Biohazard");

        provider.add("allele.forestry.bee_species.cosmiccore.bee_abrasive", "Abrasive");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_energized", "Energized");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_slick", "Slick");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_pyrolytic", "Pyrolytic");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_lunar", "Lunar");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_solar", "Solar");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_cosmos", "Cosmos");

        provider.add("allele.forestry.bee_species.cosmiccore.bee_hadal", "Hadal");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_shaman", "Shaman");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_ashen", "Ashen");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_fracking", "Fracking");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_fate", "Fate");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_grand_garden", "Grand Garden");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_architect", "Architect");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_inquisitive", "Inquisitive");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_hellsmith", "Hellsmith");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_radoxia", "Radoxia");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_absent", "Absent");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_illusive", "Illusive");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_constructive", "Constructive");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_prismatic", "Prismatic");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_hydraulic", "Hydraulic");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_cobbled", "Cobbled");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_exhaustive", "Exhaustive");
        provider.add("allele.forestry.bee_species.cosmiccore.bee_virtue", "Virtue");

        provider.add("item.cosmiccore.bee_comb_lofty_oxygen", "Lofty Oxygen Comb");
        provider.add("item.cosmiccore.bee_comb_lofty_hydrogen", "Lofty Hydrogen Comb");
        provider.add("item.cosmiccore.bee_comb_lofty_nitrogen", "Lofty Nitrogen Comb");
        provider.add("item.cosmiccore.bee_comb_lofty_argon", "Lofty Argon Comb");
        provider.add("item.cosmiccore.bee_comb_rose_polymer", "Rose Polymer Comb");
        provider.add("item.cosmiccore.bee_comb_citrus_polymer", "Citrus Polymer Comb");
        provider.add("item.cosmiccore.bee_comb_waxy_polymer", "Waxy Polymer Comb");
        provider.add("item.cosmiccore.bee_comb_pale", "Pale Comb");
        provider.add("item.cosmiccore.bee_comb_soul", "Soul Comb");
        provider.add("item.cosmiccore.bee_comb_runic", "Runic Comb");
        provider.add("item.cosmiccore.bee_comb_ambrosic", "Ambrosic Comb");
        provider.add("item.cosmiccore.bee_comb_biohazard", "Biohazard Comb");

        provider.add("item.cosmiccore.bee_comb_abrasive", "Abrasive Comb");
        provider.add("item.cosmiccore.bee_comb_energized", "Energized Comb");
        provider.add("item.cosmiccore.bee_comb_slick", "Slick Comb");
        provider.add("item.cosmiccore.bee_comb_pyrolytic", "Pyrolytic Comb");
        provider.add("item.cosmiccore.bee_comb_lunar", "Lunar Comb");
        provider.add("item.cosmiccore.bee_comb_solar", "Solar Comb");
        provider.add("item.cosmiccore.bee_comb_cosmos", "Cosmos Comb");

        provider.add("item.cosmiccore.bee_comb_hadal", "Hadal Comb");
        provider.add("item.cosmiccore.bee_comb_shaman", "Shaman Comb");
        provider.add("item.cosmiccore.bee_comb_ashen", "Ashen Comb");
        provider.add("item.cosmiccore.bee_comb_fracking", "Fracking Comb");
        provider.add("item.cosmiccore.bee_comb_fate", "Fate Comb");
        provider.add("item.cosmiccore.bee_comb_grand_garden", "Grand Garden Comb");
        provider.add("item.cosmiccore.bee_comb_architect", "Architect Comb");
        provider.add("item.cosmiccore.bee_comb_inquisitive", "Inquisitive Comb");
        provider.add("item.cosmiccore.bee_comb_hellsmith", "Hellsmith Comb");
        provider.add("item.cosmiccore.bee_comb_radoxia", "Radoxia Comb");
        provider.add("item.cosmiccore.bee_comb_absent", "Absent Comb");
        provider.add("item.cosmiccore.bee_comb_illusive", "Illusive Comb");
        provider.add("item.cosmiccore.bee_comb_constructive", "Constructive Comb");
        provider.add("item.cosmiccore.bee_comb_prismatic", "Prismatic Comb");
        provider.add("item.cosmiccore.bee_comb_hydraulic", "Hydraulic Comb");
        provider.add("item.cosmiccore.bee_comb_cobbled", "Cobbled Comb");
        provider.add("item.cosmiccore.bee_comb_exhaustive", "Exhaustive Comb");
        provider.add("item.cosmiccore.bee_comb_virtue", "Virtue Comb");

        provider.add("gui.cosmiccore.iapiary", "Industrial Apiary");
        provider.add("gui.cosmiccore.iapiary.yield", "Yield: %d");
        provider.add("gui.cosmiccore.iapiary.duration", "Duration: %d");
        provider.add("gui.cosmiccore.iapiary.production_amp", "Production Amp: %d");

        provider.add("item.cosmicbees.bee.modifier.aging_multiplier", "Age Multiplier");
        provider.add("item.cosmiccore.decaying_cosmic_upgrade", "Decaying Upgrade");
        provider.add("item.cosmiccore.decaying_cosmic_upgrade.tooltip",
                "§cInstantly Kills Bees and overloads the lifetime cycle");

        provider.add("item.cosmiccore.wailing_cosmic_upgrade", "Wailing Upgrade");
        provider.add("item.cosmiccore.wailing_cosmic_upgrade.tooltip", "§cMaximum Mutation");

        multiLang(provider, "cosmiccore.machine.me.stocking_item.tooltip",
                "§fAutomatically pulls products into a singular item slot§r",
                "§bAllows Advanced Automation of the Assembly line§r",
                "§fCan be set to automatically pull the first item from AE2§r",
                "§bor manually filtered.§r",
                "§fFilter data can be copy/pasted with a data stick§r",
                "§b'If you're wondering how to parallel assembly lines§r",
                "§fthis is how. Welcome to subnets!§r");

        // Cross-Dimensional Multiblock Linking
        provider.add("cosmiccore.datastick.link_copied", "Link: %s");
        provider.add("cosmiccore.link.copied", "Link data copied from %s");
        provider.add("cosmiccore.link.established", "Link established: %s ↔ %s");

        // Link validation errors
        provider.add("cosmiccore.link.not_ready", "Machine not ready for linking");
        provider.add("cosmiccore.link.invalid_data", "Invalid link data on datastick");
        provider.add("cosmiccore.link.cannot_self_link", "Cannot link a machine to itself");
        provider.add("cosmiccore.link.partner_not_loaded", "Partner machine must be loaded to establish link");
        provider.add("cosmiccore.link.partner_missing", "Partner machine no longer exists");
        provider.add("cosmiccore.link.not_linkable", "Target machine does not support linking");
        provider.add("cosmiccore.link.different_owner", "Cannot link machines owned by different teams");
        provider.add("cosmiccore.link.incompatible_roles", "Incompatible link roles: %s cannot link to %s");
        provider.add("cosmiccore.link.limit_reached_self", "This machine has reached its link limit");
        provider.add("cosmiccore.link.limit_reached_partner", "Partner machine has reached its link limit");
        provider.add("cosmiccore.link.incompatible_self", "This machine cannot link to that type");
        provider.add("cosmiccore.link.incompatible_partner", "Partner machine cannot link to this type");
        provider.add("cosmiccore.link.already_linked", "These machines are already linked");
        provider.add("cosmiccore.link.too_far", "Partner is too far away to force-load for linking");

        // Link runtime status
        provider.add("cosmiccore.recipe.waiting_for_partner", "Waiting for linked partner");
        provider.add("cosmiccore.link.partner_offline", "Linked partner offline");
        provider.add("cosmiccore.multiblock.drone_station_machine.tier.0", "Plasmatic");
        provider.add("cosmiccore.multiblock.drone_station_machine.tier.1", "Sanguine");
        provider.add("cosmiccore.multiblock.drone_station_machine.tier.2", "Industrial");
        provider.add("cosmiccore.multiblock.drone_station_machine.tier.3", "Robust");
        provider.add("cosmiccore.multiblock.drone_station_machine.tier.4", "Rusty");
        provider.add("cosmiccore.multiblock.drone_station_machine.tier.5", "None");
        provider.add("cosmiccore.calorific.tooltip.prefix", "§5Calorific:§r %s");
        provider.add("cosmiccore.lubricant.tooltip.prefix", "§6Lubricant:§r Tier %s");
        provider.add("cosmiccore.booster.tooltip.prefix", "§bBooster:§r Tier %s");

        // Dreamer's Basin - Multithreaded Machine
        multiLang(provider, "cosmiccore.machine.dreamers_basin.tooltip",
                "§bRuns multiple unique recipes simultaneously",
                "§fEach thread requires a uniquely §6colored§f input bus/hatch",
                "§fMax threads = Energy Hatch amperage (4A=4, 16A=16)",
                "§aAll threads share output buses/hatches");

        // Multithreaded Machine Display (base)
        provider.add("cosmiccore.machine.multithreaded.thread_status", "§b=== Thread Status ===");
        provider.add("cosmiccore.machine.multithreaded.max_threads", "§7Max Threads: §f%s");
        provider.add("cosmiccore.machine.multithreaded.active_threads", "§7Active: §a%s§7/§f%s");

        // Dreamer's Basin Custom UI
        provider.add("cosmiccore.machine.dreamers_basin.thread_header", "Thread Status");
        provider.add("cosmiccore.machine.dreamers_basin.threads_summary", "%s running / %s active / %s max");
        provider.add("cosmiccore.machine.dreamers_basin.eu_budget_header", "Energy Budget");
        provider.add("cosmiccore.machine.dreamers_basin.eu_per_thread", "%s EU/t per thread (%s)");
        provider.add("cosmiccore.machine.dreamers_basin.time_remaining", "Time: %s remaining");
        provider.add("cosmiccore.machine.dreamers_basin.status_idle", "Idle - No recipe");
        provider.add("cosmiccore.machine.dreamers_basin.status_waiting", "Waiting for inputs");
        provider.add("cosmiccore.machine.dreamers_basin.status_suspended", "Suspended");
        provider.add("cosmiccore.machine.dreamers_basin.status_unknown", "Unknown");

        // Dreamer's Basin Hover Tooltips
        provider.add("cosmiccore.machine.dreamers_basin.tooltip.crafting", "Crafting:");
        provider.add("cosmiccore.machine.dreamers_basin.tooltip.no_recipe", "No recipe data");
        provider.add("cosmiccore.machine.dreamers_basin.tooltip.processing", "  Processing...");
        provider.add("cosmiccore.machine.dreamers_basin.tooltip.duration", "Recipe duration: %s");

        // Stellar Iris Widget UI
        provider.add("cosmiccore.stellar.prestige.title", "STELLAR CONVERGENCE");
        provider.add("cosmiccore.stellar.prestige.points_earned", "POINTS EARNED");
        provider.add("cosmiccore.stellar.prestige.total_points", "Total: %s points");
        provider.add("cosmiccore.stellar.prestige.current_tier", "CURRENT TIER");
        provider.add("cosmiccore.stellar.prestige.next_tier", "%s pts for %s");
        provider.add("cosmiccore.stellar.prestige.max_tier", "MAXIMUM TIER REACHED");
        provider.add("cosmiccore.stellar.prestige.tier_up", "TIER UP!");
        provider.add("cosmiccore.stellar.prestige.continue", "[Click anywhere to continue]");

        provider.add("cosmiccore.stellar.prestige.tier.novice", "NOVICE");
        provider.add("cosmiccore.stellar.prestige.tier.apprentice", "APPRENTICE");
        provider.add("cosmiccore.stellar.prestige.tier.journeyman", "JOURNEYMAN");
        provider.add("cosmiccore.stellar.prestige.tier.expert", "EXPERT");
        provider.add("cosmiccore.stellar.prestige.tier.master", "MASTER");
        provider.add("cosmiccore.stellar.prestige.tier.grandmaster", "GRANDMASTER");
        provider.add("cosmiccore.stellar.prestige.tier.unknown", "UNKNOWN");

        provider.add("cosmiccore.stellar.ignition.requires_star", "REQUIRES ACTIVE STAR");
        provider.add("cosmiccore.stellar.ignition.breaking", "!!! BREAKING !!!");
        provider.add("cosmiccore.stellar.ignition.ignite", "IGNITE");

        provider.add("cosmiccore.stellar.module.status", "Status");
        provider.add("cosmiccore.stellar.module.status.processing", "PROCESSING");
        provider.add("cosmiccore.stellar.module.status.idle", "IDLE");
        provider.add("cosmiccore.stellar.module.status.offline", "OFFLINE");
        provider.add("cosmiccore.stellar.module.status.ready", "READY");
        provider.add("cosmiccore.stellar.module.status.iris_inactive", "IRIS INACTIVE");
        provider.add("cosmiccore.stellar.module.status.disconnected", "DISCONNECTED");
        provider.add("cosmiccore.stellar.module.status.power_fail", "POWER FAIL");
        provider.add("cosmiccore.stellar.module.status.no_wireless", "NO WIRELESS");

        provider.add("cosmiccore.stellar.module.max_eut", "Max EU/t");
        provider.add("cosmiccore.stellar.module.parallel", "Parallel");
        provider.add("cosmiccore.stellar.module.parallel_max", "%sx (max %s)");
        provider.add("cosmiccore.stellar.module.current", "Current");
        provider.add("cosmiccore.stellar.module.speed_bonus", "Speed Bonus");
        provider.add("cosmiccore.stellar.module.iris_limit", "Iris Limit");
        provider.add("cosmiccore.stellar.module.stage", "Stage");
        provider.add("cosmiccore.stellar.module.waiting_iris", "Waiting for Iris");
        provider.add("cosmiccore.stellar.module.not_linked", "Not linked to Stellar Iris");
        provider.add("cosmiccore.stellar.module.config", "Module Config");

        provider.add("cosmiccore.stellar.power.title", "Power Control Panel");
        provider.add("cosmiccore.stellar.power.max_parallel", "Maximum Parallel");
        provider.add("cosmiccore.stellar.power.voltage_per_parallel", "Voltage Per Parallel");

        provider.add("cosmiccore.stellar.stage.initialization", "INITIALIZATION");
        provider.add("cosmiccore.stellar.stage.stellar_ignition", "STELLAR IGNITION");
        provider.add("cosmiccore.stellar.stage.stellar_operations", "STELLAR OPERATIONS");
        provider.add("cosmiccore.stellar.stage.critical_mass", "CRITICAL MASS");
        provider.add("cosmiccore.stellar.stage.singularity_control", "SINGULARITY CONTROL");
        provider.add("cosmiccore.stellar.stage.emergency_protocols", "EMERGENCY PROTOCOLS");
        provider.add("cosmiccore.stellar.stage.controlled_shutdown", "CONTROLLED SHUTDOWN");

        provider.add("cosmiccore.stellar.context.empty_line1", "Insert star seed and");
        provider.add("cosmiccore.stellar.context.empty_line2", "provide stellar gases");
        provider.add("cosmiccore.stellar.context.empty_line3", "to begin ignition.");
        provider.add("cosmiccore.stellar.context.growing_line1", "Stellar fusion");
        provider.add("cosmiccore.stellar.context.growing_line2", "initiating...");
        provider.add("cosmiccore.stellar.context.star_line1", "Stable fusion active");
        provider.add("cosmiccore.stellar.context.star_line2", "Processing available");
        provider.add("cosmiccore.stellar.context.superstar_line1", "WARNING: Critical mass");
        provider.add("cosmiccore.stellar.context.superstar_line2", "Collapse imminent");
        provider.add("cosmiccore.stellar.context.blackhole_line1", "Singularity contained");
        provider.add("cosmiccore.stellar.context.blackhole_line2", "Exotic processing");
        provider.add("cosmiccore.stellar.context.death_line1", "CRITICAL FAILURE");
        provider.add("cosmiccore.stellar.context.death_line2", "SOUL FUSE ENGAGED");
        provider.add("cosmiccore.stellar.context.death_graceful_line1", "Controlled shutdown");
        provider.add("cosmiccore.stellar.context.death_graceful_line2", "in progress...");

        provider.add("cosmiccore.stellar.slot.star_seed", "Star Seed");

        // =========================================================================
        // REFLECTION SYSTEM
        // =========================================================================
        initReflectionLang(provider);
    }

    private static void initReflectionLang(RegistrateLangProvider provider) {
        // UI Elements - Basic
        provider.add("reflection.cosmiccore.ui.void_title", "The Void Between");
        provider.add("reflection.cosmiccore.ui.constellation_title", "The Constellation of Bargains");
        provider.add("reflection.cosmiccore.ui.available_bargains", "Available Bargains");
        provider.add("reflection.cosmiccore.ui.your_bargains", "Your Bargains");
        provider.add("reflection.cosmiccore.ui.defiance", "Defiance");
        provider.add("reflection.cosmiccore.ui.continue", "[Continue]");
        provider.add("reflection.cosmiccore.ui.acknowledge", "[I understand]");
        provider.add("reflection.cosmiccore.ui.back", "[Back]");
        provider.add("reflection.cosmiccore.ui.exit", "[Leave]");
        provider.add("reflection.cosmiccore.ui.leave", "[Leave this place]");
        provider.add("reflection.cosmiccore.ui.view_bargains", "[View Available Bargains]");
        provider.add("reflection.cosmiccore.ui.view_active", "[View Your Bargains]");
        provider.add("reflection.cosmiccore.ui.enter_defiance", "[Enter Defiance Mode]");
        provider.add("reflection.cosmiccore.ui.defy_bargain", "[Defy This Bargain]");
        provider.add("reflection.cosmiccore.ui.confirm_defiance", "[Confirm Defiance]");
        provider.add("reflection.cosmiccore.ui.cancel", "[Cancel]");
        provider.add("reflection.cosmiccore.ui.select", "[Select]");
        provider.add("reflection.cosmiccore.ui.no_bargains", "No bargains accepted yet.");
        provider.add("reflection.cosmiccore.ui.defiance_warning",
                "Defying a bargain will cost you power but restore some of your soul.");
        provider.add("reflection.cosmiccore.ui.powers", "Powers:");
        provider.add("reflection.cosmiccore.ui.drawbacks", "Drawbacks:");
        provider.add("reflection.cosmiccore.ui.soul_erosion", "Soul Erosion: %d%%");
        provider.add("reflection.cosmiccore.ui.soul_erosion_display", "Soul Erosion: %s%%");
        provider.add("reflection.cosmiccore.ui.soul_label", "Soul");
        provider.add("reflection.cosmiccore.ui.dialogue_continue", "Click to continue...");
        provider.add("reflection.cosmiccore.ui.no_available_bargains", "The void has nothing to offer you... for now.");
        provider.add("reflection.cosmiccore.ui.select_to_view", "Select a bargain to view details");
        provider.add("reflection.cosmiccore.ui.cost", "Cost: %d erosion");
        provider.add("reflection.cosmiccore.ui.erosion", "erosion");
        provider.add("reflection.cosmiccore.ui.of", "of");
        provider.add("reflection.cosmiccore.ui.defy", "Defy");
        provider.add("reflection.cosmiccore.ui.tooltip.no_details", "No additional details");

        // Scroll indicators
        provider.add("reflection.cosmiccore.ui.scroll_up", "\u25B2 Scroll up");
        provider.add("reflection.cosmiccore.ui.scroll_down", "\u25BC Scroll down");

        // Hub menu options
        provider.add("reflection.cosmiccore.ui.review_bargains", "[Review your %s bargains]");
        provider.add("reflection.cosmiccore.ui.browse_bargains", "[Browse %s available bargains]");
        provider.add("reflection.cosmiccore.ui.gaze_constellation", "[Gaze upon the constellation]");
        provider.add("reflection.cosmiccore.ui.just_look", "[Just... look at yourself]");
        provider.add("reflection.cosmiccore.ui.unlock_cost", "Cost: %d soul erosion");
        provider.add("reflection.cosmiccore.ui.defiance_cost", "Defiance will cost %d erosion");

        // Hub option details
        provider.add("reflection.cosmiccore.ui.hub.review.power", "See what you've given away");
        provider.add("reflection.cosmiccore.ui.hub.review.drawback", "Consider defying a bargain");
        provider.add("reflection.cosmiccore.ui.hub.browse.power", "See what the void offers");
        provider.add("reflection.cosmiccore.ui.hub.browse.response", "So many choices... so little soul.");
        provider.add("reflection.cosmiccore.ui.hub.browse.response_empty", "Nothing for you. Yet.");
        provider.add("reflection.cosmiccore.ui.hub.reflect.power", "Contemplate your existence");
        provider.add("reflection.cosmiccore.ui.hub.review_response", "Let's see what you've become.");
        provider.add("reflection.cosmiccore.ui.hub.reflect_response", "Gazing into the abyss, are we?");
        provider.add("reflection.cosmiccore.ui.hub.leave_response", "Running away? How predictable.");

        // Hub greetings
        provider.add("reflection.cosmiccore.ui.hub.greeting.many_bargains_high.0", "Look at you. So much given away.");
        provider.add("reflection.cosmiccore.ui.hub.greeting.many_bargains_high.1",
                "Do you even remember what you were?");
        provider.add("reflection.cosmiccore.ui.hub.greeting.many_bargains.0", "Back again. Of course you are.");
        provider.add("reflection.cosmiccore.ui.hub.greeting.many_bargains.1", "Your soul grows thinner each time.");
        provider.add("reflection.cosmiccore.ui.hub.greeting.has_bargains.0", "Ah, you return. Hungry for more?");
        provider.add("reflection.cosmiccore.ui.hub.greeting.has_bargains.1", "I have plenty left to offer.");
        provider.add("reflection.cosmiccore.ui.hub.greeting.has_scars.0", "I see the scars of defiance.");
        provider.add("reflection.cosmiccore.ui.hub.greeting.has_scars.1", "You took power, then threw it away.");
        provider.add("reflection.cosmiccore.ui.hub.greeting.has_scars.2", "Was the cost of keeping it too high?");
        provider.add("reflection.cosmiccore.ui.hub.greeting.erosion_no_bargains.0",
                "Erosion without bargains? Curious.");
        provider.add("reflection.cosmiccore.ui.hub.greeting.erosion_no_bargains.1",
                "Something else has been taking from you.");
        provider.add("reflection.cosmiccore.ui.hub.greeting.erosion_no_bargains.2",
                "Perhaps you should let me help instead.");
        provider.add("reflection.cosmiccore.ui.hub.greeting.fresh.0", "A pristine soul. How rare.");
        provider.add("reflection.cosmiccore.ui.hub.greeting.fresh.1", "Don't worry. That won't last.");
        provider.add("reflection.cosmiccore.ui.hub.greeting.question", "What brings you to my domain?");

        // Reflection dialogues (erosion-based)
        provider.add("reflection.cosmiccore.ui.reflection.no_erosion.0", "Untouched. Pure. How boring.");
        provider.add("reflection.cosmiccore.ui.reflection.no_erosion.1", "You come here with nothing to show?");
        provider.add("reflection.cosmiccore.ui.reflection.no_erosion.2", "That will change. They always change.");
        provider.add("reflection.cosmiccore.ui.reflection.low_erosion.0", "Just a taste. That's how it starts.");
        provider.add("reflection.cosmiccore.ui.reflection.low_erosion.1", "You'll be back for more.");
        provider.add("reflection.cosmiccore.ui.reflection.mid_erosion.0", "Getting comfortable with the darkness?");
        provider.add("reflection.cosmiccore.ui.reflection.mid_erosion.1", "I can see it settling into you.");
        provider.add("reflection.cosmiccore.ui.reflection.high_erosion.0", "So much of you is gone now.");
        provider.add("reflection.cosmiccore.ui.reflection.high_erosion.1", "Do you remember what you were?");
        provider.add("reflection.cosmiccore.ui.reflection.extreme_erosion.0", "Almost nothing left. Almost.");
        provider.add("reflection.cosmiccore.ui.reflection.extreme_erosion.1",
                "One more push and you're mine completely.");
        provider.add("reflection.cosmiccore.ui.reflection.has_bargains.0", "I see you've made some... arrangements.");
        provider.add("reflection.cosmiccore.ui.reflection.has_bargains.1", "Each one a piece of you given away.");

        // Browsing bargains
        provider.add("reflection.cosmiccore.ui.browse.interesting_choice", "An interesting choice. Let me show you.");

        // Defiance UI
        provider.add("reflection.cosmiccore.ui.defiance.question", "You wish to break this bargain?");
        provider.add("reflection.cosmiccore.ui.defiance.lose_power", "You will lose all powers from this bargain");
        provider.add("reflection.cosmiccore.ui.defiance.scar_remains", "A scar will remain on your soul forever");
        provider.add("reflection.cosmiccore.ui.defiance.confirm", "[Yes, I defy this bargain]");
        provider.add("reflection.cosmiccore.ui.defiance.cancel", "[No, I've changed my mind]");
        provider.add("reflection.cosmiccore.ui.defiance.so_be_it", "So be it. Feel the pain of reclamation.");
        provider.add("reflection.cosmiccore.ui.defiance.wise", "Wise. The power is worth more than your principles.");
        provider.add("reflection.cosmiccore.ui.defiance.will_lose", "You will lose: %s");
        provider.add("reflection.cosmiccore.ui.defiance.cost_amount", "This will cost you %d erosion");
        provider.add("reflection.cosmiccore.ui.defiance.cannot_undo", "This cannot be undone");
        provider.add("reflection.cosmiccore.ui.defiance.warning1", "You would break the bargain of %s?");
        provider.add("reflection.cosmiccore.ui.defiance.warning2", "The cost of defiance is %d erosion.");
        provider.add("reflection.cosmiccore.ui.defiance.warning3", "The power will leave you. The scar will not.");
        provider.add("reflection.cosmiccore.ui.defiance.warning4", "Are you certain?");

        // Constellation UI
        provider.add("reflection.cosmiccore.ui.forever_scarred", "Forever Scarred");
        provider.add("reflection.cosmiccore.ui.click_to_bargain", "Click to bargain");
        provider.add("reflection.cosmiccore.ui.click_to_defy", "Click to defy (%d erosion)");
        provider.add("reflection.cosmiccore.ui.power", "Power");
        provider.add("reflection.cosmiccore.ui.drawback", "Drawback");

        // =========================================================================
        // BARGAINS
        // =========================================================================

        // --- Quake Movement Bargain (quake_movement) ---
        // Answers: yes, refuse | Dialogues: 4
        provider.add("reflection.cosmiccore.bargain.quake_movement.name", "Velocity");
        provider.add("reflection.cosmiccore.bargain.quake_movement.description",
                "Master momentum itself through unnatural locomotion");
        provider.add("reflection.cosmiccore.bargain.quake_movement.dialogue.0",
                "You move like prey. Hesitant. Fearful.");
        provider.add("reflection.cosmiccore.bargain.quake_movement.dialogue.1",
                "I remember when things moved differently. Before physics became so... rigid.");
        provider.add("reflection.cosmiccore.bargain.quake_movement.dialogue.2",
                "I can teach your legs to remember that older way.");
        provider.add("reflection.cosmiccore.bargain.quake_movement.dialogue.3",
                "But once they learn, they will never be content with stillness again.");
        provider.add("reflection.cosmiccore.bargain.quake_movement.question",
                "Will you embrace the velocity that waits within you?");
        provider.add("reflection.cosmiccore.bargain.quake_movement.answer.yes.text", "Teach me to move like the wind.");
        provider.add("reflection.cosmiccore.bargain.quake_movement.answer.yes.response",
                "Feel it now - your muscles rewiring, learning trajectories they were never meant to know.");
        provider.add("reflection.cosmiccore.bargain.quake_movement.answer.yes.power.0",
                "Bunny hopping preserves and builds momentum");
        provider.add("reflection.cosmiccore.bargain.quake_movement.answer.yes.power.1",
                "Air strafing for mid-air direction control");
        provider.add("reflection.cosmiccore.bargain.quake_movement.answer.yes.drawback.0",
                "Movement feels unnatural to observers");
        provider.add("reflection.cosmiccore.bargain.quake_movement.answer.refuse.text",
                "My feet know their own rhythm.");
        provider.add("reflection.cosmiccore.bargain.quake_movement.answer.refuse.response",
                "Such faith in the pedestrian. We shall see how long that serves you.");
        provider.add("reflection.cosmiccore.bargain.quake_movement.on_accept",
                "Your joints crack and reform. Movement becomes instinct.");
        provider.add("reflection.cosmiccore.bargain.quake_movement.on_defy",
                "Your legs remember what it was to walk normally. The speed fades like a dream.");

        // --- Depths Bargain (depths) ---
        // Answers: embrace, refuse | Dialogues: 6
        provider.add("reflection.cosmiccore.bargain.depths.name", "The Depths");
        provider.add("reflection.cosmiccore.bargain.depths.description", "Enhanced breath with fatal consequences");
        provider.add("reflection.cosmiccore.bargain.depths.dialogue.0",
                "You've felt the water closing over your head.");
        provider.add("reflection.cosmiccore.bargain.depths.dialogue.1",
                "That desperate burn in your lungs. The panic.");
        provider.add("reflection.cosmiccore.bargain.depths.dialogue.2",
                "I can remake those fragile organs. Give them capacity beyond mortal limits.");
        provider.add("reflection.cosmiccore.bargain.depths.dialogue.3",
                "Your breath will stretch to fill the void between heartbeats.");
        provider.add("reflection.cosmiccore.bargain.depths.dialogue.4",
                "But understand - when they finally empty, there will be no warning.");
        provider.add("reflection.cosmiccore.bargain.depths.dialogue.5",
                "No desperate gasps. No gradual fading. Just... silence.");
        provider.add("reflection.cosmiccore.bargain.depths.question", "Will you let me reshape your breath?");
        provider.add("reflection.cosmiccore.bargain.depths.answer.embrace.text", "Remake me for the depths.");
        provider.add("reflection.cosmiccore.bargain.depths.answer.embrace.response",
                "Your chest feels hollow now. That's normal. The new capacity needs room.");
        provider.add("reflection.cosmiccore.bargain.depths.answer.embrace.power.0", "5x oxygen capacity underwater");
        provider.add("reflection.cosmiccore.bargain.depths.answer.embrace.power.1",
                "Extended breath in toxic atmospheres");
        provider.add("reflection.cosmiccore.bargain.depths.answer.embrace.drawback.0",
                "Instant death when oxygen fully depletes");
        provider.add("reflection.cosmiccore.bargain.depths.answer.embrace.drawback.1",
                "No drowning damage warning - just death");
        provider.add("reflection.cosmiccore.bargain.depths.answer.refuse.text", "I'll keep my mortal breath.");
        provider.add("reflection.cosmiccore.bargain.depths.answer.refuse.response",
                "The depths wait patiently. They always have.");
        provider.add("reflection.cosmiccore.bargain.depths.on_accept",
                "Something shifts in your chest. The air tastes different now.");
        provider.add("reflection.cosmiccore.bargain.depths.on_defy",
                "You gasp - your lungs remember panic, remember struggle. You are mortal again.");

        // --- Swiftness Bargain (swiftness) ---
        // Answers: accept, refuse | Dialogues: 3
        provider.add("reflection.cosmiccore.bargain.swiftness.name", "Swiftness");
        provider.add("reflection.cosmiccore.bargain.swiftness.description",
                "Supernatural speed courses through your veins");
        provider.add("reflection.cosmiccore.bargain.swiftness.dialogue.0",
                "The world moves so slowly around you, doesn't it?");
        provider.add("reflection.cosmiccore.bargain.swiftness.dialogue.1",
                "Everyone else trudging through molasses while you ache to run.");
        provider.add("reflection.cosmiccore.bargain.swiftness.dialogue.2",
                "I can accelerate you. Make the world blur past like a fading dream.");
        provider.add("reflection.cosmiccore.bargain.swiftness.question", "Do you wish to leave the slow world behind?");
        provider.add("reflection.cosmiccore.bargain.swiftness.answer.accept.text", "Make me swift beyond measure.");
        provider.add("reflection.cosmiccore.bargain.swiftness.answer.accept.response",
                "Your blood sings now. Feel it racing faster than any heart should allow.");
        provider.add("reflection.cosmiccore.bargain.swiftness.answer.accept.power.0", "+40% movement speed");
        provider.add("reflection.cosmiccore.bargain.swiftness.answer.accept.power.1", "Sprint without hunger drain");
        provider.add("reflection.cosmiccore.bargain.swiftness.answer.accept.drawback.0",
                "Increased hunger when standing still");
        provider.add("reflection.cosmiccore.bargain.swiftness.answer.refuse.text", "I am content with my pace.");
        provider.add("reflection.cosmiccore.bargain.swiftness.answer.refuse.response",
                "Content. Such a mortal sentiment. It will fade.");
        provider.add("reflection.cosmiccore.bargain.swiftness.on_accept",
                "Lightning arcs through your muscles. You twitch with restless energy.");
        provider.add("reflection.cosmiccore.bargain.swiftness.on_defy",
                "The world speeds up around you. You are merely human once more.");

        // --- Stride Bargain (stride) ---
        // Answers: accept, suspicious, refuse | Dialogues: 4
        provider.add("reflection.cosmiccore.bargain.stride.name", "Stride");
        provider.add("reflection.cosmiccore.bargain.stride.description", "Walk over obstacles as if they were nothing");
        provider.add("reflection.cosmiccore.bargain.stride.dialogue.0",
                "Every ledge. Every block. Every small obstacle.");
        provider.add("reflection.cosmiccore.bargain.stride.dialogue.1", "They mock you with their need to be climbed.");
        provider.add("reflection.cosmiccore.bargain.stride.dialogue.2",
                "What if the world simply... accommodated your stride?");
        provider.add("reflection.cosmiccore.bargain.stride.dialogue.3",
                "Your feet need never leave the ground. The ground will rise to meet them.");
        provider.add("reflection.cosmiccore.bargain.stride.question", "Shall obstacles bow before your passage?");
        provider.add("reflection.cosmiccore.bargain.stride.answer.accept.text", "Let the world flatten before me.");
        provider.add("reflection.cosmiccore.bargain.stride.answer.accept.response",
                "Walk now. Feel how terrain reshapes itself for your convenience.");
        provider.add("reflection.cosmiccore.bargain.stride.answer.accept.power.0", "Auto step-up to 1 block height");
        provider.add("reflection.cosmiccore.bargain.stride.answer.accept.power.1", "Smooth terrain traversal");
        provider.add("reflection.cosmiccore.bargain.stride.answer.accept.drawback.0", "Cannot crouch-walk off edges");
        provider.add("reflection.cosmiccore.bargain.stride.answer.refuse.text", "I'll climb my own way.");
        provider.add("reflection.cosmiccore.bargain.stride.answer.refuse.response",
                "Such determination. It will erode. They always do.");
        provider.add("reflection.cosmiccore.bargain.stride.on_accept",
                "The earth seems to shift slightly, eager to smooth your path.");
        provider.add("reflection.cosmiccore.bargain.stride.on_defy",
                "Gravity reasserts itself. Every ledge is a challenge again.");

        // --- Darksight Bargain (darksight) ---
        // Answers: yes, refuse | Dialogues: 5
        provider.add("reflection.cosmiccore.bargain.darksight.name", "Darksight");
        provider.add("reflection.cosmiccore.bargain.darksight.description", "See through the deepest darkness");
        provider.add("reflection.cosmiccore.bargain.darksight.dialogue.0",
                "You fear the dark. Every creature does, at first.");
        provider.add("reflection.cosmiccore.bargain.darksight.dialogue.1",
                "But darkness is merely the absence of something. Not presence.");
        provider.add("reflection.cosmiccore.bargain.darksight.dialogue.2",
                "I can teach your eyes to drink the shadow. To see what lurks unseen.");
        provider.add("reflection.cosmiccore.bargain.darksight.dialogue.3",
                "Every hidden corner will surrender its secrets to you.");
        provider.add("reflection.cosmiccore.bargain.darksight.dialogue.4",
                "But be warned - the light will begin to burn.");
        provider.add("reflection.cosmiccore.bargain.darksight.question",
                "Will you trade the sun for the gift of shadow-sight?");
        provider.add("reflection.cosmiccore.bargain.darksight.answer.yes.text", "Let me see in the darkness.");
        provider.add("reflection.cosmiccore.bargain.darksight.answer.yes.response",
                "Your pupils dilate... and keep dilating. The dark becomes your domain.");
        provider.add("reflection.cosmiccore.bargain.darksight.answer.yes.power.0", "Permanent Night Vision effect");
        provider.add("reflection.cosmiccore.bargain.darksight.answer.yes.power.1", "See in complete darkness");
        provider.add("reflection.cosmiccore.bargain.darksight.answer.yes.drawback.0",
                "Blindness effect in bright sunlight");
        provider.add("reflection.cosmiccore.bargain.darksight.answer.yes.drawback.1",
                "Must wear helmet or stay underground during day");
        provider.add("reflection.cosmiccore.bargain.darksight.answer.refuse.text", "The light serves me well enough.");
        provider.add("reflection.cosmiccore.bargain.darksight.answer.refuse.response",
                "Cling to your torch then. See how long it lasts in the deep places.");
        provider.add("reflection.cosmiccore.bargain.darksight.on_accept",
                "The shadows retreat from your vision. You see everything now.");
        provider.add("reflection.cosmiccore.bargain.darksight.on_defy",
                "Light floods back. The darkness closes its secrets to you once more.");

        // --- Carapace Bargain (carapace) ---
        // Answers: survive, feel | Dialogues: 5
        provider.add("reflection.cosmiccore.bargain.carapace.name", "Carapace");
        provider.add("reflection.cosmiccore.bargain.carapace.description", "Your flesh hardens into living armor");
        provider.add("reflection.cosmiccore.bargain.carapace.dialogue.0", "Your skin is so soft. So vulnerable.");
        provider.add("reflection.cosmiccore.bargain.carapace.dialogue.1",
                "Every blade, every claw, every falling stone - they all threaten you.");
        provider.add("reflection.cosmiccore.bargain.carapace.dialogue.2",
                "I can harden your flesh. Make it something more... enduring.");
        provider.add("reflection.cosmiccore.bargain.carapace.dialogue.3",
                "Blows will glance off. Damage will diminish.");
        provider.add("reflection.cosmiccore.bargain.carapace.dialogue.4",
                "But you will feel less. Touch will become... distant.");
        provider.add("reflection.cosmiccore.bargain.carapace.question", "Will you sacrifice sensation for survival?");
        provider.add("reflection.cosmiccore.bargain.carapace.answer.survive.text", "Harden me. I choose survival.");
        provider.add("reflection.cosmiccore.bargain.carapace.answer.survive.response",
                "Your skin tightens. Hardens. You are becoming something more durable.");
        provider.add("reflection.cosmiccore.bargain.carapace.answer.survive.power.0",
                "+8 armor points (4 full armor icons)");
        provider.add("reflection.cosmiccore.bargain.carapace.answer.survive.power.1", "Stacks with worn armor");
        provider.add("reflection.cosmiccore.bargain.carapace.answer.survive.drawback.0",
                "-20% healing from all sources");
        provider.add("reflection.cosmiccore.bargain.carapace.answer.survive.drawback.1",
                "Reduced potion effectiveness");
        provider.add("reflection.cosmiccore.bargain.carapace.answer.refuse.text",
                "I would rather feel than merely endure.");
        provider.add("reflection.cosmiccore.bargain.carapace.answer.refuse.response",
                "Feeling. How fragile. How mortal. How temporary.");
        provider.add("reflection.cosmiccore.bargain.carapace.on_accept",
                "Your flesh ripples and tightens. It doesn't hurt. That's the point.");
        provider.add("reflection.cosmiccore.bargain.carapace.on_defy",
                "Sensation floods back - every breeze, every texture. You are soft again.");

        // --- Soft Landing Bargain (soft_landing) ---
        // Answers: yes, careful | Dialogues: 5
        provider.add("reflection.cosmiccore.bargain.soft_landing.name", "Soft Landing");
        provider.add("reflection.cosmiccore.bargain.soft_landing.description",
                "The ground welcomes you gently from any height");
        provider.add("reflection.cosmiccore.bargain.soft_landing.dialogue.0",
                "Heights terrify you. The primal fear of falling.");
        provider.add("reflection.cosmiccore.bargain.soft_landing.dialogue.1",
                "That sickening moment when gravity claims you.");
        provider.add("reflection.cosmiccore.bargain.soft_landing.dialogue.2", "But what if the ground... forgave you?");
        provider.add("reflection.cosmiccore.bargain.soft_landing.dialogue.3",
                "What if every fall ended softly, no matter the height?");
        provider.add("reflection.cosmiccore.bargain.soft_landing.dialogue.4", "You need never fear the drop again.");
        provider.add("reflection.cosmiccore.bargain.soft_landing.question", "Will you let the earth catch you?");
        provider.add("reflection.cosmiccore.bargain.soft_landing.answer.yes.text", "Take away my fear of falling.");
        provider.add("reflection.cosmiccore.bargain.soft_landing.answer.yes.response",
                "Jump. Go ahead. The ground will embrace you like a mother.");
        provider.add("reflection.cosmiccore.bargain.soft_landing.answer.yes.power.0", "Complete fall damage immunity");
        provider.add("reflection.cosmiccore.bargain.soft_landing.answer.yes.power.1", "Safe drops from any height");
        provider.add("reflection.cosmiccore.bargain.soft_landing.answer.yes.drawback.0",
                "+15% damage taken from all sources");
        provider.add("reflection.cosmiccore.bargain.soft_landing.answer.yes.drawback.1",
                "Reduced knockback resistance");
        provider.add("reflection.cosmiccore.bargain.soft_landing.answer.refuse.text",
                "Fear keeps me cautious. I'll keep it.");
        provider.add("reflection.cosmiccore.bargain.soft_landing.answer.refuse.response",
                "Caution. A slow path to nowhere. But walk it if you must.");
        provider.add("reflection.cosmiccore.bargain.soft_landing.on_accept",
                "Your relationship with gravity shifts. It still pulls, but gently now.");
        provider.add("reflection.cosmiccore.bargain.soft_landing.on_defy",
                "Weight crashes back into your bones. Every fall matters again.");

        // --- Cinder Bargain (cinder) ---
        // Answers: burn, refuse | Dialogues: 5
        provider.add("reflection.cosmiccore.bargain.cinder.name", "Cinder");
        provider.add("reflection.cosmiccore.bargain.cinder.description", "Fire cannot harm what has already burned");
        provider.add("reflection.cosmiccore.bargain.cinder.dialogue.0",
                "Fire consumes. It is in its nature to destroy.");
        provider.add("reflection.cosmiccore.bargain.cinder.dialogue.1",
                "Every flame that touches you leaves its mark.");
        provider.add("reflection.cosmiccore.bargain.cinder.dialogue.2",
                "But what if you had already burned? Completely. Utterly.");
        provider.add("reflection.cosmiccore.bargain.cinder.dialogue.3",
                "Fire cannot consume what is already ash and ember.");
        provider.add("reflection.cosmiccore.bargain.cinder.dialogue.4",
                "Let me burn away your vulnerability. You will walk through infernos unscathed.");
        provider.add("reflection.cosmiccore.bargain.cinder.question",
                "Will you let the flame claim you, so it can never hurt you again?");
        provider.add("reflection.cosmiccore.bargain.cinder.answer.burn.text", "Burn me completely. Make me immune.");
        provider.add("reflection.cosmiccore.bargain.cinder.answer.burn.response",
                "It hurts. Just for a moment. Then the pain becomes a memory, and fire becomes a friend.");
        provider.add("reflection.cosmiccore.bargain.cinder.answer.burn.power.0", "Complete fire and lava immunity");
        provider.add("reflection.cosmiccore.bargain.cinder.answer.burn.power.1", "Can swim in lava safely");
        provider.add("reflection.cosmiccore.bargain.cinder.answer.burn.drawback.0",
                "2x damage from freezing and cold sources");
        provider.add("reflection.cosmiccore.bargain.cinder.answer.burn.drawback.1",
                "Water extinguishes slower, feels unpleasant");
        provider.add("reflection.cosmiccore.bargain.cinder.answer.refuse.text",
                "Fire should be respected, not befriended.");
        provider.add("reflection.cosmiccore.bargain.cinder.answer.refuse.response",
                "Respect. For something that would consume you without thought. How noble.");
        provider.add("reflection.cosmiccore.bargain.cinder.on_accept",
                "Heat floods through you, then recedes. Fire will never frighten you again.");
        provider.add("reflection.cosmiccore.bargain.cinder.on_defy",
                "The warmth drains away. Flames flicker hungrily when they see you now.");

        // --- Vitality Bargain (vitality) ---
        // Answers: accept, refuse | Dialogues: 5
        provider.add("reflection.cosmiccore.bargain.vitality.name", "Vitality");
        provider.add("reflection.cosmiccore.bargain.vitality.description", "Life force beyond mortal limits");
        provider.add("reflection.cosmiccore.bargain.vitality.dialogue.0",
                "Your body has limits. A fixed amount of life.");
        provider.add("reflection.cosmiccore.bargain.vitality.dialogue.1",
                "When it empties, you die. Simple. Brutal. Final.");
        provider.add("reflection.cosmiccore.bargain.vitality.dialogue.2",
                "I can give you more. Stretch your life force beyond its natural bounds.");
        provider.add("reflection.cosmiccore.bargain.vitality.dialogue.3",
                "More blood. More breath. More heartbeats before the end.");
        provider.add("reflection.cosmiccore.bargain.vitality.dialogue.4",
                "But maintaining excess takes effort. You will heal... slower.");
        provider.add("reflection.cosmiccore.bargain.vitality.question", "Will you trade recovery for resilience?");
        provider.add("reflection.cosmiccore.bargain.vitality.answer.accept.text", "Give me more life to spend.");
        provider.add("reflection.cosmiccore.bargain.vitality.answer.accept.response",
                "Your heart swells. Literally. It has more to pump now.");
        provider.add("reflection.cosmiccore.bargain.vitality.answer.accept.power.0", "+10 max health (5 extra hearts)");
        provider.add("reflection.cosmiccore.bargain.vitality.answer.accept.power.1",
                "Increased damage absorption buffer");
        provider.add("reflection.cosmiccore.bargain.vitality.answer.accept.drawback.0",
                "-50% natural regeneration rate");
        provider.add("reflection.cosmiccore.bargain.vitality.answer.accept.drawback.1",
                "Healing potions 30% less effective");
        provider.add("reflection.cosmiccore.bargain.vitality.answer.refuse.text", "I'll work with what I was given.");
        provider.add("reflection.cosmiccore.bargain.vitality.answer.refuse.response",
                "Given. As if anyone gave you anything. You simply are. For now.");
        provider.add("reflection.cosmiccore.bargain.vitality.on_accept",
                "Your veins surge with new vigor. Everything feels more... present.");
        provider.add("reflection.cosmiccore.bargain.vitality.on_defy",
                "The excess drains away. You are mortal-sized once more.");

        // --- Satiated Bargain (satiated) ---
        // Answers: empty, food | Dialogues: 5
        provider.add("reflection.cosmiccore.bargain.satiated.name", "Satiated");
        provider.add("reflection.cosmiccore.bargain.satiated.description", "Hunger becomes a distant memory");
        provider.add("reflection.cosmiccore.bargain.satiated.dialogue.0",
                "The constant gnawing. The endless need to consume.");
        provider.add("reflection.cosmiccore.bargain.satiated.dialogue.1", "Your stomach rules you like a tyrant.");
        provider.add("reflection.cosmiccore.bargain.satiated.dialogue.2",
                "What if I could silence it? Make fullness your natural state?");
        provider.add("reflection.cosmiccore.bargain.satiated.dialogue.3",
                "You would eat for taste, for ritual - never for need.");
        provider.add("reflection.cosmiccore.bargain.satiated.dialogue.4",
                "But taste itself would fade. Food becomes... fuel. Nothing more.");
        provider.add("reflection.cosmiccore.bargain.satiated.question",
                "Will you trade the pleasure of eating for freedom from hunger?");
        provider.add("reflection.cosmiccore.bargain.satiated.answer.empty.text", "Free me from this hunger.");
        provider.add("reflection.cosmiccore.bargain.satiated.answer.empty.response",
                "The emptiness fades. You will never truly need to eat again.");
        provider.add("reflection.cosmiccore.bargain.satiated.answer.empty.power.0", "Hunger depletes 80% slower");
        provider.add("reflection.cosmiccore.bargain.satiated.answer.empty.power.1", "Food provides 3x saturation");
        provider.add("reflection.cosmiccore.bargain.satiated.answer.empty.drawback.0",
                "Food restores 50% less hunger bars");
        provider.add("reflection.cosmiccore.bargain.satiated.answer.empty.drawback.1",
                "Cannot benefit from food-based buffs");
        provider.add("reflection.cosmiccore.bargain.satiated.answer.refuse.text",
                "I enjoy my meals. I'll keep the hunger.");
        provider.add("reflection.cosmiccore.bargain.satiated.answer.refuse.response",
                "Enjoy. Such a mortal word. The hunger will remind you of your place.");
        provider.add("reflection.cosmiccore.bargain.satiated.on_accept",
                "The gnawing stops. Silence in your belly. Freedom.");
        provider.add("reflection.cosmiccore.bargain.satiated.on_defy",
                "Hunger returns with a vengeance. You remember what need feels like.");

        // --- Back Bargain (back) ---
        // Answers: accept, refuse | Dialogues: 3
        provider.add("reflection.cosmiccore.bargain.back.name", "The Way Back");
        provider.add("reflection.cosmiccore.bargain.back.description", "Return to where you fell");
        provider.add("reflection.cosmiccore.bargain.back.dialogue.0",
                "Death scatters you. Sends you back to beds, to spawns, to arbitrary points.");
        provider.add("reflection.cosmiccore.bargain.back.dialogue.1",
                "But what if you could return to where you fell?");
        provider.add("reflection.cosmiccore.bargain.back.dialogue.2",
                "I can teach you to remember. To reach back through death itself.");
        provider.add("reflection.cosmiccore.bargain.back.question", "Will you learn to retrace death's path?");
        provider.add("reflection.cosmiccore.bargain.back.answer.accept.text", "Teach me to find my way back.");
        provider.add("reflection.cosmiccore.bargain.back.answer.accept.response",
                "Death becomes a waypoint now. Not an ending - a detour.");
        provider.add("reflection.cosmiccore.bargain.back.answer.accept.power.0",
                "Teleport to death location (once per death)");
        provider.add("reflection.cosmiccore.bargain.back.answer.accept.power.1", "Death marker visible through walls");
        provider.add("reflection.cosmiccore.bargain.back.answer.accept.drawback.0", "5 erosion cost per teleport use");
        provider.add("reflection.cosmiccore.bargain.back.answer.accept.drawback.1", "Marker fades after 10 minutes");
        provider.add("reflection.cosmiccore.bargain.back.answer.refuse.text", "Death should have consequences.");
        provider.add("reflection.cosmiccore.bargain.back.answer.refuse.response",
                "Consequences. You'll have plenty of those regardless.");
        provider.add("reflection.cosmiccore.bargain.back.on_accept",
                "A thread connects you to your last breath. You can follow it back.");
        provider.add("reflection.cosmiccore.bargain.back.on_defy", "The thread snaps. Death becomes final once more.");

        // --- Home Bargain (home) ---
        // Answers: accept, refuse | Dialogues: 3
        provider.add("reflection.cosmiccore.bargain.home.name", "Homeward");
        provider.add("reflection.cosmiccore.bargain.home.description", "The way home is always open");
        provider.add("reflection.cosmiccore.bargain.home.dialogue.0", "Home. Such a powerful concept for mortals.");
        provider.add("reflection.cosmiccore.bargain.home.dialogue.1",
                "The place you return to. The anchor that grounds you.");
        provider.add("reflection.cosmiccore.bargain.home.dialogue.2",
                "I can make that connection stronger. Instant. Unbreakable.");
        provider.add("reflection.cosmiccore.bargain.home.question",
                "Will you bind yourself to your home with chains of void?");
        provider.add("reflection.cosmiccore.bargain.home.answer.accept.text", "Bind me to my home.");
        provider.add("reflection.cosmiccore.bargain.home.answer.accept.response",
                "Feel the pull now? Home is never more than a thought away.");
        provider.add("reflection.cosmiccore.bargain.home.answer.accept.power.0", "Instant teleport to spawn/bed point");
        provider.add("reflection.cosmiccore.bargain.home.answer.accept.power.1", "5 minute cooldown between uses");
        provider.add("reflection.cosmiccore.bargain.home.answer.accept.drawback.0", "10 erosion cost per teleport");
        provider.add("reflection.cosmiccore.bargain.home.answer.accept.drawback.1", "-10% XP gain while far from home");
        provider.add("reflection.cosmiccore.bargain.home.answer.refuse.text", "Home should be earned, not summoned.");
        provider.add("reflection.cosmiccore.bargain.home.answer.refuse.response",
                "Earned. Through miles of walking. How charmingly primitive.");
        provider.add("reflection.cosmiccore.bargain.home.on_accept",
                "A cord of void stretches between you and home. Pull it anytime.");
        provider.add("reflection.cosmiccore.bargain.home.on_defy",
                "The cord dissolves. Home is a journey again, not a destination.");

        // --- Ascension Bargain (ascension) ---
        // Answers: ready, refuse | Dialogues: 6
        provider.add("reflection.cosmiccore.bargain.ascension.name", "Ascension");
        provider.add("reflection.cosmiccore.bargain.ascension.description", "Rise above the crawling earth");
        provider.add("reflection.cosmiccore.bargain.ascension.dialogue.0",
                "You are bound to the ground. Chained by gravity's petty tyranny.");
        provider.add("reflection.cosmiccore.bargain.ascension.dialogue.1", "You dream of flight. All mortals do.");
        provider.add("reflection.cosmiccore.bargain.ascension.dialogue.2", "I can sever those chains. Let you rise.");
        provider.add("reflection.cosmiccore.bargain.ascension.dialogue.3",
                "Not gliding. Not falling with style. True flight.");
        provider.add("reflection.cosmiccore.bargain.ascension.dialogue.4", "The sky will open to you like a door.");
        provider.add("reflection.cosmiccore.bargain.ascension.dialogue.5",
                "But the ground... the ground will become alien. Uncomfortable. Wrong.");
        provider.add("reflection.cosmiccore.bargain.ascension.question", "Will you abandon the earth for the sky?");
        provider.add("reflection.cosmiccore.bargain.ascension.answer.ready.text", "I am ready to fly.");
        provider.add("reflection.cosmiccore.bargain.ascension.answer.ready.response",
                "Then rise. The ground has no claim on you anymore.");
        provider.add("reflection.cosmiccore.bargain.ascension.answer.ready.power.0",
                "Creative-style flight (toggle with jump while airborne)");
        provider.add("reflection.cosmiccore.bargain.ascension.answer.ready.power.1",
                "Fly indefinitely without hunger or stamina cost");
        provider.add("reflection.cosmiccore.bargain.ascension.answer.ready.power.2",
                "Full 3D movement control while flying");
        provider.add("reflection.cosmiccore.bargain.ascension.answer.ready.power.3",
                "No fall damage while flight is active");
        provider.add("reflection.cosmiccore.bargain.ascension.answer.ready.drawback.0",
                "-30% movement speed when not flying");
        provider.add("reflection.cosmiccore.bargain.ascension.answer.ready.drawback.1",
                "Vulnerable in no-fly zones or enclosed spaces");
        provider.add("reflection.cosmiccore.bargain.ascension.answer.refuse.text", "The ground has served me well.");
        provider.add("reflection.cosmiccore.bargain.ascension.answer.refuse.response",
                "Served. Like a servant. How long until you realize you were the servant all along?");
        provider.add("reflection.cosmiccore.bargain.ascension.on_accept",
                "Weight leaves you. The sky opens. You are no longer earth-bound.");
        provider.add("reflection.cosmiccore.bargain.ascension.on_defy",
                "Gravity reclaims you. The ground welcomes you back, possessively.");

        // --- Violence Bargain (violence) ---
        // Answers: accept, refuse | Dialogues: 5
        provider.add("reflection.cosmiccore.bargain.violence.name", "Violence");
        provider.add("reflection.cosmiccore.bargain.violence.description",
                "Strike with the force of something terrible");
        provider.add("reflection.cosmiccore.bargain.violence.dialogue.0", "Your blows are so... restrained. Hesitant.");
        provider.add("reflection.cosmiccore.bargain.violence.dialogue.1",
                "You hold back. Every swing. Some part of you fears the damage.");
        provider.add("reflection.cosmiccore.bargain.violence.dialogue.2",
                "I can remove that restraint. Let your violence flow freely.");
        provider.add("reflection.cosmiccore.bargain.violence.dialogue.3", "Your enemies will shatter before you.");
        provider.add("reflection.cosmiccore.bargain.violence.dialogue.4",
                "But violence is a river that flows both ways.");
        provider.add("reflection.cosmiccore.bargain.violence.question",
                "Will you embrace true, unrestrained violence?");
        provider.add("reflection.cosmiccore.bargain.violence.answer.accept.text", "Remove my restraints.");
        provider.add("reflection.cosmiccore.bargain.violence.answer.accept.response",
                "Feel it now? The urge to destroy? Don't fight it. It's yours.");
        provider.add("reflection.cosmiccore.bargain.violence.answer.accept.power.0", "+30% melee damage dealt");
        provider.add("reflection.cosmiccore.bargain.violence.answer.accept.power.1", "+15% attack speed");
        provider.add("reflection.cosmiccore.bargain.violence.answer.accept.drawback.0",
                "+20% damage taken from all sources");
        provider.add("reflection.cosmiccore.bargain.violence.answer.accept.drawback.1", "Cannot use shields");
        provider.add("reflection.cosmiccore.bargain.violence.answer.refuse.text", "Restraint is its own strength.");
        provider.add("reflection.cosmiccore.bargain.violence.answer.refuse.response",
                "Restraint. A leash you put on yourself. How adorable.");
        provider.add("reflection.cosmiccore.bargain.violence.on_accept",
                "Power surges through your arms. Everything looks so... breakable now.");
        provider.add("reflection.cosmiccore.bargain.violence.on_defy",
                "The rage drains away. Your blows return to mortal weight.");

        // --- Reach Bargain (reach) ---
        // Answers: further, practical, refuse | Dialogues: 4
        provider.add("reflection.cosmiccore.bargain.reach.name", "Reach");
        provider.add("reflection.cosmiccore.bargain.reach.description", "Your grasp extends beyond natural limits");
        provider.add("reflection.cosmiccore.bargain.reach.dialogue.0",
                "So close, yet so far. The eternal frustration of short arms.");
        provider.add("reflection.cosmiccore.bargain.reach.dialogue.1",
                "Everything just slightly out of reach. Mocking you.");
        provider.add("reflection.cosmiccore.bargain.reach.dialogue.2",
                "I can extend you. Stretch your grasp beyond mortal limits.");
        provider.add("reflection.cosmiccore.bargain.reach.dialogue.3",
                "Your arms will find what they seek. But others may find them... unsettling.");
        provider.add("reflection.cosmiccore.bargain.reach.question", "Will you extend your reach into the unnatural?");
        provider.add("reflection.cosmiccore.bargain.reach.answer.further.text",
                "Stretch me further. I want to grasp everything.");
        provider.add("reflection.cosmiccore.bargain.reach.answer.further.response",
                "There. Don't look at your hands too closely. It's easier that way.");
        provider.add("reflection.cosmiccore.bargain.reach.answer.further.power.0",
                "+3 block reach (build from further)");
        provider.add("reflection.cosmiccore.bargain.reach.answer.further.power.1", "+2 attack reach");
        provider.add("reflection.cosmiccore.bargain.reach.answer.further.drawback.0", "-15% mining speed");
        provider.add("reflection.cosmiccore.bargain.reach.answer.further.drawback.1", "Item pickup range reduced");
        provider.add("reflection.cosmiccore.bargain.reach.answer.refuse.text", "My reach is sufficient.");
        provider.add("reflection.cosmiccore.bargain.reach.answer.refuse.response",
                "For now. But you'll want more. They always do.");
        provider.add("reflection.cosmiccore.bargain.reach.on_accept",
                "Something shifts in your shoulders. Your arms remember being longer.");
        provider.add("reflection.cosmiccore.bargain.reach.on_defy",
                "Your arms contract back to normal. The world feels close and small again.");

        // --- Void Anchor Bargain (void_anchor) ---
        // Answers: anchor, refuse | Dialogues: 6
        provider.add("reflection.cosmiccore.bargain.void_anchor.name", "Void Anchor");
        provider.add("reflection.cosmiccore.bargain.void_anchor.description",
                "The void cannot claim what is already its own");
        provider.add("reflection.cosmiccore.bargain.void_anchor.dialogue.0",
                "You've felt it. The pull of the void beneath the world.");
        provider.add("reflection.cosmiccore.bargain.void_anchor.dialogue.1",
                "That endless fall. That final darkness that swallows everything.");
        provider.add("reflection.cosmiccore.bargain.void_anchor.dialogue.2",
                "I dwell there. In that space between existence and nothing.");
        provider.add("reflection.cosmiccore.bargain.void_anchor.dialogue.3",
                "I can mark you. Make you mine. And what is mine, the void cannot destroy.");
        provider.add("reflection.cosmiccore.bargain.void_anchor.dialogue.4",
                "Fall as far as you like. The darkness will recognize you. Welcome you.");
        provider.add("reflection.cosmiccore.bargain.void_anchor.dialogue.5",
                "But being marked by the void... it changes how existence sees you.");
        provider.add("reflection.cosmiccore.bargain.void_anchor.question", "Will you become an anchor in the nothing?");
        provider.add("reflection.cosmiccore.bargain.void_anchor.answer.anchor.text", "Mark me. Make me yours.");
        provider.add("reflection.cosmiccore.bargain.void_anchor.answer.anchor.response",
                "Done. The void knows your name now. It will not harm what belongs to it.");
        provider.add("reflection.cosmiccore.bargain.void_anchor.answer.anchor.power.0", "Void damage immunity (Y < 0)");
        provider.add("reflection.cosmiccore.bargain.void_anchor.answer.anchor.power.1",
                "Teleport to surface when entering void");
        provider.add("reflection.cosmiccore.bargain.void_anchor.answer.anchor.drawback.0",
                "-25% damage in lit areas (sky access)");
        provider.add("reflection.cosmiccore.bargain.void_anchor.answer.anchor.drawback.1",
                "Takes damage from direct sunlight exposure");
        provider.add("reflection.cosmiccore.bargain.void_anchor.answer.refuse.text",
                "I'll stay in the light, thank you.");
        provider.add("reflection.cosmiccore.bargain.void_anchor.answer.refuse.response",
                "The light. Yes. Such a thin shield against the endless dark. Good luck.");
        provider.add("reflection.cosmiccore.bargain.void_anchor.on_accept",
                "Something cold touches your soul. Marks it. The void knows you now.");
        provider.add("reflection.cosmiccore.bargain.void_anchor.on_defy",
                "The mark burns away. The void forgets you. It will not be merciful next time.");

        // =========================================================================
        // THRESHOLD ENCOUNTERS
        // =========================================================================

        // --- Threshold 0 (10% erosion) ---
        provider.add("reflection.cosmiccore.threshold.0.dialogue.0", "You took something that wasn't freely given.");
        provider.add("reflection.cosmiccore.threshold.0.dialogue.1", "Did it feel good? The power flooding in?");
        provider.add("reflection.cosmiccore.threshold.0.dialogue.2", "Of course it did. That's the point.");
        provider.add("reflection.cosmiccore.threshold.0.dialogue.3",
                "I'll be watching now. We have business together.");
        provider.add("reflection.cosmiccore.threshold.0.question", "Do you understand what you've started?");
        provider.add("reflection.cosmiccore.threshold.0.response", "Good. Or not. It doesn't matter now.");

        // --- Threshold 1 (20% erosion) ---
        provider.add("reflection.cosmiccore.threshold.1.dialogue.0", "Already back for more. I'm not surprised.");
        provider.add("reflection.cosmiccore.threshold.1.dialogue.1", "Your soul stretches thinner. Can you feel it?");
        provider.add("reflection.cosmiccore.threshold.1.dialogue.2", "Like taffy. Like mist. Like a memory fading.");
        provider.add("reflection.cosmiccore.threshold.1.dialogue.3", "Don't worry. You have plenty left. For now.");
        provider.add("reflection.cosmiccore.threshold.1.question", "Still comfortable?");
        provider.add("reflection.cosmiccore.threshold.1.response", "Comfort is overrated anyway.");

        // --- Threshold 2 (30% erosion) ---
        provider.add("reflection.cosmiccore.threshold.2.dialogue.0", "A third of you belongs to me now.");
        provider.add("reflection.cosmiccore.threshold.2.dialogue.1", "That's not metaphor. I can see it. Taste it.");
        provider.add("reflection.cosmiccore.threshold.2.dialogue.2", "Your edges blur. Your definition softens.");
        provider.add("reflection.cosmiccore.threshold.2.dialogue.3", "Others might start to notice soon.");
        provider.add("reflection.cosmiccore.threshold.2.question", "Having second thoughts?");
        provider.add("reflection.cosmiccore.threshold.2.response",
                "Second thoughts require a first. You never had one.");

        // --- Threshold 3 (40% erosion) ---
        provider.add("reflection.cosmiccore.threshold.3.dialogue.0", "The dreams are starting, aren't they?");
        provider.add("reflection.cosmiccore.threshold.3.dialogue.1", "The ones where you fall and never land.");
        provider.add("reflection.cosmiccore.threshold.3.dialogue.2",
                "Where you look in a mirror and something else looks back.");
        provider.add("reflection.cosmiccore.threshold.3.dialogue.3", "That's not a dream. That's prophecy.");
        provider.add("reflection.cosmiccore.threshold.3.question", "Do you still know who you are?");
        provider.add("reflection.cosmiccore.threshold.3.response", "Keep telling yourself that name means something.");

        // --- Threshold 4 (50% erosion) ---
        provider.add("reflection.cosmiccore.threshold.4.dialogue.0", "Halfway. The point of no return approaches.");
        provider.add("reflection.cosmiccore.threshold.4.dialogue.1", "Half of you, gone. Given away for trinkets.");
        provider.add("reflection.cosmiccore.threshold.4.dialogue.2", "Was it worth it? Don't answer. I don't care.");
        provider.add("reflection.cosmiccore.threshold.4.dialogue.3", "What matters is what comes next.");
        provider.add("reflection.cosmiccore.threshold.4.question", "Ready to see what's on the other side?");
        provider.add("reflection.cosmiccore.threshold.4.response", "No one ever is. But they cross anyway.");

        // --- Threshold 5 (60% erosion) ---
        provider.add("reflection.cosmiccore.threshold.5.dialogue.0", "More of you is mine than yours now.");
        provider.add("reflection.cosmiccore.threshold.5.dialogue.1", "Does that frighten you? It should.");
        provider.add("reflection.cosmiccore.threshold.5.dialogue.2", "I know thoughts you haven't had yet.");
        provider.add("reflection.cosmiccore.threshold.5.dialogue.3", "I feel feelings you've forgotten.");
        provider.add("reflection.cosmiccore.threshold.5.question", "Who's really in control?");
        provider.add("reflection.cosmiccore.threshold.5.response", "Keep pretending you still have choice.");

        // --- Threshold 6 (70% erosion) ---
        provider.add("reflection.cosmiccore.threshold.6.dialogue.0", "Your reflection doesn't quite match anymore.");
        provider.add("reflection.cosmiccore.threshold.6.dialogue.1", "The delay is slight. Others might not notice.");
        provider.add("reflection.cosmiccore.threshold.6.dialogue.2", "But you know. You feel it.");
        provider.add("reflection.cosmiccore.threshold.6.question", "What stares back at you in mirrors?");
        provider.add("reflection.cosmiccore.threshold.6.response", "Me. Always me now.");

        // --- Threshold 7 (80% erosion) ---
        provider.add("reflection.cosmiccore.threshold.7.dialogue.0", "So little left of what you were.");
        provider.add("reflection.cosmiccore.threshold.7.dialogue.1", "Fragments. Echoes. Shadows of intention.");
        provider.add("reflection.cosmiccore.threshold.7.dialogue.2", "The body walks. The mind calculates.");
        provider.add("reflection.cosmiccore.threshold.7.dialogue.3", "But the soul? The soul is almost spent.");
        provider.add("reflection.cosmiccore.threshold.7.question", "Can you remember your mother's face?");
        provider.add("reflection.cosmiccore.threshold.7.response", "No. You can't. I took that already.");

        // --- Threshold 8 (90% erosion) ---
        provider.add("reflection.cosmiccore.threshold.8.dialogue.0", "One step from the edge now.");
        provider.add("reflection.cosmiccore.threshold.8.dialogue.1", "Ten percent. A sliver. A thread.");
        provider.add("reflection.cosmiccore.threshold.8.dialogue.2", "That's all that separates you from... me.");
        provider.add("reflection.cosmiccore.threshold.8.dialogue.3", "One more bargain. Just one more.");
        provider.add("reflection.cosmiccore.threshold.8.question", "Will you take that final step?");
        provider.add("reflection.cosmiccore.threshold.8.response", "We both know you will. The question is when.");

        // --- Threshold 9 (100% erosion) ---
        provider.add("reflection.cosmiccore.threshold.9.dialogue.0", "Finally.");
        provider.add("reflection.cosmiccore.threshold.9.dialogue.1", "You gave everything. Every last piece.");
        provider.add("reflection.cosmiccore.threshold.9.dialogue.2", "There's nothing left of what walked in here.");
        provider.add("reflection.cosmiccore.threshold.9.dialogue.3", "Only power. Only hunger. Only me.");
        provider.add("reflection.cosmiccore.threshold.9.question", "What do you see when you look at yourself?");
        provider.add("reflection.cosmiccore.threshold.9.response", "Nothing. Because there's nothing left to see.");

        // Stellar Iris Module System
        provider.add("cosmiccore.multiblock.stellar_module.not_connected", "§cNot Connected to Stellar Iris");
        provider.add("cosmiccore.multiblock.stellar_module.iris_not_formed", "§cStellar Iris Not Formed");
        provider.add("cosmiccore.multiblock.stellar_module.iris_not_ready", "§eStellar Iris Not Ready");
        provider.add("cosmiccore.multiblock.stellar_module.connected", "§aConnected to Stellar Iris");
        provider.add("cosmiccore.multiblock.stellar_module.stage", "§7Iris Stage: §e%s");
        provider.add("cosmiccore.multiblock.stellar_module.speed_bonus", "§7Speed Bonus: §a%s");
        provider.add("cosmiccore.multiblock.stellar_module.parallel", "§7Parallel Limit: §b%s");
        provider.add("cosmiccore.multiblock.stellar_module.no_wireless", "§cNo Wireless Energy Network");
        provider.add("cosmiccore.multiblock.stellar_module.energy_usage", "§eWireless EU/t: §f%s");
        provider.add("cosmiccore.multiblock.stellar_module.loading", "§7Loading...");
        provider.add("cosmiccore.multiblock.stellar_module.power_failure", "§c§lPOWER FAILURE - Insufficient Energy!");
        provider.add("cosmiccore.multiblock.stellar_module.power_config", "§7Config: §b%s §7@ §a%dx §7Parallel");
        provider.add("cosmiccore.multiblock.pattern.stellar_module_slot", "§7Module Slot (Air or Formed Module)");

        // JADE
        provider.add("config.jade.plugin_cosmiccore.pcb_parallel", "[CosmicCore] PCB Foundry Parallel");
        provider.add("config.jade.plugin_cosmiccore.stellar_module", "[CosmicCore] Stellar Module Info");
        provider.add("config.jade.plugin_cosmiccore.drone_station", "[CosmicCore] Drone Station Info");
        provider.add("config.jade.plugin_cosmiccore.drone_maintenance_interface",
                "[CosmicCore] Drone Maintenance Interface");

        // JADE Stellar Module Provider
        provider.add("cosmiccore.jade.stellar_module.not_connected", "Iris: Not Connected");
        provider.add("cosmiccore.jade.stellar_module.iris_not_ready", "Iris: Not Ready");
        provider.add("cosmiccore.jade.stellar_module.connected", "Iris: Connected");
        provider.add("cosmiccore.jade.stellar_module.stage", "Stage: %s");
        provider.add("cosmiccore.jade.stellar_module.speed_bonus", "Speed: %s");
        provider.add("cosmiccore.jade.stellar_module.no_wireless", "No Wireless Network");
        provider.add("cosmiccore.jade.stellar_module.energy_usage", "Usage: %s");

        // Stellar Iris GUI - Module Toggle
        provider.add("cosmiccore.gui.stellar.show_star", "Show Star View");
        provider.add("cosmiccore.gui.stellar.show_modules", "Show Module Control");

        // EMI Bookmark Keybinds
        provider.add("key.categories.cosmiccore.emi", "CosmicCore - EMI");
        provider.add("key.cosmiccore.emi.next_bookmark_group", "Next Bookmark Group");
        provider.add("key.cosmiccore.emi.prev_bookmark_group", "Previous Bookmark Group");
        provider.add("key.cosmiccore.emi.create_bookmark_group", "Create Bookmark Group");

        // =========================================================================
        // STELLAR IRIS UPGRADE TREE
        // =========================================================================

        // Branch names
        provider.add("cosmiccore.stellar.branch.ignition", "Ignition");
        provider.add("cosmiccore.stellar.branch.fusion", "Fusion");
        provider.add("cosmiccore.stellar.branch.collapse", "Collapse");
        provider.add("cosmiccore.stellar.branch.void", "Void");

        // UI strings
        provider.add("cosmiccore.stellar.convergence.title", "Stellar Convergence");
        provider.add("cosmiccore.stellar.upgrade.cost", "Cost: %d pts");
        provider.add("cosmiccore.stellar.upgrade.tier_required", "Requires Tier %d");
        provider.add("cosmiccore.stellar.upgrade.level", "Level %d / %d");
        provider.add("cosmiccore.stellar.upgrade.max_level", "MAX LEVEL");
        provider.add("cosmiccore.stellar.upgrade.owned", "OWNED");
        provider.add("cosmiccore.stellar.upgrade.available", "Click to unlock");
        provider.add("cosmiccore.stellar.upgrade.locked", "Locked");
        provider.add("cosmiccore.stellar.upgrade.tier_locked", "Tier too low");
        provider.add("cosmiccore.stellar.upgrade.not_enough_points", "Not enough points");
        provider.add("cosmiccore.stellar.upgrade.prereqs_needed", "Prerequisites needed");

        // === IGNITION BRANCH (Star Lifecycle) ===
        provider.add("cosmiccore.stellar.upgrade.thermal_stabilizer", "Thermal Stabilizer");
        provider.add("cosmiccore.stellar.upgrade.thermal_stabilizer.desc", "Stars decay 15% slower");

        provider.add("cosmiccore.stellar.upgrade.plasma_conduits", "Plasma Conduits");
        provider.add("cosmiccore.stellar.upgrade.plasma_conduits.desc", "-10% fuel consumption per tick");

        provider.add("cosmiccore.stellar.upgrade.fusion_catalyst", "Fusion Catalyst");
        provider.add("cosmiccore.stellar.upgrade.fusion_catalyst.desc", "Stars grow to next stage 25% faster");

        provider.add("cosmiccore.stellar.upgrade.magnetic_confinement", "Magnetic Confinement");
        provider.add("cosmiccore.stellar.upgrade.magnetic_confinement.desc",
                "Star stage cannot drop during active processing");

        provider.add("cosmiccore.stellar.upgrade.core_harmonics", "Core Harmonics");
        provider.add("cosmiccore.stellar.upgrade.core_harmonics.desc", "-20% additional fuel consumption");

        provider.add("cosmiccore.stellar.upgrade.proton_recycler", "Proton Recycler");
        provider.add("cosmiccore.stellar.upgrade.proton_recycler.desc", "8% chance to not consume fuel on tick");

        provider.add("cosmiccore.stellar.upgrade.stellar_regeneration", "Stellar Regeneration");
        provider.add("cosmiccore.stellar.upgrade.stellar_regeneration.desc",
                "3% chance per tick for star to gain stage");

        provider.add("cosmiccore.stellar.upgrade.eternal_ember", "Eternal Ember");
        provider.add("cosmiccore.stellar.upgrade.eternal_ember.desc", "Stars at STAR stage never naturally decay");

        provider.add("cosmiccore.stellar.upgrade.phoenix_protocol", "Phoenix Protocol");
        provider.add("cosmiccore.stellar.upgrade.phoenix_protocol.desc",
                "When star would die, 25% chance to return to STAR stage instead");

        provider.add("cosmiccore.stellar.upgrade.solar_dominion", "Solar Dominion");
        provider.add("cosmiccore.stellar.upgrade.solar_dominion.desc", "All star lifecycle bonuses increased by 50%");

        provider.add("cosmiccore.stellar.upgrade.perpetual_ignition", "Perpetual Ignition");
        provider.add("cosmiccore.stellar.upgrade.perpetual_ignition.desc",
                "Stars are immune to decay. Fuel costs halved.");

        provider.add("cosmiccore.stellar.upgrade.supernova_core", "Supernova Core");
        provider.add("cosmiccore.stellar.upgrade.supernova_core.desc",
                "Stars at SUPERSTAR+ generate bonus prestige shards passively");

        provider.add("cosmiccore.stellar.upgrade.plasma_hurricane", "Plasma Hurricane");
        provider.add("cosmiccore.stellar.upgrade.plasma_hurricane.desc",
                "Star growth speed increased by 40%");

        provider.add("cosmiccore.stellar.upgrade.stellar_nursery", "Stellar Nursery");
        provider.add("cosmiccore.stellar.upgrade.stellar_nursery.desc",
                "New stars begin at GROWING stage instead of EMPTY");

        provider.add("cosmiccore.stellar.upgrade.corona_expansion", "Corona Expansion");
        provider.add("cosmiccore.stellar.upgrade.corona_expansion.desc",
                "Module connection range doubled");

        provider.add("cosmiccore.stellar.upgrade.helios_forge", "Helios Forge");
        provider.add("cosmiccore.stellar.upgrade.helios_forge.desc",
                "Unlock Helios-tier recipes requiring extreme stellar conditions");

        provider.add("cosmiccore.stellar.upgrade.fusion_overdrive", "Fusion Overdrive");
        provider.add("cosmiccore.stellar.upgrade.fusion_overdrive.desc",
                "+50% processing speed, +25% fuel consumption");

        provider.add("cosmiccore.stellar.upgrade.dyson_lattice", "Dyson Lattice");
        provider.add("cosmiccore.stellar.upgrade.dyson_lattice.desc",
                "Passive energy generation scales with star stage (up to 32k EU/t)");

        provider.add("cosmiccore.stellar.upgrade.solar_genesis", "Solar Genesis");
        provider.add("cosmiccore.stellar.upgrade.solar_genesis.desc",
                "Can spawn secondary micro-stars that provide bonus parallels");

        provider.add("cosmiccore.stellar.upgrade.primordial_flame", "Primordial Flame");
        provider.add("cosmiccore.stellar.upgrade.primordial_flame.desc",
                "The first fire. All Ignition bonuses doubled. Stars cannot die.");

        // === FUSION BRANCH (Processing Power) ===
        provider.add("cosmiccore.stellar.upgrade.graviton_lens", "Graviton Lens");
        provider.add("cosmiccore.stellar.upgrade.graviton_lens.desc", "+1 parallel per star stage");

        provider.add("cosmiccore.stellar.upgrade.superconducting_grid", "Superconducting Grid");
        provider.add("cosmiccore.stellar.upgrade.superconducting_grid.desc", "-10% energy cost for all recipes");

        provider.add("cosmiccore.stellar.upgrade.temporal_acceleration", "Temporal Acceleration");
        provider.add("cosmiccore.stellar.upgrade.temporal_acceleration.desc", "+15% processing speed");

        provider.add("cosmiccore.stellar.upgrade.parallel_manifold", "Parallel Manifold");
        provider.add("cosmiccore.stellar.upgrade.parallel_manifold.desc", "+2 base parallel limit");

        provider.add("cosmiccore.stellar.upgrade.stellar_compression", "Stellar Compression");
        provider.add("cosmiccore.stellar.upgrade.stellar_compression.desc", "+25% speed at SUPERSTAR or higher");

        provider.add("cosmiccore.stellar.upgrade.mass_efficiency", "Mass Efficiency");
        provider.add("cosmiccore.stellar.upgrade.mass_efficiency.desc", "-15% additional energy cost");

        provider.add("cosmiccore.stellar.upgrade.relativistic_processing", "Relativistic Processing");
        provider.add("cosmiccore.stellar.upgrade.relativistic_processing.desc",
                "Recipes under 20 ticks complete instantly");

        provider.add("cosmiccore.stellar.upgrade.quantum_tunneling", "Quantum Tunneling");
        provider.add("cosmiccore.stellar.upgrade.quantum_tunneling.desc", "10% chance to complete recipe instantly");

        provider.add("cosmiccore.stellar.upgrade.hyperdense_core", "Hyperdense Core");
        provider.add("cosmiccore.stellar.upgrade.hyperdense_core.desc", "+50% parallel limit from all sources");

        provider.add("cosmiccore.stellar.upgrade.tachyon_weave", "Tachyon Weave");
        provider.add("cosmiccore.stellar.upgrade.tachyon_weave.desc", "+30% processing speed, stacks additively");

        provider.add("cosmiccore.stellar.upgrade.singularity_engine", "Singularity Engine");
        provider.add("cosmiccore.stellar.upgrade.singularity_engine.desc",
                "Double all speed/parallel bonuses. Energy costs -40%.");

        provider.add("cosmiccore.stellar.upgrade.neutron_cascade", "Neutron Cascade");
        provider.add("cosmiccore.stellar.upgrade.neutron_cascade.desc",
                "Recipe completions have 15% chance to trigger twice");

        provider.add("cosmiccore.stellar.upgrade.warp_field_matrix", "Warp Field Matrix");
        provider.add("cosmiccore.stellar.upgrade.warp_field_matrix.desc",
                "Items teleport directly to output buses (no transport needed)");

        provider.add("cosmiccore.stellar.upgrade.particle_storm", "Particle Storm");
        provider.add("cosmiccore.stellar.upgrade.particle_storm.desc",
                "+4 base parallels, unlocks particle storm recipes");

        provider.add("cosmiccore.stellar.upgrade.subspace_harmonics", "Subspace Harmonics");
        provider.add("cosmiccore.stellar.upgrade.subspace_harmonics.desc",
                "Energy costs scale inversely with recipe duration");

        provider.add("cosmiccore.stellar.upgrade.antimatter_injection", "Antimatter Injection");
        provider.add("cosmiccore.stellar.upgrade.antimatter_injection.desc",
                "Unlock antimatter fuel. +100% speed when fueled with antimatter");

        provider.add("cosmiccore.stellar.upgrade.zero_point_tap", "Zero Point Tap");
        provider.add("cosmiccore.stellar.upgrade.zero_point_tap.desc",
                "5% of energy cost is refunded after recipe completion");

        provider.add("cosmiccore.stellar.upgrade.quark_gluon_plasma", "Quark-Gluon Plasma");
        provider.add("cosmiccore.stellar.upgrade.quark_gluon_plasma.desc",
                "Unlock QGP recipes. +8 parallels for QGP recipes only");

        provider.add("cosmiccore.stellar.upgrade.planck_resonance", "Planck Resonance");
        provider.add("cosmiccore.stellar.upgrade.planck_resonance.desc",
                "Minimum recipe time reduced to 1 tick");

        provider.add("cosmiccore.stellar.upgrade.omega_compression", "Omega Compression");
        provider.add("cosmiccore.stellar.upgrade.omega_compression.desc",
                "Ultimate processing. All Fusion bonuses tripled. Unlock Omega recipes.");

        // === COLLAPSE BRANCH (Prestige & Points) ===
        provider.add("cosmiccore.stellar.upgrade.shard_collector", "Shard Collector");
        provider.add("cosmiccore.stellar.upgrade.shard_collector.desc", "+20% prestige points earned");

        provider.add("cosmiccore.stellar.upgrade.resonant_sacrifice", "Resonant Sacrifice");
        provider.add("cosmiccore.stellar.upgrade.resonant_sacrifice.desc",
                "Prestige at SUPERSTAR gives BLACK_HOLE rewards");

        provider.add("cosmiccore.stellar.upgrade.early_harvest", "Early Harvest");
        provider.add("cosmiccore.stellar.upgrade.early_harvest.desc", "Can prestige at STAR stage (50% points)");

        provider.add("cosmiccore.stellar.upgrade.efficient_consumption", "Efficient Consumption");
        provider.add("cosmiccore.stellar.upgrade.efficient_consumption.desc",
                "15% chance prestige doesn't consume prestige item");

        provider.add("cosmiccore.stellar.upgrade.point_amplifier", "Point Amplifier");
        provider.add("cosmiccore.stellar.upgrade.point_amplifier.desc",
                "+30% prestige points (stacks with Shard Collector)");

        provider.add("cosmiccore.stellar.upgrade.dual_sacrifice", "Dual Sacrifice");
        provider.add("cosmiccore.stellar.upgrade.dual_sacrifice.desc", "Can consume 2 prestige items for 2.5x points");

        provider.add("cosmiccore.stellar.upgrade.prestige_momentum", "Prestige Momentum");
        provider.add("cosmiccore.stellar.upgrade.prestige_momentum.desc",
                "Each consecutive prestige gives +10% points (max 50%)");

        provider.add("cosmiccore.stellar.upgrade.echo_of_collapse", "Echo of Collapse");
        provider.add("cosmiccore.stellar.upgrade.echo_of_collapse.desc",
                "Prestige grants a temporary 20% speed boost for 5 minutes");

        provider.add("cosmiccore.stellar.upgrade.entropy_harvest", "Entropy Harvest");
        provider.add("cosmiccore.stellar.upgrade.entropy_harvest.desc",
                "Gain bonus points based on star lifetime before prestige");

        provider.add("cosmiccore.stellar.upgrade.infinite_recursion", "Infinite Recursion");
        provider.add("cosmiccore.stellar.upgrade.infinite_recursion.desc",
                "Prestige gives 2x points. 25% chance to keep prestige item.");

        provider.add("cosmiccore.stellar.upgrade.cascading_collapse", "Cascading Collapse");
        provider.add("cosmiccore.stellar.upgrade.cascading_collapse.desc",
                "Prestige triggers grant bonus points to connected modules");

        provider.add("cosmiccore.stellar.upgrade.temporal_echo", "Temporal Echo");
        provider.add("cosmiccore.stellar.upgrade.temporal_echo.desc",
                "Store up to 3 prestige charges, release all at once for 3x value");

        provider.add("cosmiccore.stellar.upgrade.mass_conversion", "Mass Conversion");
        provider.add("cosmiccore.stellar.upgrade.mass_conversion.desc",
                "Convert excess materials into prestige points (1000:1 ratio)");

        provider.add("cosmiccore.stellar.upgrade.stellar_debt", "Stellar Debt");
        provider.add("cosmiccore.stellar.upgrade.stellar_debt.desc",
                "Borrow prestige points from future prestiges (150% payback)");

        provider.add("cosmiccore.stellar.upgrade.entropy_engine", "Entropy Engine");
        provider.add("cosmiccore.stellar.upgrade.entropy_engine.desc",
                "Failed recipes contribute to prestige point pool");

        provider.add("cosmiccore.stellar.upgrade.sacrifice_amplifier", "Sacrifice Amplifier");
        provider.add("cosmiccore.stellar.upgrade.sacrifice_amplifier.desc",
                "Prestige items give 4x points but are always consumed");

        provider.add("cosmiccore.stellar.upgrade.cosmic_tithe", "Cosmic Tithe");
        provider.add("cosmiccore.stellar.upgrade.cosmic_tithe.desc",
                "Passively generate prestige points (1/min per tier)");

        provider.add("cosmiccore.stellar.upgrade.annihilation_yield", "Annihilation Yield");
        provider.add("cosmiccore.stellar.upgrade.annihilation_yield.desc",
                "BLACK_HOLE prestige gives 5x base points, destroys the star");

        provider.add("cosmiccore.stellar.upgrade.heat_death", "Heat Death");
        provider.add("cosmiccore.stellar.upgrade.heat_death.desc",
                "The end of all things. All Collapse bonuses tripled. Prestige is instant.");

        // === VOID BRANCH (Exotic Abilities) ===
        provider.add("cosmiccore.stellar.upgrade.hawking_radiator", "Hawking Radiator");
        provider.add("cosmiccore.stellar.upgrade.hawking_radiator.desc", "BLACK_HOLE generates 2048 EU/t passively");

        provider.add("cosmiccore.stellar.upgrade.chromatic_tuning", "Chromatic Tuning");
        provider.add("cosmiccore.stellar.upgrade.chromatic_tuning.desc",
                "Custom star color provides +5% to matching element recipes");

        provider.add("cosmiccore.stellar.upgrade.exotic_matter_tap", "Exotic Matter Tap");
        provider.add("cosmiccore.stellar.upgrade.exotic_matter_tap.desc", "Unlock exotic matter processing recipes");

        provider.add("cosmiccore.stellar.upgrade.void_whispers", "Void Whispers");
        provider.add("cosmiccore.stellar.upgrade.void_whispers.desc", "BLACK_HOLE stage provides +50% speed bonus");

        provider.add("cosmiccore.stellar.upgrade.event_horizon_lock", "Event Horizon Lock");
        provider.add("cosmiccore.stellar.upgrade.event_horizon_lock.desc", "BLACK_HOLE never decays to DEATH");

        provider.add("cosmiccore.stellar.upgrade.singularity_siphon", "Singularity Siphon");
        provider.add("cosmiccore.stellar.upgrade.singularity_siphon.desc",
                "BLACK_HOLE stage reduces recipe energy cost by 20%");

        provider.add("cosmiccore.stellar.upgrade.gravitational_mastery", "Gravitational Mastery");
        provider.add("cosmiccore.stellar.upgrade.gravitational_mastery.desc",
                "BLACK_HOLE stage grants +3 parallels");

        provider.add("cosmiccore.stellar.upgrade.void_harvester", "Void Harvester");
        provider.add("cosmiccore.stellar.upgrade.void_harvester.desc", "BLACK_HOLE prestige gives 3x base points");

        provider.add("cosmiccore.stellar.upgrade.eldritch_insight", "Eldritch Insight");
        provider.add("cosmiccore.stellar.upgrade.eldritch_insight.desc",
                "Reveals hidden recipe bonuses based on star configuration");

        provider.add("cosmiccore.stellar.upgrade.abyss_walker", "Abyss Walker");
        provider.add("cosmiccore.stellar.upgrade.abyss_walker.desc",
                "Void energy passively accumulates, boosting exotic recipes");

        provider.add("cosmiccore.stellar.upgrade.eternal_void", "Eternal Void");
        provider.add("cosmiccore.stellar.upgrade.eternal_void.desc",
                "All BLACK_HOLE bonuses are doubled. Void effects persist 30s after leaving BLACK_HOLE.");

        provider.add("cosmiccore.stellar.upgrade.dark_matter_lens", "Dark Matter Lens");
        provider.add("cosmiccore.stellar.upgrade.dark_matter_lens.desc",
                "BLACK_HOLE passively produces Dark Matter (1/min)");

        provider.add("cosmiccore.stellar.upgrade.negative_mass", "Negative Mass");
        provider.add("cosmiccore.stellar.upgrade.negative_mass.desc",
                "Recipes in BLACK_HOLE have inverted energy costs (gain EU)");

        provider.add("cosmiccore.stellar.upgrade.vacuum_decay", "Vacuum Decay");
        provider.add("cosmiccore.stellar.upgrade.vacuum_decay.desc",
                "Chance to void input items for 10x output");

        provider.add("cosmiccore.stellar.upgrade.photon_sphere", "Photon Sphere");
        provider.add("cosmiccore.stellar.upgrade.photon_sphere.desc",
                "Light-based recipes get +200% speed in BLACK_HOLE");

        provider.add("cosmiccore.stellar.upgrade.schwarzschild_radius", "Schwarzschild Radius");
        provider.add("cosmiccore.stellar.upgrade.schwarzschild_radius.desc",
                "Increase BLACK_HOLE event horizon - more items can process simultaneously");

        provider.add("cosmiccore.stellar.upgrade.ergosphere_tap", "Ergosphere Tap");
        provider.add("cosmiccore.stellar.upgrade.ergosphere_tap.desc",
                "Extract rotational energy from BLACK_HOLE (8192 EU/t passive)");

        provider.add("cosmiccore.stellar.upgrade.penrose_process", "Penrose Process");
        provider.add("cosmiccore.stellar.upgrade.penrose_process.desc",
                "Throw matter into BLACK_HOLE to extract 130% of its energy value");

        provider.add("cosmiccore.stellar.upgrade.kerr_extraction", "Kerr Extraction");
        provider.add("cosmiccore.stellar.upgrade.kerr_extraction.desc",
                "Spinning BLACK_HOLE grants +6 parallels and exotic byproducts");

        provider.add("cosmiccore.stellar.upgrade.false_vacuum", "False Vacuum");
        provider.add("cosmiccore.stellar.upgrade.false_vacuum.desc",
                "Reality unravels. All Void bonuses tripled. Unlock False Vacuum recipes.");

        // === REPEATABLE UPGRADES ===
        provider.add("cosmiccore.stellar.upgrade.stellar_efficiency", "Stellar Efficiency");
        provider.add("cosmiccore.stellar.upgrade.stellar_efficiency.desc", "+2% speed per level (max 10 levels)");

        provider.add("cosmiccore.stellar.upgrade.parallel_threading", "Parallel Threading");
        provider.add("cosmiccore.stellar.upgrade.parallel_threading.desc", "+1 parallel per level (max 8 levels)");

        provider.add("cosmiccore.stellar.upgrade.energy_optimization", "Energy Optimization");
        provider.add("cosmiccore.stellar.upgrade.energy_optimization.desc",
                "-3% energy cost per level (max 10 levels)");

        provider.add("cosmiccore.stellar.upgrade.fuel_efficiency", "Fuel Efficiency");
        provider.add("cosmiccore.stellar.upgrade.fuel_efficiency.desc",
                "-2% fuel consumption per level (max 10 levels)");

        provider.add("cosmiccore.stellar.upgrade.prestige_amplifier", "Prestige Amplifier");
        provider.add("cosmiccore.stellar.upgrade.prestige_amplifier.desc",
                "+5% prestige points per level (max 10 levels)");

        provider.add("cosmiccore.stellar.upgrade.decay_resistance", "Decay Resistance");
        provider.add("cosmiccore.stellar.upgrade.decay_resistance.desc",
                "+3% decay resistance per level (max 10 levels)");

        provider.add("cosmiccore.stellar.upgrade.growth_catalyst", "Growth Catalyst");
        provider.add("cosmiccore.stellar.upgrade.growth_catalyst.desc", "+3% growth speed per level (max 10 levels)");

        provider.add("cosmiccore.stellar.upgrade.void_attunement", "Void Attunement");
        provider.add("cosmiccore.stellar.upgrade.void_attunement.desc",
                "+2% bonus from void effects per level (max 10 levels)");

        // Cosmic Boots
        provider.add("key.categories.cosmiccore.boots", "Cosmic Boots");
        provider.add("key.cosmiccore.boots.speed_increase", "Boots: Increase Speed");
        provider.add("key.cosmiccore.boots.speed_decrease", "Boots: Decrease Speed");
        provider.add("key.cosmiccore.boots.jump_increase", "Boots: Increase Jump");
        provider.add("key.cosmiccore.boots.jump_decrease", "Boots: Decrease Jump");
        provider.add("key.cosmiccore.boots.toggle_step", "Boots: Toggle Step Assist");
        provider.add("key.cosmiccore.boots.toggle_inertia", "Boots: Toggle Inertia Dampening");

        // Quake Movement (Reflection Bargain)
        provider.add("key.categories.cosmiccore.movement", "Quake Movement");
        provider.add("key.cosmiccore.movement.dash", "Dash");

        // Boot tooltips
        provider.add("cosmiccore.boots.speed_modifier", "Speed Modifier: %s");
        provider.add("cosmiccore.boots.jump_modifier", "Jump Modifier: %s");
        provider.add("cosmiccore.boots.step_assist", "Step Assist: %s");
        provider.add("cosmiccore.boots.inertia_cancel", "Inertia Dampening: %s");

        // Boot HUD
        provider.add("cosmiccore.boots.hud.speed", "Speed: %s %s");
        provider.add("cosmiccore.boots.hud.speed_simple", "Speed: %s");
        provider.add("cosmiccore.boots.hud.jump", "Jump: %s");

        // Boot action messages
        provider.add("cosmiccore.boots.message.speed", "Speed Modifier: %s");
        provider.add("cosmiccore.boots.message.jump", "Jump Modifier: %s");
        provider.add("cosmiccore.boots.message.step", "Step Assist: %s");
        provider.add("cosmiccore.boots.message.inertia", "Inertia Dampening: %s");

        // Max speed tooltip
        provider.add("cosmiccore.boots.max_speed", "Max Speed: %s");
    }
}
