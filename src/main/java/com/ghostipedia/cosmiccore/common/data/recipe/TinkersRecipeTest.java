package com.ghostipedia.cosmiccore.common.data.recipe;

import com.ghostipedia.cosmiccore.api.recipe.ingredient.TinkerIngredient;
import com.ghostipedia.cosmiccore.common.data.tag.TagUtil;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.ToolDefinitions;

import java.util.function.Consumer;

public class TinkersRecipeTest {

    public static void init(Consumer<FinishedRecipe> provider) {
        CosmicVanillaRecipeHelper.addShapedTinkerRecipe(provider, "tinkertest",
                new ItemStack(Items.STRING), "BK ", "   ", "   ",
                'B', Items.OAK_PLANKS,
                'K', new TinkerIngredient(ToolDefinitions.SCYTHE));
    }

}
