package com.ghostipedia.cosmiccore.integration.recipes.emi;

import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AsteroidEmiRecipe implements EmiRecipe {

    private static final int W = 150;
    private static final int H = 30;

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    @Getter
    private final EmiStack icon;

    public AsteroidEmiRecipe(EmiRecipeCategory category, ResourceLocation id, List<EmiIngredient> inputs,
                             List<EmiStack> outputs, EmiStack icon) {
        this.category = category;
        this.id = id;
        this.inputs = inputs;
        this.outputs = outputs;
        this.icon = icon;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return W;
    }

    @Override
    public int getDisplayHeight() {
        return H;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, (W / 2 - 6), (H / 2 - 3));
        widgets.addSlot(inputs.get(0), 10, H / 2 - 5).drawBack(true);
        widgets.addSlot(inputs.get(1), 30, H / 2 - 5).drawBack(true);

        int ox = 100;
        int oy = H / 2 - 5;

        for (int i = 0; i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), ox + (i * 20), oy).recipeContext(this);
        }

        widgets.addText(icon.getItemStack().getHoverName(), 0, 0, 0xFFFFFF, true);
    }
}
