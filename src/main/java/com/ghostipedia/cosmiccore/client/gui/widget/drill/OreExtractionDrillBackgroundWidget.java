package com.ghostipedia.cosmiccore.client.gui.widget.drill;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class OreExtractionDrillBackgroundWidget extends Widget {

    private float forgeGlow = 0f;

    public OreExtractionDrillBackgroundWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        forgeGlow += 0.06f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        graphics.fill(x, y, x + w, y + h, 0xFF101014);
        drawForgeGlow(graphics, x, y, w, h);
        drawCornerBrackets(graphics, x, y, w, h);
        drawEdgeRivets(graphics, x, y, w, h);
    }

    private void drawForgeGlow(GuiGraphics graphics, int x, int y, int w, int h) {
        float pulse = Mth.sin(forgeGlow) * 0.3f + 0.7f;

        int glowH = 8;
        for (int row = 0; row < glowH; row++) {
            float progress = (float) row / glowH;
            int alpha = (int) (0x20 * progress * pulse);
            int color = (alpha << 24) | 0xCC4400;
            graphics.fill(x, y + h - glowH + row, x + w, y + h - glowH + row + 1, color);
        }
    }

    private void drawCornerBrackets(GuiGraphics graphics, int x, int y, int w, int h) {
        int bracketLen = 10;
        int bracketThick = 2;
        int bracketColor = 0xFF2a2a2a;
        int highlightColor = 0xFF3a3a3a;

        graphics.fill(x, y, x + bracketLen, y + bracketThick, bracketColor);
        graphics.fill(x, y, x + bracketThick, y + bracketLen, bracketColor);
        graphics.fill(x, y, x + bracketLen, y + 1, highlightColor);

        graphics.fill(x + w - bracketLen, y, x + w, y + bracketThick, bracketColor);
        graphics.fill(x + w - bracketThick, y, x + w, y + bracketLen, bracketColor);

        graphics.fill(x, y + h - bracketThick, x + bracketLen, y + h, bracketColor);
        graphics.fill(x, y + h - bracketLen, x + bracketThick, y + h, bracketColor);

        graphics.fill(x + w - bracketLen, y + h - bracketThick, x + w, y + h, bracketColor);
        graphics.fill(x + w - bracketThick, y + h - bracketLen, x + w, y + h, bracketColor);
    }

    private void drawEdgeRivets(GuiGraphics graphics, int x, int y, int w, int h) {
        int rivetColor = 0xFF222222;
        int rivetHighlight = 0xFF2a2a2a;

        for (int rx = 20; rx < w - 10; rx += 30) {
            graphics.fill(x + rx, y + 1, x + rx + 2, y + 3, rivetColor);
            graphics.fill(x + rx, y + 1, x + rx + 1, y + 2, rivetHighlight);
        }

        for (int rx = 20; rx < w - 10; rx += 30) {
            graphics.fill(x + rx, y + h - 3, x + rx + 2, y + h - 1, rivetColor);
        }

        for (int ry = 20; ry < h - 10; ry += 30) {
            graphics.fill(x + 1, y + ry, x + 3, y + ry + 2, rivetColor);
            graphics.fill(x + 1, y + ry, x + 2, y + ry + 1, rivetHighlight);
        }

        for (int ry = 20; ry < h - 10; ry += 30) {
            graphics.fill(x + w - 3, y + ry, x + w - 1, y + ry + 2, rivetColor);
        }
    }
}
