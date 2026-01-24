package com.ghostipedia.cosmiccore.integration.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.material.Fluid;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiFavorite;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe favorite that displays output (yellow highlight) followed by inputs inline.
 * Format: [OUTPUT amount] [INPUT1 amount] [INPUT2 amount] ...
 * Wraps to next row when out of horizontal space.
 */
public class CosmicRecipeFavorite extends EmiFavorite {

    public record InputEntry(EmiIngredient stack, long amount) {}

    private final List<InputEntry> inputs;
    private final long outputAmount;

    public CosmicRecipeFavorite(EmiIngredient output, long outputAmount, List<InputEntry> inputs) {
        super(output, null);
        this.outputAmount = Math.max(1, outputAmount);
        this.inputs = new ArrayList<>(inputs);
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

    public int getTotalSlots() {
        return 1 + inputs.size();
    }

    @Override
    public void render(GuiGraphics raw, int x, int y, float delta, int flags) {
        EmiDrawContext context = EmiDrawContext.wrap(raw);

        // Render output with yellow tint
        renderStackWithAmount(context, getStack(), x, y, delta, flags, outputAmount, true);
    }

    /**
     * Render the full recipe entry across multiple slots.
     * Called by our custom sidebar renderer.
     */
    public void renderFull(EmiDrawContext context, int startX, int startY, int gridWidth, float delta, int flags) {
        int slotSize = 18;
        int currentX = startX;
        int currentY = startY;
        int slotsOnRow = 0;

        // Render output with yellow highlight
        renderStackWithAmount(context, getStack(), currentX, currentY, delta, flags, outputAmount, true);
        currentX += slotSize;
        slotsOnRow++;

        // Render inputs
        for (InputEntry input : inputs) {
            if (slotsOnRow >= gridWidth) {
                currentX = startX;
                currentY += slotSize;
                slotsOnRow = 0;
            }

            renderStackWithAmount(context, input.stack, currentX, currentY, delta, flags, input.amount, false);
            currentX += slotSize;
            slotsOnRow++;
        }
    }

    /**
     * Calculate how many rows this recipe takes given grid width.
     */
    public int getRowCount(int gridWidth) {
        if (gridWidth <= 0) return 1;
        int totalSlots = getTotalSlots();
        return (totalSlots + gridWidth - 1) / gridWidth;
    }

    private void renderStackWithAmount(EmiDrawContext context, EmiIngredient stack, int x, int y,
                                       float delta, int flags, long amount, boolean highlight) {
        // Yellow highlight for output
        if (highlight) {
            context.fill(x, y, 18, 18, 0x44FFFF00);
        }

        // Render the stack without default amount
        stack.render(context.raw(), x, y, delta, flags & ~EmiIngredient.RENDER_AMOUNT);

        // Render compact amount
        renderCompactAmount(context, x, y, amount, isFluid(stack));
    }

    private void renderCompactAmount(EmiDrawContext context, int x, int y, long amount, boolean fluid) {
        String text = fluid ? formatFluidAmount(amount) : formatItemAmount(amount);
        var component = EmiPort.literal(text);
        Minecraft client = Minecraft.getInstance();
        int textWidth = client.font.width(component);

        context.push();
        context.matrices().translate(0, 0, 200);

        float scale = 0.5f;
        context.matrices().translate(x + 16, y + 16, 0);
        context.matrices().scale(scale, scale, 1);

        context.drawTextWithShadow(component, -textWidth, -client.font.lineHeight, 0xFFFFFF);
        context.pop();
    }

    private boolean isFluid(EmiIngredient stack) {
        if (stack.getEmiStacks().isEmpty()) return false;
        return stack.getEmiStacks().get(0).getKey() instanceof Fluid;
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
        return new CosmicRecipeFavorite(getStack(), outputAmount, inputs);
    }
}
