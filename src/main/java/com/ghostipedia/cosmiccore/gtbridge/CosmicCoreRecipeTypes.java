package com.ghostipedia.cosmiccore.gtbridge;


import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;

public class CosmicCoreRecipeTypes {

    public static final GTRecipeType SOUL_TESTER_RECIPES = GTRecipeTypes.register("soul_tester", GTRecipeTypes.MULTIBLOCK)
            .setMaxSize(IO.IN, SoulRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, SoulRecipeCapability.CAP, 1)
            .setMaxSize(IO.IN, ItemRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, ItemRecipeCapability.CAP, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setMaxTooltips(1);


    public static void init() {
    }
}
