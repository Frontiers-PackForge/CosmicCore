package com.ghostipedia.cosmiccore.integration.emi;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import java.util.Locale;

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
        renderCompactAmount(context, x, y, amount);
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
        if (isFluid()) {
            return formatFluidAmount(amount);
        }
        return formatItemAmount(amount);
    }

    private String formatFluidAmount(long mB) {
        if (mB < 1000) return mB + "mB";
        double buckets = mB / 1000.0;
        if (buckets >= 1_000_000_000) return String.format(Locale.ROOT, "%.1fBB", buckets / 1_000_000_000);
        if (buckets >= 1_000_000) return String.format(Locale.ROOT, "%.1fMB", buckets / 1_000_000);
        if (buckets >= 1000) return String.format(Locale.ROOT, "%.1fKB", buckets / 1000);
        return String.format(Locale.ROOT, "%.1fB", buckets);
    }

    private String formatItemAmount(long count) {
        if (count >= 1_000_000_000) return String.format(Locale.ROOT, "%.1fB", count / 1_000_000_000.0);
        if (count >= 1_000_000) return String.format(Locale.ROOT, "%.1fM", count / 1_000_000.0);
        if (count >= 1000) return String.format(Locale.ROOT, "%.1fK", count / 1000.0);
        return String.valueOf(count);
    }

    @Override
    public EmiIngredient copy() {
        return new CosmicFavorite(getStack().copy(), amount, getRecipe());
    }
}
