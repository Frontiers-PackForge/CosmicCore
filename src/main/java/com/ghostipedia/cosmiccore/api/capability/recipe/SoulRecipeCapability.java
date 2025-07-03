package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerInteger;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Collection;
import java.util.List;

public class SoulRecipeCapability extends RecipeCapability<Integer> {

    public final static SoulRecipeCapability CAP = new SoulRecipeCapability();

    protected SoulRecipeCapability() {
        super("soul", 0x5E2129FF, true, 10, SerializerInteger.INSTANCE);
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public Integer copyWithModifier(Integer content, ContentModifier modifier) {
        return modifier.apply(content);
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
        int soul = contents.stream().map(Content::getContent).mapToInt(SoulRecipeCapability.CAP::of).sum();
        if (isInput) {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("cosmiccore.recipe.soulIn", soul)));
        } else {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("cosmiccore.recipe.soulOut", soul)));
        }
    }
}
