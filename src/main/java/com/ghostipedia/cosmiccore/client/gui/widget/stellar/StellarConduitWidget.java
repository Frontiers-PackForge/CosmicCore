package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class StellarConduitWidget extends Widget {

    private final Supplier<Stage> stageSupplier;
    private final int coreX, coreY, coreSize;
    private final int panelX, panelY, panelW, panelH;

    private final List<EnergyPulse> pulses = new ArrayList<>();
    private float flowPhase = 0f;

    private static class EnergyPulse {
        float position;
        float speed;
        float intensity;
        int pathIndex;
        boolean alive = true;
    }

    public StellarConduitWidget(int x, int y, int width, int height,
                                int coreX, int coreY, int coreSize,
                                int panelX, int panelY, int panelW, int panelH,
                                Supplier<Stage> stageSupplier) {
        super(x, y, width, height);
        this.coreX = coreX;
        this.coreY = coreY;
        this.coreSize = coreSize;
        this.panelX = panelX;
        this.panelY = panelY;
        this.panelW = panelW;
        this.panelH = panelH;
        this.stageSupplier = stageSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();

        Stage stage = stageSupplier.get();
        float speed = getFlowSpeed(stage);
        flowPhase += speed;

        if (stage != Stage.EMPTY && stage != Stage.DEATH_GRACEFUL) {
            if (Math.random() < speed * 3) {
                spawnPulse(stage);
            }
        }

        pulses.removeIf(p -> !p.alive);
        for (EnergyPulse pulse : pulses) {
            pulse.position += pulse.speed;
            if (pulse.position > 1f) {
                pulse.alive = false;
            }
        }
    }

    private void spawnPulse(Stage stage) {
        if (pulses.size() > 15) return;

        EnergyPulse pulse = new EnergyPulse();
        pulse.position = 0f;
        pulse.speed = 0.015f + (float) Math.random() * 0.02f;
        pulse.intensity = 0.6f + (float) Math.random() * 0.4f;
        pulse.pathIndex = (int) (Math.random() * 6);

        if (stage == Stage.DEATH) pulse.speed *= 2.5f;
        else if (stage == Stage.BLACK_HOLE) pulse.speed *= 1.8f;
        else if (stage == Stage.SUPERSTAR) pulse.speed *= 1.4f;

        pulses.add(pulse);
    }

    private float getFlowSpeed(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0.003f;
            case GROWING -> 0.025f;
            case STAR -> 0.018f;
            case SUPERSTAR -> 0.035f;
            case BLACK_HOLE -> 0.045f;
            case DEATH -> 0.07f;
            case DEATH_GRACEFUL -> 0.008f;
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPosition().x;
        int y = getPosition().y;

        Stage stage = stageSupplier.get();
        int color = getStageColor(stage);

        drawConduitPaths(graphics, x, y, color, stage);
        drawFlowingEnergy(graphics, x, y, color, stage);
        drawPulses(graphics, x, y, color);
        drawJunctionNodes(graphics, x, y, color, stage);
    }

    private void drawConduitPaths(GuiGraphics graphics, int ox, int oy, int color, Stage stage) {
        int lineColor = 0x30000000 | (color & 0x00FFFFFF);
        int glowColor = 0x15000000 | (color & 0x00FFFFFF);

        int cx = ox + coreX + coreSize / 2;
        int cy = ox + coreY + coreSize / 2;
        int coreRadius = coreSize / 2 + 5;

        int px = ox + panelX;
        int py = oy + panelY;
        int pw = panelW;
        int ph = panelH;

        drawHorizontalConduit(graphics, cx + coreRadius, cy - 20, px - (cx + coreRadius), lineColor, glowColor);
        drawHorizontalConduit(graphics, cx + coreRadius, cy + 20, px - (cx + coreRadius), lineColor, glowColor);

        drawVerticalConduit(graphics, px + pw / 2, py, oy + 5 - py, lineColor, glowColor);
        drawVerticalConduit(graphics, px + pw / 2, py + ph, oy + getSize().height - 5 - (py + ph), lineColor, glowColor);

        drawVerticalConduit(graphics, ox + coreX + coreSize / 2, oy + 5, coreY - 10, lineColor, glowColor);
        drawVerticalConduit(graphics, ox + coreX + coreSize / 2, oy + coreY + coreSize + 5, getSize().height - coreY - coreSize - 10, lineColor, glowColor);
    }

    private void drawHorizontalConduit(GuiGraphics graphics, int x, int y, int length, int lineColor, int glowColor) {
        if (length <= 0) return;
        graphics.fill(x, y - 1, x + length, y + 2, glowColor);
        graphics.fill(x, y, x + length, y + 1, lineColor);
    }

    private void drawVerticalConduit(GuiGraphics graphics, int x, int y, int length, int lineColor, int glowColor) {
        if (length <= 0) return;
        graphics.fill(x - 1, y, x + 2, y + length, glowColor);
        graphics.fill(x, y, x + 1, y + length, lineColor);
    }

    private void drawFlowingEnergy(GuiGraphics graphics, int ox, int oy, int color, Stage stage) {
        if (stage == Stage.EMPTY) return;

        int segments = 12;
        float segmentSpacing = 1f / segments;

        int cx = ox + coreX + coreSize / 2;
        int cy = oy + coreY + coreSize / 2;
        int coreRadius = coreSize / 2 + 5;
        int px = ox + panelX;

        for (int i = 0; i < segments; i++) {
            float phase = (flowPhase + i * segmentSpacing) % 1f;
            float brightness = Mth.sin(phase * Mth.PI);
            if (brightness < 0.15f) continue;

            int alpha = (int) (0x80 * brightness);
            int segColor = (alpha << 24) | (color & 0x00FFFFFF);

            int topConduitX = cx + coreRadius + (int) ((px - cx - coreRadius) * phase);
            graphics.fill(topConduitX - 1, cy - 21, topConduitX + 2, cy - 19, segColor);

            int bottomConduitX = cx + coreRadius + (int) ((px - cx - coreRadius) * (1f - phase));
            graphics.fill(bottomConduitX - 1, cy + 19, bottomConduitX + 2, cy + 21, segColor);
        }
    }

    private void drawPulses(GuiGraphics graphics, int ox, int oy, int color) {
        int cx = ox + coreX + coreSize / 2;
        int cy = oy + coreY + coreSize / 2;
        int coreRadius = coreSize / 2 + 5;
        int px = ox + panelX;
        int py = oy + panelY;
        int pw = panelW;
        int ph = panelH;

        for (EnergyPulse pulse : pulses) {
            float brightness = pulse.intensity * (1f - pulse.position * 0.3f);
            int alpha = (int) (0xDD * brightness);
            int pulseColor = (alpha << 24) | (color & 0x00FFFFFF);
            int coreColor = (alpha << 24) | 0xFFFFFF;

            int pulseX, pulseY;

            switch (pulse.pathIndex % 6) {
                case 0 -> {
                    pulseX = cx + coreRadius + (int) ((px - cx - coreRadius) * pulse.position);
                    pulseY = cy - 20;
                }
                case 1 -> {
                    pulseX = cx + coreRadius + (int) ((px - cx - coreRadius) * pulse.position);
                    pulseY = cy + 20;
                }
                case 2 -> {
                    pulseX = px + pw / 2;
                    pulseY = py - (int) ((py - oy - 5) * pulse.position);
                }
                case 3 -> {
                    pulseX = px + pw / 2;
                    pulseY = py + ph + (int) ((oy + getSize().height - 5 - py - ph) * pulse.position);
                }
                case 4 -> {
                    pulseX = ox + coreX + coreSize / 2;
                    pulseY = oy + 5 + (int) ((coreY - 10) * pulse.position);
                }
                default -> {
                    pulseX = ox + coreX + coreSize / 2;
                    pulseY = oy + coreY + coreSize + 5 + (int) ((getSize().height - coreY - coreSize - 10) * pulse.position);
                }
            }

            graphics.fill(pulseX - 2, pulseY - 2, pulseX + 3, pulseY + 3, pulseColor);
            graphics.fill(pulseX - 1, pulseY - 1, pulseX + 2, pulseY + 2, coreColor);
        }
    }

    private void drawJunctionNodes(GuiGraphics graphics, int ox, int oy, int color, Stage stage) {
        float pulse = Mth.sin(flowPhase * 4f) * 0.3f + 0.7f;
        int nodeAlpha = (int) (0x90 * pulse);
        int nodeColor = (nodeAlpha << 24) | (color & 0x00FFFFFF);
        int nodeGlow = (nodeAlpha / 2 << 24) | (color & 0x00FFFFFF);

        int cx = ox + coreX + coreSize / 2;
        int cy = oy + coreY + coreSize / 2;
        int coreRadius = coreSize / 2 + 5;
        int px = ox + panelX;
        int py = oy + panelY;
        int pw = panelW;
        int ph = panelH;

        int[][] nodes = {
            {cx + coreRadius, cy - 20},
            {cx + coreRadius, cy + 20},
            {px, cy - 20},
            {px, cy + 20},
            {px + pw / 2, py},
            {px + pw / 2, py + ph},
            {cx, oy + 5},
            {cx, oy + getSize().height - 5},
        };

        for (int[] node : nodes) {
            graphics.fill(node[0] - 3, node[1] - 3, node[0] + 4, node[1] + 4, nodeGlow);
            graphics.fill(node[0] - 2, node[1] - 2, node[0] + 3, node[1] + 3, nodeColor);
            graphics.fill(node[0] - 1, node[1] - 1, node[0] + 2, node[1] + 2, 0xDDFFFFFF);
        }
    }

    private int getStageColor(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0x506080;
            case GROWING -> 0x6090FF;
            case STAR -> 0xFFCC44;
            case SUPERSTAR -> 0xFF7722;
            case BLACK_HOLE -> 0xAA55FF;
            case DEATH -> 0xFF3030;
            case DEATH_GRACEFUL -> 0x664040;
        };
    }
}
