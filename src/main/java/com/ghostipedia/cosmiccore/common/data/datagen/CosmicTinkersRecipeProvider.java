package com.ghostipedia.cosmiccore.common.data.datagen;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.tinkers.TinkersMaterial;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import slimeknights.mantle.recipe.data.IRecipeHelper;
import slimeknights.tconstruct.library.data.recipe.IMaterialRecipeHelper;

import java.util.function.Consumer;

import static slimeknights.tconstruct.library.recipe.material.MaterialRecipeBuilder.materialRecipe;

public class CosmicTinkersRecipeProvider extends RecipeProvider implements IRecipeHelper, IMaterialRecipeHelper {

    public CosmicTinkersRecipeProvider(PackOutput generator) {
        super(generator);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        for (TinkersMaterial tinkersMaterial : TinkersMaterial.MATERIALS) {
            String folder = "tools/material/";
            materialRecipe(consumer, tinkersMaterial.getMaterialLocation(), tinkersMaterial.getIngredient(),
                    tinkersMaterial.getValue(), tinkersMaterial.getNeeded(), folder + tinkersMaterial.getName());
        }
    }

    @Override
    public String getModId() {
        return CosmicCore.MOD_ID;
    }
}
