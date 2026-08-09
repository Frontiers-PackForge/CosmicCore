package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.recipe.EmberRecipeCapability;
import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import com.ghostipedia.cosmiccore.common.data.CosmicSounds;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.BloomwyrmRecipeKeys;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import static com.ghostipedia.cosmiccore.common.data.CosmicSounds.*;
import static com.gregtechceu.gtceu.common.data.GCYMRecipeTypes.ALLOY_BLAST_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class CosmicRecipeTypes {

    // TODO(Ore Chaos): these 6 ore-processing recipe types were referenced by their controllers +
    // CosmicCoreOreRecipeHandler but never registered (WIP gap). IO sizes inferred from the recipe builders
    // (flotation: item+fluid in / item out; powderizer: item in/out; sorter: 1 in / SORTER_IO_CAP=6 out) and
    // sensible defaults for the no-recipe-yet ones (sludge/oneiric/dissolution). Tune IO/UI/sound as needed.
    public static final GTRecipeType SLUDGE_DIGESTOR = register(CosmicCore.id("sludge_digestor"), MULTIBLOCK)
            .setMaxIOSize(3, 3, 2, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType POWDERIZER = register(CosmicCore.id("powderizer"), MULTIBLOCK)
            .setMaxIOSize(2, 2, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType INDUSTRIAL_ORE_SORTER = register(CosmicCore.id("industrial_ore_sorter"),
            MULTIBLOCK)
            .setMaxIOSize(1, 6, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType INDUSTRIAL_FLOTATION_PLANT = register(
            CosmicCore.id("industrial_flotation_plant"), MULTIBLOCK)
            .setMaxIOSize(2, 2, 2, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType ONEIRIC_SIEVE = register(CosmicCore.id("oneiric_sieve"), MULTIBLOCK)
            .setMaxIOSize(3, 3, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType DISSOLUTION_VAT = register(CosmicCore.id("dissolution_vat"), MULTIBLOCK)
            .setMaxIOSize(2, 2, 2, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType PHASE_SEPARATOR = register(CosmicCore.id("phase_separator"), MULTIBLOCK)
            .setSound(GTSoundEntries.CENTRIFUGE)
            .setMaxIOSize(1, 0, 1, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType SIMPLE_DESALTER = register(CosmicCore.id("simple_desalter"), ELECTRIC)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(0, 0, 3, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType DESALTER = register(CosmicCore.id("desalter"), MULTIBLOCK)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(0, 0, 3, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType STEAM_CRACKING_FURNACE = register(
            CosmicCore.id("steam_cracking_furnace"), MULTIBLOCK)
            .setSound(GTSoundEntries.FIRE)
            .setMaxIOSize(1, 0, 2, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType FRACTIONAL_CONDENSER = register(
            CosmicCore.id("fractional_condenser"), MULTIBLOCK)
            .setSound(GTSoundEntries.COOLING)
            .setMaxIOSize(0, 1, 1, 5)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType FLUID_CATALYTIC_CRACKING = register(
            CosmicCore.id("fluid_catalytic_cracking"), MULTIBLOCK)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(2, 2, 3, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType HYDROTREATING = register(CosmicCore.id("hydrotreating"), MULTIBLOCK)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(2, 2, 3, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType HYDROCRACKING = register(CosmicCore.id("hydrocracking"), MULTIBLOCK)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(2, 2, 3, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType CATALYTIC_REFORMING = register(
            CosmicCore.id("catalytic_reforming"), MULTIBLOCK)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(2, 2, 3, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType DELAYED_COKING = register(CosmicCore.id("delayed_coking"), MULTIBLOCK)
            .setSound(GTSoundEntries.FIRE)
            .setMaxIOSize(2, 2, 3, 6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    public static final GTRecipeType COSMIC_DUMMY_SPAM_YEETER = GTRecipeTypes
            .register(CosmicCore.id("fuckassbeeball"), ELECTRIC)
            .setMaxIOSize(54, 54, 54, 54);

    public static final GTRecipeType LAMINATOR = GTRecipeTypes
            .register(CosmicCore.id("laminator"), ELECTRIC)
            .setSound(CosmicSounds.LAMINATOR)
            .setMaxIOSize(3, 2, 2, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType CHEMICAL_DEHYDRATOR = GTRecipeTypes
            .register(CosmicCore.id("chemical_dehydrator"), ELECTRIC)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(2, 9, 2, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType CRYSTALLIZER = GTRecipeTypes
            .register(CosmicCore.id("crystallizer"), ELECTRIC)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxIOSize(0, 1, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CRYSTALLIZATION));
    public static final GTRecipeType DAWNFORGE_ECLIPSED = GTRecipeTypes
            .register(CosmicCore.id("eclipsed_dawnforge"), ELECTRIC)
            .setSound(CosmicSounds.LAMINATOR)
            .setMaxIOSize(12, 1, 3, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType VORAX = GTRecipeTypes
            .register(CosmicCore.id("vorax"), ELECTRIC)
            .setSound(CosmicSounds.VOARX)
            .setMaxIOSize(1, 0, 4, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType MANA_FLUIDIZER = GTRecipeTypes
            .register(CosmicCore.id("mana_fluidizer"), ELECTRIC)
            .setMaxIOSize(1, 1, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType PCB_FABRICATOR = GTRecipeTypes
            .register(CosmicCore.id("pcb_fab"), ELECTRIC)
            .setMaxIOSize(8, 1, 4, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    public static final GTRecipeType TITAN_FUSION_RECIPES = GTRecipeTypes
            .register(CosmicCore.id("titan_fusion"), ELECTRIC)
            .setMaxIOSize(3, 3, 3, 6)
            .setSound(GTSoundEntries.REPLICATOR)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    public static final GTRecipeType LUNAR_HAMMER = GTRecipeTypes
            .register(CosmicCore.id("lunar_hammer"), ELECTRIC)
            .setMaxIOSize(3, 3, 2, 2)
            .setSound(GTSoundEntries.JET_ENGINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    public static final GTRecipeType CRYOGENICS_CHAMBER = GTRecipeTypes
            .register(CosmicCore.id("cryo_chamber"), ELECTRIC)
            .setMaxIOSize(6, 3, 3, 6)
            .setSound(GTSoundEntries.JET_ENGINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    public static final GTRecipeType SOUL_TESTER_RECIPES = GTRecipeTypes
            .register(CosmicCore.id("soul_tester"), GTRecipeTypes.MULTIBLOCK)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxIOSize(1, 1, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    /*
     * public static final GTRecipeType EMBER_TESTER_RECIPES = GTRecipeTypes
     * .register(CosmicCore.id("ember_tester"), GTRecipeTypes.MULTIBLOCK)
     * .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
     * .setMaxSize(IO.OUT, EmberRecipeCapability.CAP, 1)
     * .setMaxIOSize(1, 1, 0, 0)
     * .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
     */

    public static final GTRecipeType VOID_MINER = GTRecipeTypes
            .register(CosmicCore.id("void_miner"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 27, 2, 0)
            .setSound(MINING_MACHINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType HEAVY_ASSEMBLER = GTRecipeTypes
            .register(CosmicCore.id("heavy_assembler"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(12, 3, 6, 0)
            .setSound(HEAVY_ASSEM)
            // TODO(8.0.0): custom CosmicGuiTextures.PROGRESS_BAR_HEAVY (LDLib ResourceTexture) has no
            // ProgressBarTextureSet equivalent; using PROGRESS_ASSEMBLER as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ASSEMBLER));

    public static final GTRecipeType PLASMITE_FORGE = GTRecipeTypes
            .register(CosmicCore.id("plasmite_forge"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 3, 3, 3)
            .setSound(HEAVY_ASSEM)
            // TODO(8.0.0): custom CosmicGuiTextures.PROGRESS_BAR_HEAVY (LDLib ResourceTexture) has no
            // ProgressBarTextureSet equivalent; using PROGRESS_ASSEMBLER as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ASSEMBLER));

    public static final GTRecipeType PRISMA_FOUNDRY = GTRecipeTypes
            .register(CosmicCore.id("prisma_foundry"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 0)
            .setSound(MINING_MACHINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType ATMOSPHERE_SIPHON = GTRecipeTypes
            .register(CosmicCore.id("atmo_siphon"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 0, 4, 16)
            .setSound(GAS_SUCC)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType MANA_DIGITIZER = GTRecipeTypes
            .register(CosmicCore.id("mana_digitizer"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 2, 2)
            .setSound(GAS_SUCC)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType COMPONENT_ASSEMBLY_LINE = GTRecipeTypes
            .register(CosmicCore.id("component_assembly_line"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(16, 1, 4, 2)
            .setSound(GAS_SUCC)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType GROVE_RECIPES = GTRecipeTypes
            .register(CosmicCore.id("drygmy_grove"), GTRecipeTypes.MULTIBLOCK)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxIOSize(2, 9, 1, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public final static GTRecipeType INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES = register(
            CosmicCore.id("industrial_primitive_blast_furnace"), MULTIBLOCK)
            .setMaxIOSize(3, 3, 1, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.FIRE);
    public static final GTRecipeType LEACHING_PLANT = GTRecipeTypes
            .register(CosmicCore.id("leaching_plant"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType HELLFIRE_FOUNDRY = GTRecipeTypes
            .register(CosmicCore.id("hellfire_foundry"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(5, 1, 1, 0)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxTooltips(4)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType SUFFERING_CHAMBER = GTRecipeTypes
            .register(CosmicCore.id("suffering_chamber"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 0, 0)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxTooltips(5)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType ARCANE_DISTILLERY = GTRecipeTypes
            .register(CosmicCore.id("arcane_distillery"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 3, 2)
            .setMaxTooltips(4)
            .setSound(CosmicSounds.ARCANE_DISTIL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType ARCANE_FOLDING = GTRecipeTypes
            .register(CosmicCore.id("arcane_folding"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 1, 0)
            .setMaxTooltips(4)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType POLYMERIZER = GTRecipeTypes
            .register(CosmicCore.id("polymerizer"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 3, 2)
            .setMaxTooltips(4)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType HEMOPHAGIC_TRANSFUSER = GTRecipeTypes
            .register(CosmicCore.id("hemophagic_transfuser"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 3, 3)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxTooltips(4)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType CHROMATIC_FLOTATION_PLANT = GTRecipeTypes
            .register(CosmicCore.id("chromatic_flotation_plant"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 4, 3, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType SPIRIT_CRUCIBLE = GTRecipeTypes
            .register(CosmicCore.id("spirit_crucible"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 0)
            .setSound(ARCANE_DISTIL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType SOUL_FOUNDRY = GTRecipeTypes
            .register(CosmicCore.id("soul_foundry"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 1, 3, 1)
            .setSound(CosmicSounds.LAMINATOR)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType CALX_REACTOR = GTRecipeTypes
            .register(CosmicCore.id("calx_reactor"), ELECTRIC)
            .setMaxIOSize(2, 2, 1, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType ROASTER = GTRecipeTypes
            .register(CosmicCore.id("roaster"), ELECTRIC)
            .setMaxIOSize(2, 3, 1, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType MANA_LEACHING_TUB = GTRecipeTypes
            .register(CosmicCore.id("mana_leaching_tub"), ELECTRIC)
            .setMaxIOSize(1, 1, 2, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType THERMOMAG = GTRecipeTypes
            .register(CosmicCore.id("thermomagnitizer"), ELECTRIC)
            .setMaxIOSize(3, 2, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType VAC_BUBBLER = GTRecipeTypes
            .register(CosmicCore.id("vacuum_bubbler"), ELECTRIC)
            .setMaxIOSize(2, 3, 2, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType LARGE_ROASTER = GTRecipeTypes
            .register(CosmicCore.id("large_roaster"), ELECTRIC)
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
                            layout -> new String[] { "ss", "ss" }));
    public static final GTRecipeType VILE_FISSION = GTRecipeTypes
            .register(CosmicCore.id("vile_fission"), ELECTRIC)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setMaxIOSize(1, 1, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType VOID_SALT_FISSION = GTRecipeTypes
            .register(CosmicCore.id("void_salt_fission"), ELECTRIC)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setMaxIOSize(2, 3, 2, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType RADBOLT_RECONSTRUCTOR = GTRecipeTypes
            .register(CosmicCore.id("reconstructor"), ELECTRIC)
            .setMaxIOSize(3, 2, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType SPOOLING_MACHINE = GTRecipeTypes
            .register(CosmicCore.id("spooling_machine"), ELECTRIC)
            .setMaxIOSize(2, 2, 1, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType ORBITAL_FORGE_EBF = GTRecipeTypes
            .register(CosmicCore.id("orbital_forge"), GTRecipeTypes.MULTIBLOCK)
            .setSound(CosmicSounds.ORBITAL_FORGE)
            .setHasResearchSlot(true)
            .setMaxIOSize(3, 3, 3, 3)
            // TODO(8.0.0): PROGRESS_BAR_ARC_FURNACE had no ProgressBarTextureSet; closest substitute
            // 8.0.0 removed GTRecipeType.addDataInfo(...) -> data infos registered in init() via
            // getDataInfos().add(...)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_FUSION));

    public static final GTRecipeType ORBITAL_FORGE_ABS = GTRecipeTypes
            .register(CosmicCore.id("orbital_forge_abs"), GTRecipeTypes.MULTIBLOCK)
            .setSound(CosmicSounds.ORBITAL_FORGE)
            .setHasResearchSlot(true)
            .setMaxIOSize(9, 3, 3, 3)
            // TODO(8.0.0): PROGRESS_BAR_COKE_OVEN had no ProgressBarTextureSet; closest substitute
            // 8.0.0 removed GTRecipeType.addDataInfo(...) -> data infos registered in init() via
            // getDataInfos().add(...)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType DAWN_FORGE = GTRecipeTypes
            .register(CosmicCore.id("dawn_forge"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(8, 1, 2, 0)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setSound(DAWN_FORGE_SFX)
            .setMaxTooltips(5)
            // TODO(8.0.0): custom CosmicGuiTextures.DAWN_FORGE (LDLib ResourceTexture) has no
            // ProgressBarTextureSet equivalent; using PROGRESS_ARROW_MULTIPLE as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    public static final GTRecipeType CINDER_HEARTH = GTRecipeTypes
            .register(CosmicCore.id("cinder_hearth"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 0, 3, 3)
            .setMaxSize(IO.OUT, EmberRecipeCapability.CAP, 1)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setSound(DAWN_FORGE_SFX)
            .setMaxTooltips(7)
            // TODO(8.0.0): GTGuiTextures.PROGRESS_BAR_BOILER_HEAT is a plain UITexture (not a
            // ProgressBarTextureSet) in 8.0.0; using PROGRESS_BOILER_FUEL_STEEL as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_BOILER_FUEL_STEEL));

    public static final GTRecipeType ARCANE_CRUCIBLE = GTRecipeTypes
            .register(CosmicCore.id("arcane_crucible"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 4, 3, 3)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setSound(DAWN_FORGE_SFX)
            .setMaxTooltips(7)
            // TODO(8.0.0): GTGuiTextures.PROGRESS_BAR_MASS_FAB is a plain UITexture (not a
            // ProgressBarTextureSet) in 8.0.0; using PROGRESS_ARROW_MULTIPLE as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    public static final GTRecipeType PYROTHERMIC_REFINERY = GTRecipeTypes
            .register(CosmicCore.id("pyrothermic_refinery"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 3, 3, 3)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setSound(GTSoundEntries.JET_ENGINE)
            .setMaxTooltips(3)
            // TODO(8.0.0): GTGuiTextures.PROGRESS_BAR_MASS_FAB is a plain UITexture (not a
            // ProgressBarTextureSet) in 8.0.0; using PROGRESS_ARROW_MULTIPLE as closest cosmetic substitute
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    public static final GTRecipeType MANA_ETCHING_FACTORY = GTRecipeTypes
            .register(CosmicCore.id("mana_etching"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 3, 0)
            .setMaxSize(IO.IN, EmberRecipeCapability.CAP, 1)
            .setSound(DAWN_FORGE_SFX)
            .setMaxTooltips(5)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CRYSTALLIZATION));
    public static final GTRecipeType BIO_LAB = GTRecipeTypes.register(CosmicCore.id("bio_lab"), ELECTRIC)
            .setMaxIOSize(6, 2, 3, 2)
            .setSound(DAWN_FORGE_SFX)
            .setMaxTooltips(5)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CRYSTALLIZATION));

    public static final GTRecipeType STAR_LADDER_RESEARCH = GTRecipeTypes
            .register(CosmicCore.id("star_ladder_research"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(16, 16, 16, 16)
            // .setSound(CosmicSounds.BLACK_HOLE_CRY)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType STELLAR_IRIS = GTRecipeTypes
            .register(CosmicCore.id("stellar_iris"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(16, 16, 16, 16)
            // .setSound(CosmicSounds.BLACK_HOLE_CRY)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    // Stellar Iris Module Recipe Types
    public static final GTRecipeType STELLAR_SMELTING = GTRecipeTypes
            .register(CosmicCore.id("ignition_complex"), GTRecipeTypes.MULTIBLOCK)
            .setSound(GAS_SUCC)
            .setMaxIOSize(9, 9, 3, 3)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType CHROMATIC_DISTILLATION_PLANT = GTRecipeTypes
            .register(CosmicCore.id("chormatic_distillation_plant"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 1, 1, 16)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType CELESTIAL_BORE = GTRecipeTypes
            .register(CosmicCore.id("celestial_bore"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 54, 3, 18)
            .setSound(CosmicSounds.LAMINATOR)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    // 8.0.0 removed GTRecipeType.addDataInfo(...) -> data infos registered in init() via getDataInfos().add(...)
    public static final GTRecipeType NAQUAHINE_REACTOR = GTRecipeTypes
            .register(CosmicCore.id("naquahine_reactor"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 1, 0)
            .setSound(GTSoundEntries.ARC)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));
    public static final GTRecipeType MINI_NAQUAHINE_REACTOR = GTRecipeTypes
            .register(CosmicCore.id("mini_naquahine_reactor"), GTRecipeTypes.GENERATOR)
            .setMaxIOSize(1, 0, 1, 0)
            .setSound(GTSoundEntries.ARC)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR));
    public static final GTRecipeType INDUSTRIAL_CHEMVAT = GTRecipeTypes
            .register(CosmicCore.id("industrial_chemvat"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 6, 6)
            .setHasResearchSlot(true)
            .setSound(CHEMVAT)
            .setMaxTooltips(5)
            .onRecipeBuild(ResearchManager::createDefaultResearchRecipe)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    public static final GTRecipeType BIOVAT = GTRecipeTypes
            .register(CosmicCore.id("biovat"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 3, 3)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxTooltips(6)
            .onRecipeBuild(ResearchManager::createDefaultResearchRecipe)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    // 8.0.0 removed GTRecipeType.addDataInfo(...) -> data info registered in init() via getDataInfos().add(...)
    public static final GTRecipeType WASP_RECIPES = GTRecipeTypes
            .register(CosmicCore.id("wasp"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 15, 3, 5)
            .setMaxTooltips(4)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_SIFTER));

    // Dummy recipe. Maybe add recipes here? Either way it won't be used for recipe searching, that's taken care of by
    // BeeRecipeLogic
    public static final GTRecipeType BEES = GTRecipeTypes
            .register(CosmicCore.id("bees"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 9, 0, 0)
            .setMaxTooltips(6)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    /*
     * TODO: Multiblocks that might not need a RecipeType or might use it to do really weird things
     * RIFTGENERATOR
     * PENROSE STUFF
     * LOCAL DYSON
     */
    // Todo - Custom JEI page / Custom Heating Logic, Custom Slag Generation Logic, THE WHOLE FUCKING PIPENET PROPERTY
    // DEDICATED TO SUPERMOLTEN SLAG [FEAR]
    public static final GTRecipeType VOMAHINE_CORE_DRILL = GTRecipeTypes
            .register(CosmicCore.id("core_drill"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 6)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds, VFX
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    // Todo - Custom JEI page
    public static final GTRecipeType REGOLITH_SIFTER = GTRecipeTypes
            .register(CosmicCore.id("regolith_sifter"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(0, 6, 2, 0)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    /*
     * TODO: Soul Folding with LifeEssence lets you create your first potential which is your first source of
     * L.Infinity. Later soul folding/forging allows for unique machine augmentations.
     */
    public static final GTRecipeType LIFE_FORCE_MANIPULATOR = GTRecipeTypes
            .register(CosmicCore.id("life_force_manipulator"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 3, 6, 6)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    /*
     * TODO: Neutron Forge Pressure/Heat Buildup Mechanic, feeding it astronomically large amounts of plasma allow it to
     * unlock 'COSMIC PARALLELS' - Which Allow MULTIPLE UNIQUE RECIPES to run at once.
     */
    public static final GTRecipeType NEUTRON_FORGE = GTRecipeTypes
            .register(CosmicCore.id("neutron_forge"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(12, 12, 12, 12)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    public static final GTRecipeType MULTITHREADED_PROCESSOR = GTRecipeTypes
            .register(CosmicCore.id("dream_basin"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(9, 9, 9, 9)
            .setSound(GTSoundEntries.ASSEMBLER)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
    /*
     * TODO - Allow This block to replace the Master Ritual stone, and then set the structure shape based on the ritual
     */
    public static final GTRecipeType MECHANICAL_RITUAL = GTRecipeTypes
            .register(CosmicCore.id("mechanical_ritual"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(4, 4, 4, 4) // TODO - Figure out what's the optimal outputs
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.CHEMICAL) // TODO - Sounds
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));
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
    public static final GTRecipeType LINK_TEST_RECIPES = GTRecipeTypes
            .register(CosmicCore.id("link_test"), ELECTRIC)
            .setMaxIOSize(2, 2, 0, 0)
            .setSound(GTSoundEntries.ASSEMBLER)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW));

    public static final GTRecipeType ABYSSAL_CULTURE_VAT = GTRecipeTypes
            .register(CosmicCore.id("abyssal_culture_vat"), MULTIBLOCK)
            .setMaxIOSize(1, 1, 3, 3)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .addRecipeUIModifier(BloomwyrmRecipeUI.COMMON));

    public static final GTRecipeType SCULK_BIOCHAMBER = GTRecipeTypes
            .register(CosmicCore.id("sculk_biochamber"), MULTIBLOCK)
            .setMaxIOSize(6, 6, 5, 5)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .addRecipeUIModifier(BloomwyrmRecipeUI.COMMON));

    public static final GTRecipeType BIOMANA_DIGESTOR = GTRecipeTypes
            .register(CosmicCore.id("biomana_digestor"), MULTIBLOCK)
            .setMaxIOSize(4, 4, 4, 4)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .addRecipeUIModifier(BloomwyrmRecipeUI.COMMON));

    public static final GTRecipeType MANAWOMB_LEECHING_POND = GTRecipeTypes
            .register(CosmicCore.id("manawomb_leeching_pond"), MULTIBLOCK)
            .setMaxIOSize(4, 6, 4, 4)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
                    .addRecipeUIModifier(BloomwyrmRecipeUI.COMMON));

    public static void init() {
        for (GTRecipeType type : new GTRecipeType[] {
                SLUDGE_DIGESTOR, POWDERIZER, INDUSTRIAL_ORE_SORTER, INDUSTRIAL_FLOTATION_PLANT, ONEIRIC_SIEVE,
                DISSOLUTION_VAT, PHASE_SEPARATOR, SIMPLE_DESALTER, DESALTER, STEAM_CRACKING_FURNACE,
                FRACTIONAL_CONDENSER, FLUID_CATALYTIC_CRACKING, HYDROTREATING, HYDROCRACKING,
                CATALYTIC_REFORMING, DELAYED_COKING, COSMIC_DUMMY_SPAM_YEETER, LAMINATOR, CHEMICAL_DEHYDRATOR,
                CRYSTALLIZER,
                DAWNFORGE_ECLIPSED,
                VORAX, MANA_FLUIDIZER,
                PCB_FABRICATOR, TITAN_FUSION_RECIPES, LUNAR_HAMMER, CRYOGENICS_CHAMBER, SOUL_TESTER_RECIPES,
                VOID_MINER, HEAVY_ASSEMBLER, PLASMITE_FORGE, PRISMA_FOUNDRY, ATMOSPHERE_SIPHON, MANA_DIGITIZER,
                COMPONENT_ASSEMBLY_LINE, GROVE_RECIPES, LEACHING_PLANT, HELLFIRE_FOUNDRY, SUFFERING_CHAMBER,
                ARCANE_DISTILLERY, ARCANE_FOLDING, POLYMERIZER, HEMOPHAGIC_TRANSFUSER, CHROMATIC_FLOTATION_PLANT,
                SPIRIT_CRUCIBLE, SOUL_FOUNDRY, CALX_REACTOR, ROASTER, MANA_LEACHING_TUB, THERMOMAG, VAC_BUBBLER,
                LARGE_ROASTER, VILE_FISSION, VOID_SALT_FISSION, RADBOLT_RECONSTRUCTOR, SPOOLING_MACHINE,
                ORBITAL_FORGE_EBF, ORBITAL_FORGE_ABS, DAWN_FORGE, CINDER_HEARTH, ARCANE_CRUCIBLE,
                PYROTHERMIC_REFINERY, MANA_ETCHING_FACTORY, BIO_LAB, STAR_LADDER_RESEARCH, STELLAR_IRIS,
                STELLAR_SMELTING, CHROMATIC_DISTILLATION_PLANT, CELESTIAL_BORE, NAQUAHINE_REACTOR,
                INDUSTRIAL_CHEMVAT, BIOVAT, WASP_RECIPES, BEES, VOMAHINE_CORE_DRILL, REGOLITH_SIFTER,
                LIFE_FORCE_MANIPULATOR, NEUTRON_FORGE, MULTITHREADED_PROCESSOR, MECHANICAL_RITUAL,
                LINK_TEST_RECIPES, ABYSSAL_CULTURE_VAT, SCULK_BIOCHAMBER, BIOMANA_DIGESTOR,
                MANAWOMB_LEECHING_POND }) {
            type.setEUIO(IO.IN);
        }
        MINI_NAQUAHINE_REACTOR.setEUIO(IO.OUT);

        // 8.0.0: GTRecipeType.addDataInfo(...) was removed; register XEI data-info lines on the dataInfos list.
        ORBITAL_FORGE_EBF.getDataInfos().add(data -> {
            int temp = data.getInt("ebf_temp");
            return LocalizationUtils.format("gtceu.recipe.temperature", temp);
        });
        ORBITAL_FORGE_EBF.getDataInfos().add(data -> {
            int temp = data.getInt("ebf_temp");
            ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);
            if (requiredCoil != null && !requiredCoil.getMaterial().isNull()) {
                return LocalizationUtils.format("gtceu.recipe.coil.tier",
                        I18n.get(requiredCoil.getMaterial().getUnlocalizedName()));
            }
            return "";
        });
        ORBITAL_FORGE_ABS.getDataInfos().add(data -> {
            int temp = data.getInt("ebf_temp");
            return LocalizationUtils.format("gtceu.recipe.temperature", temp);
        });
        ORBITAL_FORGE_ABS.getDataInfos().add(data -> {
            int temp = data.getInt("ebf_temp");
            ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);
            if (requiredCoil != null && !requiredCoil.getMaterial().isNull()) {
                return LocalizationUtils.format("gtceu.recipe.coil.tier",
                        I18n.get(requiredCoil.getMaterial().getUnlocalizedName()));
            }
            return "";
        });
        NAQUAHINE_REACTOR.getDataInfos().add(data -> {
            int minStrength = data.getInt("min_field");
            return LocalizationUtils.format("cosmiccore.recipe.minField", minStrength);
        });
        NAQUAHINE_REACTOR.getDataInfos().add(data -> {
            int decayRate = data.getInt("decay_rate");
            if (!data.getBoolean("per_tick")) {
                return LocalizationUtils.format("cosmiccore.recipe.fieldSlam", decayRate);
            }
            return LocalizationUtils.format("cosmiccore.recipe.fieldDecay", decayRate);
        });
        WASP_RECIPES.getDataInfos()
                .add(data -> LocalizationUtils.format("cosmiccore.recipe.asteroid_weight_greater_1"));
        for (GTRecipeType type : new GTRecipeType[] {
                ABYSSAL_CULTURE_VAT, SCULK_BIOCHAMBER, BIOMANA_DIGESTOR, MANAWOMB_LEECHING_POND }) {
            type.getDataInfos().add(data -> data.contains(BloomwyrmRecipeKeys.BIOPOWER_INPUT) ?
                    LocalizationUtils.format(
                            "cosmiccore.bloomwyrm.recipe.biopower_input",
                            data.getInt(BloomwyrmRecipeKeys.BIOPOWER_INPUT)) :
                    "");
            type.getDataInfos().add(data -> data.contains(BloomwyrmRecipeKeys.BIOPOWER_OUTPUT) ?
                    LocalizationUtils.format(
                            "cosmiccore.bloomwyrm.recipe.biopower_output",
                            data.getInt(BloomwyrmRecipeKeys.BIOPOWER_OUTPUT)) :
                    "");
            type.getDataInfos().add(data -> data.contains(BloomwyrmRecipeKeys.CHARGE_INPUT) ?
                    LocalizationUtils.format(
                            "cosmiccore.bloomwyrm.recipe.charge_input",
                            data.getLong(BloomwyrmRecipeKeys.CHARGE_INPUT)) :
                    "");
            type.getDataInfos().add(data -> data.contains(BloomwyrmRecipeKeys.CHARGE_OUTPUT) ?
                    LocalizationUtils.format(
                            "cosmiccore.bloomwyrm.recipe.charge_output",
                            data.getLong(BloomwyrmRecipeKeys.CHARGE_OUTPUT)) :
                    "");
            type.getDataInfos().add(data -> data.contains(BloomwyrmRecipeKeys.MAX_PARALLEL) ?
                    LocalizationUtils.format(
                            "cosmiccore.bloomwyrm.recipe.max_parallel",
                            data.getInt(BloomwyrmRecipeKeys.MAX_PARALLEL)) :
                    "");
        }

        LASER_ENGRAVER_RECIPES.setMaxIOSize(2, 2, 1, 1);
        // Oh my God
        MIXER_RECIPES.setMaxTooltips(4);
        BREWING_RECIPES.setMaxTooltips(4);
        FERMENTING_RECIPES.setMaxTooltips(4);
        DISTILLERY_RECIPES.setMaxTooltips(4);
        AUTOCLAVE_RECIPES.setMaxTooltips(4);
        FLUID_HEATER_RECIPES.setMaxTooltips(4);
        CRACKING_RECIPES.setMaxIOSize(2, 2, 2, 2);
        CHEMICAL_RECIPES.onRecipeBuild((builder, provider) -> {
            LARGE_CHEMICAL_RECIPES.copyFrom(builder)
                    .save(provider);
            INDUSTRIAL_CHEMVAT.copyFrom(builder)
                    .save(provider);
        });
        LARGE_CHEMICAL_RECIPES.onRecipeBuild((builder, provider) -> {
            INDUSTRIAL_CHEMVAT.copyFrom(builder)
                    .save(provider);
        });

        // Eclipsed Forge is the big EU powered version of the Dawn Forge
        DAWN_FORGE.onRecipeBuild((builder, provider) -> {
            DAWNFORGE_ECLIPSED.copyFrom(builder)
                    .save(provider);
        });
        // Large Roaster can do all the small Roaster can, but also allows Ember :)
        ROASTER.onRecipeBuild((builder, provider) -> {
            LARGE_ROASTER.copyFrom(builder)
                    .save(provider);
        });

        BLAST_RECIPES.onRecipeBuild((builder, provider) -> {
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

        // BLAST_RECIPES.onRecipeBuild((builder, provider) -> {
        // STELLAR_SMELTING.copyFrom(builder)
        // .save(provider);
        // });

        ALLOY_BLAST_RECIPES.onRecipeBuild((builder, provider) -> {
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
        });
    }
}
