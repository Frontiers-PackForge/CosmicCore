package com.ghostipedia.cosmiccore.common.data.materials;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.data.material.property.CCoreMaterialIconSet;
import com.ghostipedia.cosmiccore.api.item.MeldingOmniTool;
import com.ghostipedia.cosmiccore.common.power.ConductorAmpacityRules;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialRegistryEntry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterialSet.MAGIC;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class CosmicMaterials {

    public static MaterialRegistryEntry Prisma;
    public static MaterialRegistryEntry DilutedPrisma;
    public static MaterialRegistryEntry Virtue;
    public static MaterialRegistryEntry PrismaticTungstensteel;
    public static MaterialRegistryEntry VoidSpark;
    public static MaterialRegistryEntry ResonantVirtueMeld;
    public static MaterialRegistryEntry NaquadicSuperalloy;
    public static MaterialRegistryEntry Trinavine;
    public static MaterialRegistryEntry TriniumNaqide;
    public static MaterialRegistryEntry LivingIgniclad;
    public static MaterialRegistryEntry PsionicGalvorn;
    public static MaterialRegistryEntry ProgrammableMatter;
    public static MaterialRegistryEntry ShimmeringNeutronium;
    public static MaterialRegistryEntry CausalFabric;
    public static MaterialRegistryEntry Potential;
    public static MaterialRegistryEntry FalseInfinity;
    public static MaterialRegistryEntry SuitableInfinity;
    public static MaterialRegistryEntry LogicalInfinity;
    public static MaterialRegistryEntry Taranium;
    public static MaterialRegistryEntry Spacetime;
    public static MaterialRegistryEntry StygianPlague;
    public static MaterialRegistryEntry EnragedStygianPlague;
    public static MaterialRegistryEntry Cosmocite;
    public static MaterialRegistryEntry TearsOfTheUniverse;
    public static MaterialRegistryEntry Utherium;
    public static MaterialRegistryEntry Aphotite;
    public static MaterialRegistryEntry Arcanite;
    public static MaterialRegistryEntry Gloomarcine;
    public static MaterialRegistryEntry Veilspar;
    public static MaterialRegistryEntry Bathyst;
    public static MaterialRegistryEntry Nyctophyte;
    public static MaterialRegistryEntry Hadalite;
    public static MaterialRegistryEntry Abyssbloom;
    public static MaterialRegistryEntry Ichor;
    public static MaterialRegistryEntry Ichorium;
    public static MaterialRegistryEntry Chronon;
    public static MaterialRegistryEntry Temmerite;
    public static MaterialRegistryEntry Triphenylphosphine;
    public static MaterialRegistryEntry Neutronite;
    public static MaterialRegistryEntry PolyethyleneOxide;
    public static MaterialRegistryEntry RainSealant;
    public static MaterialRegistryEntry MultiPhaseOil;
    public static MaterialRegistryEntry HotPyrolysisVapors;
    public static MaterialRegistryEntry CrudeBenzene;
    public static MaterialRegistryEntry VolatileMultiPhaseOil;
    public static MaterialRegistryEntry SaltLadenLightOil;
    public static MaterialRegistryEntry SaltLadenOil;
    public static MaterialRegistryEntry SaltLadenHeavyOil;
    public static MaterialRegistryEntry WetNaturalGases;
    public static MaterialRegistryEntry OilRichWastewater;
    public static MaterialRegistryEntry OilySludge;
    public static MaterialRegistryEntry SourBrine;
    public static MaterialRegistryEntry SourProcessWater;
    public static MaterialRegistryEntry AtmosphericResidue;
    public static MaterialRegistryEntry LeanAmineSolution;
    public static MaterialRegistryEntry RichAmineSolution;
    public static MaterialRegistryEntry AcidicGases;
    public static MaterialRegistryEntry TailGas;
    public static MaterialRegistryEntry HotLightEffluents;
    public static MaterialRegistryEntry HotNaphthaEffluents;
    public static MaterialRegistryEntry HotGasOilEffluents;
    public static MaterialRegistryEntry HeavyPyrolysisOils;
    public static MaterialRegistryEntry OlefinGases;
    public static MaterialRegistryEntry CrackedNaphtha;
    public static MaterialRegistryEntry AromaticOil;
    public static MaterialRegistryEntry CondensedRefineryGas;
    public static MaterialRegistryEntry HeavyNaphtha;
    public static MaterialRegistryEntry CrackedGasoline;
    public static MaterialRegistryEntry LightCycleOil;
    public static MaterialRegistryEntry SlurryOils;
    public static MaterialRegistryEntry HighOctaneReformate;
    public static MaterialRegistryEntry VacuumGasOils;
    public static MaterialRegistryEntry WaxyDistillates;
    public static MaterialRegistryEntry VacuumResiduals;
    public static MaterialRegistryEntry Bitumen;
    public static MaterialRegistryEntry PetroleumCoke;
    public static MaterialRegistryEntry CalcinedPetroleumCoke;
    public static MaterialRegistryEntry Syngas;
    public static MaterialRegistryEntry WoodSpirit;
    public static MaterialRegistryEntry AcidicWoodLiquor;
    public static MaterialRegistryEntry LightTarOils;
    public static MaterialRegistryEntry PhenolicOils;
    public static MaterialRegistryEntry TarPitch;
    public static MaterialRegistryEntry HotCokingVapors;
    public static MaterialRegistryEntry AmmoniaRichLiquor;
    public static MaterialRegistryEntry NaphthaleneOils;
    public static MaterialRegistryEntry Vitrius;
    public static MaterialRegistryEntry SolSteel;
    public static MaterialRegistryEntry StarMetal;
    public static MaterialRegistryEntry Nevramite;
    public static MaterialRegistryEntry PaleSteel;
    public static MaterialRegistryEntry PaleOil;
    public static MaterialRegistryEntry PrimordialSoup;
    public static MaterialRegistryEntry Moondrop;
    public static MaterialRegistryEntry Somanone;

    public static MaterialRegistryEntry AuricForcinium;
    public static MaterialRegistryEntry SuperheavyBedrockAlloy;
    public static MaterialRegistryEntry Nostium;
    public static MaterialRegistryEntry GalvanizedEthersteel;
    public static MaterialRegistryEntry DraconicTarragon;
    public static MaterialRegistryEntry Kuvite;
    public static MaterialRegistryEntry BlinkwardAlloy;

    public static MaterialRegistryEntry Lumium;
    public static MaterialRegistryEntry Signalum;
    public static MaterialRegistryEntry Enderium;
    public static MaterialRegistryEntry Tenbrium;
    public static MaterialRegistryEntry Halizine;
    public static MaterialRegistryEntry Rosmotosin;

    public static MaterialRegistryEntry VibrantAlloy;
    public static MaterialRegistryEntry EnergeticAluminium;
    public static MaterialRegistryEntry MelodicAlloy;
    public static MaterialRegistryEntry StarstruckHalite;

    public static MaterialRegistryEntry Lignite;
    public static MaterialRegistryEntry Anthracene;

    // Homage Fluids
    public static MaterialRegistryEntry EssenceOfTerraFirmaGreg;
    public static MaterialRegistryEntry EssenceOfMoni;

    public static MaterialRegistryEntry basicNutrientProductionFluid;
    public static MaterialRegistryEntry advancedNutrientProductionFluid;
    public static MaterialRegistryEntry superNutrientProductionFluid;
    public static MaterialRegistryEntry perfectedNutrientProductionFluid;

    public static MaterialRegistryEntry basicNutrientOverclockFluid;
    public static MaterialRegistryEntry advancedNutrientOverclockFluid;
    public static MaterialRegistryEntry superNutrientOverclockFluid;
    public static MaterialRegistryEntry perfectedNutrientOverclockFluid;

    public static MaterialRegistryEntry AdvancedDrillingFluid;
    public static MaterialRegistryEntry ExtremeDrillingFluid;
    public static MaterialRegistryEntry HighPressureSteam;
    public static MaterialRegistryEntry BiomanaSlurry;
    public static MaterialRegistryEntry BloomingSludge;
    public static MaterialRegistryEntry BloomRichAlgaeSolution;
    public static MaterialRegistryEntry PhytoGrease;
    public static MaterialRegistryEntry BiomeldNutrientMV;
    public static MaterialRegistryEntry BiomeldNutrientEV;
    public static MaterialRegistryEntry BiomeldNutrientLuV;

    public static void register() {
        HighPressureSteam = REGISTRATE.material("high_pressure_steam", builder -> builder
                .gas(new FluidBuilder().state(FluidState.GAS).temperature(473))
                .color(0xC6E7FF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1));
        BiomanaSlurry = REGISTRATE.material("biomana_slurry", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).temperature(295))
                .color(0x176F6A));
        BloomingSludge = REGISTRATE.material("blooming_sludge", builder -> builder
                .liquid()
                .color(0x3F5A2D).secondaryColor(0x1C2D16));
        BloomRichAlgaeSolution = REGISTRATE.material("bloom_rich_algae_solution", builder -> builder
                .liquid()
                .color(0x65A83F).secondaryColor(0xB4D66C));
        PhytoGrease = REGISTRATE.material("phyto_grease", builder -> builder
                .liquid()
                .color(0xA0A943).secondaryColor(0x596427));
        BiomeldNutrientMV = REGISTRATE.material("biomeld_nutrient", builder -> builder
                .liquid()
                .color(0x7FAE58).secondaryColor(0xC77A82));
        BiomeldNutrientEV = REGISTRATE.material("biomeld_nutrient_ev", builder -> builder
                .liquid()
                .color(0x6F8F67).secondaryColor(0xB85D7B));
        BiomeldNutrientLuV = REGISTRATE.material("biomeld_nutrient_luv", builder -> builder
                .liquid()
                .color(0x5C7080).secondaryColor(0x9F4D85));
        AdvancedDrillingFluid = REGISTRATE.material("advanced_drilling_fluid", builder -> builder
                .liquid()
                .color(0x4CB7C5));
        ExtremeDrillingFluid = REGISTRATE.material("extreme_drilling_fluid", builder -> builder
                .liquid()
                .color(0x9A4DFF));

        // Nutrient Fluids (Production)

        basicNutrientProductionFluid = REGISTRATE.material("basic_production_nutrient", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .formula("\uD80C\uDDA6"));
        advancedNutrientProductionFluid = REGISTRATE.material("advanced_production_nutrient", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .formula("\uD80C\uDDA6"));
        superNutrientProductionFluid = REGISTRATE.material("super_production_nutrient", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .formula("\uD80C\uDDA6"));
        perfectedNutrientProductionFluid = REGISTRATE.material("perfect_production_nutrient", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .formula("\uD80C\uDDA6"));

        basicNutrientOverclockFluid = REGISTRATE.material("basic_overclock_nutrient", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .formula("\uD80C\uDDA6"));
        advancedNutrientOverclockFluid = REGISTRATE.material("advanced_overclock_nutrient", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .formula("\uD80C\uDDA6"));
        superNutrientOverclockFluid = REGISTRATE.material("super_overclock_nutrient", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .formula("\uD80C\uDDA6"));
        perfectedNutrientOverclockFluid = REGISTRATE.material("perfect_overclock_nutrient", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .formula("\uD80C\uDDA6"));

        Prisma = REGISTRATE.material("prisma", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .formula("֍")
                .element(CosmicElements.Pi));
        Vitrius = REGISTRATE.material("vitrius", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .formula("֍⋄")
                .element(CosmicElements.Pi));
        DilutedPrisma = REGISTRATE.material("diluted_prisma", builder -> builder
                .liquid()
                .color(0x4995b3).secondaryColor(0x4995b3)
                .components(Prisma, 1, Water, 4)
                .flags(DISABLE_DECOMPOSITION));
        PolyethyleneOxide = REGISTRATE.material("polyethylene_oxide", builder -> builder
                .liquid()
                .color(0xdbe4e0).secondaryColor(0xb4c6bf)
                .components(Carbon, 2, Hydrogen, 4, Oxygen, 1)
                .flags(DISABLE_DECOMPOSITION));
        MultiPhaseOil = RawOil;
        RainSealant = REGISTRATE.material("rain_sealant", builder -> builder
                .liquid(new FluidBuilder().temperature(295))
                .color(0xB9D9C8).secondaryColor(0x668F80)
                .iconSet(CosmicMaterialSet.FLUID_B)
                .flags(DISABLE_DECOMPOSITION));
        HotPyrolysisVapors = CharcoalByproducts;
        CrudeBenzene = REGISTRATE.material("crude_benzene", builder -> builder
                .liquid()
                .color(0x665132).secondaryColor(0xA98A52)
                .flags(DISABLE_DECOMPOSITION));
        VolatileMultiPhaseOil = REGISTRATE.material("volatile_multi_phase_oil", builder -> builder
                .liquid()
                .color(0x5A3D22).secondaryColor(0xB9874C)
                .flags(DISABLE_DECOMPOSITION));
        SaltLadenLightOil = REGISTRATE.material("salt_laden_light_oil", builder -> builder
                .liquid()
                .color(0x8D744C).secondaryColor(0xC8AF7B)
                .flags(DISABLE_DECOMPOSITION));
        SaltLadenOil = REGISTRATE.material("salt_laden_oil", builder -> builder
                .liquid()
                .color(0x4D3828).secondaryColor(0x886647)
                .flags(DISABLE_DECOMPOSITION));
        SaltLadenHeavyOil = REGISTRATE.material("salt_laden_heavy_oil", builder -> builder
                .liquid()
                .color(0x2A211D).secondaryColor(0x51423A)
                .flags(DISABLE_DECOMPOSITION));
        WetNaturalGases = REGISTRATE.material("wet_natural_gases", builder -> builder
                .gas()
                .color(0x8FA89A).secondaryColor(0xC1D1C8)
                .flags(DISABLE_DECOMPOSITION));
        OilRichWastewater = REGISTRATE.material("oil_rich_wastewater", builder -> builder
                .liquid()
                .color(0x52635C).secondaryColor(0x899B92)
                .flags(DISABLE_DECOMPOSITION));
        OilySludge = REGISTRATE.material("oily_sludge", builder -> builder
                .liquid()
                .color(0x27231F).secondaryColor(0x514B43)
                .flags(DISABLE_DECOMPOSITION));
        SourBrine = REGISTRATE.material("sour_brine", builder -> builder
                .liquid()
                .color(0x6B806F).secondaryColor(0xA2B6A6)
                .flags(DISABLE_DECOMPOSITION));
        SourProcessWater = REGISTRATE.material("sour_process_water", builder -> builder
                .liquid()
                .color(0x596963).secondaryColor(0x8C9D95)
                .flags(DISABLE_DECOMPOSITION));
        AtmosphericResidue = REGISTRATE.material("atmospheric_residue", builder -> builder
                .liquid()
                .color(0x241B17).secondaryColor(0x4E3A2D)
                .flags(DISABLE_DECOMPOSITION));
        LeanAmineSolution = REGISTRATE.material("lean_amine_solution", builder -> builder
                .liquid()
                .color(0x3D8B78).secondaryColor(0x8FD7C2)
                .flags(DISABLE_DECOMPOSITION));
        RichAmineSolution = REGISTRATE.material("rich_amine_solution", builder -> builder
                .liquid()
                .color(0x665A3C).secondaryColor(0xB09B61)
                .flags(DISABLE_DECOMPOSITION));
        AcidicGases = REGISTRATE.material("acidic_gases", builder -> builder
                .gas()
                .color(0xA5A13B).secondaryColor(0xD7D16B)
                .flags(DISABLE_DECOMPOSITION));
        TailGas = REGISTRATE.material("tail_gas", builder -> builder
                .gas()
                .color(0x55515F).secondaryColor(0x8E879B)
                .flags(DISABLE_DECOMPOSITION));
        HotLightEffluents = REGISTRATE.material("hot_light_effluents", builder -> builder
                .gas(new FluidBuilder().temperature(775))
                .color(0xD18442).secondaryColor(0xF2B56C)
                .flags(DISABLE_DECOMPOSITION));
        HotNaphthaEffluents = REGISTRATE.material("hot_naphtha_effluents", builder -> builder
                .gas(new FluidBuilder().temperature(800))
                .color(0xA96535).secondaryColor(0xD99B5A)
                .flags(DISABLE_DECOMPOSITION));
        HotGasOilEffluents = REGISTRATE.material("hot_gas_oil_effluents", builder -> builder
                .gas(new FluidBuilder().temperature(825))
                .color(0x6B4138).secondaryColor(0xA66D5D)
                .flags(DISABLE_DECOMPOSITION));
        HeavyPyrolysisOils = REGISTRATE.material("heavy_pyrolysis_oils", builder -> builder
                .liquid()
                .color(0x271F29).secondaryColor(0x554259)
                .flags(DISABLE_DECOMPOSITION));
        OlefinGases = REGISTRATE.material("olefin_gases", builder -> builder
                .gas()
                .color(0x8FAEBA).secondaryColor(0xC7DCE3)
                .flags(DISABLE_DECOMPOSITION));
        CrackedNaphtha = REGISTRATE.material("cracked_naphtha", builder -> builder
                .liquid()
                .color(0xB9793D).secondaryColor(0xD9A761)
                .flags(DISABLE_DECOMPOSITION));
        AromaticOil = REGISTRATE.material("aromatic_oil", builder -> builder
                .liquid()
                .color(0x4A283C).secondaryColor(0x805069)
                .flags(DISABLE_DECOMPOSITION));
        CondensedRefineryGas = REGISTRATE.material("condensed_refinery_gas", builder -> builder
                .liquid()
                .color(0x8CA0AC).secondaryColor(0xC4D3DA)
                .flags(DISABLE_DECOMPOSITION));
        HeavyNaphtha = REGISTRATE.material("heavy_naphtha", builder -> builder
                .liquid()
                .color(0xB88742).secondaryColor(0xDBB66F)
                .flags(DISABLE_DECOMPOSITION));
        CrackedGasoline = REGISTRATE.material("cracked_gasoline", builder -> builder
                .liquid()
                .color(0xC66E2E).secondaryColor(0xE7A052)
                .flags(DISABLE_DECOMPOSITION));
        LightCycleOil = REGISTRATE.material("light_cycle_oil", builder -> builder
                .liquid()
                .color(0x87612E).secondaryColor(0xB5904E)
                .flags(DISABLE_DECOMPOSITION));
        SlurryOils = REGISTRATE.material("slurry_oils", builder -> builder
                .liquid()
                .color(0x28231F).secondaryColor(0x55483D)
                .flags(DISABLE_DECOMPOSITION));
        HighOctaneReformate = REGISTRATE.material("high_octane_reformate", builder -> builder
                .liquid()
                .color(0xB94E2F).secondaryColor(0xDC7E52)
                .flags(DISABLE_DECOMPOSITION));
        VacuumGasOils = REGISTRATE.material("vacuum_gas_oils", builder -> builder
                .liquid()
                .color(0x42332B).secondaryColor(0x705547)
                .flags(DISABLE_DECOMPOSITION));
        WaxyDistillates = REGISTRATE.material("waxy_distillates", builder -> builder
                .liquid()
                .color(0xB39A64).secondaryColor(0xD8C18A)
                .flags(DISABLE_DECOMPOSITION));
        VacuumResiduals = REGISTRATE.material("vacuum_residuals", builder -> builder
                .liquid()
                .color(0x211C1B).secondaryColor(0x51433D)
                .flags(DISABLE_DECOMPOSITION));
        Bitumen = REGISTRATE.material("bitumen", builder -> builder
                .liquid()
                .color(0x181311).secondaryColor(0x40332D)
                .flags(DISABLE_DECOMPOSITION));
        PetroleumCoke = REGISTRATE.material("petroleum_coke", builder -> builder
                .dust()
                .color(0x24201D).secondaryColor(0x514A44)
                .flags(DISABLE_DECOMPOSITION));
        CalcinedPetroleumCoke = REGISTRATE.material("calcined_petroleum_coke", builder -> builder
                .dust()
                .color(0x171717).secondaryColor(0x393939)
                .flags(DISABLE_DECOMPOSITION));
        Syngas = REGISTRATE.material("syngas", builder -> builder
                .gas()
                .color(0xA7BAC2).secondaryColor(0xD7E5E9)
                .flags(DISABLE_DECOMPOSITION));
        WoodSpirit = REGISTRATE.material("wood_spirit", builder -> builder
                .liquid()
                .color(0xB58C55).secondaryColor(0xDEC18A)
                .flags(DISABLE_DECOMPOSITION));
        AcidicWoodLiquor = REGISTRATE.material("acidic_wood_liquor", builder -> builder
                .liquid()
                .color(0x8B684A).secondaryColor(0xB99971)
                .flags(DISABLE_DECOMPOSITION));
        LightTarOils = REGISTRATE.material("light_tar_oils", builder -> builder
                .liquid()
                .color(0x5D4130).secondaryColor(0x967052)
                .flags(DISABLE_DECOMPOSITION));
        PhenolicOils = REGISTRATE.material("phenolic_oils", builder -> builder
                .liquid()
                .color(0x493237).secondaryColor(0x795561)
                .flags(DISABLE_DECOMPOSITION));
        TarPitch = REGISTRATE.material("tar_pitch", builder -> builder
                .liquid()
                .color(0x21191A).secondaryColor(0x49383A)
                .flags(DISABLE_DECOMPOSITION));
        HotCokingVapors = REGISTRATE.material("hot_coking_vapors", builder -> builder
                .gas()
                .color(0x2D2925).secondaryColor(0x6B5A48)
                .flags(DISABLE_DECOMPOSITION));
        AmmoniaRichLiquor = REGISTRATE.material("ammonia_rich_liquor", builder -> builder
                .liquid()
                .color(0x735F3B).secondaryColor(0xB5A06C)
                .flags(DISABLE_DECOMPOSITION));
        NaphthaleneOils = REGISTRATE.material("naphthalene_oils", builder -> builder
                .liquid()
                .color(0x332C40).secondaryColor(0x655B78)
                .flags(DISABLE_DECOMPOSITION));
        Moondrop = REGISTRATE.material("moondrop", builder -> builder
                .formula("☽")
                .ore()
                .gem()
                .dust()
                .color(0xE8F1FF).secondaryColor(0x2A2F6E).iconSet(MaterialIconSet.EMERALD));
        Virtue = REGISTRATE.material("virtue_meld", builder -> builder
                .liquid(new FluidBuilder().temperature(666))
                .formula("߷")
                .element(CosmicElements.ViR));
        TriniumNaqide = REGISTRATE.material("trinium_naqide", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(3400))
                .color(0x6f42cf).secondaryColor(0xc71414).iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_FINE_WIRE)
                .components(Trinium, 2, NaquadahEnriched, 4)
                .blastTemp(3600, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.LuV], 1200));
        PrismaticTungstensteel = REGISTRATE.material("prismatic_tungstensteel", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(933))
                .color(0x6f42cf).secondaryColor(0xc71414).iconSet(CCoreMaterialIconSet.PRISMATIC)
                .flags(GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME,
                        GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .components(Prisma, 1, TungstenSteel, 1)
                .cableProperties(GTValues.V[4], 1, 1)
                .fluidPipeProperties(1166, 100, true)
                .blastTemp(3600, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        EnergeticAluminium = REGISTRATE.material("energetic_aluminium", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(933))
                .color(0xffb545).secondaryColor(0xe13923).iconSet(MaterialIconSet.METALLIC)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .components(Aluminium, 1, Redstone, 2, Glowstone, 2)
                .blastTemp(1700, BlastProperty.GasTier.LOW, GTValues.VA[GTValues.HV], 1200));

        VibrantAlloy = REGISTRATE.material("vibrant_alloy", builder -> builder
                .ingot()
                .formula(
                        "Au((CuAu4)(ZnCu3)Fe2(Ni(AuAgCu3)Fe3)4)?4(Ma2FeMnNi(Si(Fe2S2)5(Cr2(ZiAbAl)Hg3)(Ti2LPt)(BeK4N5)(CS)",
                        true)
                .liquid(new FluidBuilder().temperature(1100))
                .color(0xa4ff70).secondaryColor(0x24e048).iconSet(MaterialIconSet.METALLIC)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(3600, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        MelodicAlloy = REGISTRATE.material("melodic_alloy", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340))
                .color(0xc48aff).secondaryColor(0x6A3CBF).iconSet(MaterialIconSet.METALLIC)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .components(BlackSteel, 2, Moondrop, 4, EnderPearl, 2, Hafnium, 3)
                .blastTemp(4500, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.IV], 2430));

        SuperheavyBedrockAlloy = REGISTRATE.material("superheavy_bedrock_alloy", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340))
                .color(0x2F2C3A).secondaryColor(0x424145).iconSet(MAGIC)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(4500, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        Nostium = REGISTRATE.material("nostium", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340).customStill())
                .iconSet(CosmicMaterialSet.NOSTIUM)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(1700, BlastProperty.GasTier.LOW, GTValues.VA[GTValues.LV], 1200));

        GalvanizedEthersteel = REGISTRATE.material("galvanized_ethersteel", builder -> builder
                .ingot()
                .liquid()
                .element(CosmicElements.EtherSteel)
                .color(0xEB4258).secondaryColor(0x8F0E5C)
                .iconSet(CosmicMaterialSet.GALVANIZED_ETHERSTEEL)
                .cableProperties(GTValues.V[GTValues.HV], 4, 4, false)
                .flags(GENERATE_PLATE, GENERATE_BOLT_SCREW, GENERATE_ROD, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FOIL, GENERATE_FINE_WIRE, NO_SMELTING, GENERATE_FRAME, GENERATE_GEAR,
                        GENERATE_SMALL_GEAR));

        DraconicTarragon = REGISTRATE.material("draconic_tarragon", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340))
                .color(0x86045).secondaryColor(0xA0684E).iconSet(MAGIC)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(4500, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        Kuvite = REGISTRATE.material("kuvite", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340).color(0xff6a4d))
                .iconSet(CosmicMaterialSet.KUVITE)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(4500, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        BlinkwardAlloy = REGISTRATE.material("blinkward_alloy", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340))
                .color(0xb4c267).secondaryColor(0x2abd6c).iconSet(CosmicMaterialSet.CRYSTAL)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(4500, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        StarstruckHalite = REGISTRATE.material("starstruck_halite", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340))
                .fluid(FluidStorageKeys.MOLTEN, new FluidBuilder().temperature(4500))
                .color(0x45a2ff).secondaryColor(0x8ad8f5).iconSet(CosmicMaterialSet.CRYSTAL)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(4500, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        Lumium = REGISTRATE.material("lumium", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340))
                .color(0xf6ff99).secondaryColor(0xff7400).iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(4500, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        Signalum = REGISTRATE.material("signalum", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340))
                .color(0xff6b0f).secondaryColor(0xc32e00).iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(4500, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        Enderium = REGISTRATE.material("enderium", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340))
                .color(0x1f6b62).secondaryColor(0x16455f).iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(4500, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        Tenbrium = REGISTRATE.material("tenbrium", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340))
                .color(0x8a8a8a).secondaryColor(0x404040).iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(4500, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        Halizine = REGISTRATE.material("halizine", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340))
                .color(0x8b1a7d).secondaryColor(0x3d0835).iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(4500, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        Rosmotosin = REGISTRATE.material("rosmotosin", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1340))
                .color(0xdc143c).secondaryColor(0x8b0000).iconSet(CosmicMaterialSet.CRYSTAL)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .blastTemp(4500, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        VoidSpark = REGISTRATE.material("voidspark", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(933))
                .formula("\uD802\uDC05")
                .color(0x5b2a9c).secondaryColor(0x2567b8).iconSet(CCoreMaterialIconSet.VOIDSPARKICO)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .components(Prisma, 1, TungstenSteel, 1)
                .blastTemp(3600, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        SolSteel = REGISTRATE.material("sol_steel", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(933))
                .formula(" ⃘ ")
                .color(0xf0891a).secondaryColor(0xf0891a).iconSet(CCoreMaterialIconSet.SOL_STEEL)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .components(Prisma, 1, TungstenSteel, 1)
                .cableProperties(GTValues.V[8], 4, 1)
                .fluidPipeProperties(65000, 9500, true, true, true, true)
                .blastTemp(3600, BlastProperty.GasTier.HIGH, GTValues.VA[GTValues.EV], 1200));

        StarMetal = REGISTRATE.material("starmetal", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(9800))
                .formula("✬")
                .iconSet(CCoreMaterialIconSet.STARMETALICO)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .cableProperties(GTValues.V[8], 4, 1)
                .fluidPipeProperties(65000, 9500, true, true, true, false)
                .blastTemp(8400, BlastProperty.GasTier.HIGHEST, GTValues.VA[GTValues.EV], 1200));

        Nevramite = REGISTRATE.material("nevramite", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(9800))
                .plasma(109000)
                .formula("✫")
                .color(0x372536)
                .iconSet(CosmicMaterialSet.NEVRAMITE)
                .components(Osmium, 8, Gadolinium, 3, Rhenium, 3, Molybdenum, 2, ActivatedCarbon, 1, StarMetal, 1)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .cableProperties(GTValues.V[8], 4, 1)
                .fluidPipeProperties(65000, 9500, true, true, true, false)
                .blastTemp(8400, BlastProperty.GasTier.HIGHEST, GTValues.VA[GTValues.EV], 1200));

        ResonantVirtueMeld = REGISTRATE.material("resonant_virtue_meld", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1240))
                .color(0x1acde1).secondaryColor(0x00935d).iconSet(MaterialIconSet.SHINY)
                .flags(GENERATE_ROTOR, GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR,
                        GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .components(Virtue, 4, Naquadah, 1, TungstenSteel, 15, Chromium, 3, Molybdenum, 6, Vanadium, 3, Prisma,
                        8)
                .cableProperties(GTValues.V[5], 4, 6)
                .blastTemp(4200, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.IV], 1800)
                .formula("ERROR4Nq((FeW)5CrMo2V)3Pi8"));
        NaquadicSuperalloy = REGISTRATE.material("naquadric_superalloy", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1300))
                .color(0x001f04).secondaryColor(0x000000).iconSet(MaterialIconSet.RADIOACTIVE)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .components(BlackSteel, 4, NaquadahEnriched, 3, RhodiumPlatedPalladium, 6)
                .cableProperties(GTValues.V[6], 4, 6)
                .blastTemp(5400, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.LuV], 2400));
        // TODO: Pack Progression Materials in core when needed. For now things below this are sorted out well enough -
        // I don't really feel like having to write 10000000 elements and placeholder materials just to do component
        // based auto-gen rn
        Trinavine = REGISTRATE.material("trinavine", builder -> builder
                .ingot()
                .formula("⋖Tv⋗")
                .liquid(new FluidBuilder().temperature(1500))
                .color(0xfe2d2d).secondaryColor(0x353535).iconSet(CosmicMaterialSet.TRINAVINE)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DENSE)
                .cableProperties(GTValues.V[7], 4, 3)
                .toolStats(ToolProperty.Builder.of(16, 40, 8192, 6, MeldingOmniTool.MELD_TOOL_LUV).magnetic()
                        .unbreakable().build())
                .blastTemp(5400, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.LuV], 900));

        LivingIgniclad = REGISTRATE.material("living_igniclad", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1760))
                .color(0x940002).secondaryColor(0xe35e17)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[8], 4, 6)
                .blastTemp(9000, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.UV], 5600));
        PsionicGalvorn = REGISTRATE.material("psionic_galvorn", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1760))
                .color(0x004094).secondaryColor(0x0962ab)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[9], 4, 6)
                .blastTemp(10500, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.UV], 5600));
        ProgrammableMatter = REGISTRATE.material("programmable_matter", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1760))
                .color(0x003e4f).secondaryColor(0x0a2830)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[10], 4, 6)
                .blastTemp(13000, BlastProperty.GasTier.HIGHEST, GTValues.VA[GTValues.UHV], 5600));
        ShimmeringNeutronium = REGISTRATE.material("shimmering_neutronium", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1760))
                .color(0xc7e9f2).secondaryColor(0xffffff)
                .flags(GENERATE_BOLT_SCREW, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME,
                        GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[11], 4, 6)
                .blastTemp(15000, BlastProperty.GasTier.HIGHEST, GTValues.VA[GTValues.UEV], 5600));
        CausalFabric = REGISTRATE.material("causal_fabric", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(1760))
                .color(0x270054).secondaryColor(0x481c7a)
                .flags(GENERATE_BOLT_SCREW, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME,
                        GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[11], 4, 6)
                .blastTemp(19000, BlastProperty.GasTier.HIGHEST, GTValues.VA[GTValues.UIV], 5600));
        Triphenylphosphine = REGISTRATE.material("triphenylphosphine", builder -> builder
                .liquid(new FluidBuilder().temperature(300))
                .color(0xfca103).secondaryColor(0xfcbe03).iconSet(MaterialIconSet.BRIGHT));
        // Misc Materials
        Neutronite = REGISTRATE.material("neutronite", builder -> builder
                .ingot()
                .formula("⦽")
                .blastTemp(8900)
                .liquid(new FluidBuilder().temperature(7500).customStill())
                .cableProperties(GTValues.V[6], 12, 4)
                .fluidPipeProperties(65000, 3500, true, true, true, true)
                .iconSet(CCoreMaterialIconSet.VIBRANIUM)// .color(0x489BC3).secondaryColor(0x51ADCA)
                .toolStats(ToolProperty.Builder.of(90.0F, 50.0F, 65535, 6)
                        .attackSpeed(0.5F).enchantability(33).magnetic().unbreakable().build())
                .flags(
                        GENERATE_PLATE,
                        GENERATE_DENSE,
                        GENERATE_ROD,
                        GENERATE_BOLT_SCREW,
                        GENERATE_FRAME,
                        GENERATE_GEAR,
                        GENERATE_SMALL_GEAR,
                        GENERATE_SPRING,
                        GENERATE_SPRING_SMALL,
                        GENERATE_FOIL,
                        GENERATE_RING,
                        GENERATE_SPRING,
                        GENERATE_SPRING_SMALL,
                        GENERATE_SMALL_GEAR,
                        GENERATE_FINE_WIRE,
                        GENERATE_ROTOR,
                        GENERATE_ROUND));
        // TODO - Colors , Textures, Fluid Textures, they're all gonna look the same in game for now.
        // TODO - Infinity Line Scripts On KubeJS side.
        // Potential = REGISTRATE.material("potential", builder -> builder
        // .dust()
        // .liquid(new FluidBuilder().temperature(1500))
        // .color(0x828282).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.BRIGHT)
        // .flags(DISABLE_DECOMPOSITION)
        // );
        // FalseInfinity = REGISTRATE.material("false_infinity", builder -> builder
        // .liquid(new FluidBuilder().temperature(3500))
        // .color(0x508582).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.BRIGHT)
        // .flags(DISABLE_DECOMPOSITION)
        // );
        // SuitableInfinity = REGISTRATE.material("suitable_infinity", builder -> builder
        // .liquid(new FluidBuilder().temperature(7900))
        // .color(0x508582).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.BRIGHT)
        // .flags(DISABLE_DECOMPOSITION)
        // );
        // TODO - Actual Textures for the entire material set... Let's do something that isn't the typical rainbow
        // infinity, maybe make it something special.
        LogicalInfinity = REGISTRATE.material("infinity", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(10750))
                .iconSet(CosmicMaterialSet.INFINITY)
                .flags(GENERATE_ROTOR, GENERATE_FRAME, GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR,
                        GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[7], 16, 0, true));
        // TODO - Taranium Bedrock Soot + Adamantium Processing
        Taranium = REGISTRATE.material("taranium", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(18000))
                .color(0x383838).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[12], 64, 0, true));
        // Todo Temporal Tear
        Chronon = REGISTRATE.material("chronon", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(100000000))
                .iconSet(CCoreMaterialIconSet.CHRONIC)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[12], 64, 0, true));
        // ZPM Magnet Material
        // the current tier, but super easy after lol
        Temmerite = REGISTRATE.material("temmerite", builder -> builder
                .ingot()
                .formula("⦽")
                .liquid(new FluidBuilder().temperature(19500))
                .color(0x42f5a1).secondaryColor(0x42f5e0).iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[12], 64, 0, true));
        // If For some reason using hebrew as denotations for absolute constants upsets people, go back to using Greek,
        // I doubt it'll be an issue but denote this just incase.
        // TODO - Thaumic Tinker Inspired chaos Ft Virtue Meld, Make your dead gods cry rivers of mutated blood -
        // Resistant to Acausal decay and contains the Universe constance of "Dalet" (ד)
        Ichor = REGISTRATE.material("ichor", builder -> builder
                .formula("⦿")
                .liquid(new FluidBuilder().temperature(19500))
                .color(0xfca103).secondaryColor(0xfcbe03).iconSet(MaterialIconSet.BRIGHT));
        // TODO - The Finalized Version of Virtue Meld, only present on Grand Virtues. Resistant to Atemporal decay and
        // contains the Universe constance of "Alef" (א)
        Ichorium = REGISTRATE.material("ichorium", builder -> builder
                .formula("⦾")
                .ingot()
                .liquid(new FluidBuilder().temperature(19500))
                .color(0xfca103).secondaryColor(0xfcbe03).iconSet(MaterialIconSet.BRIGHT)
                .flags(GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                        GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE)
                .cableProperties(GTValues.V[12], 64, 0, true));
        // TODO - A ????? of nothing from the empty space between each instance of cylindrical multiverses. Dissolves
        // the Constant of "Resh" (ר)
        Cosmocite = REGISTRATE.material("cosmocite", builder -> builder
                .liquid(new FluidBuilder().temperature(12123500))
                .color(0xdb5e5e).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.SHINY));
        // TODO - A ????? of ???? comprised of the collective agony of Universe 0S37 (I keep forgetting the descriptor
        // for the main universe the game is in) . Dissolves the Constant of "Waw/Vav" (ו)
        TearsOfTheUniverse = REGISTRATE.material("universe_tears", builder -> builder
                .liquid(new FluidBuilder().temperature(1))
                .color(0xdb5e5e).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.SHINY));

        Utherium = REGISTRATE.material("utherium", builder -> builder
                .gem()
                .ore()
                .formula("ThSiO4")
                .color(0xC3434C).secondaryColor(0x7A2430).iconSet(CosmicMaterialSet.UTHERIUM)
                .flags(GENERATE_PLATE, GENERATE_BOLT_SCREW, GENERATE_ROD, GENERATE_SPRING,
                        GENERATE_SPRING_SMALL, GENERATE_FOIL, NO_SMELTING, NO_ORE_SMELTING,
                        DISABLE_DECOMPOSITION));
        Aphotite = REGISTRATE.material("aphotite", builder -> builder
                .dust()
                .formula("K3Na(SO4)2")
                .color(0x86BF54).secondaryColor(0x4E7A2E).iconSet(MaterialIconSet.ROUGH)
                .flags(DISABLE_DECOMPOSITION));
        Arcanite = REGISTRATE.material("arcanite", builder -> builder
                .gem()
                .formula("K2NaYAl2Si4O12F4")
                .color(0x8C6AE8).secondaryColor(0x5A3FA8).iconSet(MaterialIconSet.CERTUS)
                .flags(DISABLE_DECOMPOSITION));
        Gloomarcine = REGISTRATE.material("gloomarcine", builder -> builder
                .dust()
                .formula("Mg4Si6O15(OH)2(H2O)6")
                .color(0x4F6D8C).secondaryColor(0x2E4258).iconSet(MaterialIconSet.DULL)
                .flags(DISABLE_DECOMPOSITION));
        Veilspar = REGISTRATE.material("veilspar", builder -> builder
                .gem()
                .formula("KAlSi3O8")
                .color(0xC9D8E8).secondaryColor(0x8FA6BC).iconSet(MaterialIconSet.QUARTZ)
                .flags(DISABLE_DECOMPOSITION));
        Bathyst = REGISTRATE.material("bathyst", builder -> builder
                .dust()
                .formula("BaTiO3")
                .color(0x6E5A8C).secondaryColor(0x453466).iconSet(MaterialIconSet.DULL)
                .flags(DISABLE_DECOMPOSITION));
        MaterialRegistryEntry nyctophytate = REGISTRATE.material("nyctophytate", builder -> builder
                .formula("C8H10NO6S")
                .color(0x8C7A5A).secondaryColor(0x5E4E38)
                .flags(DISABLE_DECOMPOSITION));
        Nyctophyte = REGISTRATE.material("nyctophyte", builder -> builder
                .dust()
                .formula("Ca(C8H10NO6S)2")
                .color(0x8C7A5A).secondaryColor(0x5E4E38).iconSet(MaterialIconSet.SAND)
                .components(Calcium, 1, nyctophytate, 2)
                .flags(DISABLE_DECOMPOSITION));
        Hadalite = REGISTRATE.material("hadalite", builder -> builder
                .gem()
                .formula("Tl2S3(MgFeSi2O4)4(H2O)")
                .color(0x5A3F7A).secondaryColor(0x241640).iconSet(MaterialIconSet.SHINY)
                .flags(DISABLE_DECOMPOSITION));
        Abyssbloom = REGISTRATE.material("abyssbloom", builder -> builder
                .dust()
                .formula("Fe3(PO4)2(H2O)8")
                .color(0x3F4E7A).secondaryColor(0x141E38).iconSet(MaterialIconSet.ROUGH)
                .flags(DISABLE_DECOMPOSITION));
        // TODO - A dangerous bioweapon unleashed on many orbital bodies in space, entirely designed to sterilize life
        // from anything it touches.
        // Oh it's also like, self-aware but not fully sentient. Only works on impulse kinda - I'm not lore dumping more
        // here lmfao
        StygianPlague = REGISTRATE.material("stygian_plague", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill().temperature(240))
                .color(0x4f035e).iconSet(MaterialIconSet.SHINY));
        // TODO - Now it's mad. Now it's airborne vs waterborne, and it knows how to mentally abuse you.
        EnragedStygianPlague = REGISTRATE.material("enraged_stygian_plague", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill().temperature(350))
                .color(0x5e031d).iconSet(MaterialIconSet.SHINY));
        // TODO - OMG! ! ! ! GTNH ! ! ! !?!?!?! SPACE!?!?!TIMEE?E!?!?!?!?! WOEAEAOWE SO FUNNY, Fuck off - Make this part
        // of the fuel for the Temporal Tear.
        Spacetime = REGISTRATE.material("spacetime", builder -> builder
                .liquid(new FluidBuilder().temperature(10))
                .color(0xdb5e5e).secondaryColor(0x5e0f3d).iconSet(MaterialIconSet.SHINY));

        PaleSteel = REGISTRATE.material("pale_steel", builder -> builder
                .formula("✧⚙")
                .ingot()
                .liquid(new FluidBuilder().temperature(50))
                .fluid(FluidStorageKeys.MOLTEN, new FluidBuilder().temperature(1800))
                .flags(GENERATE_ROTOR, GENERATE_FRAME, GENERATE_BOLT_SCREW, GENERATE_ROUND, GENERATE_GEAR,
                        GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FINE_WIRE)
                .color(0xE8F3FF).secondaryColor(0x1F2A36).iconSet(MaterialIconSet.METALLIC));
        PaleOil = REGISTRATE.material("pale_oil", builder -> builder
                .formula("✧⚙")
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .color(0xE8F3FF).secondaryColor(0x1F2A36).iconSet(MaterialIconSet.METALLIC));
        PrimordialSoup = REGISTRATE.material("primordial_soup", builder -> builder
                .liquid(new FluidBuilder().state(FluidState.LIQUID).temperature(320))
                .color(0x6F568F).secondaryColor(0xB9A8CF).iconSet(MaterialIconSet.DULL));

        Somanone = REGISTRATE.material("somanone", builder -> builder
                .formula("✧+")
                .ore()
                .gem()
                .dust()
                .color(0x36E3C3).secondaryColor(0x111827).iconSet(MaterialIconSet.DIAMOND));

        // TODO(cosmiccore): re-add a PLASMA fluid to Chlorine. In GTCEu 8.0 CC cannot do this via
        // enqueueRegistration at all: GTCEu both creates materials (initMaterials) AND freezes every material's
        // fluid storage (GTFluids.init -> registered=true) inside its OWN single RegisterEvent handler, which
        // runs before CC's RegisterEvent and before PostMaterialEvent - so by ANY CC hook Chlorine's storage is
        // already frozen ("Cannot enqueue a builder after registration"). Needs a different path (a standalone
        // plasma fluid, or a GTCEu-side material-modification mechanism). Removed for now to let the client boot.
    }

    public static void modifyMaterials() {
        ConductorAmpacityRules.applyMaterialBaseAmperages();
        var rubyOreProp = Ruby.getProperty(PropertyKey.ORE);
        var ilmeniteOreProp = Ilmenite.getProperty(PropertyKey.ORE);
        var aluminiumOreProp = Aluminium.getProperty(PropertyKey.ORE);
        var bauxiteOreProp = Bauxite.getProperty(PropertyKey.ORE);
        var nickelOreProp = Nickel.getProperty(PropertyKey.ORE);
        var chromiteOreProp = Chromite.getProperty(PropertyKey.ORE);

        rubyOreProp.setOreByProducts(Cinnabar, GarnetRed, GarnetRed);
        ilmeniteOreProp.setOreByProducts(Iron, Ilmenite, Ilmenite);
        aluminiumOreProp.setOreByProducts(Bauxite, Bauxite, Ilmenite, Ilmenite);
        bauxiteOreProp.setOreByProducts(Grossular, Gallium, Gallium);
        nickelOreProp.setOreByProducts(Cobalt, Iron, Cooperite);
        nickelOreProp.setWashedIn(Mercury);
        chromiteOreProp.setOreByProducts(Iron, Magnesium, Chromite);
        Holmium.value().setMaterialARGB(0xf07a96);
        Holmium.value().setMaterialSecondaryARGB(0xfc9db3);
        Neutronium.value().getMaterialInfo().setIconSet(CCoreMaterialIconSet.VIBRANIUM_NEUTRONIUM);
        Neutronium.value().addFlags(NO_SMELTING, NO_ORE_SMELTING);
        Neutronium.setProperty(PropertyKey.BLAST, new BlastProperty(15000));

        addPeriodicTableFluids();
    }

    /**
     * Pack KubeJS (Periodic_table.js) used to enqueue these fluids onto stock GTCEu materials, but GTCEu 8.0 freezes
     * every material's fluid storage during GTFluids#init - before any KubeJS material hook runs - so the enqueue threw
     * "Cannot enqueue a builder after registration". PostMaterialEvent (this method's caller) is the pre-freeze window
     * GTCEu itself uses for the same purpose (see AlloyBlastPropertyAddition), so the additions live here instead.
     */
    private static void addPeriodicTableFluids() {
        for (MaterialRegistryEntry material : new MaterialRegistryEntry[] { Gold, Europium, Copper, Aluminium,
                Tritanium }) {
            enqueueFluid(material, FluidStorageKeys.PLASMA, new FluidBuilder().state(FluidState.PLASMA));
        }
        for (MaterialRegistryEntry material : new MaterialRegistryEntry[] { Naquadah, NaquadahEnriched, Naquadria }) {
            enqueueFluid(material, FluidStorageKeys.MOLTEN, new FluidBuilder().state(FluidState.LIQUID));
        }
        for (MaterialRegistryEntry material : new MaterialRegistryEntry[] { Agar, Scandium, Strontium, Caesium,
                Francium, Radium, Actinium,
                Zirconium, Hafnium, Technetium, Rhenium, Bohrium, Rubidium, Rutherfordium, Dubnium, Seaborgium, Hassium,
                Praseodymium, Protactinium, Promethium, Neptunium, Meitnerium, Roentgenium, Copernicium, Thallium,
                Nihonium, Flerovium, Moscovium, Gadolinium, Curium, Terbium, Berkelium, Dysprosium, Californium,
                Einsteinium, Fermium, Mendelevium, Nobelium, Lawrencium, Holmium, Erbium, Thulium, Ytterbium, Germanium,
                Livermorium, Tennessine, Oganesson, Polonium, Astatine, Tellurium, Selenium, Salt }) {
            enqueueFluid(material, FluidStorageKeys.LIQUID, new FluidBuilder());
        }
    }

    private static void enqueueFluid(MaterialRegistryEntry material, FluidStorageKey key, FluidBuilder builder) {
        FluidProperty existing = material.getProperty(PropertyKey.FLUID);
        if (existing != null) {
            try {
                existing.enqueueRegistration(key, builder);
            } catch (RuntimeException alreadyRegisteredOrQueued) {
                CosmicCore.LOGGER.debug("Skipping {} fluid for {} (already provided/frozen by GTCEu)", key, material);
            }
            return;
        }
        FluidProperty created = new FluidProperty();
        try {
            created.enqueueRegistration(key, builder);
        } catch (RuntimeException cannotEnqueue) {
            CosmicCore.LOGGER.debug("Skipping {} fluid for {} (could not enqueue)", key, material);
            return;
        }
        material.setProperty(PropertyKey.FLUID, created);
    }
}
