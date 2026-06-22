package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicCoreOreRecipeHandler;
import com.ghostipedia.cosmiccore.common.data.worldgen.CosmicWorldGenLayers;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.CosmicVeinGenerators;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.RecipeOutput;

import java.util.HashSet;
import java.util.Set;

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
        Set<Material> chunkMetals = new HashSet<>();
        for (Material bundleOre : CosmicBundleMaterials.bundleOres()) {
            CosmicCoreOreRecipeHandler.bundleInit(provider, bundleOre);
            chunkMetals.addAll(CosmicBundleMaterials.outputsOf(bundleOre));
        }
        for (Material metal : chunkMetals) {
            CosmicCoreOreRecipeHandler.processChunkBasics(provider, metal);
        }
    }

    @Override
    public void registerRecipeKeys(KJSRecipeKeyEvent event) {
        // TODO(cosmiccore-42.14): re-register the SOUL recipe key once the KubeJS integration
        // (integration.kjs.recipe.components.CosmicRecipeComponent) is ported to the 1.21 KJS API.
    }

    @Override
    public void registerWorldgenLayers() {
        CosmicWorldGenLayers.init();
    }

    @Override
    public void registerVeinGenerators() {
        CosmicVeinGenerators.init();
    }
}
