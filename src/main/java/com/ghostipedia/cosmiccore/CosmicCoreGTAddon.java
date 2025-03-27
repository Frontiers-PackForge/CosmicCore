package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.data.CosmicCoreMaterialIconType;
import com.ghostipedia.cosmiccore.api.data.CosmicCustomTags;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicElements;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicCoreOreRecipeHandler;
import com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipes;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

import com.mojang.datafixers.util.Pair;

import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.integration.kjs.recipe.components.CosmicRecipeComponent.SOUL_IN;
import static com.ghostipedia.cosmiccore.integration.kjs.recipe.components.CosmicRecipeComponent.SOUL_OUT;

@GTAddon
public class CosmicCoreGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return CosmicRegistration.REGISTRATE;
    }

    @Override
    public void registerTagPrefixes() {
        CosmicCoreMaterialIconType.init();
        CosmicCustomTags.initTagPrefixes();
    }

    @Override
    public void initializeAddon() {
        CosmicCore.LOGGER.info("CosmicCoreGTAddon has loaded!");
    }

    @Override
    public void registerElements() {
        IGTAddon.super.registerElements();
        CosmicElements.init();
    }

    @Override
    public String addonModId() {
        return CosmicCore.MOD_ID;
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        CosmicRecipeTypes.init();
        CosmicCoreRecipes.init(provider);
        CosmicCoreOreRecipeHandler.init(provider);
    }

    @Override
    public void registerRecipeCapabilities() {
        CosmicRecipeCapabilities.init();
    }

    @Override
    public void registerRecipeKeys(KJSRecipeKeyEvent event) {
        event.registerKey(CosmicRecipeCapabilities.SOUL, Pair.of(SOUL_IN, SOUL_OUT));
    }
}
