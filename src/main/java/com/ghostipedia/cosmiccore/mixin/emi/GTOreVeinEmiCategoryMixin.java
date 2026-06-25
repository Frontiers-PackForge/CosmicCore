package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.integration.recipeviewer.emi.orevein.GTOreVeinEmiCategory;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GTOreVeinEmiCategory.class, remap = false)
public class GTOreVeinEmiCategoryMixin {

    @Redirect(method = "registerDisplays",
              remap = false,
              at = @At(value = "INVOKE",
                       target = "Ldev/emi/emi/api/EmiRegistry;addRecipe(Ldev/emi/emi/api/recipe/EmiRecipe;)V"))
    private static void cosmiccore$onlyCosmicVeins(EmiRegistry registry, EmiRecipe recipe) {
        if (recipe.getId() != null && recipe.getId().getNamespace().equals(CosmicCore.MOD_ID)) {
            registry.addRecipe(recipe);
        }
    }
}
