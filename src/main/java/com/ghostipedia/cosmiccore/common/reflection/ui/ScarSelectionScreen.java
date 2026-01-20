package com.ghostipedia.cosmiccore.common.reflection.ui;

import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainRegistry;
import com.ghostipedia.cosmiccore.utils.StringUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Simple selection screen for choosing which defiance scar to mend with a Cluster of Perpetuity.
 */
@OnlyIn(Dist.CLIENT)
public class ScarSelectionScreen extends Screen {

    private static final int ENTRY_WIDTH = 280;
    private static final int ENTRY_HEIGHT = 32;
    private static final int ENTRY_SPACING = 4;
    private static final int MAX_VISIBLE = 6;
    private static final int FADE_TICKS = 15;

    private final List<ScarEntry> entries = new ArrayList<>();
    private int hoveredIndex = -1;
    private int scrollOffset = 0;
    private int listStartY;
    private int ticks = 0;

    public ScarSelectionScreen(Set<ResourceLocation> scars) {
        super(Component.literal("Mend a Scar"));

        for (ResourceLocation scarId : scars) {
            Optional<Bargain> bargain = BargainRegistry.get(scarId);
            String name = bargain.map(b -> b.getDisplayName().getString())
                    .orElseGet(() -> StringUtil.toTitleCase(scarId.getPath()));
            String desc = bargain.map(b -> b.getDescription().getString())
                    .orElse("A forgotten bargain");
            entries.add(new ScarEntry(scarId, name, desc));
        }
    }

    public static void open(Set<ResourceLocation> scars) {
        Minecraft.getInstance().setScreen(new ScarSelectionScreen(scars));
    }

    @Override
    protected void init() {
        super.init();
        int visibleCount = Math.min(entries.size(), MAX_VISIBLE);
        listStartY = height / 2 - (visibleCount * (ENTRY_HEIGHT + ENTRY_SPACING)) / 2;
    }

    @Override
    public void tick() {
        super.tick();
        if (ticks < FADE_TICKS) ticks++;
    }

    private float getFade() {
        return Math.min(1f, (float) ticks / FADE_TICKS);
    }

    private int withAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha * getFade());
        return (a << 24) | (color & 0x00FFFFFF);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(0, 0, width, height, withAlpha(0xCC0a0a12, 1f));

        // Title
        Component title = Component.literal("Mend a Defiance Scar");
        graphics.drawCenteredString(font, title, width / 2, listStartY - 30, withAlpha(0xFFBB99DD, 1f));

        // Subtitle
        graphics.drawCenteredString(font, "Choose which scar to heal", width / 2, listStartY - 16,
                withAlpha(0xFF888888, 1f));

        // Entries
        hoveredIndex = -1;
        int visibleCount = Math.min(MAX_VISIBLE, entries.size());

        for (int i = 0; i < visibleCount; i++) {
            int entryIndex = i + scrollOffset;
            if (entryIndex >= entries.size()) break;

            ScarEntry entry = entries.get(entryIndex);
            int x = (width - ENTRY_WIDTH) / 2;
            int y = listStartY + i * (ENTRY_HEIGHT + ENTRY_SPACING);

            boolean hovered = mouseX >= x && mouseX < x + ENTRY_WIDTH && mouseY >= y && mouseY < y + ENTRY_HEIGHT;
            if (hovered) hoveredIndex = entryIndex;

            renderEntry(graphics, entry, x, y, hovered);
        }

        // Scroll hints
        if (scrollOffset > 0) {
            graphics.drawCenteredString(font, "\u25B2", width / 2, listStartY - 8, withAlpha(0xFF666666, 1f));
        }
        if (scrollOffset + MAX_VISIBLE < entries.size()) {
            int bottomY = listStartY + visibleCount * (ENTRY_HEIGHT + ENTRY_SPACING);
            graphics.drawCenteredString(font, "\u25BC", width / 2, bottomY, withAlpha(0xFF666666, 1f));
        }

        // Cancel hint
        graphics.drawCenteredString(font, "ESC to cancel", width / 2, height - 20, withAlpha(0xFF555555, 1f));
    }

    private void renderEntry(GuiGraphics graphics, ScarEntry entry, int x, int y, boolean hovered) {
        // Background
        int bg = withAlpha(hovered ? 0xDD2a2a3a : 0xAA1a1a24, 1f);
        graphics.fill(x, y, x + ENTRY_WIDTH, y + ENTRY_HEIGHT, bg);

        // Border
        int border = withAlpha(hovered ? 0xFF8866AA : 0x88443355, 1f);
        graphics.fill(x, y, x + ENTRY_WIDTH, y + 1, border);
        graphics.fill(x, y + ENTRY_HEIGHT - 1, x + ENTRY_WIDTH, y + ENTRY_HEIGHT, border);
        graphics.fill(x, y, x + 1, y + ENTRY_HEIGHT, border);
        graphics.fill(x + ENTRY_WIDTH - 1, y, x + ENTRY_WIDTH, y + ENTRY_HEIGHT, border);

        // Icon
        graphics.drawString(font, "\u2620", x + 8, y + (ENTRY_HEIGHT - font.lineHeight) / 2,
                withAlpha(0xFFAA6688, 1f), false);

        // Name
        int nameColor = withAlpha(hovered ? 0xFFDDCCEE : 0xFFAA99BB, 1f);
        graphics.drawString(font, entry.name, x + 24, y + 6, nameColor, false);

        // Description (truncate if needed)
        String desc = entry.description;
        if (font.width(desc) > ENTRY_WIDTH - 32) {
            desc = font.plainSubstrByWidth(desc, ENTRY_WIDTH - 40) + "...";
        }
        graphics.drawString(font, desc, x + 24, y + 18, withAlpha(0xFF666677, 1f), false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredIndex >= 0 && hoveredIndex < entries.size()) {
            ScarSelectionPackets.sendScarRemoval(entries.get(hoveredIndex).id);
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (entries.size() > MAX_VISIBLE) {
            int maxScroll = entries.size() - MAX_VISIBLE;
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.signum(delta)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record ScarEntry(ResourceLocation id, String name, String description) {}
}
