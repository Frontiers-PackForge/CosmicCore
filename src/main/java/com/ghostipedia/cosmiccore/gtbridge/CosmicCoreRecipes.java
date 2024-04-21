package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipeTypes.SOUL_TESTER_RECIPES;

public class CosmicCoreRecipes {


    public static void init (Consumer<FinishedRecipe> provider) {
        SOUL_TESTER_RECIPES.recipeBuilder("soul_to_dirt")
                .inputItems(Blocks.DIRT.asItem())
                .output(SoulRecipeCapability.CAP, 10)
                .duration(60)
                .save(provider);
    }

}
