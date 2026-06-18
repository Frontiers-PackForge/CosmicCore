package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class UpgradeTreeButton extends Widget {

    private static final int BG_COLOR = 0xFF202030;
    private static final int BORDER_COLOR = 0xFF404060;
    private static final int HOVER_COLOR = 0xFF303050;
    private static final int ACCENT_COLOR = 0xFFFFCC44;

    private final Consumer<Boolean> onClick;
    private final Supplier<IrisMultiblockMachine> machineSupplier;
    private boolean hovered = false;
    private float pulsePhase = 0f;

    public UpgradeTreeButton(int x, int y, int width, int height,
                             Consumer<Boolean> onClick,
                             Supplier<IrisMultiblockMachine> machineSupplier) {
        super(x, y, width, height);
        this.onClick = onClick;
        this.machineSupplier = machineSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        pulsePhase += 0.1f;
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

        // Background
        int bgColor = hovered ? HOVER_COLOR : BG_COLOR;
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, bgColor);

        // Border - pulse if has spendable points
        int borderColor = BORDER_COLOR;
        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine != null && machine.getSpendablePoints() > 0) {
            float pulse = (float) (Math.sin(pulsePhase) * 0.5 + 0.5);
            int r = (int) (((ACCENT_COLOR >> 16) & 0xFF) * pulse + ((BORDER_COLOR >> 16) & 0xFF) * (1 - pulse));
            int g = (int) (((ACCENT_COLOR >> 8) & 0xFF) * pulse + ((BORDER_COLOR >> 8) & 0xFF) * (1 - pulse));
            int b = (int) ((ACCENT_COLOR & 0xFF) * pulse + (BORDER_COLOR & 0xFF) * (1 - pulse));
            borderColor = 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        // Draw upgrade tree icon (stylized star with branches)
        var font = Minecraft.getInstance().font;
        int iconColor = hovered ? 0xFFFFFFFF : 0xFFAAAAAA;

        // Simple "UP" text or star symbol
        String icon = "\u2726"; // Star symbol
        int iconX = x + (w - font.width(icon)) / 2;
        int iconY = y + (h - 8) / 2;
        graphics.drawString(font, icon, iconX, iconY, iconColor, false);

        // Draw point count badge if has points
        if (machine != null && machine.getSpendablePoints() > 0) {
            String points = String.valueOf(machine.getSpendablePoints());
            int badgeW = font.width(points) + 4;
            int badgeX = x + w - badgeW + 2;
            int badgeY = y - 2;

            DrawerHelper.drawSolidRect(graphics, badgeX, badgeY, badgeW, 8, ACCENT_COLOR);
            graphics.drawString(font, points, badgeX + 2, badgeY, 0xFF000000, false);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.8f));
            onClick.accept(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
