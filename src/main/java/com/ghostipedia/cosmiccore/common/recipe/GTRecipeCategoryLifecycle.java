package com.ghostipedia.cosmiccore.common.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

public final class GTRecipeCategoryLifecycle {

    private GTRecipeCategoryLifecycle() {}

    public static void clear() {
        for (RecipeType<?> type : BuiltInRegistries.RECIPE_TYPE) {
            if (type instanceof GTRecipeType gtRecipeType) {
                gtRecipeType.getCategoryMap().clear();
            }
        }
    }

    public static void rebuild(Iterable<RecipeHolder<?>> recipes) {
        clear();
        for (RecipeHolder<?> holder : recipes) {
            if (holder.value() instanceof GTRecipe recipe) {
                recipe.recipeCategory.addRecipe(recipe);
            }
        }
    }
}
