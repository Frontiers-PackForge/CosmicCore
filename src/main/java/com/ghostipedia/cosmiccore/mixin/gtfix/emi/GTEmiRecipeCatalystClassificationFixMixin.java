package com.ghostipedia.cosmiccore.mixin.gtfix.emi;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe.GTEmiRecipe;

import brachy.modularui.integration.emi.EmiStackConverter;
import dev.emi.emi.api.stack.EmiIngredient;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = GTEmiRecipe.class, remap = false)
public abstract class GTEmiRecipeCatalystClassificationFixMixin {

    @Shadow
    @Final
    private GTRecipe recipe;

    @Inject(method = "getInputs", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$excludeCatalystsFromInputs(CallbackInfoReturnable<List<EmiIngredient>> cir) {
        cir.setReturnValue(cosmiccore$ingredients(false));
    }

    public List<EmiIngredient> getCatalysts() {
        return cosmiccore$ingredients(true);
    }

    private List<EmiIngredient> cosmiccore$ingredients(boolean catalysts) {
        List<EmiIngredient> ingredients = new ObjectArrayList<>();

        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            var item = ItemRecipeCapability.CAP.of(content.content());
            boolean catalyst = content.chance() == 0 ||
                    item.ingredient().getCustomIngredient() instanceof IntCircuitIngredient;
            if (catalyst != catalysts) continue;

            float chance = (float) content.chance() / content.maxChance();
            var mapped = ItemRecipeCapability.mapIngredientToEntryList(item);
            ingredients.add(EmiStackConverter.ITEM.convertTo(mapped, chance));
        }

        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            if ((content.chance() == 0) != catalysts) continue;

            float chance = (float) content.chance() / content.maxChance();
            var mapped = FluidRecipeCapability.mapIngredientToEntryList(
                    FluidRecipeCapability.CAP.of(content.content()));
            ingredients.add(EmiStackConverter.FLUID.convertTo(mapped, chance));
        }

        return ingredients;
    }
}
