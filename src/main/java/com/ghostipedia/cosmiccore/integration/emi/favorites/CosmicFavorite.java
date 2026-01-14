package com.ghostipedia.cosmiccore.integration.emi.favorites;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiFavorite;
import org.jetbrains.annotations.Nullable;

public class CosmicFavorite extends EmiFavorite {

    private long customAmount;
    private boolean hasCustomAmount;

    public CosmicFavorite(EmiIngredient stack, @Nullable EmiRecipe recipe, long customAmount) {
        super(stack, recipe);
        this.customAmount = customAmount;
        this.hasCustomAmount = true;
    }

    public CosmicFavorite(EmiIngredient stack, @Nullable EmiRecipe recipe) {
        super(stack, recipe);
        this.customAmount = stack.getAmount();
        this.hasCustomAmount = false;
    }

    public void setCustomAmount(long amount) {
        this.customAmount = Math.max(1, amount);
        this.hasCustomAmount = true;
    }

    public void adjustAmount(long delta) {
        if (!hasCustomAmount) {
            this.customAmount = getStack().getAmount();
            this.hasCustomAmount = true;
        }
        this.customAmount = Math.max(1, this.customAmount + delta);
    }

    public static CosmicFavorite fromEmiFavorite(EmiFavorite favorite, long amount) {
        return new CosmicFavorite(favorite.getStack(), favorite.getRecipe(), amount);
    }

    public static CosmicFavorite withAmount(EmiIngredient stack, @Nullable EmiRecipe recipe, long amount) {
        return new CosmicFavorite(stack, recipe, amount);
    }

    @Override
    public long getAmount() {
        return hasCustomAmount ? customAmount : super.getAmount();
    }

    public long getCustomAmount() {
        return customAmount;
    }

    public boolean hasCustomAmount() {
        return hasCustomAmount;
    }

    @Override
    public void render(GuiGraphics raw, int x, int y, float delta, int flags) {
        EmiDrawContext context = EmiDrawContext.wrap(raw);

        if (hasCustomAmount) {
            getStack().render(context.raw(), x, y, delta, flags & ~EmiIngredient.RENDER_AMOUNT);
            renderCompactAmount(context, x, y, customAmount);

            if ((flags & EmiIngredient.RENDER_INGREDIENT) != 0 && getRecipe() != null) {
                EmiRenderHelper.renderRecipeFavorite(getStack(), context, x, y);
            }
        } else {
            super.render(raw, x, y, delta, flags);
        }
    }

    private void renderCompactAmount(EmiDrawContext context, int x, int y, long amount) {
        String text = formatCompact(amount);
        Component component = EmiPort.literal(text);
        Minecraft client = Minecraft.getInstance();
        int textWidth = client.font.width(component);

        context.push();
        context.matrices().translate(0, 0, 200);

        float scale = 0.5f;
        context.matrices().translate(x + 16, y + 16, 0);
        context.matrices().scale(scale, scale, 1);

        int tx = -textWidth;
        int ty = -client.font.lineHeight;

        context.drawTextWithShadow(component, tx, ty, 0xFFFFFF);
        context.pop();
    }

    private String formatCompact(long amount) {
        EmiIngredient stack = getStack();
        if (!stack.getEmiStacks().isEmpty()) {
            EmiStack first = stack.getEmiStacks().get(0);
            if (first.getKey() instanceof Fluid) {
                if (amount >= 1000) {
                    double buckets = amount / 1000.0;
                    if (buckets >= 1_000_000_000) {
                        return String.format("%.1fBB", buckets / 1_000_000_000);
                    } else if (buckets >= 1_000_000) {
                        return String.format("%.1fMB", buckets / 1_000_000);
                    } else if (buckets >= 1000) {
                        return String.format("%.1fKB", buckets / 1000);
                    } else {
                        return String.format("%.1fB", buckets);
                    }
                }
                return amount + "mB";
            }
        }
        if (amount >= 1_000_000_000) {
            return String.format("%.1fB", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000.0);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000.0);
        }
        return String.valueOf(amount);
    }

    @Override
    public EmiIngredient copy() {
        return new CosmicFavorite(getStack(), getRecipe(), customAmount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CosmicFavorite other) {
            return super.equals(obj) && this.customAmount == other.customAmount;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + Long.hashCode(customAmount);
    }
}
