package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.LEFT_TO_RIGHT;

public class CosmicRecipeTypes {

    public static final GTRecipeType SOUL_TESTER_RECIPES = GTRecipeTypes
            .register("soul_tester", GTRecipeTypes.MULTIBLOCK)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxIOSize(1, 1, 0, 0)
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
    public static final GTRecipeType CHROMATIC_FLOTATION_PLANT = GTRecipeTypes
            .register("chromatic_flotation_plant", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 9, 3, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
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
    public static final GTRecipeType VOMAHINE_INDUSTRIAL_CHEMVAT = GTRecipeTypes
            .register("vomahine_industrial_chemvat", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 6, 6)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.CHEMICAL)
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
            .register("vomahine_core_drill", GTRecipeTypes.MULTIBLOCK)
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
        CHEMICAL_RECIPES.onRecipeBuild((builder, provider) -> {
            LARGE_CHEMICAL_RECIPES.copyFrom(builder)
                    .save(provider);
            VOMAHINE_INDUSTRIAL_CHEMVAT.copyFrom(builder)
                    .save(provider);
        });
        LARGE_CHEMICAL_RECIPES.onRecipeBuild((builder, provider) -> {
            VOMAHINE_INDUSTRIAL_CHEMVAT.copyFrom(builder)
                    .save(provider);
        });
    }
}
