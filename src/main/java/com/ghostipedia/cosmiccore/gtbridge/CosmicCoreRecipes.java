package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipeTypes.SOUL_TESTER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Oxygen;

public class CosmicCoreRecipes {


    public static void init(Consumer<FinishedRecipe> provider) {
        MultiBlockRecipes(provider);
    }


    private static void MultiBlockRecipes(Consumer<FinishedRecipe> provider) {
        SOUL_TESTER_RECIPES.recipeBuilder("soul_to_dirt")
                .inputItems(Blocks.DIRT.asItem())
                .outputFluids(Oxygen.getFluid(1)) //Make sure this is correct
                .duration(60)
                .save(provider);
    }

}
