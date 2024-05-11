package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.gregtechceu.gtceu.api.GTValues;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipeTypes.GROVE_RECIPES;

public class CosmicCoreRecipes {


    public static void init (Consumer<FinishedRecipe> provider) {
//        SOUL_TESTER_RECIPES.recipeBuilder("dirt_to_soul")
//                .inputItems(Blocks.DIRT.asItem(), 1)
//                .output(SoulRecipeCapability.CAP, 1000000)
//                .duration(10)
//                .save(provider);
//
//        SOUL_TESTER_RECIPES.recipeBuilder("soul_to_diamond")
//                .input(SoulRecipeCapability.CAP, 7000000)
//                .outputItems(Items.DRAGON_EGG, 1)
//                .duration(10)
//                .save(provider);
        GROVE_RECIPES.recipeBuilder("dirt_movement")
                .notConsumable(CosmicItems.DONK)
                .notConsumable(Items.ZOMBIE_HEAD)
                .outputItems(Items.ROTTEN_FLESH, 1)
                .duration(20)
                .inputEU(GTValues.HV)
                .save(provider);
    }

}
