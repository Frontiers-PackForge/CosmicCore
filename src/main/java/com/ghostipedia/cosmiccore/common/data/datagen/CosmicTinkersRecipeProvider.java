package com.ghostipedia.cosmiccore.common.data.datagen;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.tinkers.TinkersMaterial;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import slimeknights.mantle.recipe.data.IRecipeHelper;


import java.util.function.Consumer;

public class CosmicTinkersRecipeProvider extends RecipeProvider implements IRecipeHelper, IConditionBuilder {
    public CosmicTinkersRecipeProvider(PackOutput generator) {
        super(generator);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {

        for(TinkersMaterial tinkersMaterial : TinkersMaterial.MATERIALS){



        }

    }

    @Override
    public String getModId() {
        return CosmicCore.MOD_ID;
    }
}
