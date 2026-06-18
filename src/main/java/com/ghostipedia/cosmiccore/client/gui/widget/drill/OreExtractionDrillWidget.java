package com.ghostipedia.cosmiccore.client.gui.widget.drill;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.OreExtractionDrillLogic;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.OreExtractionDrillMachine;

import com.gregtechceu.gtceu.api.GTValues;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class OreExtractionDrillWidget extends WidgetGroup {

    public static final int WIDTH = 280;
    public static final int HEIGHT = 150;

    private static final int[] TIER_COLORS = {
            0xFFC08040,
            0xFF60A0D0,
            0xFF8080C0,
            0xFFD0A040
    };

    private final Supplier<OreExtractionDrillMachine> machineSupplier;

    private int tier = 0;
    private int phase = 0;
    private float scanProgress = 0f;
    private int currentOre = 0;
    private int totalOres = 0;
    private int miningProgressSec = 0;
    private int totalMiningSec = 10;
    private float removalChance = 0.5f;
    private int yieldMultiplier = 2;
    private Map<String, Integer> oreTypes = new LinkedHashMap<>();

    private float animPhase = 0f;
    private float drillAnim = 0f;

    // Ore list scrolling
    private int oreScrollOffset = 0;
    private static final int MAX_VISIBLE_ORES = 8;

    public OreExtractionDrillWidget(Supplier<OreExtractionDrillMachine> machineSupplier) {
        super(0, 0, WIDTH, HEIGHT);
        this.machineSupplier = machineSupplier;
        initWidgets();
    }

    private void initWidgets() {
        addWidget(new OreExtractionDrillBackgroundWidget(0, 0, WIDTH, HEIGHT));
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        syncDrillData(buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        readDrillData(buffer);
    }

    private void syncDrillData(FriendlyByteBuf buffer) {
        OreExtractionDrillMachine machine = machineSupplier.get();
        if (machine == null) {
            buffer.writeInt(0);
            buffer.writeInt(0);
            buffer.writeFloat(0f);
            buffer.writeInt(0);
            buffer.writeInt(0);
            buffer.writeInt(0);
            buffer.writeInt(10);
            buffer.writeFloat(0.5f);
            buffer.writeInt(2);
            buffer.writeInt(0);
            return;
        }

        OreExtractionDrillLogic logic = machine.getRecipeLogic();
        buffer.writeInt(machine.getTierIndex());
        buffer.writeInt(logic.getPhase().ordinal());
        buffer.writeFloat(logic.getScanProgressPercent());
        buffer.writeInt(logic.getCurrentOreIndex());
        buffer.writeInt(logic.getPendingOreCount());
        buffer.writeInt(logic.getMiningProgressSeconds());
        buffer.writeInt(logic.getTotalMiningSeconds());
        buffer.writeFloat(machine.getRemovalChance());
        buffer.writeInt(machine.getEffectiveYieldMultiplier());

        Map<String, Integer> oreCounts = logic.getOreTypeCounts();
        buffer.writeInt(oreCounts.size());
        for (Map.Entry<String, Integer> entry : oreCounts.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
    }

    private void readDrillData(FriendlyByteBuf buffer) {
        tier = buffer.readInt();
        phase = buffer.readInt();
        scanProgress = buffer.readFloat();
        currentOre = buffer.readInt();
        totalOres = buffer.readInt();
        miningProgressSec = buffer.readInt();
        totalMiningSec = buffer.readInt();
        removalChance = buffer.readFloat();
        yieldMultiplier = buffer.readInt();

        oreTypes.clear();
        int count = buffer.readInt();
        for (int i = 0; i < count; i++) {
            String key = buffer.readUtf();
            int value = buffer.readInt();
            oreTypes.put(key, value);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        OreExtractionDrillMachine machine = machineSupplier.get();
        if (machine == null) return;

        OreExtractionDrillLogic logic = machine.getRecipeLogic();
        int newPhase = logic.getPhase().ordinal();
        int newCurrent = logic.getCurrentOreIndex();
        int newTotal = logic.getPendingOreCount();
        float newScan = logic.getScanProgressPercent();

        if (newPhase != phase || newCurrent != currentOre || newTotal != totalOres ||
                Math.abs(newScan - scanProgress) > 1f) {
            writeUpdateInfo(501, this::syncDrillData);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 501) {
            readDrillData(buffer);
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        animPhase += 0.05f;
        if (phase == 2) {
            drillAnim += 0.15f;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        int tierColor = getTierColor();
        DrawerHelper.drawBorder(graphics, x, y, w, h, (tierColor & 0x00FFFFFF) | 0x60000000, 1);

        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        drawPhasePanel(graphics, x, y, w);
        drawStatsPanel(graphics, x, y, w, h);
        drawOreList(graphics, x, y, w, h);
    }

    private void drawPhasePanel(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;
        int tierColor = getTierColor();

        int panelY = y + 5;
        int panelH = 36;

        DrawerHelper.drawSolidRect(graphics, x + 5, panelY, w - 10, panelH, 0xF0181820);

        String phaseName = switch (phase) {
            case 0 -> "IDLE";
            case 1 -> "SCANNING";
            case 2 -> "MINING";
            case 3 -> "COMPLETE";
            default -> "UNKNOWN";
        };

        int phaseColor = switch (phase) {
            case 0 -> 0xFF808080;
            case 1 -> 0xFFFFCC44;
            case 2 -> 0xFF44FF88;
            case 3 -> 0xFF44CCFF;
            default -> 0xFFFFFFFF;
        };

        float pulse = Mth.sin(animPhase * 2f) * 0.2f + 0.8f;
        if (phase == 1 || phase == 2) {
            int pulseAlpha = (int) (0xFF * pulse);
            phaseColor = (pulseAlpha << 24) | (phaseColor & 0x00FFFFFF);
        }

        graphics.drawString(font, "Phase: " + phaseName, x + 10, panelY + 4, phaseColor, false);

        int barY = panelY + 16;
        int barW = w - 20;
        int barH = 14;

        DrawerHelper.drawSolidRect(graphics, x + 10, barY, barW, barH, 0xFF202030);

        float progress = 0f;
        String progressText = "";

        if (phase == 1) {
            progress = scanProgress / 100f;
            progressText = String.format("%.1f%% - %d ores found", scanProgress, totalOres);
        } else if (phase == 2) {
            progress = totalOres > 0 ? (float) currentOre / totalOres : 0f;
            progressText = String.format("Ore %d / %d (%ds / %ds)", currentOre + 1, totalOres, miningProgressSec,
                    totalMiningSec);
        } else if (phase == 3) {
            progress = 1f;
            progressText = "Area cleared - Use screwdriver to restart";
        }

        int fillW = (int) (barW * progress);
        if (fillW > 0) {
            int fillColor = darkenColor(tierColor, 0.7f);
            DrawerHelper.drawGradientRect(graphics, x + 10, barY, fillW, barH, fillColor, tierColor, true);

            if (phase == 2 && fillW > 4) {
                float edgePulse = Mth.sin(drillAnim) * 0.4f + 0.6f;
                int edgeAlpha = (int) (0xFF * edgePulse);
                int edgeColor = (edgeAlpha << 24) | 0xFFFFFF;
                graphics.fill(x + 10 + fillW - 3, barY, x + 10 + fillW, barY + barH, edgeColor);
            }
        }

        int textColor = progress > 0.5f ? 0xFF000000 : 0xFFCCCCCC;
        int textX = x + 10 + (barW - font.width(progressText)) / 2;
        graphics.drawString(font, progressText, textX, barY + 3, textColor, false);
    }

    private void drawStatsPanel(GuiGraphics graphics, int x, int y, int w, int h) {
        var font = Minecraft.getInstance().font;
        int tierColor = getTierColor();

        int panelX = x + 5;
        int panelY = y + 45;
        int panelW = 115;
        int panelH = h - 51;

        DrawerHelper.drawSolidRect(graphics, panelX, panelY, panelW, panelH, 0xF0181820);
        DrawerHelper.drawBorder(graphics, panelX, panelY, panelW, panelH, (tierColor & 0x00FFFFFF) | 0x60000000, 1);

        graphics.drawString(font, "STATS", panelX + 4, panelY + 3, 0xFF909090, false);

        int statY = panelY + 15;
        int statGap = 11;

        String chanceText = String.format("%.1f%% Depletion", removalChance * 100f);
        graphics.drawString(font, chanceText, panelX + 6, statY, 0xFFFFCC44, false);
        statY += statGap;

        long euPerTick = GTValues.V[tierIndexToGTTier(tier)];
        String euText = euPerTick + " EU/t";
        graphics.drawString(font, euText, panelX + 6, statY, 0xFF44CCFF, false);
        statY += statGap;

        String areaText = "144x144 area";
        graphics.drawString(font, areaText, panelX + 6, statY, 0xFF808080, false);
    }

    private java.util.List<Map.Entry<String, Integer>> getGroupedOres() {
        Map<String, Integer> grouped = new LinkedHashMap<>();
        for (var entry : oreTypes.entrySet()) {
            grouped.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        return grouped.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .toList();
    }

    private void drawOreList(GuiGraphics graphics, int x, int y, int w, int h) {
        var font = Minecraft.getInstance().font;
        int tierColor = getTierColor();

        int panelX = x + 125;
        int panelY = y + 45;
        int panelW = w - 130;
        int panelH = h - 51;

        DrawerHelper.drawSolidRect(graphics, panelX, panelY, panelW, panelH, 0xF0181820);
        DrawerHelper.drawBorder(graphics, panelX, panelY, panelW, panelH, (tierColor & 0x00FFFFFF) | 0x60000000, 1);

        graphics.drawString(font, "ORE TYPES", panelX + 4, panelY + 3, 0xFF909090, false);

        if (oreTypes.isEmpty()) {
            graphics.drawString(font, "No ores found", panelX + 6, panelY + 16, 0xFF606060, false);
            return;
        }

        var groupedOres = getGroupedOres();

        int totalOreTypes = groupedOres.size();
        int maxScroll = Math.max(0, totalOreTypes - MAX_VISIBLE_ORES);
        oreScrollOffset = Math.min(oreScrollOffset, maxScroll);

        int oreY = panelY + 16;
        int endIndex = Math.min(oreScrollOffset + MAX_VISIBLE_ORES, totalOreTypes);

        for (int i = oreScrollOffset; i < endIndex; i++) {
            var entry = groupedOres.get(i);
            String oreName = entry.getKey();
            String countText = "x" + entry.getValue();

            int countWidth = font.width(countText);
            int maxNameWidth = panelW - countWidth - 16;

            String displayName = oreName;
            if (font.width(displayName) > maxNameWidth) {
                while (font.width(displayName + "..") > maxNameWidth && displayName.length() > 1) {
                    displayName = displayName.substring(0, displayName.length() - 1);
                }
                displayName = displayName + "..";
            }

            graphics.drawString(font, displayName, panelX + 6, oreY, 0xFFB0B0B0, false);
            graphics.drawString(font, countText, panelX + panelW - countWidth - 6, oreY, 0xFFAAAAAA, false);

            oreY += 10;
        }

        if (totalOreTypes > MAX_VISIBLE_ORES) {
            int scrollBarX = panelX + panelW - 4;
            int scrollBarY = panelY + 14;
            int scrollBarH = panelH - 18;

            graphics.fill(scrollBarX, scrollBarY, scrollBarX + 2, scrollBarY + scrollBarH, 0xFF0a0a0a);

            float scrollPercent = maxScroll > 0 ? (float) oreScrollOffset / maxScroll : 0f;
            int thumbH = Math.max(8, scrollBarH * MAX_VISIBLE_ORES / totalOreTypes);
            int thumbY = scrollBarY + (int) ((scrollBarH - thumbH) * scrollPercent);
            graphics.fill(scrollBarX, thumbY, scrollBarX + 2, thumbY + thumbH, (tierColor & 0x00FFFFFF) | 0xA0000000);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        int panelX = x + 125;
        int panelY = y + 45;
        int panelW = w - 130;
        int panelH = h - 51;

        if (mouseX >= panelX && mouseX < panelX + panelW &&
                mouseY >= panelY && mouseY < panelY + panelH) {
            int totalOreTypes = getGroupedOres().size();
            int maxScroll = Math.max(0, totalOreTypes - MAX_VISIBLE_ORES);

            if (wheelDelta > 0) {
                oreScrollOffset = Math.max(0, oreScrollOffset - 1);
            } else if (wheelDelta < 0) {
                oreScrollOffset = Math.min(maxScroll, oreScrollOffset + 1);
            }
            return true;
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    private int getTierColor() {
        if (tier < 0) return TIER_COLORS[0];
        if (tier >= TIER_COLORS.length) return TIER_COLORS[TIER_COLORS.length - 1];
        return TIER_COLORS[tier];
    }

    private int tierIndexToGTTier(int index) {
        return switch (index) {
            case 0 -> GTValues.LV;
            case 1 -> GTValues.HV;
            case 2 -> GTValues.IV;
            case 3 -> GTValues.ZPM;
            default -> GTValues.LV;
        };
    }

    private int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
