package com.ghostipedia.cosmiccore.client.gui.widget.starladder;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Widget that displays what blocks are needed for the next tier upgrade.
 * Supports scrolling when there are many requirements.
 */
public class NextTierRequirementsWidget extends Widget {

    private static final int[] TIER_COLORS = {
            0xFF4080C0, // T0 - Blue
            0xFF40C080, // T1 - Green
            0xFFC0A040, // T2 - Gold
            0xFFC040C0  // T3 - Purple
    };

    private static final int LINE_HEIGHT = 16;
    private static final int ITEM_SIZE = 14;
    private static final int HEADER_HEIGHT = 14;
    private static final int SCROLLBAR_WIDTH = 6;

    private final IntSupplier tierSupplier;
    private final Supplier<Boolean> canUpgradeSupplier;
    private final Supplier<Map<Block, Integer>> requirementsSupplier;

    private float animPhase = 0f;
    private int scrollOffset = 0;
    private boolean isDraggingScrollbar = false;

    // Cached requirements for rendering
    private List<BlockRequirement> cachedRequirements = new ArrayList<>();
    private int cachedTier = -1;

    private record BlockRequirement(Block block, int count, ItemStack displayStack) {}

    public NextTierRequirementsWidget(int x, int y, int width, int height,
                                      IntSupplier tierSupplier,
                                      Supplier<Boolean> canUpgradeSupplier,
                                      Supplier<Map<Block, Integer>> requirementsSupplier) {
        super(x, y, width, height);
        this.tierSupplier = tierSupplier;
        this.canUpgradeSupplier = canUpgradeSupplier;
        this.requirementsSupplier = requirementsSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        animPhase += 0.06f;

        // Update cached requirements if tier changed
        int currentTier = tierSupplier.getAsInt();
        if (currentTier != cachedTier) {
            cachedTier = currentTier;
            updateCachedRequirements();
        }
    }

    private void updateCachedRequirements() {
        cachedRequirements.clear();
        if (!canUpgradeSupplier.get()) return;

        Map<Block, Integer> reqs = requirementsSupplier.get();
        if (reqs == null || reqs.isEmpty()) return;

        for (Map.Entry<Block, Integer> entry : reqs.entrySet()) {
            Block block = entry.getKey();
            int count = entry.getValue();
            ItemStack stack = new ItemStack(block.asItem());
            cachedRequirements.add(new BlockRequirement(block, count, stack));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        int tier = tierSupplier.getAsInt();
        int tierColor = getTierColor(tier);

        // Background
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, 0xA0101828);
        DrawerHelper.drawBorder(graphics, x, y, w, h, adjustAlpha(tierColor, 0.4f), 1);

        var font = Minecraft.getInstance().font;

        if (tier >= 3) {
            // Max tier reached
            drawMaxTierMessage(graphics, x, y, w, h, font);
        } else if (!canUpgradeSupplier.get()) {
            // Cannot upgrade (not formed?)
            drawNotAvailableMessage(graphics, x, y, w, h, font);
        } else if (cachedRequirements.isEmpty()) {
            // No requirements (shouldn't happen but handle it)
            drawNoRequirementsMessage(graphics, x, y, w, h, font);
        } else {
            // Show requirements
            drawRequirements(graphics, x, y, w, h, font, tier, tierColor);
        }
    }

    private void drawMaxTierMessage(GuiGraphics graphics, int x, int y, int w, int h,
                                    net.minecraft.client.gui.Font font) {
        float pulse = Mth.sin(animPhase * 2f) * 0.15f + 0.85f;
        int alpha = (int) (0xFF * pulse);
        int color = (alpha << 24) | 0xC0A040;

        String text = "MAXIMUM TIER";
        int textX = x + (w - font.width(text)) / 2;
        int textY = y + (h - font.lineHeight) / 2;
        graphics.drawString(font, text, textX, textY, color, false);
    }

    private void drawNotAvailableMessage(GuiGraphics graphics, int x, int y, int w, int h,
                                         net.minecraft.client.gui.Font font) {
        String text = "---";
        int textX = x + (w - font.width(text)) / 2;
        int textY = y + (h - font.lineHeight) / 2;
        graphics.drawString(font, text, textX, textY, 0xFF505060, false);
    }

    private void drawNoRequirementsMessage(GuiGraphics graphics, int x, int y, int w, int h,
                                           net.minecraft.client.gui.Font font) {
        String text = "READY";
        int textX = x + (w - font.width(text)) / 2;
        int textY = y + (h - font.lineHeight) / 2;
        graphics.drawString(font, text, textX, textY, 0xFF40C080, false);
    }

    private void drawRequirements(GuiGraphics graphics, int x, int y, int w, int h,
                                  net.minecraft.client.gui.Font font, int tier, int tierColor) {
        // Header
        String header = "NEXT: T" + (tier + 1);
        int nextTierColor = getTierColor(tier + 1);
        graphics.drawString(font, header, x + 4, y + 3, nextTierColor, false);

        // Calculate visible area
        int contentY = y + HEADER_HEIGHT;
        int contentH = h - HEADER_HEIGHT - 2;
        int visibleItems = contentH / LINE_HEIGHT;
        int totalItems = cachedRequirements.size();
        boolean needsScrollbar = totalItems > visibleItems;

        // Clamp scroll offset
        int maxScroll = Math.max(0, totalItems - visibleItems);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

        // Content width (account for scrollbar)
        int contentW = needsScrollbar ? w - SCROLLBAR_WIDTH - 4 : w - 4;

        // Enable scissor to clip content
        graphics.enableScissor(x, contentY, x + w - (needsScrollbar ? SCROLLBAR_WIDTH + 2 : 0), y + h);

        // Draw items
        for (int i = 0; i < totalItems; i++) {
            int visualIdx = i - scrollOffset;
            if (visualIdx < 0) continue;
            if (visualIdx >= visibleItems + 1) break; // +1 for partial visibility

            BlockRequirement req = cachedRequirements.get(i);
            int drawY = contentY + visualIdx * LINE_HEIGHT;

            // Draw item icon
            graphics.pose().pushPose();
            graphics.pose().translate(x + 4, drawY, 0);
            float scale = ITEM_SIZE / 16f;
            graphics.pose().scale(scale, scale, 1f);
            graphics.renderItem(req.displayStack, 0, 0);
            graphics.pose().popPose();

            // Draw count next to item
            String countStr = "x" + req.count;
            graphics.drawString(font, countStr, x + 4 + ITEM_SIZE + 4, drawY + 3, 0xFFA0A0B0, false);
        }

        graphics.disableScissor();

        // Draw scrollbar if needed
        if (needsScrollbar) {
            drawScrollbar(graphics, x + w - SCROLLBAR_WIDTH - 2, contentY, SCROLLBAR_WIDTH, contentH,
                    scrollOffset, maxScroll, visibleItems, totalItems, tierColor);
        }
    }

    private void drawScrollbar(GuiGraphics graphics, int x, int y, int w, int h,
                               int offset, int maxOffset, int visibleItems, int totalItems, int tierColor) {
        // Background track
        graphics.fill(x, y, x + w, y + h, 0x40000000);

        // Calculate thumb size and position
        float thumbRatio = (float) visibleItems / totalItems;
        int thumbHeight = Math.max(10, (int) (h * thumbRatio));
        float scrollRatio = maxOffset > 0 ? (float) offset / maxOffset : 0;
        int thumbY = y + (int) ((h - thumbHeight) * scrollRatio);

        // Draw thumb
        int thumbColor = adjustAlpha(tierColor, 0.7f);
        graphics.fill(x + 1, thumbY, x + w - 1, thumbY + thumbHeight, thumbColor);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);

        // Tooltip for hovered items
        if (!isMouseOverElement(mouseX, mouseY)) return;
        if (cachedRequirements.isEmpty()) return;

        int x = getPosition().x;
        int y = getPosition().y;
        int h = getSize().height;

        int contentY = y + HEADER_HEIGHT;
        int contentH = h - HEADER_HEIGHT - 2;
        int visibleItems = contentH / LINE_HEIGHT;

        for (int i = 0; i < cachedRequirements.size(); i++) {
            int visualIdx = i - scrollOffset;
            if (visualIdx < 0 || visualIdx >= visibleItems) continue;

            BlockRequirement req = cachedRequirements.get(i);
            int drawY = contentY + visualIdx * LINE_HEIGHT;

            if (mouseX >= x + 4 && mouseX < x + 4 + ITEM_SIZE &&
                    mouseY >= drawY && mouseY < drawY + ITEM_SIZE) {
                graphics.renderTooltip(Minecraft.getInstance().font, req.displayStack, mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY) && !cachedRequirements.isEmpty()) {
            int h = getSize().height;
            int contentH = h - HEADER_HEIGHT - 2;
            int visibleItems = contentH / LINE_HEIGHT;
            int totalItems = cachedRequirements.size();

            if (totalItems > visibleItems) {
                int maxScroll = totalItems - visibleItems;
                scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(wheelDelta), 0, maxScroll);
                return true;
            }
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    private int getTierColor(int tier) {
        if (tier < 0) return TIER_COLORS[0];
        if (tier >= TIER_COLORS.length) return TIER_COLORS[TIER_COLORS.length - 1];
        return TIER_COLORS[tier];
    }

    private int adjustAlpha(int color, float factor) {
        int a = (int) (((color >> 24) & 0xFF) * factor);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
