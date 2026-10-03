package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.data.recipe.BlastingRecipeCompatibility;

import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.generated.MaterialRecipeHandler;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = MaterialRecipeHandler.class, remap = false)
public abstract class MaterialRecipeHandlerBlastingFixMixin {

    @Redirect(
              method = "processDust",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/data/recipe/VanillaRecipeHelper;addSmeltingRecipe(Lnet/minecraft/data/recipes/RecipeOutput;Ljava/lang/String;Lnet/minecraft/tags/TagKey;Lnet/minecraft/world/item/ItemStack;)V"),
              require = 3)
    private static void cosmiccore$addEligibleDustBlasting(RecipeOutput provider, String id, TagKey<Item> input,
                                                           ItemStack output) {
        VanillaRecipeHelper.addSmeltingRecipe(provider, id, input, output);
        if (BlastingRecipeCompatibility.permits(output)) {
            VanillaRecipeHelper.addBlastingRecipe(provider, id, input, output);
        }
    }
}
