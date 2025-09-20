package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.api.recipe.lookup.MapEmberIngredient;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerDouble;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

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
