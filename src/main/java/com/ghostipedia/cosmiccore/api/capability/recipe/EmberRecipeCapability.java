package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableEmberContainer;
import com.ghostipedia.cosmiccore.api.recipe.lookup.MapEmberIngredient;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerDouble;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.recipe.RecipeHelper.addToRecipeHandlerMap;

public class EmberRecipeCapability extends RecipeCapability<Double> {

    public static final EmberRecipeCapability CAP = new EmberRecipeCapability();

    protected EmberRecipeCapability() {
        super("ember", 0xFFFF9900, true, 12, SerializerDouble.INSTANCE);
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
        // TODO: Figure out what it needs to do
        return super.compressIngredients(ingredients);
    }

    private static Double getInputContents(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        if (handlerLists.isEmpty()) return 0d;

        Double total = 0d;

        for (var handlerList : handlerLists) {
            if (!handlerList.hasCapability(EmberRecipeCapability.CAP)) continue;
            var emberHandlers = handlerList.getCapability(EmberRecipeCapability.CAP);
            for(var handler : emberHandlers){
                var emberHandler = (NotifiableEmberContainer) handler;
                for(var content : handler.getContents()){
                    // At most, an ember hatch can contribute the minimum of the max allowed consumption per tick, or the current amount stored
                    total += Math.min((Double) content, emberHandler.getMaxConsumption());
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

        // Find all the items in the combined Item Input inventories and create oversized ItemStacks
        double totalEmberInHatches = getInputContents(holder);
        if (totalEmberInHatches == 0) return 0;

        // map the recipe ingredients to account for duplicated and notConsumable ingredients.
        // notConsumable ingredients are not counted towards the max ratio
        var nonConsumable = 0d;
        var consumable = 0d;
        for (Content content : inputs) {
            double required = (Double) content.content;

            if (content.chance == 0) {
                nonConsumable += required;
            } else {
                consumable += required;
            }
        }

        if (consumable == 0 && nonConsumable == 0) return limit;

        if(nonConsumable > totalEmberInHatches) return 0;
        if(consumable == 0) return limit;
        return (int) Math.min(limit, totalEmberInHatches / consumable);
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipe recipe, List<Content> contents, boolean perTick,
                           boolean isInput, MutableInt yOffset) {
        double ember = contents.stream().map(Content::getContent).mapToDouble(EmberRecipeCapability.CAP::of).sum();
        if (isInput) {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    Component.translatable("cosmiccore.recipe.ember_in", ember)));
        } else {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    Component.translatable("cosmiccore.recipe.ember_out", ember)));
        }
    }
}
