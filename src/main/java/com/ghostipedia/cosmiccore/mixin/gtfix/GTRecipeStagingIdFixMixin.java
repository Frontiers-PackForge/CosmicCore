package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;

import net.minecraft.world.item.crafting.RecipeHolder;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = RecipeManagerHandler.class, remap = false)
public abstract class GTRecipeStagingIdFixMixin {

    @ModifyArg(
               method = "addRecipesToLookup",
               at = @At(
                        value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/api/recipe/lookup/RecipeAdditionHandler;addStaging(Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;)V"),
               index = 0,
               require = 1)
    private static GTRecipe cosmiccore$restoreRecipeHolderId(GTRecipe recipe, @Local RecipeHolder<?> holder) {
        recipe.setId(holder.id());
        return recipe;
    }
}
