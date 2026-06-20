package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class ModuleToggleButton extends Widget {

    private static final ResourceLocation GEAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("gtceu",
            "textures/item/material_sets/dull/gear_small.png");

    private final Consumer<Boolean> onToggle;
    private final Supplier<Stage> stageSupplier;
    private boolean showingModules = false;
    private boolean hovered = false;
    private float hoverProgress = 0f;
    private float pulsePhase = 0f;

    public ModuleToggleButton(int x, int y, int width, int height, Consumer<Boolean> onToggle,
                              Supplier<Stage> stageSupplier) {
        super(x, y, width, height);
        this.onToggle = onToggle;
        this.stageSupplier = stageSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        pulsePhase += 0.1f;

        if (hovered && hoverProgress < 1f) {
            hoverProgress = Math.min(1f, hoverProgress + 0.15f);
        } else if (!hovered && hoverProgress > 0f) {
            hoverProgress = Math.max(0f, hoverProgress - 0.1f);
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

        hovered = isMouseOverElement(mouseX, mouseY);

        int bgAlpha = (int) (0xC0 + 0x20 * hoverProgress);
        int bgColor = (bgAlpha << 24) | 0x101820;
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, bgColor);

        Stage stage = stageSupplier != null ? stageSupplier.get() : Stage.EMPTY;
        int accentColor = getStageColor(stage);
        int borderAlpha = (int) (0x60 + 0x40 * hoverProgress);
        int borderColor = (borderAlpha << 24) | accentColor;
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        drawIcon(graphics, x, y, w, h);

        if (hoverProgress > 0) {
            int glowAlpha = (int) (0x20 * hoverProgress);
            int glowColor = (glowAlpha << 24) | accentColor;
            DrawerHelper.drawBorder(graphics, x - 1, y - 1, w + 2, h + 2, glowColor, 1);
        }
    }

    private void drawIcon(GuiGraphics graphics, int x, int y, int w, int h) {
        Stage stage = stageSupplier != null ? stageSupplier.get() : Stage.EMPTY;
        int stageColor = getStageColor(stage);
        float pulseAlpha = 0.8f + 0.2f * Mth.sin(pulsePhase);

        float r = ((stageColor >> 16) & 0xFF) / 255f;
        float g = ((stageColor >> 8) & 0xFF) / 255f;
        float b = (stageColor & 0xFF) / 255f;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(r, g, b, pulseAlpha);

        int gearSize = Math.min(w, h) - 4;
        int gearX = x + (w - gearSize) / 2;
        int gearY = y + (h - gearSize) / 2;
        graphics.blit(GEAR_TEXTURE, gearX, gearY, 0, 0, gearSize, gearSize, gearSize, gearSize);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    private int getStageColor(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0x404050;      // Gray
            case GROWING -> 0x6080FF;    // Blue
            case STAR -> 0xFFCC44;       // Yellow/Gold
            case SUPERSTAR -> 0xFF8844;  // Orange
            case BLACK_HOLE -> 0x8040FF; // Purple
            case DEATH -> 0xFF2020;      // Red
            case DEATH_GRACEFUL -> 0x804040; // Dark red
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
            showingModules = !showingModules;
            if (onToggle != null) {
                onToggle.accept(showingModules);
            }
            playButtonClickSound();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);

        if (hovered) {
            String tooltipText = showingModules ? "cosmiccore.gui.stellar.show_star" :
                    "cosmiccore.gui.stellar.show_modules";
            graphics.renderTooltip(Minecraft.getInstance().font,
                    List.of(Component.translatable(tooltipText)),
                    java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    public boolean isShowingModules() {
        return showingModules;
    }

    public void setShowingModules(boolean showing) {
        this.showingModules = showing;
    }
}
