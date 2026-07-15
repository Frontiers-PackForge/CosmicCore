package com.ghostipedia.cosmiccore.common.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

public final class GTRecipeReloadLifecycle {

    private GTRecipeReloadLifecycle() {}

    public static void clearCategories() {
        for (RecipeType<?> type : BuiltInRegistries.RECIPE_TYPE) {
            if (type instanceof GTRecipeType gtRecipeType) {
                gtRecipeType.getCategoryMap().clear();
            }
        }
    }

    public static void resetForReload() {
        PowerlessJetpack.FUELS.clear();
        for (RecipeType<?> type : BuiltInRegistries.RECIPE_TYPE) {
            if (type instanceof GTRecipeType gtRecipeType) {
                gtRecipeType.db().clear();
                gtRecipeType.getCategoryMap().clear();
            }
        }
    }

    public static void rebuildCategories(Iterable<RecipeHolder<?>> recipes) {
        clearCategories();
        for (RecipeHolder<?> holder : recipes) {
            if (holder.value() instanceof GTRecipe recipe) {
                recipe.recipeCategory.addRecipe(recipe);
            }
        }
    }
}
