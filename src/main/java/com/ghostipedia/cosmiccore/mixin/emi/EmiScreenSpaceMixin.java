package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkGroup;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicRecipeBookmark;
import com.ghostipedia.cosmiccore.integration.emi.favorites.TodoListBoundsHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.StackBatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EmiScreenManager.ScreenSpace.class, remap = false)
public abstract class EmiScreenSpaceMixin {

    @Shadow
    @Final
    public int tx;
    @Shadow
    @Final
    public int ty;
    @Shadow
    @Final
    public int tw;
    @Shadow
    @Final
    public int th;
    @Shadow
    @Final
    public int pageSize;

    @Shadow
    @Final
    public StackBatcher batcher;

    @Shadow
    public abstract SidebarType getType();

    @Shadow
    public abstract List<? extends EmiIngredient> getStacks();

    @Unique
    private static final int SLOT_SIZE = 18;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$renderTodoList(EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex,
                                           CallbackInfo ci) {
        // Only intercept FAVORITES sidebar
        if (getType() != SidebarType.FAVORITES) {
            return;
        }

        // Check if we're in TODO_LIST mode
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (manager.getActiveViewMode() != CosmicBookmarkGroup.ViewMode.TODO_LIST) {
            return;
        }

        // CANCEL the original render - we're taking over completely in TODO_LIST mode
        // This is MUTUALLY EXCLUSIVE with DEFAULT mode - no fallback to regular favorites
        ci.cancel();

        // Store bounds for click handling (even if empty, so clicks are still blocked)
        TodoListBoundsHelper.setBounds(tx, ty, tw, th);

        List<CosmicRecipeBookmark> recipeBookmarks = manager.getActiveRecipeBookmarks();

        // If no recipe bookmarks, show a hint message instead of nothing
        if (recipeBookmarks.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;
            String hint = "CTRL+SHIFT+A on recipe";
            int textX = tx + (tw * SLOT_SIZE - font.width(hint)) / 2;
            int textY = ty + (th * SLOT_SIZE) / 2 - font.lineHeight / 2;
            context.drawTextWithShadow(net.minecraft.network.chat.Component.literal(hint), textX, textY, 0xFF888888);
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        GuiGraphics gui = context.raw();

        // Only recipe bookmarks in TODO_LIST mode
        int totalRows = recipeBookmarks.size();

        // Calculate pagination
        int rowsPerPage = Math.max(1, this.th);
        int currentPage = startIndex / Math.max(1, this.pageSize);
        int rowStartIndex = currentPage * rowsPerPage;

        // Find hovered slot
        int hoveredRow = -1;
        int hoveredCol = -1;
        if (mouseX >= tx && mouseX < tx + tw * SLOT_SIZE && mouseY >= ty && mouseY < ty + th * SLOT_SIZE) {
            hoveredRow = (mouseY - ty) / SLOT_SIZE;
            hoveredCol = (mouseX - tx) / SLOT_SIZE;
        }

        // FIRST PASS: Render all items/fluids and borders
        for (int displayRow = 0; displayRow < rowsPerPage; displayRow++) {
            int recipeIndex = rowStartIndex + displayRow;
            if (recipeIndex >= totalRows) {
                break;
            }

            CosmicRecipeBookmark recipe = recipeBookmarks.get(recipeIndex);
            int rowY = ty + displayRow * SLOT_SIZE;
            int availableSlots = this.tw;

            // Draw hover highlight
            if (hoveredRow == displayRow && hoveredCol >= 0 && hoveredCol < availableSlots) {
                int slotX = tx + hoveredCol * SLOT_SIZE;
                EmiRenderHelper.drawSlotHightlight(context, slotX, rowY, SLOT_SIZE, SLOT_SIZE, 0);
            }

            // Render the OUTPUT in first slot
            EmiStack output = recipe.getOutput();
            if (!output.isEmpty()) {
                output.render(gui, tx + 1, rowY + 1, delta, EmiIngredient.RENDER_ICON);
            }

            // Draw gold border around output slot (recipe indicator)
            cosmiccore$drawOutputBorder(context, tx, rowY);

            // Render INPUTS in subsequent slots
            List<EmiIngredient> inputs = recipe.getInputs();
            int maxInputs = Math.min(inputs.size(), availableSlots - 1);

            for (int i = 0; i < maxInputs; i++) {
                EmiIngredient input = inputs.get(i);
                int inputX = tx + (i + 1) * SLOT_SIZE;

                if (!input.isEmpty()) {
                    input.render(gui, inputX + 1, rowY + 1, delta, EmiIngredient.RENDER_ICON);
                }
            }

            // Show "+N" if more inputs exist
            if (inputs.size() > maxInputs) {
                int moreCount = inputs.size() - maxInputs;
                String moreText = "+" + moreCount;
                int moreX = tx + availableSlots * SLOT_SIZE - font.width(moreText) - 2;
                gui.drawString(font, moreText, moreX, rowY + 5, 0xFFAAAAAA, true);
            }
        }

        // SECOND PASS: Render all amount overlays ON TOP of items
        for (int displayRow = 0; displayRow < rowsPerPage; displayRow++) {
            int recipeIndex = rowStartIndex + displayRow;
            if (recipeIndex >= totalRows) {
                break;
            }

            CosmicRecipeBookmark recipe = recipeBookmarks.get(recipeIndex);
            int rowY = ty + displayRow * SLOT_SIZE;
            int availableSlots = this.tw;
            long multiplier = recipe.getMultiplier();

            // Render output amount
            EmiStack output = recipe.getOutput();
            if (!output.isEmpty()) {
                long outputAmount = output.getAmount() * multiplier;
                if (outputAmount > 1) {
                    cosmiccore$renderCompactAmount(context, tx, rowY, outputAmount, output);
                }
            }

            // Render input amounts
            List<EmiIngredient> inputs = recipe.getInputs();
            int maxInputs = Math.min(inputs.size(), availableSlots - 1);

            for (int i = 0; i < maxInputs; i++) {
                EmiIngredient input = inputs.get(i);
                int inputX = tx + (i + 1) * SLOT_SIZE;

                if (!input.isEmpty()) {
                    long inputAmount = input.getAmount() * multiplier;
                    if (inputAmount > 1) {
                        cosmiccore$renderCompactAmount(context, inputX, rowY, inputAmount, input);
                    }
                }
            }
        }
    }

    @Unique
    private void cosmiccore$drawOutputBorder(EmiDrawContext context, int x, int y) {
        int color = 0x80FFD700; // Semi-transparent gold
        context.fill(x, y, SLOT_SIZE, 1, color);
        context.fill(x, y + SLOT_SIZE - 1, SLOT_SIZE, 1, color);
        context.fill(x, y, 1, SLOT_SIZE, color);
        context.fill(x + SLOT_SIZE - 1, y, 1, SLOT_SIZE, color);
    }

    @Unique
    private void cosmiccore$renderCompactAmount(EmiDrawContext context, int slotX, int slotY, long amount,
                                                EmiIngredient ingredient) {
        String text = cosmiccore$formatCompact(amount, ingredient);
        Minecraft client = Minecraft.getInstance();
        int textWidth = client.font.width(text);

        context.push();
        context.matrices().translate(0, 0, 200); // Render on top of items

        float scale = 0.5f;
        // Position at bottom-right of slot (slot is 18x18, item is 16x16 centered)
        context.matrices().translate(slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0);
        context.matrices().scale(scale, scale, 1);

        int tx = -textWidth;
        int ty = -client.font.lineHeight;

        context.drawTextWithShadow(net.minecraft.network.chat.Component.literal(text), tx, ty, 0xFFFFFF);
        context.pop();
    }

    @Unique
    private String cosmiccore$formatCompact(long amount, EmiIngredient ingredient) {
        // Check if this is a fluid
        if (!ingredient.getEmiStacks().isEmpty()) {
            EmiStack first = ingredient.getEmiStacks().get(0);
            if (first.getKey() instanceof net.minecraft.world.level.material.Fluid) {
                // Format fluids in buckets
                if (amount >= 1000) {
                    double buckets = amount / 1000.0;
                    if (buckets >= 1_000_000_000) {
                        return String.format("%.1fBB", buckets / 1_000_000_000);
                    } else if (buckets >= 1_000_000) {
                        return String.format("%.1fMB", buckets / 1_000_000);
                    } else if (buckets >= 1_000) {
                        return String.format("%.1fKB", buckets / 1_000);
                    } else {
                        return String.format("%.1fB", buckets);
                    }
                }
                return amount + "mB";
            }
        }

        // Format items
        if (amount >= 1_000_000_000) {
            return String.format("%.1fB", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000.0);
        } else if (amount >= 1_000) {
            return String.format("%.1fK", amount / 1_000.0);
        }
        return String.valueOf(amount);
    }
}
