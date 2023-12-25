package com.ghostipedia.cosmiccore.gtbridge;


import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;

public class CosmicCoreRecipeTypes {

    public final static GTRecipeType ALTERNATOR_MACHINE = GTRecipeTypes.register("alternator_machine", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 1, 0, 0)
            //.prepareBuilder(gtRecipeBuilder -> gtRecipeBuilder.EUt(4))
            .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_BATH, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setMaxTooltips(1)
            .setSound(GTSoundEntries.COOLING);


    public static void init() {
    }
}
