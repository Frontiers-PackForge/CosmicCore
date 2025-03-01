package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.api.noctyx.NoctyxStack;
import com.ghostipedia.cosmiccore.api.noctyx.NoctyxType;
import com.ghostipedia.cosmiccore.api.recipe.lookup.MapNoctyxIngredient;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NoctyxRecipeCapability extends RecipeCapability<NoctyxStack> {

    public static final NoctyxRecipeCapability CAP = new NoctyxRecipeCapability();

    protected NoctyxRecipeCapability() {
        super("noctyx", 0xFF5538FF, true, 11, NoctyxStack.SERIALIZER);
    }

    @Override
    public NoctyxStack copyInner(NoctyxStack content) {
        return content.copy();
    }

    @Override
    public NoctyxStack copyWithModifier(NoctyxStack content, ContentModifier modifier) {
        return content.copyAmount(modifier.apply(content.getAmount()));
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object ingredient) {
        var ingredients = new ObjectArrayList<AbstractMapIngredient>();
        if (ingredient instanceof NoctyxStack[] stacks) {
            ingredients.add(new MapNoctyxIngredient(stacks));
        }
        return ingredients;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        var ingredientMap = new Object2IntOpenHashMap<NoctyxType>();
        for (var item : ingredients) {
            if (item instanceof NoctyxStack[] ingredient) {
                for (var stack : ingredient) {
                    ingredientMap.compute(stack.getType(),
                            (type, amount) -> (amount == null ? 0 : amount) + stack.getAmount());
                }
            } else if (item instanceof MapNoctyxIngredient ingredient) {
                for (var stack : ingredient.getStacks()) {
                    ingredientMap.compute(stack.getType(),
                            (type, amount) -> (amount == null ? 0 : amount) + stack.getAmount());
                }
            }
        }
        return ingredientMap.object2IntEntrySet().stream()
                .map(e -> new NoctyxStack(e.getKey(), e.getIntValue()))
                .collect(ArrayList::new, List::add, ArrayList::addAll);
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    protected static final String xeiLangPrefix = "cosmiccore.recipe.noctyx.";

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipe recipe, List<Content> contents, boolean perTick,
                           boolean isInput, MutableInt yOffset) {
        final var lang = xeiLangPrefix + (isInput ? "input" : "output");
        contents.stream().map(Content::getContent).map(NoctyxRecipeCapability.CAP::of)
                .forEach(stack -> group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                        Component.translatable(lang, stack.displayName()))));
    }
}
