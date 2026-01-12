package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class StageContextPanel extends WidgetGroup {

    private final Supplier<IrisMultiblockMachine> machineSupplier;
    private final StellarIrisWidget parentWidget;

    public StageContextPanel(int x, int y, int width, int height,
                             Supplier<IrisMultiblockMachine> machineSupplier,
                             StellarIrisWidget parentWidget) {
        super(x, y, width, height);
        this.machineSupplier = machineSupplier;
        this.parentWidget = parentWidget;
        initWidgets();
    }

    private void initWidgets() {
        addWidget(new LabelWidget(5, 5, this::getStagePanelTitle));

        addWidget(new FuelGaugeWidget(5, 22, getSize().width - 10, 30, parentWidget::getFuelLevel));

        addWidget(new IgnitionButtonWidget(
            5, 58, getSize().width - 10, 24,
            parentWidget::canIgnite,
            () -> getCurrentStage() == Stage.EMPTY || parentWidget.canIgnite(),
            parentWidget::requestIgnition
        ));

        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine != null) {
            SlotWidget starSeedSlot = new SlotWidget(machine.getInventory().storage, 0, 5, 88, true, true);
            starSeedSlot.setBackground(GuiTextures.SLOT, GuiTextures.ATOMIC_OVERLAY_1);
            addWidget(starSeedSlot);
            addWidget(new LabelWidget(28, 92, "Star Seed").setTextColor(0xFF808090));
        }
    }

    private Stage getCurrentStage() {
        IrisMultiblockMachine machine = machineSupplier.get();
        return machine != null ? machine.getStage() : Stage.EMPTY;
    }

    private String getStagePanelTitle() {
        return switch (getCurrentStage()) {
            case EMPTY -> "INITIALIZATION";
            case GROWING -> "STELLAR IGNITION";
            case STAR -> "STELLAR OPERATIONS";
            case SUPERSTAR -> "CRITICAL MASS";
            case BLACK_HOLE -> "SINGULARITY CONTROL";
            case DEATH -> "EMERGENCY PROTOCOLS";
            case DEATH_GRACEFUL -> "CONTROLLED SHUTDOWN";
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        DrawerHelper.drawSolidRect(graphics, x, y, w, h, 0xCC0a0a14);

        int accentColor = getStageAccentColor();
        DrawerHelper.drawBorder(graphics, x, y, w, h, accentColor, 1);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 3, accentColor);

        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        drawStageInfo(graphics, x, y, w, h);
    }

    private void drawStageInfo(GuiGraphics graphics, int x, int y, int w, int h) {
        var font = Minecraft.getInstance().font;
        int infoY = y + h - 35;
        int textColor = 0xFF808090;

        switch (getCurrentStage()) {
            case EMPTY -> {
                graphics.drawString(font, "Insert star seed and", x + 5, infoY, textColor, false);
                graphics.drawString(font, "provide stellar gases", x + 5, infoY + 10, textColor, false);
                graphics.drawString(font, "to begin ignition.", x + 5, infoY + 20, textColor, false);
            }
            case GROWING -> {
                graphics.drawString(font, "Stellar fusion", x + 5, infoY, 0xFFAAAAFF, false);
                graphics.drawString(font, "initiating...", x + 5, infoY + 10, 0xFFAAAAFF, false);
            }
            case STAR -> {
                graphics.drawString(font, "Stable fusion active", x + 5, infoY, 0xFFFFCC44, false);
                graphics.drawString(font, "Processing available", x + 5, infoY + 10, textColor, false);
            }
            case SUPERSTAR -> {
                graphics.drawString(font, "WARNING: Critical mass", x + 5, infoY, 0xFFFF8844, false);
                graphics.drawString(font, "Collapse imminent", x + 5, infoY + 10, 0xFFFF6622, false);
            }
            case BLACK_HOLE -> {
                graphics.drawString(font, "Singularity contained", x + 5, infoY, 0xFFAA66FF, false);
                graphics.drawString(font, "Exotic processing", x + 5, infoY + 10, 0xFF8844DD, false);
            }
            case DEATH -> {
                if (parentWidget.getTickCounter() % 20 < 10) {
                    graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0x30FF0000);
                }
                graphics.drawString(font, "CRITICAL FAILURE", x + 5, infoY, 0xFFFF0000, false);
                graphics.drawString(font, "EVACUATE AREA", x + 5, infoY + 10, 0xFFFF4444, false);
            }
            case DEATH_GRACEFUL -> {
                graphics.drawString(font, "Controlled shutdown", x + 5, infoY, 0xFF884444, false);
                graphics.drawString(font, "in progress...", x + 5, infoY + 10, textColor, false);
            }
        }
    }

    private int getStageAccentColor() {
        return switch (getCurrentStage()) {
            case EMPTY -> 0xFF404060;
            case GROWING -> 0xFF6080FF;
            case STAR -> 0xFFFFCC44;
            case SUPERSTAR -> 0xFFFF8844;
            case BLACK_HOLE -> 0xFF8040FF;
            case DEATH -> 0xFFFF2020;
            case DEATH_GRACEFUL -> 0xFF804040;
        };
    }
}
