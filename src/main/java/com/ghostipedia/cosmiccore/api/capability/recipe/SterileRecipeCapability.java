package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.api.recipe.lookup.MapSterileIngredient;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SterileRecipeCapability extends RecipeCapability<SizedFluidIngredient> {

    public final static SterileRecipeCapability CAP = new SterileRecipeCapability();

    protected SterileRecipeCapability() {
        super("sterile", 0x5E2129FF, true, 10, SerializerFluidIngredient.INSTANCE);
    }

    @Override
    public SizedFluidIngredient copyInner(SizedFluidIngredient content) {
        return SizedIngredientExtensions.copy(content);
    }

    @Override
    public SizedFluidIngredient copyWithModifier(SizedFluidIngredient content, ContentModifier modifier) {
        return FluidRecipeCapability.CAP.copyWithModifier(content, modifier);
    }

    @Override
    public @Nullable List<AbstractMapIngredient> getDefaultMapIngredient(Object ingredient) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (ingredient instanceof FluidStack fluid) ingredients.add(new MapSterileIngredient(fluid));
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
        for (var stack : contents) {
            var sterileIngredient = SterileRecipeCapability.CAP.of(stack.getContent());
            if (isInput) {
                group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                        LocalizationUtils.format("cosmiccore.recipe.sterile_in",
                                sterileIngredient.getFluids()[0].getHoverName().getString(),
                                sterileIngredient.getFluids()[0].getAmount() + (perTick ? "/t" : ""))));
            } else {
                group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                        LocalizationUtils.format("cosmiccore.recipe.sterile_out",
                                sterileIngredient.getFluids()[0].getHoverName().getString(),
                                sterileIngredient.getFluids()[0].getAmount() + (perTick ? "/t" : ""))));
            }

        }
    }
}
