package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.api.CosmicGuiTextures;
import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import com.ghostipedia.cosmiccore.common.data.CosmicSounds;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

import static com.ghostipedia.cosmiccore.common.data.CosmicSounds.*;
import static com.gregtechceu.gtceu.common.data.GCYMRecipeTypes.ALLOY_BLAST_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.LEFT_TO_RIGHT;

public class CosmicRecipeTypes {

    public static final GTRecipeType LAMINATOR = GTRecipeTypes
            .register("laminator", ELECTRIC)
            .setSound(CosmicSounds.LAMINATOR)
            .setMaxIOSize(3, 2, 2, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType VORAX = GTRecipeTypes
            .register("vorax", ELECTRIC)
            .setSound(CosmicSounds.VOARX)
            .setMaxIOSize(1, 0, 4, 2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType MANA_FLUIDIZER = GTRecipeTypes
            .register("mana_fluidizer", ELECTRIC)
            .setMaxIOSize(1, 1, 1, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static final GTRecipeType SOUL_TESTER_RECIPES = GTRecipeTypes
            .register("soul_tester", GTRecipeTypes.MULTIBLOCK)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxIOSize(1, 1, 0, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static final GTRecipeType VOID_MINER = GTRecipeTypes
            .register("void_miner", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 27, 2, 0)
            .setSound(MINING_MACHINE)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static final GTRecipeType HEAVY_ASSEMBLER = GTRecipeTypes
            .register("heavy_assembler", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(12, 3, 6, 0)
            .setSound(HEAVY_ASSEM)
            .setProgressBar(CosmicGuiTextures.PROGRESS_BAR_HEAVY, ProgressTexture.FillDirection.UP_TO_DOWN);

    public static final GTRecipeType PLASMITE_FORGE = GTRecipeTypes
            .register("plasmite_forge", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 3, 3, 3)
            .setSound(HEAVY_ASSEM)
            .setProgressBar(CosmicGuiTextures.PROGRESS_BAR_HEAVY, ProgressTexture.FillDirection.UP_TO_DOWN);

    public static final GTRecipeType PRISMA_FOUNDRY = GTRecipeTypes
            .register("prisma_foundry", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 0)
            .setSound(MINING_MACHINE)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static final GTRecipeType ATMOSPHERE_SIPHON = GTRecipeTypes
            .register("atmo_siphon", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 0, 4, 16)
            .setSound(GAS_SUCC)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static final GTRecipeType MANA_DIGITIZER = GTRecipeTypes
            .register("mana_digitizer", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 2, 2)
            .setSound(GAS_SUCC)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType GROVE_RECIPES = GTRecipeTypes.register("drygmy_grove", GTRecipeTypes.MULTIBLOCK)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxIOSize(2, 9, 1, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public final static GTRecipeType INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES = register(
            "industrial_primitive_blast_furnace", MULTIBLOCK)
            .setMaxIOSize(3, 3, 1, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT)
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.FIRE);
    public static final GTRecipeType LEACHING_PLANT = GTRecipeTypes.register("leaching_plant", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static final GTRecipeType HELLFIRE_FOUNDRY = GTRecipeTypes
            .register("hellfire_foundry", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(5, 1, 1, 0)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxTooltips(4)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.ALWAYS_FULL);
    public static final GTRecipeType SUFFERING_CHAMBER = GTRecipeTypes
            .register("suffering_chamber", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 0, 0)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxTooltips(5)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, LEFT_TO_RIGHT);
    public static final GTRecipeType ARCANE_DISTILLERY = GTRecipeTypes
            .register("arcane_distillery", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 3, 2)
            .setMaxTooltips(4)
            .setSound(CosmicSounds.ARCANE_DISTIL)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT);
    public static final GTRecipeType ARCANE_FOLDING = GTRecipeTypes
            .register("arcane_folding", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 1, 0)
            .setMaxTooltips(4)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT);
    public static final GTRecipeType POLYMERIZER = GTRecipeTypes
            .register("polymerizer", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 2, 3, 2)
            .setMaxTooltips(4)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, LEFT_TO_RIGHT);
    public static final GTRecipeType HEMOPHAGIC_TRANSFUSER = GTRecipeTypes
            .register("hemophagic_transfuser", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 3, 3)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxTooltips(4)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.ALWAYS_FULL);
    public static final GTRecipeType CHROMATIC_FLOTATION_PLANT = GTRecipeTypes
            .register("chromatic_flotation_plant", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 4, 3, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static final GTRecipeType SPIRIT_CRUCIBLE = GTRecipeTypes
            .register("spirit_crucible", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 0)
            .setSound(ARCANE_DISTIL)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static final GTRecipeType SOUL_FOUNDRY = GTRecipeTypes
            .register("soul_foundry", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 1, 3, 1)
            .setSound(CosmicSounds.LAMINATOR)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static final GTRecipeType SPOOLING_MACHINE = GTRecipeTypes
            .register("spooling_machine", ELECTRIC)
            .setMaxIOSize(2, 2, 1, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType ORBITAL_FORGE_EBF = GTRecipeTypes
            .register("orbital_forge", GTRecipeTypes.MULTIBLOCK)
            .setSound(CosmicSounds.ORBITAL_FORGE)
            .setHasResearchSlot(true)
            .setMaxIOSize(3, 3, 3, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .addDataInfo(data -> {
                int temp = data.getInt("ebf_temp");
                return LocalizationUtils.format("gtceu.recipe.temperature", temp);
            })
            .addDataInfo(data -> {
                int temp = data.getInt("ebf_temp");
                ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);

                if (requiredCoil != null && !requiredCoil.getMaterial().isNull()) {
                    return LocalizationUtils.format("gtceu.recipe.coil.tier",
                            I18n.get(requiredCoil.getMaterial().getUnlocalizedName()));
                }
                return "";
            });

    public static final GTRecipeType ORBITAL_FORGE_ABS = GTRecipeTypes
            .register("orbital_forge_abs", GTRecipeTypes.MULTIBLOCK)
            .setSound(CosmicSounds.ORBITAL_FORGE)
            .setHasResearchSlot(true)
            .setMaxIOSize(9, 3, 3, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_COKE_OVEN, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .addDataInfo(data -> {
                int temp = data.getInt("ebf_temp");
                return LocalizationUtils.format("gtceu.recipe.temperature", temp);
            })
            .addDataInfo(data -> {
                int temp = data.getInt("ebf_temp");
                ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);

                if (requiredCoil != null && !requiredCoil.getMaterial().isNull()) {
                    return LocalizationUtils.format("gtceu.recipe.coil.tier",
                            I18n.get(requiredCoil.getMaterial().getUnlocalizedName()));
                }
                return "";
            });
    public static final GTRecipeType STELLAR_IRIS = GTRecipeTypes.register("stellar_iris", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(16, 16, 16, 16)
            // .setSound(CosmicSounds.BLACK_HOLE_CRY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType CHROMATIC_DISTILLATION_PLANT = GTRecipeTypes
            .register("chormatic_distillation_plant", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 1, 1, 16)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType CELESTIAL_BORE = GTRecipeTypes.register("celestial_bore", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 54, 3, 18)
            .setSound(CosmicSounds.LAMINATOR)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType NAQUAHINE_REACTOR = GTRecipeTypes
            .register("naquahine_reactor", GTRecipeTypes.MULTIBLOCK)
            .addDataInfo(data -> {
                int minStrength = data.getInt("min_field");
                return LocalizationUtils.format("cosmiccore.recipe.minField", minStrength);
            })
            .addDataInfo(data -> {
                int decayRate = data.getInt("decay_rate");
                if (!data.getBoolean("per_tick")) {
                    return LocalizationUtils.format("cosmiccore.recipe.fieldSlam", decayRate);
                }
                return LocalizationUtils.format("cosmiccore.recipe.fieldDecay", decayRate);
            })
            .setMaxIOSize(1, 0, 1, 0)
            .setSound(GTSoundEntries.ARC)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType MINI_NAQUAHINE_REACTOR = GTRecipeTypes
            .register("mini_naquahine_reactor", GTRecipeTypes.GENERATOR)
            .setMaxIOSize(1, 0, 1, 0)
            .setSound(GTSoundEntries.ARC)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressTexture.FillDirection.DOWN_TO_UP);
    public static final GTRecipeType INDUSTRIAL_CHEMVAT = GTRecipeTypes
            .register("industrial_chemvat", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 6, 6)
            .setHasResearchSlot(true)
            .setSound(CHEMVAT)
            .setMaxTooltips(5)
            .onRecipeBuild(ResearchManager::createDefaultResearchRecipe)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType BIOVAT = GTRecipeTypes
            .register("biovat", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 3, 3)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.CHEMICAL)
            .setMaxTooltips(6)
            .onRecipeBuild(ResearchManager::createDefaultResearchRecipe)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    /*
     * TODO: Multiblocks that might not need a RecipeType or might use it to do really weird things
     * RIFTGENERATOR
     * PENROSE STUFF
     * LOCAL DYSON
     */
    // Todo - Custom JEI page / Custom Heating Logic, Custom Slag Generation Logic, THE WHOLE FUCKING PIPENET PROPERTY
    // DEDICATED TO SUPERMOLTEN SLAG [FEAR]
    public static final GTRecipeType VOMAHINE_CORE_DRILL = GTRecipeTypes
            .register("core_drill", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 6)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds, VFX
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    // Todo - Custom JEI page
    public static final GTRecipeType REGOLITH_SIFTER = GTRecipeTypes
            .register("regolith_sifter", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(0, 6, 2, 0)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    /*
     * TODO: Soul Folding with LifeEssence lets you create your first potential which is your first source of
     * L.Infinity. Later soul folding/forging allows for unique machine augmentations.
     */
    public static final GTRecipeType LIFE_FORCE_MANIPULATOR = GTRecipeTypes
            .register("life_force_manipulator", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 3, 6, 6)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    /*
     * TODO: Neutron Forge Pressure/Heat Buildup Mechanic, feeding it astronomically large amounts of plasma allow it to
     * unlock 'COSMIC PARALLELS' - Which Allow MULTIPLE UNIQUE RECIPES to run at once.
     */
    public static final GTRecipeType NEUTRON_FORGE = GTRecipeTypes.register("neutron_forge", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(12, 12, 12, 12)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.REPLICATOR) // TODO - Sounds
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    /*
     * TODO - Allow This block to replace the Master Ritual stone, and then set the structure shape based on the ritual
     */
    public static final GTRecipeType MECHANICAL_RITUAL = GTRecipeTypes
            .register("mechanical_ritual", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(4, 4, 4, 4) // TODO - Figure out what's the optimal outputs
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.CHEMICAL) // TODO - Sounds
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    /*
     * TODO - Go Find all the info about the actual Concept Incinerator, That's so much lore to dig through but I don't
     * remember if this also erases the concept out of peoples memories as well as all traces of an idea.
     * This Thing is meant to allow the deconstruction of concepts into laws, and laws are meant to be dissolved with
     * their counterparts. Every Hebrew Letter needs a Pairing Counterlaw.
     */
    public static final GTRecipeType CONCEPT_INCINERATOR = GTRecipeTypes
            .register("concept_incinerator", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(4, 4, 4, 4)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.CHEMICAL) // TODO - Sounds
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    /*
     * TODO
     * Retcon Hashers allow the player to target potential sequence breaks in the already established environment
     * Every sequence break is recorded to a teams data and should be semi-random with mandatory breaks needed for
     * progression
     */
    public static final GTRecipeType RETCON_HASHER = GTRecipeTypes.register("retcon_hasher", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(4, 4, 4, 4)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.CHEMICAL) // TODO - Sounds
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static void init() {
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

        BLAST_RECIPES.onRecipeBuild((builder, provider) -> {
            var orbitBuilderEBF = ORBITAL_FORGE_EBF.copyFrom(builder);
            // Orbital Forge ONLY copies Standard EBF recipes, if an EBF recipe contains a dimension condition, it is
            // assumed it can't be done in space
            if (!builder.conditions.isEmpty() &&
                    builder.conditions.stream().anyMatch(cond -> cond instanceof DimensionCondition)) {
                // Do Nothing if the recipe Contains a Dimension
            } else {
                // If It Doesn't have a Dimension, add the recipe and give it an dimension req of 'Sun Orbit'
                orbitBuilderEBF.addCondition(new DimensionCondition(new ResourceLocation("frontiers:sun_orbit")))
                        .save(provider);
            }
        });
        ALLOY_BLAST_RECIPES.onRecipeBuild((builder, provider) -> {
            var orbitBuilderABS = ORBITAL_FORGE_ABS.copyFrom(builder);
            // Orbital Forge ONLY copies Standard ABS recipes, if an ABS recipe contains a dimension condition, it is
            // assumed it can't be done in space
            if (!builder.conditions.isEmpty() &&
                    builder.conditions.stream().anyMatch(cond -> cond instanceof DimensionCondition)) {
                // Do Nothing if the recipe Contains a Dimension
            } else {
                // If It Doesn't have a Dimension, add the recipe and give it an dimension req of 'Sun Orbit'
                orbitBuilderABS.addCondition(new DimensionCondition(new ResourceLocation("frontiers:sun_orbit")))
                        .save(provider);
            }
        });
    }
}
