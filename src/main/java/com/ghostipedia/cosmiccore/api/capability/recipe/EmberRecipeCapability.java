package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableEmberContainer;
import com.ghostipedia.cosmiccore.api.recipe.lookup.MapEmberIngredient;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerDouble;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class EmberRecipeCapability extends RecipeCapability<Double> {

    public static final EmberRecipeCapability CAP = new EmberRecipeCapability();

    protected EmberRecipeCapability() {
        super(CosmicCore.id("ember"), 0xFFFF9900, true, 12, SerializerDouble.INSTANCE);
    }

    @Override
    public Double copyInner(Double content) {
        return content;
    }

    @Override
    public Double copyWithModifier(Double content, ContentModifier modifier) {
        return modifier.apply(content);
    }

    @Override
    public @Nullable List<AbstractMapIngredient> getDefaultMapIngredient(Object ingredient) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (ingredient instanceof Double ember) ingredients.add(new MapEmberIngredient(ember));
        return ingredients;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        return super.compressIngredients(ingredients);
    }

    private static double getInputContents(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        if (handlerLists.isEmpty()) return 0d;

        double total = 0d;

        for (var handlerList : handlerLists) {
            if (!handlerList.hasCapability(EmberRecipeCapability.CAP)) continue;
            var emberHandlers = handlerList.getCapability(EmberRecipeCapability.CAP);
            for (var handler : emberHandlers) {
                var emberHandler = (NotifiableEmberContainer) handler;
                for (var content : handler.getContents()) {
                    total += Math.min((double) content, emberHandler.getMaxConsumption());
                }
            }
        }
        return total;
    }

    @Override
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) {
        if (!holder.hasCapabilityProxies()) return 0;

        var inputs = (tick ? recipe.tickInputs : recipe.inputs).get(this);
        if (inputs == null || inputs.isEmpty()) return limit;

        double totalEmberInHatches = getInputContents(holder);
        if (totalEmberInHatches == 0) return 0;

        var nonConsumable = 0d;
        var consumable = 0d;
        for (Content content : inputs) {
            double required = (double) content.content();

            if (content.chance() == 0) {
                nonConsumable += required;
            } else {
                consumable += required;
            }
        }

        if (consumable == 0 && nonConsumable == 0) return limit;

        if (nonConsumable > totalEmberInHatches) return 0;
        if (consumable == 0) return limit;
        return (int) Math.min(limit, (totalEmberInHatches - nonConsumable) / consumable);
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    // TODO(8.0.0): re-add XEI display via the new XEI category API.
    // RecipeCapability#addXEIInfo was removed in 8.0.0; the original LDLib LabelWidget rendering
    // (ember_in / ember_out) lived here and needs reimplementing against the new XEI category hook.
}
