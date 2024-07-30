package com.ghostipedia.cosmiccore.gtbridge;


import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class CosmicRecipeTypes {

    public static final GTRecipeType SOUL_TESTER_RECIPES = GTRecipeTypes.register("soul_tester", GTRecipeTypes.MULTIBLOCK)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxIOSize(1, 1, 0, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

    public static final GTRecipeType GROVE_RECIPES = GTRecipeTypes.register("drygmy_grove", GTRecipeTypes.MULTIBLOCK)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxIOSize(2, 9, 1, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType LEACHING_PLANT = GTRecipeTypes.register("leaching_plant", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 6, 3, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType CHROMATIC_FLOTATION_PLANT = GTRecipeTypes.register("chromatic_flotation_plant", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3, 9, 3, 3)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType CELESTIAL_BORE = GTRecipeTypes.register("celestial_bore", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 54, 3, 18)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    public static final GTRecipeType NAQUAHINE_REACTOR = GTRecipeTypes.register("naquahine_reactor", GTRecipeTypes.MULTIBLOCK)
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
    public static final GTRecipeType VOMAHINE_INDUSTRIAL_CHEMVAT = GTRecipeTypes.register("vomahine_industrial_chemvat", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 6, 6, 6)
            .setHasResearchSlot(true)
            .setSound(GTSoundEntries.CHEMICAL)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);

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
