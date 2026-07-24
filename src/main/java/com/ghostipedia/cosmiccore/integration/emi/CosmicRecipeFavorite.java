package com.ghostipedia.cosmiccore.integration.emi;

import com.ghostipedia.cosmiccore.client.gui.CompactAmountRenderer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.material.Fluid;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiFavorite;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CosmicRecipeFavorite extends EmiFavorite {

    public record InputEntry(EmiIngredient stack, long amount) {}

    private final String bookmarkId;
    private final @Nullable String recipeId;
    private final List<InputEntry> inputs;
    private final long outputAmount;

    public CosmicRecipeFavorite(String bookmarkId, @Nullable String recipeId, EmiIngredient output, long outputAmount,
                                List<InputEntry> inputs, @Nullable EmiRecipe recipe) {
        super(output, recipe);
        this.bookmarkId = bookmarkId;
        this.recipeId = recipeId;
        this.outputAmount = Math.max(1, outputAmount);
        this.inputs = inputs.stream()
                .map(input -> new InputEntry(input.stack().copy(), Math.max(1, input.amount())))
                .toList();
    }

    public String getBookmarkId() {
        return bookmarkId;
    }

    public @Nullable String getRecipeId() {
        return recipeId;
    }

    public List<InputEntry> getInputs() {
        return inputs;
    }

    public long getOutputAmount() {
        return outputAmount;
    }

    @Override
    public long getAmount() {
        return outputAmount;
    }

    @Override
    public void render(GuiGraphics raw, int x, int y, float delta, int flags) {
        EmiDrawContext context = EmiDrawContext.wrap(raw);
        context.fill(x - 1, y - 1, 18, 1, 0xB0FFD700);
        context.fill(x - 1, y + 16, 18, 1, 0xB0FFD700);
        context.fill(x - 1, y - 1, 1, 18, 0xB0FFD700);
        context.fill(x + 16, y - 1, 1, 18, 0xB0FFD700);
        getStack().render(raw, x, y, delta, flags & ~EmiIngredient.RENDER_AMOUNT);
        renderCompactAmount(raw, x, y, outputAmount);
        if ((flags & EmiIngredient.RENDER_INGREDIENT) != 0 && getRecipe() != null) {
            EmiRenderHelper.renderRecipeFavorite(getStack(), context, x, y);
        }
    }

    private void renderCompactAmount(GuiGraphics graphics, int x, int y, long amount) {
        if (amount <= 1) return;
        boolean fluid = !getStack().getEmiStacks().isEmpty() &&
                getStack().getEmiStacks().get(0).getKey() instanceof Fluid;
        if (fluid) {
            CompactAmountRenderer.drawFluidAmount(graphics, x, y, 16, 16, amount);
        } else {
            CompactAmountRenderer.drawItemAmount(graphics, x, y, 16, 16, amount);
        }
    }

    @Override
    public EmiIngredient copy() {
        return new CosmicRecipeFavorite(bookmarkId, recipeId, getStack().copy(), outputAmount, inputs, getRecipe());
    }

    @Override
    public boolean strictEquals(EmiIngredient other) {
        if (other instanceof CosmicRecipeFavorite recipeFavorite) {
            return bookmarkId.equals(recipeFavorite.bookmarkId);
        }
        return super.strictEquals(other);
    }
}
