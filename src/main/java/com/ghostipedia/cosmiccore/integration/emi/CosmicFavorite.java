package com.ghostipedia.cosmiccore.integration.emi;

import com.ghostipedia.cosmiccore.client.gui.CompactAmountRenderer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiFavorite;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CosmicFavorite extends EmiFavorite {

    private long amount;

    public CosmicFavorite(EmiIngredient stack, long amount) {
        this(stack, amount, null);
    }

    public CosmicFavorite(EmiIngredient stack, long amount, @Nullable EmiRecipe recipe) {
        super(stack, recipe);
        this.amount = Math.max(1, amount);
    }

    private boolean isFluid() {
        if (getStack().getEmiStacks().isEmpty()) return false;
        return getStack().getEmiStacks().get(0).getKey() instanceof Fluid;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public void render(GuiGraphics raw, int x, int y, float delta, int flags) {
        EmiDrawContext context = EmiDrawContext.wrap(raw);

        getStack().render(context.raw(), x, y, delta, flags & ~EmiIngredient.RENDER_AMOUNT);
        if (isFluid()) {
            CompactAmountRenderer.drawFluidAmount(raw, x, y, 16, 16, amount);
        } else {
            CompactAmountRenderer.drawItemAmount(raw, x, y, 16, 16, amount);
        }
        if ((flags & EmiIngredient.RENDER_INGREDIENT) != 0 && getRecipe() != null &&
                shouldRenderRecipeFavoriteIndicator()) {
            EmiRenderHelper.renderRecipeFavorite(getStack(), context, x, y);
        }
    }

    protected boolean shouldRenderRecipeFavoriteIndicator() {
        return true;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> tooltip = new ArrayList<>(super.getTooltip());
        if (isFluid()) {
            Component amountText = Component.literal(EmiRenderHelper.TEXT_FORMAT.format(amount) + " mB")
                    .withStyle(ChatFormatting.AQUA);
            tooltip.add(ClientTooltipComponent.create(EmiPort.ordered(Component.translatable(
                    "cosmiccore.emi.bookmarks.fluid_amount",
                    amountText).withStyle(ChatFormatting.GRAY))));
        }
        return tooltip;
    }

    @Override
    public EmiIngredient copy() {
        return new CosmicFavorite(getStack().copy(), amount, getRecipe());
    }
}
