package com.ghostipedia.cosmiccore;


import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistries;
import com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipeTypes;
import com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipes;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

@GTAddon
public class CosmicCoreGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return CosmicRegistries.REGISTRATE;
    }

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
        CosmicCoreRecipes.init(provider);
    }


    public static final ContentJS<Integer> SOUL_IN = new ContentJS<>(NumberComponent.INT, CosmicRecipeCapabilities.SOUL, false);
    public static final ContentJS<Integer> SOUL_OUT = new ContentJS<>(NumberComponent.INT, CosmicRecipeCapabilities.SOUL, true);

    @Override
    public void registerRecipeKeys(KJSRecipeKeyEvent event) {
        event.registerKey(CosmicRecipeCapabilities.SOUL, Pair.of(SOUL_IN, SOUL_OUT));
    }
}