package com.ghostipedia.cosmiccore.common.data.recipe;

import com.ghostipedia.cosmiccore.api.recipe.ingredient.TinkerIngredient;

import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import slimeknights.tconstruct.tools.ToolDefinitions;

import java.util.function.Consumer;

public class TinkersRecipeTest {

    public static void init(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "tinkertest",
                new ItemStack(Items.STRING), "BK ", "   ", "   ",
                'B', Items.OAK_PLANKS,
                'K', new TinkerIngredient(ToolDefinitions.SCYTHE));
    }
}
