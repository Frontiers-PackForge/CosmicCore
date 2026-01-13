package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class EnergyConduitWidget extends Widget {

    private final Supplier<Stage> stageSupplier;
    private final List<EnergyPulse> pulses = new ArrayList<>();

    private float flowPhase = 0f;
    private float pulseSpawnTimer = 0f;

    private static class EnergyPulse {

        float position;
        float speed;
        float intensity;
        int conduitIndex;
        boolean alive = true;
    }

    public EnergyConduitWidget(int x, int y, int width, int height, Supplier<Stage> stageSupplier) {
        super(x, y, width, height);
        this.stageSupplier = stageSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();

        Stage stage = stageSupplier.get();
        float flowSpeed = getFlowSpeed(stage);
        flowPhase += flowSpeed;

        pulseSpawnTimer += flowSpeed * 2f;
        if (pulseSpawnTimer > 1f && stage != Stage.EMPTY && stage != Stage.DEATH_GRACEFUL) {
            pulseSpawnTimer = 0f;
            spawnPulse(stage);
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
        if (pulses.size() > 20) return;

        EnergyPulse pulse = new EnergyPulse();
        pulse.position = 0f;
        pulse.speed = 0.02f + (float) Math.random() * 0.03f;
        pulse.intensity = 0.5f + (float) Math.random() * 0.5f;
        pulse.conduitIndex = (int) (Math.random() * 4);

        if (stage == Stage.DEATH) {
            pulse.speed *= 2f;
        } else if (stage == Stage.BLACK_HOLE) {
            pulse.speed *= 1.5f;
        }

        pulses.add(pulse);
    }

    private float getFlowSpeed(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0.005f;
            case GROWING -> 0.03f;
            case STAR -> 0.02f;
            case SUPERSTAR -> 0.04f;
            case BLACK_HOLE -> 0.05f;
            case DEATH -> 0.08f;
            case DEATH_GRACEFUL -> 0.01f;
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        Stage stage = stageSupplier.get();
        int baseColor = getStageColor(stage);

        drawConduitLines(graphics, x, y, w, h, baseColor, stage);
        drawFlowingEnergy(graphics, x, y, w, h, baseColor, stage);
        drawPulses(graphics, x, y, w, h, baseColor);
        drawConduitNodes(graphics, x, y, w, h, baseColor, stage);
    }

    private void drawConduitLines(GuiGraphics graphics, int x, int y, int w, int h, int baseColor, Stage stage) {
        int lineColor = 0x40000000 | (baseColor & 0x00FFFFFF);
        int glowColor = 0x20000000 | (baseColor & 0x00FFFFFF);

        int topY = y + 8;
        graphics.fill(x, topY - 1, x + w, topY + 2, glowColor);
        graphics.fill(x, topY, x + w, topY + 1, lineColor);

        int bottomY = y + h - 8;
        graphics.fill(x, bottomY - 1, x + w, bottomY + 2, glowColor);
        graphics.fill(x, bottomY, x + w, bottomY + 1, lineColor);

        int leftX = x + 8;
        graphics.fill(leftX - 1, y, leftX + 2, y + h, glowColor);
        graphics.fill(leftX, y, leftX + 1, y + h, lineColor);

        int rightX = x + w - 8;
        graphics.fill(rightX - 1, y, rightX + 2, y + h, glowColor);
        graphics.fill(rightX, y, rightX + 1, y + h, lineColor);
    }

    private void drawFlowingEnergy(GuiGraphics graphics, int x, int y, int w, int h, int baseColor, Stage stage) {
        if (stage == Stage.EMPTY) return;

        int segmentCount = 20;
        float segmentSpacing = 1f / segmentCount;

        for (int i = 0; i < segmentCount; i++) {
            float segmentPhase = (flowPhase + i * segmentSpacing) % 1f;
            float brightness = Mth.sin(segmentPhase * Mth.PI) * 0.8f;
            if (brightness < 0.1f) continue;

            int alpha = (int) (0x60 * brightness);
            int color = (alpha << 24) | (baseColor & 0x00FFFFFF);

            int topY = y + 8;
            int segX = x + (int) (w * segmentPhase);
            graphics.fill(segX - 1, topY - 1, segX + 2, topY + 2, color);

            int bottomY = y + h - 8;
            int reverseX = x + w - (int) (w * segmentPhase);
            graphics.fill(reverseX - 1, bottomY - 1, reverseX + 2, bottomY + 2, color);

            int leftX = x + 8;
            int segY = y + (int) (h * segmentPhase);
            graphics.fill(leftX - 1, segY - 1, leftX + 2, segY + 2, color);

            int rightX = x + w - 8;
            int reverseY = y + h - (int) (h * segmentPhase);
            graphics.fill(rightX - 1, reverseY - 1, rightX + 2, reverseY + 2, color);
        }
    }

    private void drawPulses(GuiGraphics graphics, int x, int y, int w, int h, int baseColor) {
        for (EnergyPulse pulse : pulses) {
            float brightness = pulse.intensity * (1f - pulse.position * 0.5f);
            int alpha = (int) (0xC0 * brightness);
            int color = (alpha << 24) | (baseColor & 0x00FFFFFF);
            int coreColor = (alpha << 24) | 0xFFFFFF;

            int px, py;
            switch (pulse.conduitIndex) {
                case 0 -> {
                    px = x + (int) (w * pulse.position);
                    py = y + 8;
                }
                case 1 -> {
                    px = x + w - (int) (w * pulse.position);
                    py = y + h - 8;
                }
                case 2 -> {
                    px = x + 8;
                    py = y + (int) (h * pulse.position);
                }
                default -> {
                    px = x + w - 8;
                    py = y + h - (int) (h * pulse.position);
                }
            }

            graphics.fill(px - 3, py - 3, px + 4, py + 4, color);
            graphics.fill(px - 1, py - 1, px + 2, py + 2, coreColor);
        }
    }

    private void drawConduitNodes(GuiGraphics graphics, int x, int y, int w, int h, int baseColor, Stage stage) {
        float nodePulse = Mth.sin(flowPhase * 3f) * 0.3f + 0.7f;
        int nodeAlpha = (int) (0x80 * nodePulse);
        int nodeColor = (nodeAlpha << 24) | (baseColor & 0x00FFFFFF);
        int nodeGlow = (nodeAlpha / 2 << 24) | (baseColor & 0x00FFFFFF);

        int[][] nodePositions = {
                { x + 8, y + 8 },
                { x + w - 8, y + 8 },
                { x + 8, y + h - 8 },
                { x + w - 8, y + h - 8 }
        };

        for (int[] pos : nodePositions) {
            graphics.fill(pos[0] - 4, pos[1] - 4, pos[0] + 5, pos[1] + 5, nodeGlow);
            graphics.fill(pos[0] - 2, pos[1] - 2, pos[0] + 3, pos[1] + 3, nodeColor);
            graphics.fill(pos[0] - 1, pos[1] - 1, pos[0] + 2, pos[1] + 2, 0xFFFFFFFF);
        }
    }

    private int getStageColor(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0x405060;
            case GROWING -> 0x6090FF;
            case STAR -> 0xFFCC44;
            case SUPERSTAR -> 0xFF7722;
            case BLACK_HOLE -> 0xAA55FF;
            case DEATH -> 0xFF3030;
            case DEATH_GRACEFUL -> 0x664040;
        };
    }
}
