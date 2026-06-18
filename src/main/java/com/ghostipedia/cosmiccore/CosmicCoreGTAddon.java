package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.data.worldgen.CosmicWorldGenLayers;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.CosmicVeinGenerators;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.RecipeOutput;

import static com.ghostipedia.cosmiccore.integration.kjs.recipe.components.CosmicRecipeComponent.SOUL_IN;

@GTAddon(CosmicCore.MOD_ID)
public class CosmicCoreGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return CosmicRegistration.REGISTRATE;
    }

    @Override
    public void gtInitComplete() {
        CosmicCore.LOGGER.info("CosmicCoreGTAddon has loaded!");
    }

    @Override
    public void addRecipes(RecipeOutput provider) {
        // TODO(recipe-gen / bead 42.9): migrate CosmicCoreRecipes + CosmicCoreOreRecipeHandler +
        // CosmicMaterialRecipeHandlers from Consumer<FinishedRecipe> to 1.21 RecipeOutput, then re-enable:
        // CosmicCoreRecipes.init(provider);
        // for (var material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
        //     CosmicCoreOreRecipeHandler.init(provider, material);
        //     CosmicMaterialRecipeHandlers.init(provider, material);
        // }
    }

    @Override
    public void registerRecipeKeys(KJSRecipeKeyEvent event) {
        // 8.0: registerKey takes a single ContentJS (was Pair<in,out>); in/out handled by schema soulInput/soulOutput.
        event.registerKey(CosmicRecipeCapabilities.SOUL, SOUL_IN);
    }

    @Override
    public void registerWorldgenLayers() {
        CosmicWorldGenLayers.init();
        CosmicWorldGenLayers.migrateOreVeinsToOverworldLayer();
    }

    @Override
    public void registerVeinGenerators() {
        CosmicVeinGenerators.init();
    }
}
