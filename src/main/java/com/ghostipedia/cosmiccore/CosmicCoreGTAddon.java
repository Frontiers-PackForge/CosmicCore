package com.ghostipedia.cosmiccore;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import com.lowdragmc.lowdraglib.LDLib;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Consumer;


@GTAddon
public class CosmicCoreGTAddon implements IGTAddon {
    @Override
    public void initializeAddon() {
        CosmicCore.LOGGER.info("CosmicCoreGTAddon has loaded!");
    }

    @Override
    public String addonModId() {
        return CosmicCore.MOD_ID;
    }
}
