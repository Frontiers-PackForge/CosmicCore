package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.mojang.serialization.Codec;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class SoulRecipeCapability extends RecipeCapability<SoulIngredient> {

    public final static SoulRecipeCapability CAP = new SoulRecipeCapability();

    protected SoulRecipeCapability() {
        super("soul", 0x5E2129FF, true, 10, SerializerSoulIngredient.INSTANCE);
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    //TODO: try to remove
    @Override
    public SoulIngredient copyInner(SoulIngredient content) {
        return super.copyInner(content);
    }

    @Override
    public SoulIngredient copyWithModifier(SoulIngredient content, ContentModifier modifier) {
        var modifiedStack = content.stack().withAmount(modifier.apply(content.stack().amount()));
        return SoulIngredient.of(modifiedStack);
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        List<Object> list = new ArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof SoulIngredient soul) {
                var isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof SoulIngredient soulIngredient && soul.equals(soulIngredient)) {
                        isEqual = true;
                        break;
                    }
                }
                if (isEqual) continue;
                list.add(item);
            }
        }
        return list;
    }

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipe recipe, List<Content> contents, boolean perTick,
                           boolean isInput, MutableInt yOffset) {

        String type = contents.stream().map(Content::getContent).map(SoulRecipeCapability.CAP::of).map(SoulIngredient::stack).map(SoulStack::type).map(SoulType::getSerializedName).findFirst().orElse("");
        long soul = contents.stream().map(Content::getContent).map(SoulRecipeCapability.CAP::of).map(SoulIngredient::stack).mapToLong(SoulStack::amount).sum();
        if (isInput) {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("recipe.cosmiccore." + type + "_soul_in", soul)));
        } else {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("recipe.cosmiccore." + type + "_soul_out", soul)));
        }
    }

    private static class SerializerSoulIngredient implements IContentSerializer<SoulIngredient> {

        public static SerializerSoulIngredient INSTANCE = new SerializerSoulIngredient();

        @Override
        public SoulIngredient of(Object o) {
            if (o instanceof SoulStack stack) return SoulIngredient.of(stack);
            else if (o instanceof SoulIngredient ingredient) return  ingredient;
            return SoulIngredient.of(new SoulStack(SoulType.Raw, 0));
        }

        @Override
        public SoulIngredient defaultValue() {
            return SoulIngredient.of(new SoulStack(SoulType.Raw, 0));
        }

        @Override
        public Class<SoulIngredient> contentClass() {
            return SoulIngredient.class;
        }

        @Override
        public Codec<SoulIngredient> codec() {
            return SoulIngredient.CODEC;
        }
    }
}
