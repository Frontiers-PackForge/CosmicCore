package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;

public class IgnitionButtonWidget extends Widget {

    private final BooleanSupplier canIgnite;
    private final BooleanSupplier isVisible;
    private final Runnable onIgnite;

    private float hoverProgress = 0f;
    private float pulsePhase = 0f;
    private float chargeProgress = 0f;
    private boolean isCharging = false;
    private boolean wasHovered = false;

    private static final int CHARGE_TICKS = 40;
    private int chargeTicks = 0;

    public IgnitionButtonWidget(int x, int y, int width, int height,
                                BooleanSupplier canIgnite,
                                BooleanSupplier isVisible,
                                Runnable onIgnite) {
        super(x, y, width, height);
        this.canIgnite = canIgnite;
        this.isVisible = isVisible;
        this.onIgnite = onIgnite;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        pulsePhase += 0.15f;

        super.setVisible(isVisible.getAsBoolean());

        boolean hovered = isMouseOverElement(lastMouseX, lastMouseY);
        float targetHover = hovered ? 1f : 0f;
        hoverProgress = Mth.lerp(0.2f, hoverProgress, targetHover);

        if (hovered && !wasHovered && isVisible()) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.5f, 0.3f));
        }
        wasHovered = hovered;

        if (isCharging && canIgnite.getAsBoolean()) {
            chargeTicks++;
            chargeProgress = (float) chargeTicks / CHARGE_TICKS;
            if (chargeTicks >= CHARGE_TICKS) {
                onIgnite.run();
                isCharging = false;
                chargeTicks = 0;
                chargeProgress = 0f;
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 0.8f, 1.0f));
            }
        } else if (isCharging) {
            isCharging = false;
            chargeTicks = 0;
            chargeProgress = Mth.lerp(0.3f, chargeProgress, 0f);
        } else {
            chargeProgress = Mth.lerp(0.2f, chargeProgress, 0f);
        }
    }

    private int lastMouseX, lastMouseY;

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        if (!isVisible()) return;

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        boolean enabled = canIgnite.getAsBoolean();

        int bgColor;
        int borderColor;
        int textColor;

        if (!enabled) {
            bgColor = 0xFF1A1A2A;
            borderColor = 0xFF303040;
            textColor = 0xFF505060;
        } else if (isCharging) {
            float chargeGlow = 0.5f + chargeProgress * 0.5f;
            int r = (int) (255 * chargeGlow);
            int g = (int) (100 * (1f - chargeProgress * 0.5f));
            bgColor = 0xFF000000 | (r << 16) | (g << 8);
            borderColor = 0xFFFF8040;
            textColor = 0xFFFFFFFF;
        } else {
            float pulse = (float) (0.6f + 0.4f * Math.sin(pulsePhase));
            float hover = hoverProgress;
            int baseR = (int) (40 + 60 * pulse + 40 * hover);
            int baseG = (int) (30 + 40 * pulse + 30 * hover);
            int baseB = (int) (10 + 20 * pulse);
            bgColor = 0xFF000000 | (baseR << 16) | (baseG << 8) | baseB;
            borderColor = 0xFFFFAA40;
            textColor = 0xFFFFDD80;
        }

        DrawerHelper.drawSolidRect(graphics, x, y, w, h, bgColor);

        if (enabled) {
            float glowIntensity = hoverProgress * 0.5f + chargeProgress * 0.5f;
            if (glowIntensity > 0.01f) {
                int glowAlpha = (int) (glowIntensity * 60);
                int glowColor = (glowAlpha << 24) | (borderColor & 0x00FFFFFF);
                DrawerHelper.drawSolidRect(graphics, x - 2, y - 2, w + 4, h + 4, glowColor);
                DrawerHelper.drawSolidRect(graphics, x - 1, y - 1, w + 2, h + 2, glowColor);
            }
        }

        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        if (chargeProgress > 0.01f) {
            int chargeW = (int) (w * chargeProgress);
            int chargeColor = lerpColor(0x40FF8040, 0x80FF4020, chargeProgress);
            DrawerHelper.drawSolidRect(graphics, x + 1, y + 1, chargeW - 2, h - 2, chargeColor);

            if (chargeW > 3) {
                graphics.fill(x + chargeW - 2, y + 1, x + chargeW, y + h - 1, 0xCCFFFFFF);
            }
        }

        if (isCharging && chargeProgress > 0.3f) {
            drawEnergyArcs(graphics, x, y, w, h, chargeProgress);
        }

        var font = Minecraft.getInstance().font;
        String text = getButtonText(enabled);
        int textW = font.width(text);
        int textX = x + (w - textW) / 2;
        int textY = y + (h - font.lineHeight) / 2;

        if (enabled) {
            graphics.drawString(font, text, textX + 1, textY + 1, 0xFF000000, false);
        }
        graphics.drawString(font, text, textX, textY, textColor, false);

        if (enabled && !isCharging) {
            float shimmerPos = (pulsePhase * 2) % (w + 40) - 20;
            if (shimmerPos > 0 && shimmerPos < w) {
                int shimmerX = x + (int) shimmerPos;
                int shimmerW = Math.min(8, w - (int) shimmerPos);
                graphics.fill(shimmerX, y + 1, shimmerX + shimmerW, y + h - 1, 0x15FFFFFF);
            }
        }

        if (enabled) {
            int accentColor = isCharging ? 0xFFFF6040 : 0xFFFFAA40;
            graphics.fill(x, y, x + 4, y + 1, accentColor);
            graphics.fill(x, y, x + 1, y + 4, accentColor);
            graphics.fill(x + w - 4, y, x + w, y + 1, accentColor);
            graphics.fill(x + w - 1, y, x + w, y + 4, accentColor);
            graphics.fill(x, y + h - 1, x + 4, y + h, accentColor);
            graphics.fill(x, y + h - 4, x + 1, y + h, accentColor);
            graphics.fill(x + w - 4, y + h - 1, x + w, y + h, accentColor);
            graphics.fill(x + w - 1, y + h - 4, x + w, y + h, accentColor);
        }
    }

    private void drawEnergyArcs(GuiGraphics graphics, int x, int y, int w, int h, float intensity) {
        long time = System.currentTimeMillis();
        int centerX = x + w / 2;
        int centerY = y + h / 2;
        int particleCount = 3 + (int) (intensity * 5);

        for (int i = 0; i < particleCount; i++) {
            float particlePhase = ((time / 800f) + i * 0.15f) % 1f;
            float angle = (i * 2.39996f) + (time / 2000f);
            float edgeX, edgeY;

            if (i % 4 == 0) {
                edgeX = x + (w * ((i * 0.37f) % 1f));
                edgeY = y;
            } else if (i % 4 == 1) {
                edgeX = x + w;
                edgeY = y + (h * ((i * 0.37f) % 1f));
            } else if (i % 4 == 2) {
                edgeX = x + (w * ((i * 0.37f) % 1f));
                edgeY = y + h;
            } else {
                edgeX = x;
                edgeY = y + (h * ((i * 0.37f) % 1f));
            }

            float progress = particlePhase * particlePhase;
            int particleX = (int) Mth.lerp(progress, edgeX, centerX);
            int particleY = (int) Mth.lerp(progress, edgeY, centerY);

            float brightness = 0.4f + 0.6f * progress;
            int alpha = (int) (brightness * 200 * intensity);
            int particleColor = (alpha << 24) | 0xFFFF80;

            int size = 1 + (int) (progress * 2);
            graphics.fill(particleX - size, particleY - size, particleX + size, particleY + size, particleColor);

            if (progress > 0.1f) {
                float trailProgress = progress - 0.1f;
                int trailX = (int) Mth.lerp(trailProgress * trailProgress, edgeX, centerX);
                int trailY = (int) Mth.lerp(trailProgress * trailProgress, edgeY, centerY);
                int trailAlpha = (int) (alpha * 0.3f);
                int trailColor = (trailAlpha << 24) | 0xFFAA40;
                graphics.fill(trailX - 1, trailY - 1, trailX + 1, trailY + 1, trailColor);
            }
        }
    }

    private String getButtonText(boolean enabled) {
        if (!enabled) {
            return "INSUFFICIENT FUEL";
        } else if (isCharging) {
            int percent = (int) (chargeProgress * 100);
            return "CHARGING... " + percent + "%";
        } else {
            return "[ IGNITE STELLAR CORE ]";
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible() || !isMouseOverElement(mouseX, mouseY)) {
            return false;
        }

        if (button == 0 && canIgnite.getAsBoolean()) {
            isCharging = true;
            chargeTicks = 0;
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.RESPAWN_ANCHOR_CHARGE, 1.2f, 0.8f));
            return true;
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isCharging) {
            isCharging = false;
            if (chargeTicks < CHARGE_TICKS) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.FIRE_EXTINGUISH, 1.0f, 0.5f));
            }
            return true;
        }
        return false;
    }

    private int lerpColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) Mth.lerp(t, a1, a2);
        int r = (int) Mth.lerp(t, r1, r2);
        int g = (int) Mth.lerp(t, g1, g2);
        int b = (int) Mth.lerp(t, b1, b2);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
