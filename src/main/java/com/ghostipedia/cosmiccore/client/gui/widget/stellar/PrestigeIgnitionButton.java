package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;
import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;

public class PrestigeIgnitionButton extends Widget {

    private final BooleanSupplier isPrestigeItemPresent;
    private final BooleanSupplier hasActiveStar;
    private final Runnable onPrestigeTriggered;

    private float warningScrollPhase = 0f;
    private float glitchPhase = 0f;
    private float hoverProgress = 0f;
    private boolean wasHovered = false;
    private int lastMouseX, lastMouseY;

    private int crackStage = 0;
    private float crackAnimProgress = 0f;
    private boolean isHolding = false;
    private int holdTicks = 0;
    private static final int HOLD_THRESHOLD = 25;

    private boolean isBreaking = false;
    private float breakAnimProgress = 0f;
    private float[] shardOffsets;

    private final Random scrambleRandom = new Random();
    private String currentScrambledText = "";
    private int scrambleUpdateCounter = 0;
    private static final String SCRAMBLE_CHARS = "!@#$%^&*<>?/\\|=-+";

    private static final int BG_COLOR_DARK = 0xFF1A0808;
    private static final int BG_COLOR_MID = 0xFF2A1010;
    private static final int BORDER_COLOR = 0xFFFF2020;
    private static final int WARNING_STRIPE_1 = 0xFFCC0000;
    private static final int WARNING_STRIPE_2 = 0xFF440000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int WARNING_ICON_COLOR = 0xFFFFCC00;

    public PrestigeIgnitionButton(int x, int y, int width, int height,
                                  BooleanSupplier isPrestigeItemPresent,
                                  BooleanSupplier hasActiveStar,
                                  Runnable onPrestigeTriggered) {
        super(x, y, width, height);
        this.isPrestigeItemPresent = isPrestigeItemPresent;
        this.hasActiveStar = hasActiveStar;
        this.onPrestigeTriggered = onPrestigeTriggered;

        shardOffsets = new float[12];
        for (int i = 0; i < shardOffsets.length; i++) {
            shardOffsets[i] = (float) (Math.random() * 2 - 1);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();

        if (!isPrestigeItemPresent.getAsBoolean()) {
            return;
        }

        warningScrollPhase += 0.8f;
        glitchPhase += 0.15f;

        boolean hovered = isMouseOverElement(lastMouseX, lastMouseY);
        float targetHover = hovered ? 1f : 0f;
        hoverProgress = Mth.lerp(0.15f, hoverProgress, targetHover);

        if (hovered && !wasHovered && !isBreaking) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 0.8f, 0.2f));
        }
        wasHovered = hovered;

        if (isHolding && !isBreaking && crackStage < 3) {
            holdTicks++;
            if (holdTicks >= HOLD_THRESHOLD) {
                advanceCrackStage();
                holdTicks = 0;
            }
        }

        crackAnimProgress = Mth.lerp(0.1f, crackAnimProgress, 0f);

        if (isBreaking) {
            breakAnimProgress += 0.05f;
            if (breakAnimProgress >= 1f) {
                onPrestigeTriggered.run();
                isBreaking = false;
                breakAnimProgress = 0f;
                crackStage = 0;
            }
        }

        scrambleUpdateCounter++;
        if (scrambleUpdateCounter >= 3) {
            scrambleUpdateCounter = 0;
            updateScrambledText();
        }
    }

    private void advanceCrackStage() {
        crackStage++;
        crackAnimProgress = 1f;

        float pitch = 0.6f + crackStage * 0.15f;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.GLASS_BREAK, pitch, 0.8f));

        if (crackStage == 1) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.STONE_BREAK, 0.5f, 0.5f));
        } else if (crackStage == 2) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.ANVIL_LAND, 0.3f, 0.3f));
        } else if (crackStage >= 3) {
            isBreaking = true;
            breakAnimProgress = 0f;

            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.GLASS_BREAK, 0.4f, 1.0f));
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE, 0.7f, 0.5f));
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.TOTEM_USE, 1.2f, 0.8f));
        }
    }

    private void updateScrambledText() {
        StringBuilder sb = new StringBuilder();
        float glitchIntensity = 0.3f + crackStage * 0.2f;
        String baseText = Component.translatable("cosmiccore.stellar.ignition.ignite").getString();

        for (int i = 0; i < baseText.length(); i++) {
            if (scrambleRandom.nextFloat() < glitchIntensity) {
                sb.append(SCRAMBLE_CHARS.charAt(scrambleRandom.nextInt(SCRAMBLE_CHARS.length())));
            } else {
                sb.append(baseText.charAt(i));
            }
        }
        currentScrambledText = sb.toString();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        if (!isPrestigeItemPresent.getAsBoolean()) {
            return;
        }

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        boolean canActivate = hasActiveStar.getAsBoolean();

        if (isBreaking) {
            drawBreakingAnimation(graphics, x, y, w, h);
            return;
        }

        drawWarningBackground(graphics, x, y, w, h);
        drawMainButton(graphics, x, y, w, h, canActivate);
        drawCracks(graphics, x, y, w, h);
        drawWarningIcons(graphics, x, y, w, h);
        drawButtonText(graphics, x, y, w, h, canActivate);

        if (isHolding && crackStage < 3) {
            drawHoldProgress(graphics, x, y, w, h);
        }
    }

    private void drawWarningBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        float pulse = Mth.sin(glitchPhase) * 0.3f + 0.7f;
        int glowAlpha = (int) (40 * pulse * (1 + hoverProgress * 0.5f));
        int glowColor = (glowAlpha << 24) | (BORDER_COLOR & 0x00FFFFFF);

        for (int i = 3; i > 0; i--) {
            DrawerHelper.drawSolidRect(graphics, x - i, y - i, w + i * 2, h + i * 2, glowColor);
        }

        DrawerHelper.drawSolidRect(graphics, x, y, w, h, BG_COLOR_DARK);
    }

    private void drawMainButton(GuiGraphics graphics, int x, int y, int w, int h, boolean canActivate) {
        int stripeHeight = h;
        int stripeWidth = 16;
        float scrollOffset = warningScrollPhase % (stripeWidth * 2);

        graphics.enableScissor(x + 1, y + 1, x + w - 1, y + h - 1);

        for (int sx = (int) (-stripeWidth * 4 + scrollOffset); sx < w + stripeWidth * 2; sx += stripeWidth) {
            int stripe1X = x + sx;
            int stripe2X = x + sx + stripeWidth / 2;

            drawDiagonalStripe(graphics, stripe1X, y, stripeWidth / 2, h, WARNING_STRIPE_1);
            drawDiagonalStripe(graphics, stripe2X, y, stripeWidth / 2, h, WARNING_STRIPE_2);
        }

        graphics.disableScissor();

        int centerPadding = 25;
        int centerAlpha = canActivate ? 0xD0 : 0xE0;
        int centerColor = (centerAlpha << 24) | (BG_COLOR_MID & 0x00FFFFFF);
        DrawerHelper.drawSolidRect(graphics, x + centerPadding, y + 4, w - centerPadding * 2, h - 8, centerColor);

        int borderColor = canActivate ? BORDER_COLOR : 0xFF602020;
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        int innerBorderColor = canActivate ? 0xFF801010 : 0xFF401010;
        DrawerHelper.drawBorder(graphics, x + 1, y + 1, w - 2, h - 2, innerBorderColor, 1);

        if (canActivate) {
            float accentPulse = Mth.sin(glitchPhase * 2) * 0.5f + 0.5f;
            int accentAlpha = (int) (150 + 100 * accentPulse);
            int accentColor = (accentAlpha << 24) | (BORDER_COLOR & 0x00FFFFFF);
            graphics.fill(x + 2, y + 2, x + w - 2, y + 3, accentColor);
            graphics.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, accentColor);
        }
    }

    private void drawDiagonalStripe(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        int skew = h / 3;
        for (int row = 0; row < h; row++) {
            int offset = (row * skew) / h;
            graphics.fill(x + offset, y + row, x + offset + w, y + row + 1, color);
        }
    }

    private void drawCracks(GuiGraphics graphics, int x, int y, int w, int h) {
        if (crackStage == 0) return;

        int crackColor = 0xFF000000;
        int highlightColor = 0x60FFFFFF;

        int shakeX = 0, shakeY = 0;
        if (crackAnimProgress > 0.5f) {
            shakeX = (int) ((Math.random() - 0.5) * 4 * crackAnimProgress);
            shakeY = (int) ((Math.random() - 0.5) * 4 * crackAnimProgress);
        }

        int cx = x + w / 2 + shakeX;
        int cy = y + h / 2 + shakeY;

        if (crackStage >= 1) {
            drawCrackLine(graphics, x + 5, y + 3, cx - 10, cy - 5, crackColor);
            drawCrackLine(graphics, x + 6, y + 4, cx - 9, cy - 4, highlightColor);
        }

        if (crackStage >= 2) {
            drawCrackLine(graphics, x + w - 5, y + h - 3, cx + 10, cy + 5, crackColor);
            drawCrackLine(graphics, x + w - 6, y + h - 4, cx + 9, cy + 4, highlightColor);

            drawCrackLine(graphics, cx, cy, cx + 15, cy - 8, crackColor);
            drawCrackLine(graphics, cx, cy, cx - 12, cy + 10, crackColor);
        }

        if (crackStage >= 3) {
            for (int i = 0; i < 8; i++) {
                float angle = i * Mth.TWO_PI / 8;
                int endX = cx + (int) (Mth.cos(angle) * 20);
                int endY = cy + (int) (Mth.sin(angle) * 10);
                drawCrackLine(graphics, cx, cy, endX, endY, crackColor);
            }
        }
    }

    private void drawCrackLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int px = x1, py = y1;
        int jitterCounter = 0;

        while (true) {
            int jx = px + (jitterCounter % 3 == 0 ? (int) (Math.random() * 2 - 1) : 0);
            int jy = py + (jitterCounter % 4 == 0 ? (int) (Math.random() * 2 - 1) : 0);
            graphics.fill(jx, jy, jx + 1, jy + 1, color);

            if (px == x2 && py == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                px += sx;
            }
            if (e2 < dx) {
                err += dx;
                py += sy;
            }
            jitterCounter++;
        }
    }

    private void drawWarningIcons(GuiGraphics graphics, int x, int y, int w, int h) {
        int iconSize = 12;
        int iconY = y + (h - iconSize) / 2;

        drawWarningTriangle(graphics, x + 6, iconY, iconSize);
        drawWarningTriangle(graphics, x + w - 6 - iconSize, iconY, iconSize);
    }

    private void drawWarningTriangle(GuiGraphics graphics, int x, int y, int size) {
        float flash = Mth.sin(glitchPhase * 3) * 0.3f + 0.7f;
        int alpha = (int) (255 * flash);
        int color = (alpha << 24) | (WARNING_ICON_COLOR & 0x00FFFFFF);

        int cx = x + size / 2;
        for (int row = 0; row < size; row++) {
            int halfWidth = (row * size) / (size * 2);
            graphics.fill(cx - halfWidth, y + row, cx + halfWidth + 1, y + row + 1, color);
        }

        var font = Minecraft.getInstance().font;
        int exclamationWidth = font.width("!");
        graphics.drawString(font, "!", cx - exclamationWidth / 2, y + size / 3, 0xFF000000, false);
    }

    private void drawButtonText(GuiGraphics graphics, int x, int y, int w, int h, boolean canActivate) {
        var font = Minecraft.getInstance().font;

        String displayText;
        int textColor;

        if (!canActivate) {
            displayText = Component.translatable("cosmiccore.stellar.ignition.requires_star").getString();
            textColor = 0xFF804040;
        } else if (crackStage >= 3) {
            displayText = Component.translatable("cosmiccore.stellar.ignition.breaking").getString();
            textColor = 0xFFFFFFFF;
        } else {
            displayText = "[ " + currentScrambledText + " ]";
            textColor = TEXT_COLOR;
        }

        int textW = font.width(displayText);
        int textX = x + (w - textW) / 2;
        int textY = y + (h - font.lineHeight) / 2;

        if (canActivate) {
            graphics.drawString(font, displayText, textX + 1, textY + 1, 0xFF000000, false);
        }

        graphics.drawString(font, displayText, textX, textY, textColor, false);

        if (canActivate && crackStage > 0) {
            float glitchIntensity = crackStage * 0.3f + crackAnimProgress;
            if (scrambleRandom.nextFloat() < glitchIntensity * 0.5f) {
                int ghostAlpha = (int) (60 * glitchIntensity);
                int ghostColor = (ghostAlpha << 24) | 0x00FFFF;
                int offsetX = (int) ((Math.random() - 0.5) * 4);
                graphics.drawString(font, displayText, textX + offsetX, textY, ghostColor, false);
            }
        }
    }

    private void drawHoldProgress(GuiGraphics graphics, int x, int y, int w, int h) {
        float progress = (float) holdTicks / HOLD_THRESHOLD;

        int barHeight = 3;
        int barY = y + h - barHeight - 2;
        int barW = (int) ((w - 4) * progress);

        graphics.fill(x + 2, barY, x + w - 2, barY + barHeight, 0x80000000);

        int r = (int) (255 * (0.8f + 0.2f * progress));
        int g = (int) (255 * (1f - progress * 0.7f));
        int fillColor = 0xFF000000 | (r << 16) | (g << 8);
        graphics.fill(x + 2, barY, x + 2 + barW, barY + barHeight, fillColor);

        if (barW > 2) {
            graphics.fill(x + 2 + barW - 1, barY, x + 2 + barW, barY + barHeight, 0xCCFFFFFF);
        }
    }

    private void drawBreakingAnimation(GuiGraphics graphics, int x, int y, int w, int h) {
        int pieceW = w / 3;
        int pieceH = h / 2;

        for (int i = 0; i < 6; i++) {
            int row = i / 3;
            int col = i % 3;

            float dx = shardOffsets[i * 2] * breakAnimProgress * 50;
            float dy = shardOffsets[i * 2 + 1] * breakAnimProgress * 30 + breakAnimProgress * breakAnimProgress * 20;

            int px = x + col * pieceW + (int) dx;
            int py = y + row * pieceH + (int) dy;

            int alpha = (int) (255 * (1f - breakAnimProgress));
            int color = (alpha << 24) | (BG_COLOR_MID & 0x00FFFFFF);

            graphics.fill(px, py, px + pieceW - 1, py + pieceH - 1, color);

            int borderAlpha = alpha / 2;
            int borderColor = (borderAlpha << 24) | (BORDER_COLOR & 0x00FFFFFF);
            DrawerHelper.drawBorder(graphics, px, py, pieceW - 1, pieceH - 1, borderColor, 1);
        }

        for (int i = 0; i < 10; i++) {
            float particleProgress = breakAnimProgress + i * 0.05f;
            if (particleProgress > 1f) continue;

            float px = x + w / 2 + (float) Math.cos(i * 0.7) * particleProgress * 60;
            float py = y + h / 2 + (float) Math.sin(i * 0.7) * particleProgress * 40 +
                    particleProgress * particleProgress * 30;

            int particleAlpha = (int) (200 * (1f - particleProgress));
            int particleColor = (particleAlpha << 24) | 0xFFAA00;
            graphics.fill((int) px - 1, (int) py - 1, (int) px + 2, (int) py + 2, particleColor);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);

        if (isBreaking && breakAnimProgress < 0.3f) {
            int flashAlpha = (int) (150 * (1f - breakAnimProgress / 0.3f));
            graphics.fill(-1000, -1000, 2000, 2000, (flashAlpha << 24) | 0xFFFFFF);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isPrestigeItemPresent.getAsBoolean()) {
            return false;
        }

        if (!isMouseOverElement(mouseX, mouseY)) {
            return false;
        }

        if (button == 0 && hasActiveStar.getAsBoolean() && !isBreaking) {
            isHolding = true;
            holdTicks = 0;

            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, 0.8f, 0.6f));

            return true;
        }

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isHolding) {
            isHolding = false;
            holdTicks = 0;

            if (crackStage < 3 && !isBreaking) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, 0.9f, 0.4f));
            }

            return true;
        }
        return false;
    }

    public void reset() {
        crackStage = 0;
        crackAnimProgress = 0f;
        isHolding = false;
        holdTicks = 0;
        isBreaking = false;
        breakAnimProgress = 0f;
    }

    public int getCrackStage() {
        return crackStage;
    }

    public boolean isBreaking() {
        return isBreaking;
    }
}
