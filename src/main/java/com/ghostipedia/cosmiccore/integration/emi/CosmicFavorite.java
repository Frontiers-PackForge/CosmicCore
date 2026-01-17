package com.ghostipedia.cosmiccore.integration.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiFavorite;

public class CosmicFavorite extends EmiFavorite {

    private long amount;

    public CosmicFavorite(EmiIngredient stack, long amount) {
        super(stack, null);
        this.amount = Math.max(1, amount);
    }

    public void adjustAmount(long delta) {
        this.amount = Math.max(1, amount + delta);
    }

    public long getScrollStep(boolean large) {
        if (!large) return 1;
        return isFluid() ? 1000 : 64;
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

        // Render the stack without the default amount overlay
        getStack().render(context.raw(), x, y, delta, flags & ~EmiIngredient.RENDER_AMOUNT);

        // Render our compact amount at half scale
        renderCompactAmount(context, x, y, amount);

        // Render recipe favorite indicator if applicable
        if ((flags & EmiIngredient.RENDER_INGREDIENT) != 0 && getRecipe() != null) {
            EmiRenderHelper.renderRecipeFavorite(getStack(), context, x, y);
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
        if (isFluid()) {
            return formatFluidAmount(amount);
        }
        return formatItemAmount(amount);
    }

    private String formatFluidAmount(long mB) {
        if (mB < 1000) return mB + "mB";
        double buckets = mB / 1000.0;
        if (buckets >= 1_000_000_000) return String.format("%.1fBB", buckets / 1_000_000_000);
        if (buckets >= 1_000_000) return String.format("%.1fMB", buckets / 1_000_000);
        if (buckets >= 1000) return String.format("%.1fKB", buckets / 1000);
        return String.format("%.1fB", buckets);
    }

    private String formatItemAmount(long count) {
        if (count >= 1_000_000_000) return String.format("%.1fB", count / 1_000_000_000.0);
        if (count >= 1_000_000) return String.format("%.1fM", count / 1_000_000.0);
        if (count >= 1000) return String.format("%.1fK", count / 1000.0);
        return String.valueOf(count);
    }

    @Override
    public EmiIngredient copy() {
        return new CosmicFavorite(getStack(), amount);
    }
}
