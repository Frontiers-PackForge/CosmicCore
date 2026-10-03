package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.SimpleCookingRecipeBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.BlastingRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = VanillaRecipeHelper.class, remap = false)
public abstract class VanillaRecipeHelperBlastingFixMixin {

    @Redirect(
              method = "addBlastingRecipe(Lnet/minecraft/data/recipes/RecipeOutput;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/item/crafting/Ingredient;Lnet/minecraft/world/item/ItemStack;F)V",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/data/recipe/builder/SimpleCookingRecipeBuilder;smelting(Lnet/minecraft/resources/ResourceLocation;)Lcom/gregtechceu/gtceu/data/recipe/builder/SimpleCookingRecipeBuilder;"))
    private static SimpleCookingRecipeBuilder<BlastingRecipe> cosmiccore$useIngredientBlastingBuilder(
                                                                                                      ResourceLocation id) {
        return SimpleCookingRecipeBuilder.blasting(id);
    }

    @Redirect(
              method = "addBlastingRecipe(Lnet/minecraft/data/recipes/RecipeOutput;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/tags/TagKey;Lnet/minecraft/world/item/ItemStack;F)V",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/data/recipe/builder/SimpleCookingRecipeBuilder;smelting(Lnet/minecraft/resources/ResourceLocation;)Lcom/gregtechceu/gtceu/data/recipe/builder/SimpleCookingRecipeBuilder;"))
    private static SimpleCookingRecipeBuilder<BlastingRecipe> cosmiccore$useTagBlastingBuilder(ResourceLocation id) {
        return SimpleCookingRecipeBuilder.blasting(id);
    }
}
