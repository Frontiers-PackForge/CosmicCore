package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicCoreOreRecipeHandler;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicGemQualityRecipeHandler;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicWoods;
import com.ghostipedia.cosmiccore.common.data.worldgen.CosmicWorldGenLayers;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.CosmicVeinGenerators;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.data.recipe.WoodTypeEntry;
import com.gregtechceu.gtceu.data.recipe.misc.WoodMachineRecipes;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;

import net.minecraft.data.recipes.RecipeOutput;

import dev.latvian.mods.kubejs.recipe.component.NumberComponent;

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
        CosmicGemQualityRecipeHandler.register(provider);
        CosmicCoreOreRecipeHandler.registerFlocculant(provider);
        CosmicCoreOreRecipeHandler.registerCrystallization(provider);
        for (Material bundleOre : CosmicBundleMaterials.bundleOres()) {
            CosmicCoreOreRecipeHandler.bundleInit(provider, bundleOre);
        }
        for (Material metal : CosmicBundleMaterials.outputMaterials()) {
            CosmicCoreOreRecipeHandler.processChunkBasics(provider, metal);
        }
        for (WoodTypeEntry wood : CosmicWoods.entries()) {
            WoodMachineRecipes.registerWoodTypeRecipe(provider, wood);
        }
        CosmicWoods.registerLogToPlankRecipes(provider);
    }

    @Override
    public void registerRecipeKeys(KJSRecipeKeyEvent event) {
        // TODO(cosmiccore-42.14): re-register the SOUL recipe key once the KubeJS integration
        // (integration.kjs.recipe.components.CosmicRecipeComponent) is ported to the 1.21 KJS API.
        event.registerKey(CosmicRecipeCapabilities.EMBER,
                ContentJS.create(NumberComponent.NON_NEGATIVE_DOUBLE, CosmicRecipeCapabilities.EMBER));
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
