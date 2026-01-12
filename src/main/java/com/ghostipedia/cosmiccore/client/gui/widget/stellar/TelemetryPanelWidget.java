package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TelemetryPanelWidget extends Widget {

    private final Supplier<IrisMultiblockMachine> machineSupplier;
    private final Supplier<Stage> stageSupplier;

    private final List<String> logLines = new ArrayList<>();
    private float scrollOffset = 0f;
    private float dataUpdateTimer = 0f;
    private float glitchPhase = 0f;

    private float displayedTemp = 0f;
    private float displayedPressure = 0f;
    private float displayedMass = 0f;
    private float displayedEnergy = 0f;

    public TelemetryPanelWidget(int x, int y, int width, int height,
                                Supplier<IrisMultiblockMachine> machineSupplier,
                                Supplier<Stage> stageSupplier) {
        super(x, y, width, height);
        this.machineSupplier = machineSupplier;
        this.stageSupplier = stageSupplier;
        initLogLines();
    }

    private void initLogLines() {
        logLines.add("[SYS] Stellar Iris v3.7.2 initialized");
        logLines.add("[SYS] Containment field generators online");
        logLines.add("[SYS] Plasma injectors standby");
        logLines.add("[SYS] Gravitational stabilizers active");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();

        Stage stage = stageSupplier.get();
        scrollOffset += 0.02f;
        dataUpdateTimer += 1f;
        glitchPhase += 0.1f;

        updateDisplayedValues(stage);

        if (dataUpdateTimer > 60f) {
            dataUpdateTimer = 0f;
            addLogLine(stage);
        }
    }

    private void updateDisplayedValues(Stage stage) {
        float targetTemp = getTemperature(stage);
        float targetPressure = getPressure(stage);
        float targetMass = getMass(stage);
        float targetEnergy = getEnergy(stage);

        float lerpSpeed = 0.05f;
        displayedTemp = Mth.lerp(lerpSpeed, displayedTemp, targetTemp);
        displayedPressure = Mth.lerp(lerpSpeed, displayedPressure, targetPressure);
        displayedMass = Mth.lerp(lerpSpeed, displayedMass, targetMass);
        displayedEnergy = Mth.lerp(lerpSpeed, displayedEnergy, targetEnergy);

        if (stage == Stage.DEATH) {
            displayedTemp += (float) (Math.random() - 0.5) * 1000;
            displayedPressure += (float) (Math.random() - 0.5) * 50;
        }
    }

    private float getTemperature(Stage stage) {
        return switch (stage) {
            case EMPTY -> 2.7f;
            case GROWING -> 5_000_000f;
            case STAR -> 15_000_000f;
            case SUPERSTAR -> 100_000_000f;
            case BLACK_HOLE -> Float.POSITIVE_INFINITY;
            case DEATH -> 500_000_000f;
            case DEATH_GRACEFUL -> 1_000_000f;
        };
    }

    private float getPressure(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0f;
            case GROWING -> 150f;
            case STAR -> 250f;
            case SUPERSTAR -> 450f;
            case BLACK_HOLE -> 999f;
            case DEATH -> 800f;
            case DEATH_GRACEFUL -> 50f;
        };
    }

    private float getMass(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0f;
            case GROWING -> 0.3f;
            case STAR -> 1f;
            case SUPERSTAR -> 8f;
            case BLACK_HOLE -> 25f;
            case DEATH -> 12f;
            case DEATH_GRACEFUL -> 0.1f;
        };
    }

    private float getEnergy(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0f;
            case GROWING -> 1_000f;
            case STAR -> 50_000f;
            case SUPERSTAR -> 500_000f;
            case BLACK_HOLE -> 10_000_000f;
            case DEATH -> 100_000_000f;
            case DEATH_GRACEFUL -> 500f;
        };
    }

    private void addLogLine(Stage stage) {
        String newLine = generateLogLine(stage);
        logLines.add(newLine);
        if (logLines.size() > 50) {
            logLines.remove(0);
        }
    }

    private String generateLogLine(Stage stage) {
        long tick = System.currentTimeMillis() / 50;
        String timestamp = String.format("[%04d]", tick % 10000);

        return switch (stage) {
            case EMPTY -> timestamp + " [IDLE] Awaiting ignition sequence";
            case GROWING -> {
                String[] msgs = {
                    " [CORE] Fusion rate increasing",
                    " [FUEL] Hydrogen consumption nominal",
                    " [TEMP] Core temperature rising",
                    " [STAB] Plasma containment stable"
                };
                yield timestamp + msgs[(int) (tick % msgs.length)];
            }
            case STAR -> {
                String[] msgs = {
                    " [CORE] Main sequence fusion active",
                    " [OUT] Energy output: " + (int) displayedEnergy + " TW",
                    " [FUEL] Helium ash accumulating",
                    " [STAB] All systems nominal"
                };
                yield timestamp + msgs[(int) (tick % msgs.length)];
            }
            case SUPERSTAR -> {
                String[] msgs = {
                    " [WARN] Core pressure critical",
                    " [WARN] Mass exceeding safe limits",
                    " [ALERT] Collapse threshold approaching",
                    " [CORE] Heavy element fusion detected"
                };
                yield timestamp + msgs[(int) (tick % msgs.length)];
            }
            case BLACK_HOLE -> {
                String[] msgs = {
                    " [SING] Event horizon stable",
                    " [GRAV] Hawking radiation detected",
                    " [CONT] Exotic matter containment active",
                    " [DATA] Spacetime curvature nominal"
                };
                yield timestamp + msgs[(int) (tick % msgs.length)];
            }
            case DEATH -> {
                String[] msgs = {
                    " [CRIT] CONTAINMENT FAILURE",
                    " [CRIT] EMERGENCY PROTOCOLS ACTIVE",
                    " [CRIT] EVACUATE IMMEDIATELY",
                    " [CRIT] SYSTEM FAILURE IMMINENT"
                };
                yield timestamp + msgs[(int) (tick % msgs.length)];
            }
            case DEATH_GRACEFUL -> timestamp + " [SHUT] Controlled shutdown in progress";
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
        int accentColor = getStageColor(stage);

        DrawerHelper.drawSolidRect(graphics, x, y, w, h, 0xDD080810);
        DrawerHelper.drawBorder(graphics, x, y, w, h, (0x80 << 24) | accentColor, 1);

        graphics.fill(x + 1, y + 1, x + w - 1, y + 3, (0x60 << 24) | accentColor);

        int dataHeight = 50;
        drawDataReadouts(graphics, x + 5, y + 8, w - 10, dataHeight, stage, accentColor);

        int logY = y + 8 + dataHeight + 5;
        int logH = h - dataHeight - 18;
        drawLogPanel(graphics, x + 5, logY, w - 10, logH, stage, accentColor);
    }

    private void drawDataReadouts(GuiGraphics graphics, int x, int y, int w, int h, Stage stage, int accentColor) {
        var font = Minecraft.getInstance().font;

        graphics.fill(x, y, x + w, y + h, 0x40000000);
        DrawerHelper.drawBorder(graphics, x, y, w, h, (0x40 << 24) | accentColor, 1);

        int col1 = x + 5;
        int col2 = x + w / 2 + 5;
        int row1 = y + 5;
        int row2 = y + 15;
        int row3 = y + 25;
        int row4 = y + 35;

        int labelColor = 0xFF606080;
        int valueColor = 0xFFCCCCCC;

        graphics.drawString(font, "CORE TEMP:", col1, row1, labelColor, false);
        graphics.drawString(font, formatTemperature(displayedTemp), col1 + 60, row1, getTemperatureColor(displayedTemp), false);

        graphics.drawString(font, "PRESSURE:", col1, row2, labelColor, false);
        graphics.drawString(font, String.format("%.1f GPa", displayedPressure), col1 + 60, row2, valueColor, false);

        graphics.drawString(font, "MASS:", col2, row1, labelColor, false);
        graphics.drawString(font, String.format("%.2f M\u2609", displayedMass), col2 + 35, row1, valueColor, false);

        graphics.drawString(font, "OUTPUT:", col2, row2, labelColor, false);
        graphics.drawString(font, formatEnergy(displayedEnergy), col2 + 45, row2, valueColor, false);

        String status = getStatusString(stage);
        int statusColor = getStatusColor(stage);
        graphics.drawString(font, "STATUS:", col1, row3, labelColor, false);

        if (stage == Stage.DEATH && ((int) (glitchPhase * 2) % 3 == 0)) {
            int glitchOffset = (int) ((Math.random() - 0.5) * 4);
            graphics.drawString(font, status, col1 + 45 + glitchOffset, row3, statusColor, false);
        } else {
            graphics.drawString(font, status, col1 + 45, row3, statusColor, false);
        }

        String stageLabel = "PHASE: " + stage.name().replace("_", " ");
        graphics.drawString(font, stageLabel, col1, row4, (0xC0 << 24) | accentColor, false);
    }

    private void drawLogPanel(GuiGraphics graphics, int x, int y, int w, int h, Stage stage, int accentColor) {
        var font = Minecraft.getInstance().font;

        graphics.fill(x, y, x + w, y + h, 0x60000000);
        DrawerHelper.drawBorder(graphics, x, y, w, h, (0x30 << 24) | accentColor, 1);

        graphics.drawString(font, "SYSTEM LOG", x + 3, y + 2, (0x80 << 24) | accentColor, false);

        int logStartY = y + 12;
        int logHeight = h - 14;
        int lineHeight = 9;
        int visibleLines = logHeight / lineHeight;

        graphics.enableScissor(x + 2, logStartY, x + w - 2, y + h - 2);

        int startIndex = Math.max(0, logLines.size() - visibleLines);
        for (int i = startIndex; i < logLines.size(); i++) {
            int lineY = logStartY + (i - startIndex) * lineHeight;
            String line = logLines.get(i);

            int lineColor;
            if (line.contains("[CRIT]")) {
                lineColor = 0xFFFF4444;
            } else if (line.contains("[WARN]") || line.contains("[ALERT]")) {
                lineColor = 0xFFFFAA44;
            } else if (line.contains("[SYS]")) {
                lineColor = 0xFF44AAFF;
            } else {
                lineColor = 0xFF888888;
            }

            if (stage == Stage.DEATH && line.contains("[CRIT]")) {
                if ((int) (glitchPhase * 3) % 2 == 0) {
                    int glitchX = (int) ((Math.random() - 0.5) * 3);
                    graphics.drawString(font, line, x + 3 + glitchX, lineY, lineColor, false);
                }
            } else {
                graphics.drawString(font, line, x + 3, lineY, lineColor, false);
            }
        }

        graphics.disableScissor();
    }

    private String formatTemperature(float temp) {
        if (Float.isInfinite(temp)) {
            return "\u221E K";
        } else if (temp >= 1_000_000_000) {
            return String.format("%.1f GK", temp / 1_000_000_000);
        } else if (temp >= 1_000_000) {
            return String.format("%.1f MK", temp / 1_000_000);
        } else if (temp >= 1000) {
            return String.format("%.1f kK", temp / 1000);
        } else {
            return String.format("%.1f K", temp);
        }
    }

    private String formatEnergy(float energy) {
        if (energy >= 1_000_000) {
            return String.format("%.1f PW", energy / 1_000_000);
        } else if (energy >= 1000) {
            return String.format("%.1f TW", energy / 1000);
        } else {
            return String.format("%.0f GW", energy);
        }
    }

    private int getTemperatureColor(float temp) {
        if (temp >= 100_000_000) return 0xFFFF4444;
        if (temp >= 10_000_000) return 0xFFFFAA44;
        if (temp >= 1_000_000) return 0xFFFFFF44;
        return 0xFFCCCCCC;
    }

    private String getStatusString(Stage stage) {
        return switch (stage) {
            case EMPTY -> "DORMANT";
            case GROWING -> "IGNITING";
            case STAR -> "STABLE";
            case SUPERSTAR -> "CRITICAL";
            case BLACK_HOLE -> "CONTAINED";
            case DEATH -> "FAILURE";
            case DEATH_GRACEFUL -> "SHUTDOWN";
        };
    }

    private int getStatusColor(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0xFF606060;
            case GROWING -> 0xFF66AAFF;
            case STAR -> 0xFF66FF66;
            case SUPERSTAR -> 0xFFFFAA44;
            case BLACK_HOLE -> 0xFFAA66FF;
            case DEATH -> 0xFFFF4444;
            case DEATH_GRACEFUL -> 0xFF886666;
        };
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
