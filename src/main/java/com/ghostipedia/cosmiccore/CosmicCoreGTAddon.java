package com.ghostipedia.cosmiccore;


import com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipeTypes;
import com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipes;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import net.minecraft.data.recipes.FinishedRecipe;

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

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        CosmicCoreRecipeTypes.init();
        CosmicCoreRecipes.init(provider);
        IGTAddon.super.addRecipes(provider);
    }
}