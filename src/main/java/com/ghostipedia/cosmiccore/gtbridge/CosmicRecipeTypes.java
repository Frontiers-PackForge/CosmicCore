package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.api.capability.recipe.EmberRecipeCapability;
import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import com.ghostipedia.cosmiccore.common.data.CosmicSounds;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.BloomwyrmRecipeKeys;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.builder.GTRecipeTypeBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.providers.ProviderType;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicSounds.*;
import static com.gregtechceu.gtceu.common.data.GCYMRecipeTypes.ALLOY_BLAST_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class CosmicRecipeTypes {

    private static GTRecipeTypeBuilder recipeType(String id, String group) {
        return REGISTRATE.recipeType(id, group).setData(ProviderType.LANG, (context, provider) -> {});
    }

    public static final GTRecipeTypeEntry LEYLINE_FABRICATION = recipeType("leyline_fabrication", MULTIBLOCK)
            .setMaxIOSize(0, 0, 0, 0)
            .setSound(GTSoundEntries.COMPRESSOR)
            .register();

    public static final GTRecipeTypeEntry MECHANICAL_MINESHAFT = recipeType("mechanical_mineshaft", MULTIBLOCK)
            .setMaxIOSize(4, 4, 4, 4)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry ESSENCE_REACTOR = recipeType("essence_reactor", MULTIBLOCK)
            .setMaxIOSize(4, 4, 4, 4)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry SLUDGE_DIGESTOR = recipeType("sludge_digestor", MULTIBLOCK)
            .setMaxIOSize(3, 3, 2, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry POWDERIZER = recipeType("powderizer", MULTIBLOCK)
            .setMaxIOSize(2, 2, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry INDUSTRIAL_ORE_SORTER = recipeType("industrial_ore_sorter",
            MULTIBLOCK)
            .setMaxIOSize(1, 6, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry INDUSTRIAL_FLOTATION_PLANT = recipeType("industrial_flotation_plant",
            MULTIBLOCK)
            .setMaxIOSize(2, 2, 2, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry ONEIRIC_SIEVE = recipeType("oneiric_sieve", MULTIBLOCK)
            .setMaxIOSize(3, 3, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry DISSOLUTION_VAT = recipeType("dissolution_vat", MULTIBLOCK)
            .setMaxIOSize(2, 2, 2, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry PHASE_SEPARATOR = recipeType("phase_separator", MULTIBLOCK)
            .setSound(GTSoundEntries.CENTRIFUGE)
            .setMaxIOSize(1, 0, 2, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry SIMPLE_DESALTER = recipeType("simple_desalter", ELECTRIC)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(0, 0, 3, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry DESALTER = recipeType("desalter", MULTIBLOCK)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(0, 0, 3, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry STEAM_CRACKING_FURNACE = recipeType("steam_cracking_furnace", MULTIBLOCK)
            .setSound(GTSoundEntries.FIRE)
            .setMaxIOSize(1, 0, 2, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry FRACTIONAL_CONDENSER = recipeType("fractional_condenser", MULTIBLOCK)
            .setSound(GTSoundEntries.COOLING)
            .setMaxIOSize(0, 1, 1, 5)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry FLUID_CATALYTIC_CRACKING = recipeType("fluid_catalytic_cracking", MULTIBLOCK)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(2, 2, 3, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry HYDROTREATING = recipeType("hydrotreating", MULTIBLOCK)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(2, 2, 3, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry HYDROCRACKING = recipeType("hydrocracking", MULTIBLOCK)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(2, 2, 3, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry CATALYTIC_REFORMING = recipeType("catalytic_reforming", MULTIBLOCK)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(2, 2, 3, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry DELAYED_COKING = recipeType("delayed_coking", MULTIBLOCK)
            .setSound(GTSoundEntries.FIRE)
            .setMaxIOSize(2, 2, 3, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry VACUUM_DISTILLATION = recipeType("vacuum_distillation", MULTIBLOCK)
            .setSound(GTSoundEntries.COOLING)
            .setMaxIOSize(0, 1, 1, 12)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry COSMIC_DUMMY_SPAM_YEETER = recipeType("fuckassbeeball", ELECTRIC)
            .setMaxIOSize(54, 54, 54, 54)
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry LAMINATOR = recipeType("laminator", ELECTRIC)
            .setSound(CosmicSounds.LAMINATOR)
            .setMaxIOSize(3, 2, 2, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry AIO_LITHOGRAPHY_PROCESSOR = recipeType("aio_lithography_processor", ELECTRIC)
            .setSound(GTSoundEntries.COOLING)
            .setMaxIOSize(6, 3, 2, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry CHEMICAL_DEHYDRATOR = recipeType("chemical_dehydrator", ELECTRIC)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(2, 9, 2, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry CRYSTALLIZER = recipeType("crystallizer", ELECTRIC)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(0, 1, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CRYSTALLIZATION))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry DAWNFORGE_ECLIPSED = recipeType("eclipsed_dawnforge", ELECTRIC)
            .setSound(CosmicSounds.LAMINATOR)
            .setMaxIOSize(12, 1, 3, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry VORAX = recipeType("vorax", ELECTRIC)
            .setSound(CosmicSounds.VOARX)
            .setMaxIOSize(1, 0, 4, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry MANA_FLUIDIZER = recipeType("mana_fluidizer", ELECTRIC)
            .setMaxIOSize(1, 1, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry PCB_FABRICATOR = recipeType("pcb_fab", ELECTRIC)
            .setMaxIOSize(8, 1, 4, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry TITAN_FUSION_RECIPES = recipeType("titan_fusion", ELECTRIC)
            .setMaxIOSize(3, 3, 3, 6)
            .setSound(GTSoundEntries.REPLICATOR)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry LUNAR_HAMMER = recipeType("lunar_hammer", ELECTRIC)
            .setMaxIOSize(3, 3, 2, 2)
            .setSound(GTSoundEntries.JET_ENGINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry CRYOGENICS_CHAMBER = recipeType("cryo_chamber", ELECTRIC)
            .setMaxIOSize(6, 3, 3, 6)
            .setSound(GTSoundEntries.JET_ENGINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry SOUL_TESTER_RECIPES = recipeType("soul_tester", GTRecipeTypes.MULTIBLOCK)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxIOSize(1, 1, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    /*
     * public static final GTRecipeType EMBER_TESTER_RECIPES = GTRecipeTypes
     * .register(CosmicCore.id("ember_tester"), GTRecipeTypes.MULTIBLOCK)
     * .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
     * .setMaxSize(IO.OUT, EmberRecipeCapability.CAP, 1)
     * .setMaxIOSize(1, 1, 0, 0)
     * .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
     */

    public static final GTRecipeTypeEntry VOID_MINER = recipeType("void_miner", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 27, 2, 0)
            .setSound(MINING_MACHINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry HEAVY_ASSEMBLER = recipeType("heavy_assembler", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(12, 3, 6, 0)
            .setSound(HEAVY_ASSEM)
            // TODO(8.0.0): custom CosmicGuiTextures.PROGRESS_BAR_HEAVY (LDLib ResourceTexture) has no
            // ProgressBarTextureSet equivalent; using PROGRESS_ASSEMBLER as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ASSEMBLER))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry PLASMITE_FORGE = recipeType("plasmite_forge", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 3, 3, 3)
            .setSound(HEAVY_ASSEM)
            // TODO(8.0.0): custom CosmicGuiTextures.PROGRESS_BAR_HEAVY (LDLib ResourceTexture) has no
            // ProgressBarTextureSet equivalent; using PROGRESS_ASSEMBLER as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ASSEMBLER))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry PRISMA_FOUNDRY = recipeType("prisma_foundry", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 0)
            .setSound(MINING_MACHINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry ATMOSPHERE_SIPHON = recipeType("atmo_siphon", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 0, 4, 16)
            .setSound(GAS_SUCC)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry MANA_DIGITIZER = recipeType("mana_digitizer", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 2, 2)
            .setSound(GAS_SUCC)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry COMPONENT_ASSEMBLY_LINE = recipeType("component_assembly_line",
            GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(16, 1, 4, 2)
            .setSound(GAS_SUCC)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry GROVE_RECIPES = recipeType("drygmy_grove", GTRecipeTypes.MULTIBLOCK)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxIOSize(2, 9, 1, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES = recipeType(
            "industrial_primitive_blast_furnace", MULTIBLOCK)
            .setMaxIOSize(3, 3, 1, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setSound(GTSoundEntries.FIRE).register();
    public static final GTRecipeTypeEntry LEACHING_PLANT = recipeType("leaching_plant", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry HELLFIRE_FOUNDRY = recipeType("hellfire_foundry", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(5, 1, 1, 0)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry SUFFERING_CHAMBER = recipeType("suffering_chamber", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 0, 0)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry ARCANE_DISTILLERY = recipeType("arcane_distillery", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 3, 2)
            .setSound(CosmicSounds.ARCANE_DISTIL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry ARCANE_FOLDING = recipeType("arcane_folding", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 1, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry POLYMERIZER = recipeType("polymerizer", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 3, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry HEMOPHAGIC_TRANSFUSER = recipeType("hemophagic_transfuser",
            GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 3, 3)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry IMBUMENT_PYLON = recipeType("imbument_pylon", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 3, 3)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .addRecipeUIModifier(VitaeCampusRecipeUI.ALTAR_TIER))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry CHROMATIC_FLOTATION_PLANT = recipeType("chromatic_flotation_plant",
            GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 4, 3, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry SPIRIT_CRUCIBLE = recipeType("spirit_crucible", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 0)
            .setSound(ARCANE_DISTIL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry SOUL_FOUNDRY = recipeType("soul_foundry", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 1, 3, 1)
            .setSound(CosmicSounds.LAMINATOR)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry CALX_REACTOR = recipeType("calx_reactor", ELECTRIC)
            .setMaxIOSize(2, 2, 1, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry ROASTER = recipeType("roaster", ELECTRIC)
            .setMaxIOSize(2, 3, 1, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry MANA_LEACHING_TUB = recipeType("mana_leaching_tub", ELECTRIC)
            .setMaxIOSize(1, 1, 2, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry THERMOMAG = recipeType("thermomagnitizer", ELECTRIC)
            .setMaxIOSize(3, 2, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry VAC_BUBBLER = recipeType("vacuum_bubbler", ELECTRIC)
            .setMaxIOSize(2, 3, 2, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry LARGE_ROASTER = recipeType("large_roaster", ELECTRIC)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setMaxIOSize(4, 4, 4, 4)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setRecipeViewerLayoutGridBuilder(ItemRecipeCapability.CAP, IO.IN,
                            layout -> new String[] { "ss", "ss" })
                    .setRecipeViewerLayoutGridBuilder(ItemRecipeCapability.CAP, IO.OUT,
                            layout -> new String[] { "ss", "ss" })
                    .setRecipeViewerLayoutGridBuilder(FluidRecipeCapability.CAP, IO.IN,
                            layout -> new String[] { "ss", "ss" })
                    .setRecipeViewerLayoutGridBuilder(FluidRecipeCapability.CAP, IO.OUT,
                            layout -> new String[] { "ss", "ss" }))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry VILE_FISSION = recipeType("vile_fission", ELECTRIC)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setMaxIOSize(1, 1, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry VOID_SALT_FISSION = recipeType("void_salt_fission", ELECTRIC)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setMaxIOSize(2, 3, 2, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry RADBOLT_RECONSTRUCTOR = recipeType("reconstructor", ELECTRIC)
            .setMaxIOSize(3, 2, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry ORBITAL_FORGE_EBF = recipeType("orbital_forge", GTRecipeTypes.MULTIBLOCK)
            .setSound(CosmicSounds.ORBITAL_FORGE)
            .setHasResearchSlot(true)
            .setMaxIOSize(3, 3, 3, 3)
            // TODO(8.0.0): PROGRESS_BAR_ARC_FURNACE had no ProgressBarTextureSet; closest substitute
            // 8.0.0 removed GTRecipeType.addDataInfo(...) -> data infos registered in init() via
            // getDataInfos().add(...)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_FUSION))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry ORBITAL_FORGE_ABS = recipeType("orbital_forge_abs", GTRecipeTypes.MULTIBLOCK)
            .setSound(CosmicSounds.ORBITAL_FORGE)
            .setHasResearchSlot(true)
            .setMaxIOSize(9, 3, 3, 3)
            // TODO(8.0.0): PROGRESS_BAR_COKE_OVEN had no ProgressBarTextureSet; closest substitute
            // 8.0.0 removed GTRecipeType.addDataInfo(...) -> data infos registered in init() via
            // getDataInfos().add(...)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry ALLOY_BLASTING_KILN = recipeType("alloy_blasting_kiln",
            GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(9, 0, 3, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder
                    .setItemSlotsOverlay(IO.IN, 0, 8, GTGuiTextures.FURNACE_OVERLAY_1)
                    .setFluidSlotsOverlay(IO.IN, 0, 2, GTGuiTextures.FURNACE_OVERLAY_2)
                    .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.FURNACE_OVERLAY_2)
                    .setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .addRecipeUIModifier(AlloyBlastingKilnRecipeUI.COIL_INFO)
                    .addRecipeUIModifier(AlloyBlastingKilnRecipeUI.PYROFLUX_INFO))
            .setSound(GTSoundEntries.ARC)
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry DAWN_FORGE = recipeType("dawn_forge", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(8, 1, 2, 0)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setSound(DAWN_FORGE_SFX)
            // TODO(8.0.0): custom CosmicGuiTextures.DAWN_FORGE (LDLib ResourceTexture) has no
            // ProgressBarTextureSet equivalent; using PROGRESS_ARROW_MULTIPLE as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry CINDER_HEARTH = recipeType("cinder_hearth", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 0, 3, 3)
            .setMaxSize(IO.OUT, EmberRecipeCapability.CAP, 1)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setSound(DAWN_FORGE_SFX)
            // TODO(8.0.0): GTGuiTextures.PROGRESS_BAR_BOILER_HEAT is a plain UITexture (not a
            // ProgressBarTextureSet) in 8.0.0; using PROGRESS_BOILER_FUEL_STEEL as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_BOILER_FUEL_STEEL))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry ARCANE_CRUCIBLE = recipeType("arcane_crucible", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 4, 3, 3)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setSound(DAWN_FORGE_SFX)
            // TODO(8.0.0): GTGuiTextures.PROGRESS_BAR_MASS_FAB is a plain UITexture (not a
            // ProgressBarTextureSet) in 8.0.0; using PROGRESS_ARROW_MULTIPLE as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry PYROTHERMIC_REFINERY = recipeType("pyrothermic_refinery",
            GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 3, 3, 3)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setSound(GTSoundEntries.JET_ENGINE)
            // TODO(8.0.0): GTGuiTextures.PROGRESS_BAR_MASS_FAB is a plain UITexture (not a
            // ProgressBarTextureSet) in 8.0.0; using PROGRESS_ARROW_MULTIPLE as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry MANA_ETCHING_FACTORY = recipeType("mana_etching", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 3, 0)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setSound(DAWN_FORGE_SFX)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CRYSTALLIZATION))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry BIO_LAB = recipeType("bio_lab", ELECTRIC)
            .setMaxIOSize(6, 2, 3, 2)
            .setSound(DAWN_FORGE_SFX)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CRYSTALLIZATION))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry STAR_LADDER_RESEARCH = recipeType("star_ladder_research",
            GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(16, 16, 16, 16)
            // .setSound(CosmicSounds.BLACK_HOLE_CRY)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry STELLAR_IRIS = recipeType("stellar_iris", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(16, 16, 16, 16)
            // .setSound(CosmicSounds.BLACK_HOLE_CRY)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    // Stellar Iris Module Recipe Types
    public static final GTRecipeTypeEntry STELLAR_SMELTING = recipeType("ignition_complex", GTRecipeTypes.MULTIBLOCK)
            .setSound(GAS_SUCC)
            .setMaxIOSize(9, 9, 3, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry CHROMATIC_DISTILLATION_PLANT = recipeType("chormatic_distillation_plant",
            GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 1, 1, 16)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry CELESTIAL_BORE = recipeType("celestial_bore", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 54, 3, 18)
            .setSound(CosmicSounds.LAMINATOR)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    // 8.0.0 removed GTRecipeType.addDataInfo(...) -> data infos registered in init() via getDataInfos().add(...)
    public static final GTRecipeTypeEntry NAQUAHINE_REACTOR = recipeType("naquahine_reactor", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 1, 0)
            .setSound(GTSoundEntries.ARC)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry MINI_NAQUAHINE_REACTOR = recipeType("mini_naquahine_reactor",
            GTRecipeTypes.GENERATOR)
            .setMaxIOSize(1, 0, 1, 0)
            .setSound(GTSoundEntries.ARC)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
            .setEUIO(IO.OUT)
            .register();
    public static final GTRecipeTypeEntry TURBINE_POWER_STATION = recipeType("turbine_power_station",
            GTRecipeTypes.GENERATOR)
            .setMaxIOSize(0, 0, 1, 1)
            .setEUIO(IO.OUT)
            .setSound(GAS_SUCC)
            .UI(builder -> builder.setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CENTRIFUGE_OVERLAY)
                    .setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
            .register();
    public static final GTRecipeTypeEntry COMBUSTION_POWER_STATION = recipeType("combustion_power_station",
            GTRecipeTypes.GENERATOR)
            .setMaxIOSize(0, 0, 1, 1)
            .setEUIO(IO.OUT)
            .setSound(GAS_SUCC)
            .UI(builder -> builder.setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.FURNACE_OVERLAY_2)
                    .setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .register();
    public static final GTRecipeTypeEntry INDUSTRIAL_CHEMVAT = recipeType("industrial_chemvat",
            GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 6, 6)
            .setHasResearchSlot(true)
            .setSound(CHEMVAT)
            .onRecipeBuild(ResearchManager::createDefaultResearchRecipe)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    public static final GTRecipeTypeEntry BIOVAT = recipeType("biovat", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 3, 3)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.CHEMICAL)
            .onRecipeBuild(ResearchManager::createDefaultResearchRecipe)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();

    // 8.0.0 removed GTRecipeType.addDataInfo(...) -> data info registered in init() via getDataInfos().add(...)
    public static final GTRecipeTypeEntry WASP_RECIPES = recipeType("wasp", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 15, 3, 5)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_SIFTER))
            .setEUIO(IO.IN)
            .register();

    // Dummy recipe. Maybe add recipes here? Either way it won't be used for recipe searching, that's taken care of by
    // BeeRecipeLogic
    public static final GTRecipeTypeEntry BEES = recipeType("bees", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 9, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();

    /*
     * TODO: Multiblocks that might not need a RecipeType or might use it to do really weird things
     * RIFTGENERATOR
     * PENROSE STUFF
     * LOCAL DYSON
     */
    // Todo - Custom JEI page / Custom Heating Logic, Custom Slag Generation Logic, THE WHOLE FUCKING PIPENET PROPERTY
    // DEDICATED TO SUPERMOLTEN SLAG [FEAR]
    public static final GTRecipeTypeEntry VOMAHINE_CORE_DRILL = recipeType("core_drill", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 6)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds, VFX
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    // Todo - Custom JEI page
    public static final GTRecipeTypeEntry REGOLITH_SIFTER = recipeType("regolith_sifter", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(0, 6, 2, 0)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    /*
     * TODO: Soul Folding with LifeEssence lets you create your first potential which is your first source of
     * L.Infinity. Later soul folding/forging allows for unique machine augmentations.
     */
    public static final GTRecipeTypeEntry LIFE_FORCE_MANIPULATOR = recipeType("life_force_manipulator",
            GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 3, 6, 6)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    /*
     * TODO: Neutron Forge Pressure/Heat Buildup Mechanic, feeding it astronomically large amounts of plasma allow it to
     * unlock 'COSMIC PARALLELS' - Which Allow MULTIPLE UNIQUE RECIPES to run at once.
     */
    public static final GTRecipeTypeEntry NEUTRON_FORGE = recipeType("neutron_forge", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(12, 12, 12, 12)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry MULTITHREADED_PROCESSOR = recipeType("dream_basin", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(9, 9, 9, 9)
            .setSound(GTSoundEntries.ASSEMBLER)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    /*
     * TODO - Allow This block to replace the Master Ritual stone, and then set the structure shape based on the ritual
     */
    public static final GTRecipeTypeEntry MECHANICAL_RITUAL = recipeType("mechanical_ritual", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(4, 4, 4, 4) // TODO - Figure out what's the optimal outputs
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.CHEMICAL) // TODO - Sounds
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
            .setEUIO(IO.IN)
            .register();
    /*
     * TODO - Go Find all the info about the actual Concept Incinerator, That's so much lore to dig through but I don't
     * remember if this also erases the concept out of peoples memories as well as all traces of an idea.
     * This Thing is meant to allow the deconstruction of concepts into laws, and laws are meant to be dissolved with
     * their counterparts. Every Hebrew Letter needs a Pairing Counterlaw.
     */
    // public static final GTRecipeType CONCEPT_INCINERATOR = GTRecipeTypes
    // .register(CosmicCore.id("concept_incinerator"), GTRecipeTypes.MULTIBLOCK)
    // .setMaxIOSize(4, 4, 4, 4)
    // .setHasResearchSlot(true)
    // .setSound(GTSoundEntries.CHEMICAL) // TODO - Sounds
    // .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    /*
     * TODO
     * Retcon Hashers allow the player to target potential sequence breaks in the already established environment
     * Every sequence break is recorded to a teams data and should be semi-random with mandatory breaks needed for
     * progression
     */
    // public static final GTRecipeType RETCON_HASHER = GTRecipeTypes.register(CosmicCore.id("retcon_hasher"),
    // GTRecipeTypes.MULTIBLOCK)
    // .setMaxIOSize(4, 4, 4, 4)
    // .setHasResearchSlot(true)
    // .setSound(GTSoundEntries.CHEMICAL) // TODO - Sounds
    // .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    // Link Test Station recipe type for testing cross-dimensional linking
    public static final GTRecipeTypeEntry LINK_TEST_RECIPES = recipeType("link_test", ELECTRIC)
            .setMaxIOSize(2, 2, 0, 0)
            .setSound(GTSoundEntries.ASSEMBLER)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry ABYSSAL_CULTURE_VAT = recipeType("abyssal_culture_vat", MULTIBLOCK)
            .setMaxIOSize(1, 1, 3, 3)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .addRecipeUIModifier(BloomwyrmRecipeUI.COMMON))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry BIOMELD_VIVARIUM = recipeType("biomeld_vivarium", MULTIBLOCK)
            .setMaxIOSize(0, 8, 1, 1)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .addRecipeUIModifier(BloomwyrmRecipeUI.COMMON))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry SCULK_BIOCHAMBER = recipeType("sculk_biochamber", MULTIBLOCK)
            .setMaxIOSize(6, 6, 5, 5)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .addRecipeUIModifier(BloomwyrmRecipeUI.COMMON))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry BIOMANA_DIGESTOR = recipeType("biomana_digestor", MULTIBLOCK)
            .setMaxIOSize(4, 4, 4, 4)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .addRecipeUIModifier(BloomwyrmRecipeUI.COMMON))
            .setEUIO(IO.IN)
            .register();

    public static final GTRecipeTypeEntry MANAWOMB_LEECHING_POND = recipeType("manawomb_leeching_pond", MULTIBLOCK)
            .setMaxIOSize(6, 6, 3, 4)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .addRecipeUIModifier(BloomwyrmRecipeUI.COMMON))
            .setEUIO(IO.IN)
            .register();

    public static void init() {}

    private static void setMaxIOSize(GTRecipeType type, int itemInputs, int itemOutputs,
                                     int fluidInputs, int fluidOutputs) {
        type.maxInputs.put(ItemRecipeCapability.CAP, itemInputs);
        type.maxOutputs.put(ItemRecipeCapability.CAP, itemOutputs);
        type.maxInputs.put(FluidRecipeCapability.CAP, fluidInputs);
        type.maxOutputs.put(FluidRecipeCapability.CAP, fluidOutputs);
    }

    public static void configure() {
        if (VACUUM_DISTILLATION.get().getIconSupplier() == null) {
            VACUUM_DISTILLATION.get().setIconSupplier(
                    GTMultiMachines.DISTILLATION_TOWER::asStack);
        }
        MIXER_RECIPES.get().maxInputs.put(EmberRecipeCapability.CAP, 1);

        // 8.0.0: GTRecipeType.addDataInfo(...) was removed; register XEI data-info lines on the dataInfos list.
        ORBITAL_FORGE_EBF.get().getDataInfos().add(data -> {
            int temp = data.getInt("ebf_temp");
            return Component.translatable("gtceu.recipe.temperature", temp).getString();
        });
        ORBITAL_FORGE_EBF.get().getDataInfos().add(data -> {
            int temp = data.getInt("ebf_temp");
            ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);
            if (requiredCoil != null && requiredCoil.getMaterial() != null) {
                return Component.translatable("gtceu.recipe.coil.tier",
                        Component.translatable(requiredCoil.getMaterial().value().getUnlocalizedName()).getString())
                        .getString();
            }
            return "";
        });
        ORBITAL_FORGE_ABS.get().getDataInfos().add(data -> {
            int temp = data.getInt("ebf_temp");
            return Component.translatable("gtceu.recipe.temperature", temp).getString();
        });
        ORBITAL_FORGE_ABS.get().getDataInfos().add(data -> {
            int temp = data.getInt("ebf_temp");
            ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);
            if (requiredCoil != null && requiredCoil.getMaterial() != null) {
                return Component.translatable("gtceu.recipe.coil.tier",
                        Component.translatable(requiredCoil.getMaterial().value().getUnlocalizedName()).getString())
                        .getString();
            }
            return "";
        });
        NAQUAHINE_REACTOR.get().getDataInfos().add(data -> {
            int minStrength = data.getInt("min_field");
            return Component.translatable("cosmiccore.recipe.minField", minStrength).getString();
        });
        NAQUAHINE_REACTOR.get().getDataInfos().add(data -> {
            int decayRate = data.getInt("decay_rate");
            if (!data.getBoolean("per_tick")) {
                return Component.translatable("cosmiccore.recipe.fieldSlam", decayRate).getString();
            }
            return Component.translatable("cosmiccore.recipe.fieldDecay", decayRate).getString();
        });
        WASP_RECIPES.get().getDataInfos()
                .add(data -> Component.translatable("cosmiccore.recipe.asteroid_weight_greater_1").getString());
        for (GTRecipeTypeEntry type : new GTRecipeTypeEntry[] {
                ABYSSAL_CULTURE_VAT, BIOMELD_VIVARIUM, SCULK_BIOCHAMBER, BIOMANA_DIGESTOR,
                MANAWOMB_LEECHING_POND }) {
            type.get().getDataInfos().add(data -> data.contains(BloomwyrmRecipeKeys.BIOPOWER_INPUT) ?
                    Component.translatable(
                            "cosmiccore.bloomwyrm.recipe.biopower_input",
                            data.getInt(BloomwyrmRecipeKeys.BIOPOWER_INPUT)).getString() :
                    "");
            type.get().getDataInfos().add(data -> data.contains(BloomwyrmRecipeKeys.BIOPOWER_OUTPUT) ?
                    Component.translatable(
                            "cosmiccore.bloomwyrm.recipe.biopower_output",
                            data.getInt(BloomwyrmRecipeKeys.BIOPOWER_OUTPUT)).getString() :
                    "");
            type.get().getDataInfos().add(data -> data.contains(BloomwyrmRecipeKeys.CHARGE_INPUT) ?
                    Component.translatable(
                            "cosmiccore.bloomwyrm.recipe.charge_input",
                            data.getLong(BloomwyrmRecipeKeys.CHARGE_INPUT)).getString() :
                    "");
            type.get().getDataInfos().add(data -> data.contains(BloomwyrmRecipeKeys.CHARGE_OUTPUT) ?
                    Component.translatable(
                            "cosmiccore.bloomwyrm.recipe.charge_output",
                            data.getLong(BloomwyrmRecipeKeys.CHARGE_OUTPUT)).getString() :
                    "");
            type.get().getDataInfos().add(data -> data.contains(BloomwyrmRecipeKeys.MAX_PARALLEL) ?
                    Component.translatable(
                            "cosmiccore.bloomwyrm.recipe.max_parallel",
                            data.getInt(BloomwyrmRecipeKeys.MAX_PARALLEL)).getString() :
                    "");
        }

        setMaxIOSize(LASER_ENGRAVER_RECIPES.get(), 2, 2, 1, 1);
        setMaxIOSize(PACKER_RECIPES.get(), 3, 2, 0, 0);
        setMaxIOSize(SPOOLING_RECIPES.get(), 3, 2, 1, 0);
        // Oh my God
        setMaxIOSize(CRACKING_RECIPES.get(), 2, 2, 2, 2);
        CHEMICAL_RECIPES.get().getRecipeBuilder().onSave((builder, provider) -> {
            LARGE_CHEMICAL_RECIPES.copyFrom(builder)
                    .save(provider);
            INDUSTRIAL_CHEMVAT.copyFrom(builder)
                    .save(provider);
        });
        LARGE_CHEMICAL_RECIPES.get().getRecipeBuilder().onSave((builder, provider) -> {
            INDUSTRIAL_CHEMVAT.copyFrom(builder)
                    .save(provider);
        });
        STEAM_TURBINE_FUELS.get().getRecipeBuilder().onSave((builder, provider) -> {
            if (!builder.id.equals(GTCEu.id("steam"))) {
                TURBINE_POWER_STATION.copyFrom(builder)
                        .save(provider);
            }
        });
        GAS_TURBINE_FUELS.get().getRecipeBuilder().onSave((builder, provider) -> {
            TURBINE_POWER_STATION.copyFrom(builder)
                    .save(provider);
        });
        COMBUSTION_GENERATOR_FUELS.get().getRecipeBuilder().onSave((builder, provider) -> {
            COMBUSTION_POWER_STATION.copyFrom(builder)
                    .save(provider);
        });

        // Eclipsed Forge is the big EU powered version of the Dawn Forge
        DAWN_FORGE.get().getRecipeBuilder().onSave((builder, provider) -> {
            DAWNFORGE_ECLIPSED.copyFrom(builder)
                    .save(provider);
        });
        // Large Roaster can do all the small Roaster can, but also allows Ember :)
        ROASTER.get().getRecipeBuilder().onSave((builder, provider) -> {
            LARGE_ROASTER.copyFrom(builder)
                    .save(provider);
        });

        BLAST_RECIPES.get().getRecipeBuilder().onSave((builder, provider) -> {
            var orbitBuilderEBF = ORBITAL_FORGE_EBF.copyFrom(builder);
            // Orbital Forge ONLY copies Standard EBF recipes, if an EBF recipe contains a dimension condition, it is
            // assumed it can't be done in space
            if (!builder.conditions.isEmpty() &&
                    builder.conditions.stream().anyMatch(cond -> cond instanceof DimensionCondition)) {
                // Do Nothing if the recipe Contains a Dimension
            } else {
                // If It Doesn't have a Dimension, add the recipe and give it an dimension req of 'Sun Orbit'
                orbitBuilderEBF
                        .addCondition(new DimensionCondition(
                                ResourceKey.create(Registries.DIMENSION,
                                        ResourceLocation.parse("frontiers:sun_orbit"))))
                        .save(provider);
            }
        });

        // BLAST_RECIPES.get().getRecipeBuilder().onSave((builder, provider) -> {
        // STELLAR_SMELTING.copyFrom(builder)
        // .save(provider);
        // });

        ALLOY_BLAST_RECIPES.getCategory().setXEIVisible(false);
        ALLOY_BLAST_RECIPES.get().getRecipeBuilder().onSave((builder, provider) -> {
            var orbitBuilderABS = ORBITAL_FORGE_ABS.copyFrom(builder);
            // Orbital Forge ONLY copies Standard ABS recipes, if an ABS recipe contains a dimension condition, it is
            // assumed it can't be done in space
            if (!builder.conditions.isEmpty() &&
                    builder.conditions.stream().anyMatch(cond -> cond instanceof DimensionCondition)) {
                // Do Nothing if the recipe Contains a Dimension
            } else {
                // If It Doesn't have a Dimension, add the recipe and give it an dimension req of 'Sun Orbit'
                orbitBuilderABS
                        .addCondition(new DimensionCondition(
                                ResourceKey.create(Registries.DIMENSION,
                                        ResourceLocation.parse("frontiers:sun_orbit"))))
                        .save(provider);
            }
            builder.recipeType(ALLOY_BLASTING_KILN.get()).category(ALLOY_BLASTING_KILN.getCategory());
        });
    }
}
