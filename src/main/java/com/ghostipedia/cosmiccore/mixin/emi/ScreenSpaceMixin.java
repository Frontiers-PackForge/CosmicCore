package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.CosmicRecipeFavorite;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;
import com.ghostipedia.cosmiccore.integration.emi.favorites.RecipeRenderInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiFavorites;
import dev.emi.emi.screen.EmiScreenManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom rendering for recipe-type bookmark groups.
 * Recipe favorites display: [output with gold border] [input1] [input2] ...
 * Wraps to next row when out of horizontal space.
 */
@Mixin(value = EmiScreenManager.ScreenSpace.class, remap = false)
public abstract class ScreenSpaceMixin {

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
    public abstract SidebarType getType();

    @Shadow
    public abstract List<? extends EmiIngredient> getStacks();

    @Unique
    private static final int SLOT_SIZE = 18;

    /**
     * Override getStacks for recipe groups to return a fake list sized for proper pagination.
     * EMI calculates page count as stacks.size() / pageSize, so we need enough "stacks"
     * to create the right number of pages based on our row-based layout.
     */
    @Inject(method = "getStacks", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$getStacksForRecipeGroup(CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        if (getType() != SidebarType.FAVORITES) return;

        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (!manager.getActiveGroup().isRecipeGroup()) return;

        int recipePageCount = manager.getRecipePageCount(tw, th);

        // Return a list sized so EMI calculates the correct page count
        // EMI does: (stacks.size() + pageSize - 1) / pageSize for total pages
        // We want: recipePageCount pages
        // So we need: recipePageCount * pageSize stacks (approximately)
        int neededSize = recipePageCount * pageSize;

        // Create a list with the right size - contents don't matter since we override render
        List<EmiStack> fakeList = new ArrayList<>(neededSize);
        for (int i = 0; i < neededSize; i++) {
            fakeList.add(EmiStack.EMPTY);
        }
        cir.setReturnValue(fakeList);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$renderRecipeGroup(EmiDrawContext context, int mouseX, int mouseY, float delta,
                                              int startIndex, CallbackInfo ci) {
        if (getType() != SidebarType.FAVORITES) return;

        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (!manager.getActiveGroup().isRecipeGroup()) return;

        ci.cancel();

        if (Minecraft.getInstance() == null || Minecraft.getInstance().screen == null) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        GuiGraphics gui = context.raw();

        // Get recipes directly from EmiFavorites, not getStacks() which may filter them
        List<CosmicRecipeFavorite> recipes = EmiFavorites.favorites.stream()
                .filter(f -> f instanceof CosmicRecipeFavorite)
                .map(f -> (CosmicRecipeFavorite) f)
                .toList();

        if (recipes.isEmpty()) {
            String hint = "CTRL+SHIFT+A on recipe";
            int textX = tx + (tw * SLOT_SIZE - font.width(hint)) / 2;
            int textY = ty + (th * SLOT_SIZE) / 2 - font.lineHeight / 2;
            context.drawTextWithShadow(Component.literal(hint), textX, textY, 0xFF888888);
            return;
        }

        int gridWidth = tw;
        int gridHeight = th;
        int totalRows = gridHeight;

        // Calculate which recipes fit on current page with wrapping
        // EMI's startIndex is slot-based, convert to recipe page number
        // pageSize = tw * th (e.g., 81 for 9x9), startIndex increments by pageSize per page
        int emiPage = pageSize > 0 ? startIndex / pageSize : 0;
        // Use EMI's page directly - we've set up the page count to match
        int currentPage = emiPage;
        List<RecipeRenderInfo> renderList = cosmiccore$calculatePageLayout(recipes, gridWidth, totalRows, currentPage);

        // Find hovered slot for highlight
        int hoveredRow = -1;
        int hoveredCol = -1;
        if (mouseX >= tx && mouseX < tx + tw * SLOT_SIZE && mouseY >= ty && mouseY < ty + th * SLOT_SIZE) {
            hoveredRow = (mouseY - ty) / SLOT_SIZE;
            hoveredCol = (mouseX - tx) / SLOT_SIZE;
        }

        // FIRST PASS: Render items and borders
        for (RecipeRenderInfo info : renderList) {
            CosmicRecipeFavorite recipe = info.recipe();
            int currentRow = info.startRow();
            int currentCol = 0;

            // Draw hover highlight if mouse is over any slot of this recipe
            if (hoveredRow >= info.startRow() && hoveredRow < info.startRow() + info.rowCount()) {
                int relativeRow = hoveredRow - info.startRow();
                int slotIndex = relativeRow * gridWidth + hoveredCol;
                if (slotIndex >= 0 && slotIndex < recipe.getTotalSlots()) {
                    int slotX = tx + hoveredCol * SLOT_SIZE;
                    int slotY = ty + hoveredRow * SLOT_SIZE;
                    EmiRenderHelper.drawSlotHightlight(context, slotX, slotY, SLOT_SIZE, SLOT_SIZE, 0);
                }
            }

            // Render OUTPUT
            EmiIngredient output = recipe.getStack();
            int outputX = tx + currentCol * SLOT_SIZE;
            int outputY = ty + currentRow * SLOT_SIZE;
            if (!output.isEmpty()) {
                output.render(gui, outputX + 1, outputY + 1, delta, EmiIngredient.RENDER_ICON);
            }
            cosmiccore$drawOutputBorder(context, outputX, outputY);
            currentCol++;

            // Render INPUTS with wrapping
            for (CosmicRecipeFavorite.InputEntry input : recipe.getInputs()) {
                if (currentCol >= gridWidth) {
                    currentCol = 0;
                    currentRow++;
                    if (currentRow >= info.startRow() + info.rowCount()) break;
                }

                int inputX = tx + currentCol * SLOT_SIZE;
                int inputY = ty + currentRow * SLOT_SIZE;
                if (!input.stack().isEmpty()) {
                    input.stack().render(gui, inputX + 1, inputY + 1, delta, EmiIngredient.RENDER_ICON);
                }
                currentCol++;
            }
        }

        // SECOND PASS: Render amount overlays ON TOP (z=200)
        for (RecipeRenderInfo info : renderList) {
            CosmicRecipeFavorite recipe = info.recipe();
            int currentRow = info.startRow();
            int currentCol = 0;

            // Output amount
            EmiIngredient output = recipe.getStack();
            int outputX = tx + currentCol * SLOT_SIZE;
            int outputY = ty + currentRow * SLOT_SIZE;
            if (!output.isEmpty()) {
                long outputAmount = recipe.getOutputAmount();
                if (outputAmount > 1) {
                    cosmiccore$renderCompactAmount(context, outputX, outputY, outputAmount, output);
                }
            }
            currentCol++;

            // Input amounts with wrapping
            for (CosmicRecipeFavorite.InputEntry input : recipe.getInputs()) {
                if (currentCol >= gridWidth) {
                    currentCol = 0;
                    currentRow++;
                    if (currentRow >= info.startRow() + info.rowCount()) break;
                }

                int inputX = tx + currentCol * SLOT_SIZE;
                int inputY = ty + currentRow * SLOT_SIZE;
                if (!input.stack().isEmpty()) {
                    long inputAmount = input.amount();
                    if (inputAmount > 1) {
                        cosmiccore$renderCompactAmount(context, inputX, inputY, inputAmount, input.stack());
                    }
                }
                currentCol++;
            }
        }
    }

    @Unique
    private List<RecipeRenderInfo> cosmiccore$calculatePageLayout(List<CosmicRecipeFavorite> recipes, int gridWidth,
                                                                  int totalRows, int page) {
        List<RecipeRenderInfo> result = new ArrayList<>();

        // Calculate total rows needed for all recipes
        List<Integer> recipeRowCounts = new ArrayList<>();
        for (CosmicRecipeFavorite recipe : recipes) {
            recipeRowCounts.add(recipe.getRowCount(gridWidth));
        }

        // Find which recipes belong on this page
        int rowsUsed = 0;
        int pageStartRow = 0;
        int currentPage = 0;

        for (int i = 0; i < recipes.size(); i++) {
            int rowsNeeded = recipeRowCounts.get(i);

            // Check if this recipe would overflow the page
            if (rowsUsed + rowsNeeded > totalRows && rowsUsed > 0) {
                currentPage++;
                pageStartRow = rowsUsed;
                rowsUsed = 0;
            }

            if (currentPage == page) {
                result.add(new RecipeRenderInfo(recipes.get(i), rowsUsed, rowsNeeded));
            } else if (currentPage > page) {
                break;
            }

            rowsUsed += rowsNeeded;
        }

        return result;
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
        context.matrices().translate(0, 0, 200);

        float scale = 0.5f;
        context.matrices().translate(slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0);
        context.matrices().scale(scale, scale, 1);

        int tx = -textWidth;
        int ty = -client.font.lineHeight;

        context.drawTextWithShadow(Component.literal(text), tx, ty, 0xFFFFFF);
        context.pop();
    }

    @Unique
    private String cosmiccore$formatCompact(long amount, EmiIngredient ingredient) {
        if (!ingredient.getEmiStacks().isEmpty()) {
            EmiStack first = ingredient.getEmiStacks().get(0);
            if (first.getKey() instanceof Fluid) {
                if (amount >= 1000) {
                    double buckets = amount / 1000.0;
                    if (buckets >= 1_000_000_000) return String.format("%.1fBB", buckets / 1_000_000_000);
                    if (buckets >= 1_000_000) return String.format("%.1fMB", buckets / 1_000_000);
                    if (buckets >= 1_000) return String.format("%.1fKB", buckets / 1_000);
                    return String.format("%.1fB", buckets);
                }
                return amount + "mB";
            }
        }

        if (amount >= 1_000_000_000) return String.format("%.1fB", amount / 1_000_000_000.0);
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000.0);
        if (amount >= 1_000) return String.format("%.1fK", amount / 1_000.0);
        return String.valueOf(amount);
    }
}
