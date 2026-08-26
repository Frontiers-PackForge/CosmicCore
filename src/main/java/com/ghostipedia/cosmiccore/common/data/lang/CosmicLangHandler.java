package com.ghostipedia.cosmiccore.common.data.lang;

import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldPlacement;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import net.minecraft.core.registries.BuiltInRegistries;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class CosmicLangHandler extends LangHandler {

    private static final String[] COSMIC_RECIPE_TYPE_IDS = {
            "sludge_digestor", "powderizer", "industrial_ore_sorter", "industrial_flotation_plant",
            "oneiric_sieve", "dissolution_vat", "phase_separator", "simple_desalter", "desalter",
            "steam_cracking_furnace", "fractional_condenser", "fluid_catalytic_cracking", "hydrotreating",
            "hydrocracking", "catalytic_reforming", "delayed_coking", "vacuum_distillation", "fuckassbeeball",
            "laminator",
            "chemical_dehydrator",
            "crystallizer", "eclipsed_dawnforge", "vorax", "mana_fluidizer", "pcb_fab", "titan_fusion", "lunar_hammer",
            "cryo_chamber", "soul_tester", "void_miner", "heavy_assembler", "plasmite_forge",
            "prisma_foundry", "atmo_siphon", "mana_digitizer", "component_assembly_line", "drygmy_grove",
            "leaching_plant", "hellfire_foundry", "suffering_chamber", "arcane_distillery", "arcane_folding",
            "polymerizer", "hemophagic_transfuser", "chromatic_flotation_plant", "spirit_crucible",
            "soul_foundry", "calx_reactor", "roaster", "mana_leaching_tub", "thermomagnitizer",
            "vacuum_bubbler", "large_roaster", "vile_fission", "void_salt_fission", "reconstructor",
            "spooling_machine", "orbital_forge", "orbital_forge_abs", "dawn_forge", "cinder_hearth",
            "arcane_crucible", "pyrothermic_refinery", "mana_etching", "bio_lab", "star_ladder_research",
            "stellar_iris", "ignition_complex", "chormatic_distillation_plant", "celestial_bore",
            "naquahine_reactor", "mini_naquahine_reactor", "industrial_chemvat", "biovat", "wasp", "bees",
            "core_drill", "regolith_sifter", "life_force_manipulator", "neutron_forge", "dream_basin",
            "mechanical_ritual", "link_test", "abyssal_culture_vat", "sculk_biochamber",
            "biomana_digestor", "manawomb_leeching_pond", "industrial_primitive_blast_furnace",
            "turbine_power_station", "combustion_power_station"
    };

    private static String toTitle(String snakeCase) {
        StringBuilder out = new StringBuilder();
        for (String part : snakeCase.split("_")) {
            if (part.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

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
        provider.add("button.cosmiccore.toggle_depleted.name", "Toggle Depleted");
        provider.add("cosmiccore.dowsing.found", "The rod stirs, sensing %d ore fields nearby:");
        provider.add("cosmiccore.dowsing.none", "The rod lies still");
        provider.add("cosmiccore.dowsing.team_share", "%s discovered %s new ore field(s):");
        provider.add("cosmiccore.dowsing.tooltip.radius", "Sensing range: %d blocks");
        provider.add("cosmiccore.dowsing.tooltip.use", "§7Use: Reveal nearby ore fields");

        for (Material bundle : OreFieldPlacement.bundles()) {
            provider.add("ore_vein.cosmiccore." + bundle.getName(), toTitle(bundle.getName()) + " Ore Field");
        }
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
        provider.add("item.cosmiccore.steam_conveyor.tooltip", "Moves items between adjacent inventories.");
        provider.add("item.cosmiccore.steam_pump.tooltip", "Moves fluids between adjacent tanks.");
        provider.add("item.cosmiccore.steam_robot_arm.tooltip",
                "Moves exact quantities of items between adjacent inventories.");
        provider.add("item.cosmiccore.steam_fluid_regulator.tooltip",
                "Moves exact quantities of fluids between adjacent tanks.");
        provider.add("cosmiccore.universal.tooltip.item_transfer_rate", "Transfer Rate: %s items/s");
        provider.add("cosmiccore.universal.tooltip.fluid_transfer_rate", "Transfer Rate: %s mB/t");
        provider.add("cosmiccore.gui.factory_gauge.promise_limit", "Promise Limit");
        provider.add("cosmiccore.gui.factory_gauge.promise_limit.none", "Promise Limit: Unlimited");
        provider.add("cosmiccore.gui.factory_gauge.place_fluid", "Place a fluid to monitor");
        provider.add("tagprefix.wire_gt_twelve", "12x %s Wire");
        provider.add("tagprefix.cable_gt_twelve", "12x %s Cable");

        replace(provider, "material.cosmiccore.blooming_sludge", "Blooming Sludge");
        replace(provider, "material.cosmiccore.bloom_rich_algae_solution", "Bloom Rich Algae Solution");
        replace(provider, "material.cosmiccore.phyto_grease", "Phyto-Grease");
        replace(provider, "material.cosmiccore.energetic_aluminium", "Energized Aluminium");
        replace(provider, "material.gtceu.multi_phase_oil", "Multi-Phase Oil");
        replace(provider, "block.gtceu.multi_phase_oil", "Multi-Phase Oil");
        replace(provider, "material.gtceu.sour_refinery_gas", "Sour Refinery Gas");
        replace(provider, "material.gtceu.sour_naphtha", "Sour Naphtha");
        replace(provider, "material.gtceu.sour_middle_fraction_distillates", "Sour Middle Fraction Distillates");
        replace(provider, "material.gtceu.sour_gas_oils", "Sour Gas Oils");
        replace(provider, "material.gtceu.light_naphtha", "Light Naphtha");
        replace(provider, "material.gtceu.middle_fraction_distillates", "Middle Fraction Distillates");
        replace(provider, "material.gtceu.gas_oils", "Gas Oils");
        replace(provider, "material.gtceu.raw_coking_gas", "Raw Coking Gas");
        replace(provider, "material.gtceu.mixed_xylenes", "Mixed Xylenes");
        replace(provider, "material.cosmiccore.volatile_multi_phase_oil", "Volatile Multi-Phase Oil");
        replace(provider, "material.cosmiccore.salt_laden_light_oil", "Salt-Laden Light Oil");
        replace(provider, "material.cosmiccore.salt_laden_oil", "Salt-Laden Oil");
        replace(provider, "material.cosmiccore.salt_laden_heavy_oil", "Salt-Laden Heavy Oil");
        replace(provider, "material.cosmiccore.wet_natural_gases", "Wet Natural Gases");
        replace(provider, "material.cosmiccore.oil_rich_wastewater", "Oil-Rich Wastewater");
        replace(provider, "material.cosmiccore.oily_sludge", "Oily Sludge");
        replace(provider, "material.cosmiccore.sour_brine", "Sour Brine");
        replace(provider, "material.cosmiccore.sour_process_water", "Sour Process Water");
        replace(provider, "material.cosmiccore.atmospheric_residue", "Atmospheric Residue");
        replace(provider, "material.cosmiccore.lean_amine_solution", "Lean Amine Solution");
        replace(provider, "material.cosmiccore.rich_amine_solution", "Rich Amine Solution");
        replace(provider, "material.cosmiccore.acidic_gases", "Acidic Gases");
        replace(provider, "material.cosmiccore.tail_gas", "Tail Gas");
        replace(provider, "material.cosmiccore.hot_light_effluents", "Hot Light Effluents");
        replace(provider, "material.cosmiccore.hot_naphtha_effluents", "Hot Naphtha Effluents");
        replace(provider, "material.cosmiccore.hot_gas_oil_effluents", "Hot Gas-Oil Effluents");
        replace(provider, "material.cosmiccore.heavy_pyrolysis_oils", "Heavy Pyrolysis Oils");
        replace(provider, "material.cosmiccore.olefin_gases", "Olefin Gases");
        replace(provider, "material.cosmiccore.cracked_naphtha", "Cracked Naphtha");
        replace(provider, "material.cosmiccore.aromatic_oil", "Aromatic Oil");
        replace(provider, "material.cosmiccore.condensed_refinery_gas", "Condensed Refinery Gas");
        replace(provider, "material.cosmiccore.heavy_naphtha", "Heavy Naphtha");
        replace(provider, "material.cosmiccore.cracked_gasoline", "Cracked Gasoline");
        replace(provider, "material.cosmiccore.light_cycle_oil", "Light Cycle Oil");
        replace(provider, "material.cosmiccore.slurry_oils", "Slurry Oils");
        replace(provider, "material.cosmiccore.high_octane_reformate", "High-Octane Reformate");
        replace(provider, "material.cosmiccore.vacuum_gas_oils", "Vacuum Gas Oils");
        replace(provider, "material.cosmiccore.waxy_distillates", "Waxy Distillates");
        replace(provider, "material.cosmiccore.vacuum_residuals", "Vacuum Residuals");
        replace(provider, "material.cosmiccore.bitumen", "Bitumen");
        replace(provider, "material.cosmiccore.petroleum_coke", "Petroleum Coke");
        replace(provider, "material.cosmiccore.calcined_petroleum_coke", "Calcined Petroleum Coke");
        replace(provider, "material.cosmiccore.syngas", "Syngas");
        replace(provider, "material.cosmiccore.hot_pyrolysis_vapors", "Hot Pyrolysis Vapors");
        replace(provider, "material.cosmiccore.crude_benzene", "Crude Benzene");
        replace(provider, "material.cosmiccore.wood_spirit", "Wood Spirit");
        replace(provider, "material.cosmiccore.acidic_wood_liquor", "Acidic Wood Liquor");
        replace(provider, "material.cosmiccore.light_tar_oils", "Light Tar Oils");
        replace(provider, "material.cosmiccore.phenolic_oils", "Phenolic Oils");
        replace(provider, "material.cosmiccore.tar_pitch", "Tar Pitch");
        replace(provider, "material.cosmiccore.hot_coking_vapors", "Hot Coking Vapors");
        replace(provider, "material.cosmiccore.ammonia_rich_liquor", "Ammonia-Rich Liquor");
        replace(provider, "material.cosmiccore.naphthalene_oils", "Naphthalene Oils");
        replace(provider, "block.cosmiccore.rust_resistant_structural_casing", "Rust-Resistant Structural Casing");
        replace(provider, "block.cosmiccore.refractory_structural_casing", "Refractory Structural Casing");
        replace(provider, "block.cosmiccore.refractory_containment_casing", "Refractory Containment Casing");
        replace(provider, "block.cosmiccore.vibrant_pipe_framework", "Vibrant Pipe Framework");
        replace(provider, "block.cosmiccore.heat_exchanger_casing", "Heat Exchanger Casing");
        replace(provider, "block.cosmiccore.condensation_mesh", "Condensation Mesh");
        replace(provider, "material.gtceu.light_oil", "Light Oil");
        replace(provider, "material.gtceu.heavy_oil", "Heavy Oil");
        replace(provider, "block.gtceu.light_oil", "Light Oil");
        replace(provider, "block.gtceu.heavy_oil", "Heavy Oil");
        provider.add("recipe_category.gtceu.arc_furnace_recycling", "Arc Scrapping");
        provider.add("recipe_category.gtceu.macerator_recycling", "Part Grinding");
        provider.add("recipe_category.gtceu.extractor_recycling", "Scrap Remelting");
        provider.add("recipe_category.gtceu.ore_crushing", "Ore Grinding");
        provider.add("recipe_category.gtceu.ore_forging", "Ore Crushing");
        provider.add("recipe_category.gtceu.ore_bathing", "Ore Treating");
        provider.add("recipe_category.gtceu.chem_dyes", "Chemical Dyeing");
        provider.add("recipe_category.gtceu.ingot_molding", "Metal Molding");

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

        multiLang(provider, "cosmiccore.multiblock.desalter.tooltip",
                "§7Power and Maintenance: §fRefractory Structural Casings",
                "§7Fluid Inputs and Outputs: §fLightweight Stainless Steel Casings");
        multiLang(provider, "cosmiccore.multiblock.steam_cracking_furnace.tooltip",
                "§7Power and Maintenance: §fRefractory Structural Casings",
                "§7Item and Fluid Inputs: §fSteel-Plated Bronze Casings",
                "§7Fluid Output: §fRefractory Containment Casings");
        multiLang(provider, "cosmiccore.multiblock.fractional_condenser.tooltip",
                "§7Power and Maintenance: §fRefractory Structural Casings",
                "§7Fluid Inputs: §fLarge Tower of Lightweight Stainless Steel Casings",
                "§7Fluid and Item Outputs: §fSmall Toweer of Lightweight Stainless Steel Casings");
        multiLang(provider, "cosmiccore.multiblock.phase_separator.tooltip",
                "§7Fluid Input: §fBottom most layer of Lightweight Stainless Casing",
                "§7Power and Maintenance: §fRefractory Structural Casings",
                "§7Fluid Outputs: §fONLY Light Stainless Casing that are Surrounding by Refractory Casings");
        provider.add("gtceu.multiblock.distillation_tower.description",
                "The Distillation Tower uses multiple layers to refine output products. Each middle section and the top cap accepts at most one Fluid Output Hatch. Fluid input, power, maintenance, and the Item Output Bus belong on the base, while the top cap requires one Muffler Hatch.");
        multiLang(provider, "cosmiccore.multiblock.conversion_complex.tooltip",
                "§bA modular refinery with various extensions.",
                "§bChoose a blueprint while unformed; the completed structure detects it automatically");
        provider.add("cosmiccore.multiblock.conversion_complex.tooltip.shift_header",
                "§6Mode requirements: §fStandard I/O Hatches go on the structures core.");
        provider.add("cosmiccore.multiblock.conversion_complex.tooltip.core",
                "§8Core: §7Construction chassis only. Does nothing on its own.");
        provider.add("cosmiccore.multiblock.conversion_complex.tooltip.fcc",
                "§5FCC: §fConsume Catalysts to produce more complicated materials.");
        provider.add("cosmiccore.multiblock.conversion_complex.tooltip.hydrotreating",
                "§9Hydrotreating: §7Consumes Hydrogen and Catalysts to clean impurities from refinery products.");
        provider.add("cosmiccore.multiblock.conversion_complex.tooltip.hydrocracking",
                "§3Hydrocracking: §fUses highly pressurized hydrogen and catalysts to split apart heavily refinery products.");
        provider.add("cosmiccore.multiblock.conversion_complex.tooltip.reforming",
                "§6Catalytic Reforming: §7eRearranges heavy products into reformates and aromatic compounds and excess hydrogen");
        provider.add("cosmiccore.multiblock.conversion_complex.tooltip.coking",
                "§cDelayed Coking: §fCracks nearly solid refinery products into pure coke and other heavy oils.");
        provider.add("cosmiccore.fluid_catalytic_cracking", "Fluid Catalytic Cracking");
        provider.add("cosmiccore.hydrotreating", "Hydrotreating");
        provider.add("cosmiccore.hydrocracking", "Hydrocracking");
        provider.add("cosmiccore.catalytic_reforming", "Catalytic Reforming");
        provider.add("cosmiccore.delayed_coking", "Delayed Coking");

        multiLang(provider, "cosmiccore.multiblock.star_ladder.tooltip",
                "§cThe peaks of creation reach out into the stars",
                "§c§lDANGER: DATA LOSS PRESENT",
                "§c§lDANGER: RECOVERY IS POSSIBLE",
                "§aPinacle Multiblock : The Final Goal of ACT1 (Steam to IV)");

        // Star Ladder Uplink Fight
        provider.add("cosmiccore.star_ladder.title", "STAR LADDER");
        provider.add("cosmiccore.star_ladder.initiate", "INITIATE UPLINK");
        provider.add("cosmiccore.star_ladder.interrupted", "UPLINK INTERRUPTED");
        provider.add("cosmiccore.star_ladder.resisting", "Something is resisting.");
        provider.add("cosmiccore.star_ladder.demands_soul", "The Ladder demands Refined Soul.");
        provider.add("cosmiccore.star_ladder.drain_rate", "Drain rate: %d/s");
        provider.add("cosmiccore.star_ladder.confirm", "CONFIRM");
        provider.add("cosmiccore.star_ladder.abort", "ABORT");
        provider.add("cosmiccore.star_ladder.established", "UPLINK ESTABLISHED");
        provider.add("cosmiccore.star_ladder.hub_name", "Research Hub");
        provider.add("cosmiccore.star_ladder.hub_tier", "Hub Tier: T%d");
        provider.add("cosmiccore.star_ladder.uplink_progress", "UPLINK PROGRESS");
        provider.add("cosmiccore.star_ladder.soul_drain", "SOUL DRAIN: %d/s");
        provider.add("cosmiccore.star_ladder.requisition", "REQUISITION:");

        // Star Ladder — Interrupted sequence
        provider.add("cosmiccore.star_ladder.whisper.interrupted.silence", "Silence.");
        provider.add("cosmiccore.star_ladder.whisper.interrupted.pressure", "Pressure.");
        provider.add("cosmiccore.star_ladder.whisper.interrupted.no", "Hm?");

        // Star Ladder — Phase transitions
        provider.add("cosmiccore.star_ladder.whisper.transition.phase_2", "Something shifts and groans.");
        provider.add("cosmiccore.star_ladder.whisper.transition.phase_3",
                "The barrier is cracking, the world is crying.");
        provider.add("cosmiccore.star_ladder.whisper.transition.complete",
                "A loud scream followed by complete silence, you've made it.");

        // Star Ladder — Phase 1 Ambient
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.conduits_heating", "The conduits are heating up.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.groaning_walls", "Something groans in the walls.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.air_tastes_iron", "The air tastes like iron.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.floor_vibrating", "The floor is vibrating.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.low_hum", "A low hum, rising.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.sparks_corner",
                "Sparks crackle at the corners of the terminal.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.lights_flicker", "The lights flicker and fail.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.smell_of_ozone",
                "Smell of petrichor, the taste of sulfur.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.dust_falling",
                "Dust falling from the ceiling, cracks in the walls.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.pipes_rattling",
                "Pipes rattling somewhere deep, steam hisses.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.static_on_skin",
                "Static crawling across your skin, the air electrified.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.shadows_wrong",
                "The shadows are wrong, the stars sway.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.metal_ticking",
                "Metal ticking as it expands, twisting.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.pressure_dropping",
                "Pressure dropping, the rushing sound of air.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p1.something_woke_up", "Something woke up, and it's mad.");

        // Star Ladder — Phase 1 Observer
        provider.add("cosmiccore.star_ladder.whisper.observer.p1.stop", "Curious.");
        provider.add("cosmiccore.star_ladder.whisper.observer.p1.not_here", "This isn't your calling.");
        provider.add("cosmiccore.star_ladder.whisper.observer.p1.leave", "Begone.");
        provider.add("cosmiccore.star_ladder.whisper.observer.p1.no", "There is nothing for you here.");

        // Star Ladder — Phase 1 Reflection
        provider.add("cosmiccore.star_ladder.whisper.reflection.p1.keep_feeding", "I need to keep fueling it.");
        provider.add("cosmiccore.star_ladder.whisper.reflection.p1.hold", "Hold steady, my creation.");
        provider.add("cosmiccore.star_ladder.whisper.reflection.p1.its_working",
                "It works, I need to keep on pushing.");

        // Star Ladder — Phase 2 Ambient
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.structure_resonating",
                "The structure is resonating, a song is forming.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.heat_distortion",
                "Heat distortion in the air, the sky burns orange.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.something_cracks",
                "Something cracks, the earth shifts below.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.machine_screams", "The machine screams, it's in pain.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.ears_ringing",
                "Your ears are ringing, you swear you hear words.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.metal_expanding",
                "Metal expanding, popping, hissing, cracking.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.temperature_climbing",
                "The temperature is climbing, your body is soaked in sweat.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.bolts_shearing",
                "Bolts shearing off the frame, the structure feels unstable.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.gravity_hiccup", "Gravity hiccups, just for a moment.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.light_bends",
                "Light bends where it shouldn't, your eyes are playing tricks.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.blood_in_mouth", "Taste of blood in your mouth grows.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.walls_humming",
                "The walls are humming a note you can't name, discordantly.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.floor_buckling",
                "The floor is buckling, the world is revolting.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.smell_of_burning",
                "Smell of burning that isn't there, the taste of flesh and souls unknown.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.time_stutters", "Time stutters and jumps.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p2.vision_doubles",
                "Your vision doubles, you see a woman cloaked in silver.");

        // Star Ladder — Phase 2 Observer
        provider.add("cosmiccore.star_ladder.whisper.observer.p2.dont_understand",
                "You don't understand what you're doing, this is beyond you.");
        provider.add("cosmiccore.star_ladder.whisper.observer.p2.not_yours",
                "This is not yours to take, you were mine to create");
        provider.add("cosmiccore.star_ladder.whisper.observer.p2.i_was_patient", "I was patient, you're testing that.");
        provider.add("cosmiccore.star_ladder.whisper.observer.p2.you_were_warned",
                "You're digging into things that should be left in the past");
        provider.add("cosmiccore.star_ladder.whisper.observer.p2.still_time",
                "There is still time to stop, I'll neglect it this once.");
        provider.add("cosmiccore.star_ladder.whisper.observer.p2.enough", "Enough.");

        // Star Ladder — Phase 2 Reflection
        provider.add("cosmiccore.star_ladder.whisper.reflection.p2.channel_widening",
                "The data stream is surging- I've made progress");
        provider.add("cosmiccore.star_ladder.whisper.reflection.p2.dont_stop", "Don't stop, this is it!");
        provider.add("cosmiccore.star_ladder.whisper.reflection.p2.halfway", "Still standing, halfway there.");

        // Star Ladder — Phase 3 Ambient
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.everything_shaking", "Everything shakes in unison.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.veil_fraying", "The veil is fraying, the sky realigns");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.light_bending_wrong",
                "Light bends and reveals countless faces");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.hands_shaking",
                "Your hands are shaking, traces of blood not of yours cover them");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.walls_breathing",
                "The walls are breathing in tune with you.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.static_all_frequencies",
                "Your mind feels the static noise of the world");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.reality_thins", "Reality thickens and thins at random");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.colors_wrong",
                "You're perceiving new colors no human could");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.sound_from_nowhere", "A sound from inside your mind");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.edges_dissolving",
                "The edge of consciousness dissolves");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.gravity_uncertain", "Gravity feels like second nature");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.air_tastes_of_stars", "The air tastes of star dust");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.heartbeat_in_walls",
                "You can hear your heartbeat in the walls");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.ground_not_solid", "The ground shatters under you.");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.sky_too_close", "The heavens feel infinitely close");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.something_looking_back",
                "Something beyond you is glaring intensely");
        provider.add("cosmiccore.star_ladder.whisper.ambient.p3.tinnitus_screaming", "The sound is deafening");

        // Star Ladder — Phase 3 Observer
        provider.add("cosmiccore.star_ladder.whisper.observer.p3.fine", "Fine, make yourself into a monster");
        provider.add("cosmiccore.star_ladder.whisper.observer.p3.remember_this", "Remember this, you started this");
        provider.add("cosmiccore.star_ladder.whisper.observer.p3.you_chose_this",
                "You chose this and there's no going back.");
        provider.add("cosmiccore.star_ladder.whisper.observer.p3.will_not_forget",
                "I will not forget, I won't let you forget.");
        provider.add("cosmiccore.star_ladder.whisper.observer.p3.see_what_happens", "Let's see what happens, find me.");
        provider.add("cosmiccore.star_ladder.whisper.observer.p3.door_opens_both_ways",
                "The door opens both ways, to me, and to you.");
        provider.add("cosmiccore.star_ladder.whisper.observer.p3.congratulations",
                "Congratulations, you've done something beyond me.");

        // Star Ladder — Phase 3 Reflection
        provider.add("cosmiccore.star_ladder.whisper.reflection.p3.almost_through", "Almost through, closing in.");
        provider.add("cosmiccore.star_ladder.whisper.reflection.p3.one_more_push", "One more push, I can feel it");
        provider.add("cosmiccore.star_ladder.whisper.reflection.p3.can_feel_it", "I can feel everything around myself");

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
                "§aRuns Recipes in Batches of §f: §b8x§r",
                "§aBlock Recipes are More Efficient! §f: §b8x§r");

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
                "§7Local power storage for §aEarly power grids§7.",
                "§7Stacks §f1-5§7 battery layers with §f9§7 capacitor batteries per layer.§r",
                "§7Does not accept laser hatches, but accepts up to §a64A §7Energy/Dynamo hatches.§r");
        provider.add("cosmiccore.multiblock.power_capacitor.local_buffer", "Local Buffer");
        provider.add("cosmiccore.multiblock.pattern.power_capacitor_batteries",
                "Use Empty Tier 0 or LV-HV Capacitor Batteries");
        provider.add("cosmiccore.block.power_capacitor.tooltip_empty",
                "Fills an unused battery position in a Power Capacitor");

        multiLang(provider, "cosmiccore.machine.me_computation_array.tooltip",
                "§aStorage Size§r : §b%s§r component bays.",
                "Accepts §7%s-%s§r components. Each bay uses its installed component tier's listed EU and output values.",
                "Requires one or two MV or higher Energy Input Hatches and one ME Computation Uplink",
                "Power relays are prioritized before computation cores when EU is low.",
                "Fill unused component bays with ME Computation Bay Casings.");
        provider.add("cosmiccore.block.me_computation_bay_casing.tooltip.0",
                "Fills unused spots inside of Computation Arrays");
        provider.add("cosmiccore.block.me_computation_bay_casing.tooltip.1",
                "Allows the structure to form at the cost of providing no functionality.");
        provider.add("cosmiccore.machine.me_computation_core.tooltip.0",
                "Consumes up to §e%s§r §bEU/t§r to provide up to §e%s§r §aCWU/t§r through a formed Computation Array.");
        provider.add("cosmiccore.machine.me_computation_core.tooltip.1",
                "Standby consumes §e%s§r §bEU/t§r and is credited toward funded CWU demand.");
        provider.add("cosmiccore.machine.me_power_relay.tooltip.0",
                "Will inject §e%s§r §bEU/t§r as ME power through a formed Computation Array.");
        provider.add("cosmiccore.machine.me_computation_uplink.tooltip.0",
                "Acts as an output for Computation Arrays");
        provider.add("cosmiccore.machine.me_computation_uplink.tooltip.1",
                "Publishes all funded §aCWU/t§r from its formed Computation Array");
        provider.add("cosmiccore.machine.me_computation_uplink.tooltip.2",
                "Passes all §bEU/t§r from Power relays into the AE2 Network");
        provider.add("cosmiccore.machine.me_computation_uplink.tooltip.3",
                "Source reservation: §e0§r §aCWU/t§r; it cannot consume its own output.");
        provider.add("cosmiccore.machine.me_computation_uplink.tooltip.4",
                "Topology: +§e%s§r §aCWU/t§r per §e%s§r physical grid links, rounded up.");

        provider.add("emi.category.cosmiccore.asteroid_mining", "Asteroid Mining Operations");

        // recipe stuff
        provider.add("cosmiccore.recipe.soul_in", "Soul Input: %s");
        provider.add("cosmiccore.recipe.soul_out", "Soul Output: %s");
        provider.add("recipe.cosmiccore.raw_soul_in", "Raw Soul Input: %s");
        provider.add("recipe.cosmiccore.raw_soul_out", "Raw Soul Output: %s");
        provider.add("recipe.cosmiccore.refined_soul_in", "Refined Soul Input: %s");
        provider.add("recipe.cosmiccore.refined_soul_out", "Refined Soul Output: %s");
        provider.add("recipe.cosmiccore.proud_soul_in", "Proud Soul Input: %s");
        provider.add("recipe.cosmiccore.proud_soul_out", "Proud Soul Output: %s");
        provider.add("recipe.cosmiccore.greedy_soul_in", "Greedy Soul Input: %s");
        provider.add("recipe.cosmiccore.greedy_soul_out", "Greedy Soul Output: %s");
        provider.add("recipe.cosmiccore.lustful_soul_in", "Lustful Soul Input: %s");
        provider.add("recipe.cosmiccore.lustful_soul_out", "Lustful Soul Output: %s");
        provider.add("recipe.cosmiccore.envious_soul_in", "Envious Soul Input: %s");
        provider.add("recipe.cosmiccore.envious_soul_out", "Envious Soul Output: %s");
        provider.add("recipe.cosmiccore.gluttonous_soul_in", "Gluttonous Soul Input: %s");
        provider.add("recipe.cosmiccore.gluttonous_soul_out", "Gluttonous Soul Output: %s");
        provider.add("recipe.cosmiccore.wrathful_soul_in", "Wrathful Soul Input: %s");
        provider.add("recipe.cosmiccore.wrathful_soul_out", "Wrathful Soul Output: %s");
        provider.add("recipe.cosmiccore.slothful_soul_in", "Slothful Soul Input: %s");
        provider.add("recipe.cosmiccore.slothful_soul_out", "Slothful Soul Output: %s");
        provider.add("recipe.cosmiccore.temporal_soul_in", "Temporal Soul Input: %s");
        provider.add("recipe.cosmiccore.temporal_soul_out", "Temporal Soul Output: %s");
        provider.add("cosmiccore.recipe.sterile_in", "Sterilizer: %s %s");
        provider.add("cosmiccore.recipe.sterile_out", "ERROR?");
        provider.add("cosmiccore.recipe.ember_in", "Ember Input: %s");
        provider.add("cosmiccore.recipe.ember_out", "Ember Output: %s");
        provider.add("cosmiccore.recipe_maker.access_denied", "Donk requires operator permission level 4.");
        provider.add("cosmiccore.recipe_maker.id.amended",
                "Recipe copied as %s. The default ID was occupied, so route variant %s was appended.");
        provider.add("cosmiccore.recipe_maker.id.explicit_occupied",
                "Recipe copied with existing ID %s. Loading it will replace or redefine that recipe.");
        provider.add("cosmiccore.recipe_maker.id.amend_failed",
                "Automatic recipe ID amendment failed for %s. Enter a route variant manually.");
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
        provider.add("gui.cosmiccore.soul_hatch.owner", "Network Owner: %s");
        provider.add("gui.cosmiccore.soul_hatch.lp", "LP Stored: %s");
        provider.add("gui.cosmiccore.soul.network_contents", "Network Contents:");
        provider.add("gui.cosmiccore.soul.empty_network", "Network is empty.");
        provider.add("gui.cosmiccore.soul.reset", "Soul network has been reset.");
        provider.add("gui.cosmiccore.soul.raw.name", "Raw");
        provider.add("gui.cosmiccore.soul.refined.name", "Refined");
        provider.add("gui.cosmiccore.soul.proud.name", "Proud");
        provider.add("gui.cosmiccore.soul.greedy.name", "Greedy");
        provider.add("gui.cosmiccore.soul.lustful.name", "Lustful");
        provider.add("gui.cosmiccore.soul.envious.name", "Envious");
        provider.add("gui.cosmiccore.soul.gluttonous.name", "Gluttonous");
        provider.add("gui.cosmiccore.soul.wrathful.name", "Wrathful");
        provider.add("gui.cosmiccore.soul.slothful.name", "Slothful");
        provider.add("gui.cosmiccore.soul.temporal.name", "Temporal");
        provider.add("gui.cosmiccore.ember_hatch.ember", "Ember Stored: %s");
        provider.add("gui.cosmiccore.thermia_hatch.hatch_limit", "§cTemp. Limit:");
        provider.add("gui.cosmiccore.thermia_hatch.stored_temp", "§6Current Temp:");
        provider.add("gui.cosmiccore.sterilization_hatch", "Sterilization Hatch");
        provider.add("cosmiccore.multiblock.current_field_strength", "§fField Strength: %s");
        provider.add("cosmiccore.recipe.minField", "§fMin. Field Strength: %sT");
        provider.add("cosmiccore.recipe.fieldDecay", "§fField Decay: %sT/t");
        provider.add("cosmiccore.recipe.fieldSlam", "§fField Consumed: %sT");
        provider.add("cosmiccore.recipe.condition.titan.tooltip", "Requires Titan Reactor Tier: %s");
        provider.add("cosmiccore.multiblock.structure_tier", "Structure Tier: T%s");
        provider.add("cosmiccore.multiblock.tier_boost.applied_by", "§fTier Boost Applied By: §e%s");
        provider.add("cosmiccore.multiblock.tier_boost.source.single", "%sA §r%s §fEnergy Hatch");
        provider.add("cosmiccore.multiblock.tier_boost.source.multiple", "%s× §r%s §fEnergy Hatches");
        provider.add("cosmiccore.multiblock.tier_boost.hover",
                "§fThis multiblock can boost its recipe tier by one level when supplied with at least 4 A of power. §7Any additional power is used to accelerate recipes rather than increase its recipe tier further.");
        provider.add("cosmiccore.multiblock.maximum_throughput", "Maximum Throughput:");
        provider.add("cosmiccore.multiblock.maximum_throughput.value", "%s EU/t");
        provider.add("cosmiccore.multiblock.construction_blueprint", "Construction Blueprint: %s");
        provider.add("cosmiccore.multiblock.detected_configuration", "Detected Configuration: %s");
        provider.add("cosmiccore.multiblock.configuration.core", "Base");
        provider.add("cosmiccore.multiblock.configuration.core.short", "B");
        provider.add("cosmiccore.multiblock.configuration.fcc", "Fluid Catalytic Cracker");
        provider.add("cosmiccore.multiblock.configuration.fcc.short", "FC");
        provider.add("cosmiccore.multiblock.configuration.hydrotreater", "Hydrotreater");
        provider.add("cosmiccore.multiblock.configuration.hydrotreater.short", "HT");
        provider.add("cosmiccore.multiblock.configuration.hydrocracker", "Hydrocracker");
        provider.add("cosmiccore.multiblock.configuration.hydrocracker.short", "HC");
        provider.add("cosmiccore.multiblock.configuration.reformer", "Catalytic Reformer");
        provider.add("cosmiccore.multiblock.configuration.reformer.short", "CR");
        provider.add("cosmiccore.multiblock.configuration.coker", "Delayed Coker");
        provider.add("cosmiccore.multiblock.configuration.coker.short", "DC");
        provider.add("cosmiccore.multiblock.configuration.atmospheric", "Atmospheric Distillation");
        provider.add("cosmiccore.multiblock.configuration.atmospheric.short", "AT");
        provider.add("cosmiccore.multiblock.configuration.vacuum", "Vacuum Distillation");
        provider.add("cosmiccore.multiblock.configuration.vacuum.short", "VA");
        provider.add("cosmiccore.multiblock.distillation_tower.mode", "Operating Mode: %s");
        provider.add("cosmiccore.multiblock.distillation_tower.vacuum_ready",
                "Vacuum service online: fan module and tower height verified");
        provider.add("cosmiccore.multiblock.distillation_tower.vacuum_required",
                "Requires the attached frostproof fan module and at least four repeated tower layers");
        provider.add("cosmiccore.multiblock.configuration.physically_locked",
                "Configuration is determined by the installed processing module");
        provider.add("cosmiccore.multiblock.configuration.core_only",
                "Attach a processing module to run recipes");
        provider.add("cosmiccore.multiblock.ebf.streak", "Recipe Duration Reduction: %s%%");
        provider.add("cosmiccore.multiblock.ebf.streak.progress", "Consecutive Matching Recipes: %s / %s");
        provider.add("cosmiccore.multiblock.ebf.streak.rule",
                "Tier 2: Gains 5% recipe speed for each consecutive recipe up to 50%, reducing total power used and duration.");

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
        // provider.add("tagprefix.leached_ore", "Leached %s Ore");
        // provider.add("tagprefix.prisma_frothed_ore", "Prisma Frothed %s Ore");
        provider.add("tagprefix.ultradense_plate", "Ultradense %s Plate");
        provider.add("tagprefix.heavy_beam", "Heavy %s Beam");
        provider.add("tagprefix.modular_shelling", "%s Modular Shelling");
        provider.add("tagprefix.plasmites", "%s Plasmites");
        provider.add("tagprefix.wire_spool", "%s Wire Spool");
        provider.add("tagprefix.shape_memory_foil", "%s Shaping Memory Foil");
        provider.add("tagprefix.alve_foil_insulator", "%s Alve Insulator");
        provider.add("tagprefix.raw_ore_cubic", "Cubic %s Ore");
        provider.add("tagprefix.ore_chunk", "%s Ore Chunk");
        provider.add("tagprefix.powderized_ore", "Powderized %s Ore");
        provider.add("tagprefix.crystallized_ore_chunk", "Crystallized %s Ore Chunk");
        provider.add("tagprefix.atomically_purified_ore_chunk", "Atomically Purified %s Ore Chunk");
        provider.add("tagprefix.flocculated_ore", "Flocculated %s Ore");
        provider.add("tagprefix.buzzsaw_blade", "%s Buzzsaw Blade");

        provider.add("cosmiccore.multiblock.reboot_powergrid", "§aReboot All Connected Machines");
        provider.add("cosmiccore.multiblock.sleep_powergrid", "§cSuspend All Connected Machines");

        provider.add("item.cosmiccore.debug.structure_writer.selection", "Selection: %s to %s");
        provider.add("item.cosmiccore.debug.structure_writer.structural_scale", "Size: %s | Blocks: %s");
        provider.add("item.cosmiccore.debug.structure_writer.v8_order",
                "Pattern Order: slice, string, character");
        provider.add("item.cosmiccore.debug.structure_writer.direction.slice", "Slice: %s (%s)");
        provider.add("item.cosmiccore.debug.structure_writer.direction.string", "String: %s (%s)");
        provider.add("item.cosmiccore.debug.structure_writer.direction.character", "Character: %s (%s)");
        provider.add("item.cosmiccore.debug.structure_writer.relative.up", "UP");
        provider.add("item.cosmiccore.debug.structure_writer.relative.down", "DOWN");
        provider.add("item.cosmiccore.debug.structure_writer.relative.left", "LEFT");
        provider.add("item.cosmiccore.debug.structure_writer.relative.right", "RIGHT");
        provider.add("item.cosmiccore.debug.structure_writer.relative.front", "FRONT");
        provider.add("item.cosmiccore.debug.structure_writer.relative.back", "BACK");
        provider.add("item.cosmiccore.debug.structure_writer.usage",
                "Use on blocks to expand the selection. Sneak-use to clear.");
        provider.add("item.cosmiccore.debug.structure_writer.copy_pattern", "Copy Pattern");
        provider.add("item.cosmiccore.debug.structure_writer.export_to_log", "Print Pattern to Log");
        provider.add("item.cosmiccore.debug.structure_writer.rotate_along_x_axis", "Rotate X Axis");
        provider.add("item.cosmiccore.debug.structure_writer.rotate_along_y_axis", "Rotate Y Axis");
        provider.add("item.cosmiccore.debug.structure_writer.clear", "Clear Selection");
        provider.add("item.cosmiccore.debug.structure_writer.output_successful",
                "Output Successful! Check your log file!");

        // item tooltips
        // TODO reorganize, use multiLang where applicable

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
        provider.add("cosmiccore.jade.ember", "Ember: %s/%s");
        provider.add("cosmiccore.jade.ember.volatile", "Accepts Volatile Ember");
        provider.add("cosmiccore.jade.ember.transfer", "Transfer: %s");
        provider.add("config.jade.plugin_cosmiccore.me_computation_array_details",
                "[CosmicCore] ME Computation Array Details");
        provider.add("cosmiccore.jade.me_computation_array.components", "Components: %s Cores / %s Relays");
        provider.add("cosmiccore.jade.me_computation_array.compute", "Compute/tick: %s / %s CWU/t");
        provider.add("cosmiccore.jade.me_computation_array.energy", "EU Demand: %s EU/t");
        provider.add("cosmiccore.jade.me_computation_array.relay", "Provided Network Power: %s / %s EU/t");
        provider.add("cosmiccore.jade.me_computation_array.buffer", "Stored Power: %s / %s EU (%s)");
        provider.add("cosmiccore.jade.me_computation_array.uplink.online", "Uplink Online");
        provider.add("cosmiccore.jade.me_computation_array.uplink.offline", "Uplink Offline");
        provider.add("config.jade.plugin_cosmiccore.power_grid_telemetry", "[CosmicCore] Power Grid Telemetry");
        provider.add("config.jade.plugin_cosmiccore.modular_power_station_mode",
                "[CosmicCore] Modular Power Station Mode");
        provider.add("cosmiccore.jade.modular_power_station.mode", "Mode: %s");
        provider.add("cosmiccore.jade.modular_power_station.mode.turbine", "Turbine Power Station");
        provider.add("cosmiccore.jade.modular_power_station.mode.combustion", "Combustion Power Station");
        provider.add("cosmiccore.jade.power.input_rating", "§fFace INPUT§7: %s V @ %s A");
        provider.add("cosmiccore.jade.power.output_rating", "§fFace OUTPUT§7: %s V @ %s A");
        provider.add("cosmiccore.jade.power.face_disconnected", "§fFace EU§7: Not connected");
        provider.add("cosmiccore.jade.power.input_flow", "§fBlock INPUT§7: %s A @ %s EU/t");
        provider.add("cosmiccore.jade.power.output_flow", "§fBlock OUTPUT§7: %s A @ %s EU/t");
        provider.add("cosmiccore.jade.power.cable_rating", "§fCable Rating§r: %s V (%s) @ %s A");
        provider.add("cosmiccore.jade.power.cable_voltage", "§fLive Voltage§r: %s V (%s)");
        provider.add("cosmiccore.jade.power.cable_voltage_idle", "§fLive Voltage§r: §fIdle");
        provider.add("cosmiccore.jade.power.cable_load", "§fLoad§r: %s §aA §7/ %s §aEU/t §7(§r%s§b%%§7)");
        provider.add("cosmiccore.jade.power.cable_temperature", "§fTemperature: %s §bK");
        provider.add("cosmiccore.jade.power.cable_overload", "§4Overload§c: %s%% §4to failure");
        provider.add("cosmiccore.jade.power.cable_cause", "§cCause: %s");
        provider.add("cosmiccore.jade.power.cable_cause.overamperage", "§6Overamperage");
        provider.add("cosmiccore.jade.power.cable_cause.overvoltage", "§6Overvoltage");
        provider.add("cosmiccore.jade.power.cable_cause.both", "§6Overamperage and overvoltage");
        provider.add("cosmiccore.jade.power.cable_cause.residual", "§6Residual heat");
        provider.add("cosmiccore.machine.me_computation_array.display.components", "Components: %s Cores / %s Relays");
        provider.add("cosmiccore.machine.me_computation_array.display.cwu", "Compute/tick: %s / %s CWU/t");
        provider.add("cosmiccore.machine.me_computation_array.display.energy", "EU Demand: %s EU/t");
        provider.add("cosmiccore.machine.me_computation_array.display.relay",
                "Provided Network Power: %s / %s EU/t");
        provider.add("cosmiccore.machine.me_computation_array.display.stored_power", "Stored Power: %s / %s EU");

        multiLang(provider, "item.cosmiccore.the_one_ring.tooltip",
                "§6§oOne Ring to rule them all, One Ring to find them, One Ring to bring them all and in the darkness bind them.§r",
                "You might find it hard to take off.");

        // generic machine tooltips
        provider.add("item.cosmiccore.space_radio.tooltip", "§6Lets you hear sounds in space!");
        provider.add("item.cosmiccore.simple_rebreather.tooltip",
                "§7Prevents oxygen loss in §bThin Air§7 environments.");
        provider.add("item.cosmiccore.pressurized_rebreather.tooltip",
                "§6Enables oxygen tank usage. Works in §cNo Air§6 environments.");
        provider.add("item.cosmiccore.palms_of_the_globestrider.tooltip",
                "§bAir-strafe, bunny-hop, and dash while worn.");
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
        provider.add("cosmiccore.flight_diffuser.range", "Grants creative flight within %s blocks while powered.");
        provider.add("cosmiccore.flight_diffuser.power", "Consumes %s EU/t (8 A @ %s) continuously while enabled.");
        provider.add("cosmiccore.flight_diffuser.landing",
                "Leaving range will make you immune until you land safely if this was your only source of flight.");
        provider.add("cosmiccore.create.chain_conveyor.vertical_direction", "Vertical chain direction: %s");
        provider.add("cosmiccore.direction.north", "North");
        provider.add("cosmiccore.direction.east", "East");
        provider.add("cosmiccore.direction.south", "South");
        provider.add("cosmiccore.direction.west", "West");
        provider.add("cosmiccore.circuit.lore.tier.max.0", "MAX Tier Circuit");
        provider.add("cosmiccore.circuit.lore.tier.max.1", "The Final Data Processor of the Titan of ???");
        provider.add("cosmiccore.circuit.lore.tier.max.2", "In entered every world");
        provider.add("cosmiccore.circuit.lore.tier.max.3", "as did nothing leave");

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
        provider.add("cosmiccore.wireless_pda.bound", "PDA linked to Power Capacitor at %s");
        provider.add("cosmiccore.wireless_pda.unbound", "PDA will now scan your global energy pocket");
        provider.add("cosmiccore.wireless_pda.tooltip.bind", "Sneak-use on a Power Capacitor to monitor it");
        provider.add("cosmiccore.wireless_pda.tooltip.linked", "Linked: %s [%s]");
        provider.add("cosmiccore.wireless_pda.tooltip.clear", "Sneak-use in the air to clear the link");
        provider.add("cosmiccore.wireless_pda.tooltip.wireless", "Monitoring dimensional team power pocket");
        provider.add("cosmiccore.wireless_pda.hud.local", "Power Capacitor");
        provider.add("cosmiccore.wireless_pda.hud.dimensional", "Dimensional Power Storage");
        provider.add("cosmiccore.wireless_pda.hud.unavailable", "Storage unavailable");

        replace(provider, "item.cosmiccore.infinite_spray_can", "§lPrismatic Spray Can");

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
        provider.add("cosmiccore.item.spraycan.gui.title", "Prismatic Spray Can");
        provider.add("cosmiccore.item.spraycan.gui.solvent", "Solvent (Strip Color)");
        provider.add("cosmiccore.item.spraycan.locked", "Spray Can is locked");
        provider.add("cosmiccore.item.spraycan.actionbar.color", "Spray Can Color: %s");
        provider.add("cosmiccore.item.spraycan.now_locked", "Spray Can locked");
        provider.add("cosmiccore.item.spraycan.now_unlocked", "Spray Can unlocked");

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

        // Ore Extraction Drill
        provider.add("cosmiccore.machine.ore_extraction_drill.tooltip.0",
                "§bExtracts ores from a %sx%s chunk area below the drill while only requiring the drill to be chunk loaded");
        provider.add("cosmiccore.machine.ore_extraction_drill.tooltip.1",
                "§fMines each discovered ore block once, then replaces it with stone");
        provider.add("cosmiccore.machine.ore_extraction_drill.tooltip.2",
                "§fOptional drilling fluids reduce cycle time; stronger fluids require higher-tier drills");
        provider.add("cosmiccore.machine.ore_extraction_drill.tooltip.3",
                "§7Use screwdriver to restart scan after completion, will run until no ore is present");
        provider.add("cosmiccore.machine.ore_extraction_drill.restarted",
                "Drill scan restarted");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.operations", "Operations");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.ledger", "Scanned Ores");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.status.unformed", "Structure Unformed");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.status.paused", "Operations Paused");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.status.awaiting_power", "Awaiting Power");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.status.idle", "Idle");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.status.scanning", "Surveying Ore Field");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.status.mining", "Excavating");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.status.complete", "Field Exhausted");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.scan_progress", "Surveyed: %s%%");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.mining_progress", "Excavated: %s / %s");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.area", "Survey: %sx%s chunks");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.power", "Power Draw: %s EU/t");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.cycle", "Cycle Time: %ss / block");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.remaining", "Remaining: %s");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.eta", "ETA: %s");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.eta.calculating", "Calculating...");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.eta.complete", "Complete");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.duration.days", "%sd %sh");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.duration.hours", "%sh %sm");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.duration.minutes", "%sm %ss");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.duration.seconds", "%ss");
        provider.add("cosmiccore.machine.ore_extraction_drill.ui.ledger.empty", "No ores recorded");
        provider.add("cosmiccore.tooltip.create_drill.field_fluid",
                "Mounted field mining requires %s mB of drilling fluid per ore");
        provider.add("cosmiccore.tooltip.create_drill.field_yield",
                "%s%% Yield on Resource Field Ores");
        provider.add("cosmiccore.tooltip.create_drill.field_scaling",
                "Drilling fluid cost compounds above %s mounted mining heads");

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
        // EXTENDED SUBSYSTEM LANGUAGE
        // =========================================================================
        initExtendedSubsystemLang(provider);
    }

    private static void initExtendedSubsystemLang(RegistrateLangProvider provider) {
        provider.add("cosmiccore.tooltip.steam_boiler.maximum_steam_output", "§6Maximum Output§f: %s mB/t Steam");
        provider.add("cosmiccore.tooltip.steam_boiler.maximum_pressurized_output",
                "§6Maximum Output§f: %s mB/t Pressurized Steam");
        provider.add("cosmiccore.tooltip.steam_boiler.steam_equivalent", "§eSteam Equivalent§f: %s mB/t Steam");
        provider.add("cosmiccore.tooltip.steam_boiler.temperature_scaling", "§7Output scales with boiler temperature");
        provider.add("cosmiccore.tooltip.large_boiler.output_scaling", "Output scales with temperature and throttle");

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
        provider.add("gtceu.jade.changes_eu_tick", "Net Storage: %s EU/t");

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

        provider.add("cosmiccore.emi.bookmarks.header", "Group %s/%s");
        provider.add("cosmiccore.emi.bookmarks.ingredient_unavailable", "Bookmark ingredient unavailable");
        provider.add("cosmiccore.emi.bookmarks.fluid_amount", "Exact amount: %s");
        provider.add("cosmiccore.emi.bookmarks.action.previous_group", "Previous Group");
        provider.add("cosmiccore.emi.bookmarks.action.next_group", "Next Group");
        provider.add("cosmiccore.emi.bookmarks.action.previous_page", "Previous Page");
        provider.add("cosmiccore.emi.bookmarks.action.next_page", "Next Page");
        provider.add("cosmiccore.emi.bookmarks.action.create_regular", "Click the + to create a new favorite group");
        provider.add(
                "cosmiccore.emi.bookmarks.action.create_recipe",
                "Shift + Click the + to create a new recipe favorite group");
        provider.add("cosmiccore.emi.bookmarks.action.delete", "Click the - to delete this empty group forever");
        provider.add(
                "cosmiccore.emi.bookmarks.action.force_delete",
                "SHIFT+CTRL+ALT+CLICK to force delete this bookmark group.");
        provider.add("cosmiccore.emi.bookmarks.help.title", "Advanced Controls");
        provider.add("cosmiccore.emi.bookmarks.help.exact", "Ctrl + %s: Pin an exact stack amount");
        provider.add("cosmiccore.emi.bookmarks.help.recipe", "Ctrl + Shift + %s: Pin a recipe row");
        provider.add(
                "cosmiccore.emi.bookmarks.help.adjust",
                "Ctrl + Scroll: Adjust by 1 item, 1 mB, or 1 recipe batch");
        provider.add(
                "cosmiccore.emi.bookmarks.help.adjust_fluid",
                "Ctrl + Alt + Scroll: Adjust fluids by 10 mB");
        provider.add(
                "cosmiccore.emi.bookmarks.help.adjust_fast",
                "Ctrl + Shift + Scroll: Adjust by 64 items, 1 B, or 10 recipe batches");
        provider.add("cosmiccore.emi.bookmarks.help.dismiss", "To hide the !, click the star once.");
        provider.add("cosmiccore.emi.bookmarks.help.restore", "To show the ! again, click the star once.");

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

        // Globestrider Movement
        provider.add("key.categories.cosmiccore.movement", "Quake Movement");
        provider.add("key.cosmiccore.movement.dash", "Dash");

        provider.add("item.cosmiccore.travelers_boots.tooltip",
                "Negates fall damage and steps up full blocks while standing.");

        provider.add("cosmiccore.hud.oxygen.suffocating", "SUFFOCATING");
        provider.add("cosmiccore.tooltip.hand_sort", "Can be hand sorted for: %s");
        provider.add("cosmiccore.emi.composite_ore_sorting", "Ore Sorting Diagram");
        provider.add("cosmiccore.emi.composite_ore_sorting.tier", "Tier");
        provider.add("cosmiccore.emi.composite_ore_sorting.first_recovered", "First Yield");
        provider.add("cosmiccore.emi.composite_ore_sorting.no_new_mineral", "No New Ore");
        provider.add("cosmiccore.emi.composite_ore_sorting.tier_number", "Tier %s");
        provider.add("cosmiccore.emi.composite_ore_sorting.process_order", "Order: %s");
        provider.add("cosmiccore.emi.composite_ore_sorting.process_order.1", "Crush -> Wash -> Sort");
        provider.add("cosmiccore.emi.composite_ore_sorting.process_order.2", "Crush -> Wash -> Powderize -> Sort");
        provider.add("cosmiccore.emi.composite_ore_sorting.process_order.3",
                "Crush -> Wash -> Powderize -> Flocculate -> Sort");
        provider.add("cosmiccore.emi.composite_ore_sorting.process_order.4",
                "Crush -> Wash -> Powderize -> Flocculate -> Crystallize -> Sort");
        provider.add("cosmiccore.emi.composite_ore_sorting.process_order.5",
                "Crush -> Wash -> Powderize -> Flocculate -> Crystallize -> Atomic Purification -> Sort");
        provider.add("cosmiccore.emi.composite_ore_sorting.refinement_ratio",
                "Previous tier stage -> This tier stage: 1:1");
        provider.add("cosmiccore.emi.composite_ore_sorting.sorter_yield", "Sorter yields at this tier:");
        provider.add("cosmiccore.emi.composite_ore_sorting.sorter_output", "%sx %s");
        provider.add("cosmiccore.emi.composite_ore_sorting.entry", "Raw Ore -> Crushed Ore");
        provider.add("cosmiccore.emi.composite_ore_sorting.entry.hammer", "Forge Hammer: 1:1");
        provider.add("cosmiccore.emi.composite_ore_sorting.entry.macerator", "Macerator: 1:2");
        provider.add("cosmiccore.tooltip.oxygen_tank.fill", "Oxygen: %s / %s mB");
        provider.add("cosmiccore.tooltip.oxygen_tank.runtime", "No-Air breathing time: %s");
        provider.add("cosmiccore.tooltip.oxygen_tank.rebreather", "Requires Pressurized Rebreather or Diving Helmet");
        // Food system and the Hearth
        provider.add("cosmiccore.food.sickened", "Your stomach turns. Your meals are slipping away.");
        provider.add("cosmiccore.food.family.defined", "Special");
        provider.add("cosmiccore.food.family.auto", "Food");
        provider.add("cosmiccore.food.role.main", "Main Course");
        provider.add("cosmiccore.food.role.side", "Side Dish");
        provider.add("cosmiccore.food.role.drink", "Drink");
        provider.add("cosmiccore.tooltip.food.vile", "Vile");
        provider.add("cosmiccore.tooltip.food.vile_desc", "causes onset of hunger at a rapid rate");
        provider.add("cosmiccore.tooltip.food.vile_hunger", "You feel the need to eat again");
        provider.add("cosmiccore.tooltip.food.max_health", "Max health");
        provider.add("cosmiccore.tooltip.food.regen", "Health regen");
        provider.add("cosmiccore.tooltip.food.quality", "Quality bonus");
        provider.add("cosmiccore.tooltip.food.duration", "Duration");
        provider.add("cosmiccore.hearth.memory_fades", "The memory of %s fades.");
        provider.add("cosmiccore.hearth.memory_settles", "The memory of %s settles in.");
        provider.add("cosmiccore.hearth.page_broadens",
                "A memory of this meal is saved to your cookbook, broadening your palate.");
        provider.add("cosmiccore.hearth.page", "A memory of this meal is saved to your cookbook.");
        provider.add("cosmiccore.hearth.taken_root",
                "%s has become an unforgettable meal! Right Click your empty plate to seal it in.");
        provider.add("cosmiccore.hearth.inscribe.no_memory", "You carry no meal memory to inscribe.");
        provider.add("cosmiccore.hearth.inscribe.already", "%s is already part of you.");
        provider.add("cosmiccore.hearth.inscribe.not_rooted", "This meal has not taken root in you yet.");
        provider.add("cosmiccore.hearth.inscribe.full", "You already carry %s signature meals.");
        provider.add("cosmiccore.hearth.inscribe.done", "%s is part of you now.");
        provider.add("cosmiccore.hearth.plate.vile", "You wouldn't serve that.");
        provider.add("cosmiccore.hearth.plate.drink_full", "A drink is already poured.");
        provider.add("cosmiccore.hearth.plate.side_full", "The side is already plated.");
        provider.add("cosmiccore.hearth.plate.main_full", "The main course is already served.");
        provider.add("cosmiccore.hearth.plate.no_main", "A meal needs a main course.");
        provider.add("cosmiccore.hearth.plate.not_home", "Too far from your bed to feel at home.");
        provider.add("cosmiccore.command.players_only", "Players only");
        provider.add("cosmiccore.command.food.memory_fail", "Hold a real food to make a memory of it");
        provider.add("cosmiccore.command.food.dump_fail", "Food dump failed: %s");
        provider.add("cosmiccore.command.food.dump_done", "Dumped %s consumables (%s hand-defined) to %s and %s");
        provider.add("cosmiccore.tooltip.food.warming", "Warming");
        provider.add("cosmiccore.tooltip.food.cooling", "Cooling");
        provider.add("cosmiccore.abyss.tome_sealed", "The magic of this tome has been sealed away");
        provider.add("cosmiccore.abyss.seal_broken",
                "You have found the source of arcane interference, your spell book hums with energy again. ");
        provider.add("cosmiccore.tooltip.tome_sealed_1",
                "A dark and virulent force has stripped the magic conductivity away from this tome,");
        provider.add("cosmiccore.tooltip.tome_sealed_2",
                "it is nothing but a mundane book until you discover the source.");
        provider.add("cosmiccore.abyss.crush_ascend", "CRUSH DEPTH! ASCEND IMMEDIATELY!");
        provider.add("cosmiccore.abyss.no_teleport", "The deep does not permit passage");
        provider.add("cosmiccore.abyss.no_mount", "Nothing will carry you down here");
        provider.add("cosmiccore.abyss.machine_forbidden", "This machine cannot operate in the deep");
        provider.add("cosmiccore.abyss.descent", "As you descend, the water becomes darker and more violent");
        provider.add("cosmiccore.abyss.swarm_title", "The Murkbloom Swarms");
        provider.add("cosmiccore.abyss.swarm_subtitle", "Stop disturbing the water and hide!");
        provider.add("effect.cosmiccore.stealth", "Stealth");
        provider.add("cosmiccore.tooltip.stealth_coated", "Stealth Coating %s");
        provider.add("cosmiccore.tooltip.stealth_coating.use_1", "Sew onto armor or tools at a Sewing Table.");
        provider.add("cosmiccore.tooltip.stealth_coating.use_2",
                "Greatly Reduces Sound Creation in the Abyss, reduced effect on armors.");
        provider.add("cosmiccore.abyss.speed_kill",
                "Your wake screamed through the dark and the Murkbloom echos back in force.");
        provider.add("cosmiccore.abyss.stalked", "Something vast turns its attention toward you.");
        provider.add("death.attack.cosmiccore.murkbloom", "%1$s was consumed by the Murkbloom");
        provider.add("death.attack.cosmiccore.too_loud", "%1$s was too loud in the abyss");
        provider.add("cosmiccore.machine.bloomwyrm_heart.tooltip.0",
                "Orchestrates linked Bloomwyrm cultivation units.");
        provider.add("cosmiccore.machine.bloomwyrm_heart.tooltip.1",
                "Supplies shared §eEU§f, §aBiopower§f, and §3Bloomwyrm Charge§f.");
        provider.add("cosmiccore.machine.bloomwyrm_heart.tooltip.2",
                "Link other units with a §6data stick§f within §b64§f blocks.");
        provider.add("cosmiccore.machine.abyssal_culture_vat.tooltip.0",
                "Cultivates abyssal samples into §aBiopower§f and §3Bloomwyrm Charge§f.");
        provider.add("cosmiccore.machine.abyssal_culture_vat.tooltip.1",
                "§aBiopower§f yield depends on the §6active culture recipe§f.");
        provider.add("cosmiccore.machine.sculk_biochamber.tooltip.0",
                "Consumes prepared cultures, Biopower, and Bloomwyrm Charge through the Bloomwyrm campus.");
        provider.add("cosmiccore.machine.sculk_biochamber.tooltip.1",
                "Consumes shared §3Bloomwyrm Charge§f and §aBiopower§f to mutate items and §aAlgae§f.");
        provider.add("cosmiccore.machine.biomana_digestor.tooltip.0",
                "Digests §cMurkwyrm Biomass§f into raw §aBiomana Slurry§f.");
        provider.add("cosmiccore.machine.biomana_digestor.tooltip.1",
                "Consumes shared §3Bloomwyrm Charge§f and §aBiopower§f to convert §2Biomass§f into §bMana§f.");
        provider.add("cosmiccore.machine.manawomb_leeching_pond.tooltip.0",
                "Utilizes §aAlgal Beds§f suspended in §bmana§f for industrial scale chemistry");
        provider.add("cosmiccore.machine.manawomb_leeching_pond.tooltip.1",
                "Runs one process at a time.");
        provider.add("cosmiccore.machine.bloomwyrm_unit.tooltip.parallel",
                "Set the desired §nparallel count§f from the controller interface.");
        provider.add("cosmiccore.bloomwyrm.constraint.none", "Allocation ready");
        provider.add("cosmiccore.bloomwyrm.constraint.no_heart", "No Bloomwyrm Heart linked");
        provider.add("cosmiccore.bloomwyrm.constraint.no_recipe", "No eligible local recipe");
        provider.add("cosmiccore.bloomwyrm.constraint.local_io", "Limited by local inputs or outputs");
        provider.add("cosmiccore.bloomwyrm.constraint.energy", "Limited by Heart EU capacity");
        provider.add("cosmiccore.bloomwyrm.constraint.biopower", "Limited by Biopower");
        provider.add("cosmiccore.bloomwyrm.constraint.charge", "Limited by Bloomwyrm Charge");
        provider.add("cosmiccore.bloomwyrm.constraint.heart_capacity", "Limited by Bloomwyrm Charge capacity");
        provider.add("cosmiccore.bloomwyrm.constraint.structure", "Allocation cancelled: structure unformed");
        provider.add("cosmiccore.bloomwyrm.waiting_for_heart_power", "Waiting for Bloomwyrm Heart power");
        provider.add("cosmiccore.bloomwyrm.unit.linked", "Bloomwyrm Heart linked");
        provider.add("cosmiccore.bloomwyrm.unit.unlinked", "Bloomwyrm Heart not linked");
        provider.add("cosmiccore.bloomwyrm.unit.parallel_requested", "Desired: %s  Eligible request: %s");
        provider.add("cosmiccore.bloomwyrm.unit.parallel_limits", "Eligible: %s  Heart offer: %s  Active: %s");
        provider.add("cosmiccore.bloomwyrm.unit.parallel_control", "Requested parallels");
        provider.add("cosmiccore.bloomwyrm.unit.parallel_control_max", "Max 16");
        provider.add("cosmiccore.bloomwyrm.unit.allocation", "Allocation: %s EU/t, %s Bloomwyrm Charge");
        provider.add("cosmiccore.bloomwyrm.unit.biopower", "Biopower: %s used / %s provided");
        provider.add("cosmiccore.bloomwyrm.unit.waiting_for_cycle", "Waiting for next Heart cycle: %s");
        provider.add("cosmiccore.bloomwyrm.unit.cycle_blocked", "Waiting for active campus work to finish");
        provider.add("cosmiccore.bloomwyrm.heart.charge", "Bloomwyrm Charge: %s / %s");
        provider.add("cosmiccore.bloomwyrm.heart.biopower", "Biopower: %s used / %s capacity");
        provider.add("cosmiccore.bloomwyrm.heart.energy", "Campus draw: %s EU/t");
        provider.add("cosmiccore.bloomwyrm.heart.supply", "Power supply: %s EU/t at %s V");
        provider.add("cosmiccore.bloomwyrm.heart.units", "Active units: %s / %s linked");
        provider.add("cosmiccore.bloomwyrm.heart.limited", "Resource-limited units: %s");
        provider.add("cosmiccore.bloomwyrm.heart.cycle", "Next allocation cycle: %s");
        provider.add("cosmiccore.bloomwyrm.heart.cycle_blocked", "Next cycle ready; waiting for active campus work");
        provider.add("cosmiccore.bloomwyrm.recipe.biopower_input", "Biopower use: %s");
        provider.add("cosmiccore.bloomwyrm.recipe.biopower_output", "Biopower capacity: +%s");
        provider.add("cosmiccore.bloomwyrm.recipe.charge_input", "Bloomwyrm Charge use: %s");
        provider.add("cosmiccore.bloomwyrm.recipe.charge_output", "Bloomwyrm Charge yield: +%s");
        provider.add("cosmiccore.bloomwyrm.recipe.max_parallel", "Max parallel: %s");
        provider.add("cosmiccore.steam.recipe.high_pressure", "%s mB/t @ %s s (High Pressure Steam Machines)");
        provider.add("cosmiccore.steam.recipe.low_pressure", "%s mB/t @ %s s (Low Pressure Steam Machines)");
        provider.add("cosmiccore.multiblock.preview.group_repeats", "Repeated Module Groups: %s");
        provider.add("cosmiccore.multiblock.modular_power_station.tooltip.0",
                "Attach §eDrive Modules§f (1-4) and a Stator Module to create a suitable power plant.");
        provider.add("cosmiccore.multiblock.modular_power_station.tooltip.1",
                "§7Each stage boosts max power generation by 4 Amps.");
        provider.add("cosmiccore.multiblock.modular_power_station.tooltip.2",
                "§fTurbine assemblies accept Steam and Gas Turbine fuels through one hardware-selected mode.");
        provider.add("cosmiccore.multiblock.modular_power_station.tooltip.3",
                "§7Dynamo Tier is limited by stator tier.");
        provider.add("cosmiccore.multiblock.modular_power_station.drive", "Drive: %s");
        provider.add("cosmiccore.multiblock.modular_power_station.stages", "Drive Stages: %s / 4");
        provider.add("cosmiccore.multiblock.modular_power_station.stator", "Stator: %s (%s V)");
        provider.add("cosmiccore.multiblock.modular_power_station.throttle", "Output Limit: %s%% (%s EU/t, %s A)");
        provider.add("cosmiccore.multiblock.modular_power_station.fuel.idle", "Fuel Rate: Waiting for an active fuel");
        provider.add("cosmiccore.multiblock.modular_power_station.fuel.none", "Fuel Rate: No fluid fuel input");
        provider.add("cosmiccore.multiblock.modular_power_station.fuel.rate",
                "%s: %s mB/min | %s mB/h");
        provider.add("cosmiccore.multiblock.modular_power_station.drive.none", "Undetected");
        provider.add("cosmiccore.multiblock.modular_power_station.drive.turbine", "Steam/Gas Turbine");
        provider.add("cosmiccore.multiblock.modular_power_station.drive.combustion", "Combustion Engine");
        provider.add("cosmiccore.multiblock.modular_power_station.status.ready", "Assembly Ready!");
        provider.add("cosmiccore.multiblock.modular_power_station.status.no_stages", "No drive stages detected!");
        provider.add("cosmiccore.multiblock.modular_power_station.status.invalid_stage",
                "A drive stage has invalid or mixed components!");
        provider.add("cosmiccore.multiblock.modular_power_station.status.mixed_stages",
                "All drive stages must use the same drive-core type!");
        provider.add("cosmiccore.multiblock.modular_power_station.status.missing_stator",
                "No supported stator housing was detected!");
        provider.add("cosmiccore.multiblock.modular_power_station.status.mixed_stators",
                "Every stator housing must use the same voltage tier!");
        provider.add("cosmiccore.multiblock.modular_power_station.status.output_mismatch",
                "The output hatch voltage tier must match the stator!");
        provider.add("cosmiccore.ponder.modular_power_station.header", "The Modular Power Station");
        provider.add("cosmiccore.ponder.shared.modular_power_station.text_1",
                "The controller and general power hardware form the station core");
        provider.add("cosmiccore.ponder.shared.modular_power_station.text_2",
                "Each drive module adds one integral slice with a shared Part-work wall");
        provider.add("cosmiccore.ponder.shared.modular_power_station.text_3",
                "Up to four of these slices may be added, each adding an additional 4A of maximum power output.");
        provider.add("cosmiccore.ponder.shared.modular_power_station.text_4",
                "Modular Power Stations Must have all generator slices share the same integral parts, no mixing turbine and combustion modules!");
        provider.add("cosmiccore.ponder.shared.modular_power_station.text_5",
                "The ending houses a large stator to produce power, the dynamo output is limited by stator coil tier.");
        for (var recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType &&
                    gtRecipeType.registryName.getNamespace().equals("gtceu")) {
                provider.add(gtRecipeType.getTranslationKey(), toTitle(gtRecipeType.registryName.getPath()));
            }
        }
        for (String id : COSMIC_RECIPE_TYPE_IDS) {
            String name = switch (id) {
                case "fuckassbeeball" -> "Internal Recipe Type";
                case "atmo_siphon" -> "Atmosphere Siphon";
                case "pcb_fab" -> "PCB Fabricator";
                case "cryo_chamber" -> "Cryogenics Chamber";
                case "reconstructor" -> "Radbolt Reconstructor";
                case "mana_etching" -> "Mana Etching Factory";
                case "chormatic_distillation_plant" -> "Chromatic Distillation Plant";
                case "industrial_chemvat" -> "Industrial Chemical Vat";
                case "link_test" -> "Link Test Station";
                default -> toTitle(id);
            };
            provider.add("recipe_type.cosmiccore." + id, name);
        }
        provider.add("cosmiccore.machine.chemical_dehydrator.tooltip",
                "Removes water from chemical intermediates instead of returning it as a fluid output.");
        provider.add("cosmiccore.ftbquests.dependency_lines", "Dependency Lines");
        provider.add("cosmiccore.ftbquests.dependency_lines.none", "No quest dependencies");
        provider.add("cosmiccore.ftbquests.dependency_lines.dependencies", "Dependencies");
        provider.add("cosmiccore.ftbquests.dependency_lines.dependants", "Dependants");
        provider.add("cosmiccore.ftbquests.dependency_lines.task_count", "%s task dependencies");
        provider.add("cosmiccore.ftbquests.dependency_lines.task_hint",
                "Task dependencies do not draw lines. Use quest dependencies and hide individual lines here instead.");
        provider.add("cosmiccore.ftbquests.dependency_lines.entry", "%s / %s");
        provider.add("cosmiccore.ftbquests.dependency_lines.hide", "Hide line");
        provider.add("cosmiccore.ftbquests.dependency_lines.show", "Show line");
        provider.add("cosmiccore.ftbquests.dependency_lines.jump", "Jump to dependency");
        provider.add("cosmiccore.ftbquests.dependency_lines.asset", "Line Asset");
        provider.add("cosmiccore.ftbquests.dependency_lines.asset.default", "Chapter Default");
        provider.add("cosmiccore.ftbquests.dependency_lines.asset.main_questline", "Main Questline");
        provider.add("cosmiccore.ftbquests.dependency_lines.asset.offroad", "Offroad");
        provider.add("cosmiccore.ftbquests.dependency_lines.asset.choose", "Choose Asset...");
        provider.add("cosmiccore.ftbquests.dependency_lines.asset.current", "Current: %s");
        provider.add("cosmiccore.ftbquests.dependency_lines.asset.hint", "Line assets should tile horizontally");
        provider.add("cosmiccore.ftbquests.dependency_lines.paint", "Dependency Line Painter");
        provider.add("cosmiccore.ftbquests.dependency_lines.paint.none",
                "No dependency lines between selected quests");
        provider.add("cosmiccore.ftbquests.dependency_lines.paint.summary",
                "%s lines between %s selected quests (%s styles)");
        provider.add("cosmiccore.ftbquests.dependency_lines.paint.scope",
                "Only existing direct lines with both endpoints selected are changed");
        provider.add("cosmiccore.ftbquests.dependency_lines.paint.show", "Show Lines");
        provider.add("cosmiccore.ftbquests.dependency_lines.paint.hide", "Hide Lines");
        provider.add("cosmiccore.ftbquests.quest_alias", "Quest Alias");
        provider.add("cosmiccore.ftbquests.quest_alias.hint", "Another view of the same quest and its progress");
        provider.add("cosmiccore.ftbquests.quest_alias.dependant_lines", "Alias Dependant Lines");
        provider.add("cosmiccore.ftbquests.quest_alias.dependant_lines.none",
                "This quest has no dependant quest lines");
        provider.add("cosmiccore.ftbquests.quest_alias.dependant_lines.assigned",
                "This line ends at this alias. Click to return it to the canonical quest.");
        provider.add("cosmiccore.ftbquests.quest_alias.dependant_lines.unassigned",
                "Click to move this line to this alias.");
        provider.add("screen.cosmiccore.deeds", "The Inner Vault");
        provider.add("key.cosmiccore.deeds.open", "Open Deeds");
        provider.add("key.categories.cosmiccore.deeds", "CosmicCore: Deeds");
        provider.add("button.cosmiccore.deeds", "Deeds");
        provider.add("button.cosmiccore.deeds.tooltip", "Enter the Inner Vault");
        provider.add("cosmiccore.deeds.banner.first",
                "Something in the darkness of your being clatters, lost but felt.");
        provider.add("cosmiccore.deeds.banner.second",
                "Fate feels as if it's buckling around you, asking for a new story.");
        provider.add("cosmiccore.deeds.banner.prompt", "It will not let go, pull it back to you");
        provider.add("cosmiccore.deeds.banner.force_control", "[Press %s to forcefully weave]");
        provider.add("cosmiccore.deeds.banner.control", "[Press %s to enter the Inner Vault]");
        provider.add("mirror.cosmiccore.prompt.hold", "Hold to weave");
        provider.add("mirror.cosmiccore.prompt.binding", "Binding...");
        provider.add("mirror.cosmiccore.prompt.heart_hold", "Hold on tightly...");
        provider.add("mirror.cosmiccore.prompt.heart",
                "Mother Moon, here I stand, on the same stage as you once again.");
        provider.add("command.cosmiccore.deed.revoked", "Revoked %s");
        provider.add("command.cosmiccore.deed.not_held", "Deed is neither pending nor woven: %s");
        provider.add("config.cosmiccore.dev_visor", "Dev Visor");
        provider.add("config.cosmiccore.dev_visor.tooltip", "§cWARNING! WARNING! WARNING!§r\n" +
                "This config enables editing aspects of the pack not normally allowed by default. This can cause " +
                "potentially damaging effects to your progression, save, team, or otherwise anything in the pack.\n" +
                "Using this means you have CLEARLY READ THIS, and ACCEPT FAULT for enabling it.\n" +
                "If you are requesting support with this config on, there is a high chance developers will not be " +
                "happy.");
        provider.add("key.categories.cosmiccore", "CosmicCore: Development");
        provider.add("key.cosmiccore.abyss_dev_view", "Toggle Abyss Survey Vision");
        provider.add("key.cosmiccore.murkbloom_dev_stir", "Cycle Murkbloom Preview");
        provider.add("key.cosmiccore.murkbloom_dev_immunity", "Toggle Murkbloom Immunity");
        provider.add("cosmiccore.dev.abyss_view.enabled", "Abyss survey vision enabled");
        provider.add("cosmiccore.dev.abyss_view.disabled", "Abyss survey vision disabled");
        provider.add("cosmiccore.dev.murkbloom.flinch", "Murkbloom preview: flinch");
        provider.add("cosmiccore.dev.murkbloom.stir", "Murkbloom preview: %s (%s)");
        provider.add("cosmiccore.dev.murkbloom.immunity.enabled", "Murkbloom immunity enabled");
        provider.add("cosmiccore.dev.murkbloom.immunity.disabled", "Murkbloom immunity disabled");
        provider.add("cosmiccore.dev.murkbloom.immunity.denied", "Murkbloom immunity requires operator access");
        provider.add("cosmiccore.firmament.tide.title", "SET WITH THE SUN");
        provider.add("cosmiccore.firmament.tide.prompt", "Hold sneak to return to earth");
        provider.add("cosmiccore.dimension.nether_permit_required", "You need a Nether Permit to enter the Nether.");
        provider.add("cosmiccore.dimension.firmament_permit_required",
                "You need a Firmament Permit to enter the Firmament.");
        provider.add("cosmiccore.firmament.ritual.overworld_only",
                "The Firmament can only be reached from Earth.");
        provider.add("ritual.cosmiccore.firmament_ascent.started", "The heavens begin to draw near.");
        provider.add("ritual.cosmiccore.firmament_ascent.finished", "The sky releases its hold.");
        provider.add("ritual.cosmiccore.firmament_ascent.interrupted", "The path above slams shut.");
        provider.add("item.cosmiccore.firmament_ascent_ritual.tooltip",
                "Rise beyond the air and enter the Firmament.");
        provider.add("cosmiccore.ftbquests.deed.task", "Deed Seal");
        provider.add("cosmiccore.ftbquests.deed.sealed_title", "SEALED");
        provider.add("cosmiccore.ftbquests.deed.sealed_hint", "Something waits beyond this seal.");
        provider.add("cosmiccore.ftbquests.deed.visible_hint",
                "A star from within watches quietly, waiting for you.");
        provider.add("cosmiccore.ftbquests.deed.primed_visible",
                "The task at hand is complete, fate is waiting patiently.");
        provider.add("cosmiccore.ftbquests.deed.primed_sealed",
                "A great Star beckons from within, fate is waiting eagerly.");
        provider.add("deed.cosmiccore.nether_permit", "Nether Permit");
        provider.add("deed.cosmiccore.nether_permit.telling.prelude.0",
                "Cold iron against a borrowed palm....");
        provider.add("deed.cosmiccore.nether_permit.telling.coil.0",
                "Someone painted this world.\nTheir name is far gone.");
        provider.add("deed.cosmiccore.nether_permit.telling.coil.1",
                "The quiet life kept what they could not carry amongst the stars.");
        provider.add("deed.cosmiccore.nether_permit.telling.ring.0",
                "The thread recoils to where no one was meant to return.");
        provider.add("deed.cosmiccore.nether_permit.telling.ring.1",
                "Warmth passes onto you like a welcomed dream, an embrace from someone no longer here.");
        provider.add("deed.cosmiccore.nether_permit.telling.knot.0",
                "It remembers the ground, it remembers the earth holding them.");
        provider.add("deed.cosmiccore.nether_permit.telling.knot.1",
                "Chains of silver open a maw into an infinite ocean of stars, as i cast my will inside.");
        provider.add("deed.cosmiccore.current_flow", "Current Flow");
        provider.add("deed.cosmiccore.current_flow.subtitle", "The mana of physics");
        provider.add("deed.cosmiccore.current_flow.sealed_hint", "This star beckons to you, reach it.");
        provider.add("deed.cosmiccore.current_flow.telling.prelude.0",
                "The cold iron now feels familiar, in hands unborrowed, as will traces it forward.");
        provider.add("deed.cosmiccore.current_flow.telling.prelude.1",
                "A painter left a brush, lonely for a home. It will paint again.");
        provider.add("deed.cosmiccore.current_flow.telling.coil.0",
                "A metallic construct whirls and breathes life into a grove of steel and runes.");
        provider.add("deed.cosmiccore.current_flow.telling.coil.1",
                "Sparking, fleeting, and eager to touch the earth.");
        provider.add("deed.cosmiccore.current_flow.telling.coil.2",
                "It feels as if the tempest of the sky has graced the world a second time.");
        provider.add("deed.cosmiccore.current_flow.telling.ring.0",
                "\"The one who holds infinity, does such simple magicks entertain you so?\"");
        provider.add("deed.cosmiccore.current_flow.telling.ring.1",
                "\"The flow of power is simple and honest. Do not be tempted by greed; respect it.\"");
        provider.add("deed.cosmiccore.current_flow.telling.knot.0",
                "The poise of energy, the elegance of raw force, is something to cherish.");
        provider.add("deed.cosmiccore.current_flow.telling.knot.1",
                "In this field of stars, even the power of the skies looks so lonely and distant.");
        provider.add("deed.cosmiccore.current_flow.telling.knot.2",
                "A door opens inward where the current hums, and I cast my will inside once again.");
        provider.add("deed.cosmiccore.current_flow.post",
                "The steam age ends quietly, in the end. Not with a last whistle but with a hum you can feel " +
                        "through your boots: small lightnings walking their fenced circles, patient, waiting to be " +
                        "spent. You pulled this thread home yourself, and nothing tore. Somewhere in the dark below " +
                        "your ribs, something that has never once been spoken to is beginning to suspect it was.");
    }
}
