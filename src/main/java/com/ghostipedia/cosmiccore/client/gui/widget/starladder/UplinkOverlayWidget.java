package com.ghostipedia.cosmiccore.client.gui.widget.starladder;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderUplinkState;
import com.ghostipedia.cosmiccore.common.network.packet.StarLadderUplinkPackets.ClientDemand;
import com.ghostipedia.cosmiccore.common.network.packet.StarLadderUplinkPackets.StarLadderUplinkClientState;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class UplinkOverlayWidget extends Widget {

    private static final int PROGRESS_BAR_COLOR = 0xFFC04040;
    private static final int PROGRESS_BAR_BG = 0xFF202030;
    private static final int PROGRESS_BAR_GLOW = 0x40FF4040;
    private static final int DEMAND_TIMER_COLOR = 0xFFCC8800;
    private static final int DEMAND_TIMER_BG = 0xFF302010;
    private static final int LABEL_COLOR = 0xFF8090B0;
    private static final int VALUE_COLOR = 0xFFCCDDEE;
    private static final int WARNING_COLOR = 0xFFFF4444;

    private final Random random = new Random();

    private float animPhase = 0f;
    private float glitch = 0f;
    private int[] glitchOffsets = new int[8];
    private boolean[] scanlines = new boolean[20];

    public UplinkOverlayWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
        randomizeGlitch();
    }

    private void randomizeGlitch() {
        for (int i = 0; i < glitchOffsets.length; i++) {
            glitchOffsets[i] = random.nextInt(20) - 10;
        }
        for (int i = 0; i < scanlines.length; i++) {
            scanlines[i] = random.nextFloat() < 0.3f;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        animPhase += 0.05f;

        StarLadderUplinkState state = StarLadderUplinkClientState.getState();
        if (state.isActive()) {
            float progressPct = StarLadderUplinkClientState.getUplinkProgress();
            glitch = 0.1f + progressPct * 0.3f;

            if (glitch > 0 && (int) (animPhase * 20) % 3 == 0) {
                randomizeGlitch();
            }
        } else {
            glitch = 0;
        }
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        StarLadderUplinkState state = StarLadderUplinkClientState.getState();
        if (!state.isActive()) return;

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        drawUplinkProgress(graphics, x, y, w);
        drawSoulDrainIndicator(graphics, x, y + 30, w);

        int panelTop = y + 50;
        int panelH = h - 65;
        int halfW = (w - 4) / 2;
        drawDemandPanel(graphics, StarLadderUplinkClientState.getBulkDemand(), x, panelTop, halfW, panelH);
        drawDemandPanel(graphics, StarLadderUplinkClientState.getComplexDemand(), x + halfW + 4, panelTop, halfW,
                panelH);

        if (glitch > 0) drawGlitch(graphics, x, y, w, h);
        drawVignette(graphics, x, y, w, h);
    }

    private void drawUplinkProgress(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;

        String label = Component.translatable("cosmiccore.star_ladder.uplink_progress").getString();
        graphics.drawString(font, label, x + 5, y + 2, LABEL_COLOR, false);

        int pct = (int) (StarLadderUplinkClientState.getUplinkProgress() * 100);
        String pctText = pct + "%";
        graphics.drawString(font, pctText, x + w - font.width(pctText) - 5, y + 2, VALUE_COLOR, false);

        int barY = y + 14;
        int barH = 10;
        int barW = w - 10;

        DrawerHelper.drawSolidRect(graphics, x + 5, barY, barW, barH, PROGRESS_BAR_BG);

        float progress = StarLadderUplinkClientState.getUplinkProgress();
        int fillW = (int) (barW * progress);
        if (fillW > 0) {
            float pulse = Mth.sin(animPhase * 3f) * 0.15f + 0.85f;
            int r = (int) (((PROGRESS_BAR_COLOR >> 16) & 0xFF) * pulse);
            int g = (int) (((PROGRESS_BAR_COLOR >> 8) & 0xFF) * pulse);
            int b = (int) ((PROGRESS_BAR_COLOR & 0xFF) * pulse);
            int pulsedColor = 0xFF000000 | (r << 16) | (g << 8) | b;

            DrawerHelper.drawGradientRect(graphics, x + 5, barY, fillW, barH,
                    darkenColor(pulsedColor, 0.6f), pulsedColor, true);

            DrawerHelper.drawSolidRect(graphics, x + 5, barY, fillW, 1, PROGRESS_BAR_GLOW);
        }

        DrawerHelper.drawBorder(graphics, x + 5, barY, barW, barH, 0x60FFFFFF, 1);
    }

    private void drawSoulDrainIndicator(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;

        int drainRate = StarLadderUplinkClientState.getDrainRate();
        String text = Component.translatable("cosmiccore.star_ladder.soul_drain", drainRate).getString();

        float pulse = Mth.sin(animPhase * 2f) * 0.3f + 0.7f;
        int alpha = (int) (255 * pulse);
        int color = (alpha << 24) | 0xCC4444;

        graphics.drawString(font, text, x + 5, y + 2, color, false);
    }

    private void drawDemandPanel(GuiGraphics graphics, ClientDemand demand, int x, int y, int w, int h) {
        if (!demand.isActive()) return;

        var font = Minecraft.getInstance().font;

        DrawerHelper.drawSolidRect(graphics, x + 3, y, w - 6, h - 3, 0x40102030);
        DrawerHelper.drawBorder(graphics, x + 3, y, w - 6, h - 3, 0x60CC8800, 1);

        String demandLabel = Component.translatable("cosmiccore.star_ladder.requisition").getString();
        graphics.drawString(font, demandLabel, x + 8, y + 4, WARNING_COLOR, false);

        graphics.renderItem(demand.item, x + 8, y + 16);

        String itemName = demand.item.getHoverName().getString();
        String qtyText = "x" + demand.qtyRemaining;
        graphics.drawString(font, itemName, x + 28, y + 16, VALUE_COLOR, false);
        graphics.drawString(font, qtyText, x + 28, y + 26, DEMAND_TIMER_COLOR, false);

        float timerProgress = demand.getTimerProgress();
        int timerBarY = y + h - 12;
        int timerBarW = w - 16;
        int timerBarH = 6;

        DrawerHelper.drawSolidRect(graphics, x + 8, timerBarY, timerBarW, timerBarH, DEMAND_TIMER_BG);

        float remaining = 1f - timerProgress;
        int timerFillW = (int) (timerBarW * remaining);
        if (timerFillW > 0) {
            int timerColor = remaining < 0.25f ? WARNING_COLOR : DEMAND_TIMER_COLOR;
            DrawerHelper.drawSolidRect(graphics, x + 8, timerBarY, timerFillW, timerBarH, timerColor);
        }

        DrawerHelper.drawBorder(graphics, x + 8, timerBarY, timerBarW, timerBarH, 0x40FFFFFF, 1);
    }

    private void drawGlitch(GuiGraphics graphics, int x, int y, int w, int h) {
        for (int i = 0; i < scanlines.length; i++) {
            if (scanlines[i] && random.nextFloat() < glitch) {
                int sy = y + (h * i / scanlines.length);
                int sh = h / scanlines.length;
                int off = glitchOffsets[i % glitchOffsets.length];
                int a = (int) (30 * glitch);

                graphics.fill(x + off - 2, sy, x + w + off - 2, sy + sh, (a << 24) | 0xFF0000);
                graphics.fill(x - off + 2, sy, x + w - off + 2, sy + sh, (a << 24) | 0x00FFFF);
            }
        }

        if (random.nextFloat() < glitch * 0.3f) {
            int a = (int) (40 * glitch * random.nextFloat());
            graphics.fill(x, y, x + w, y + h, (a << 24) | 0xFFFFFF);
        }
    }

    private void drawVignette(GuiGraphics graphics, int x, int y, int w, int h) {
        float strength = 0.3f;
        int edgeAlpha = (int) (80 * strength);
        int edgeSize = h / 5;

        for (int row = 0; row < edgeSize; row++) {
            int a = (int) (edgeAlpha * (1f - (float) row / edgeSize));
            graphics.fill(x, y + row, x + w, y + row + 1, (a << 24) | 0x200000);
            graphics.fill(x, y + h - 1 - row, x + w, y + h - row, (a << 24) | 0x200000);
        }
    }

    private int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
