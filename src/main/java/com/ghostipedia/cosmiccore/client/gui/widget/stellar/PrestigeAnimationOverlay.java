package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class PrestigeAnimationOverlay extends Widget {

    private static final int UPDATE_ID_ANIMATION_STATE = 400;

    private static final int PHASE_FLICKER_DURATION = 40;
    private static final int PHASE_SHRINK_DURATION = 100;
    private static final int PHASE_FADE_DURATION = 60;
    private static final int PHASE_WINDOW_FADE_IN = 40;
    private static final int TOTAL_ANIMATION_TICKS = PHASE_FLICKER_DURATION + PHASE_SHRINK_DURATION +
            PHASE_FADE_DURATION + PHASE_WINDOW_FADE_IN;

    private final Supplier<IrisMultiblockMachine> machineSupplier;
    private final Runnable onAnimationComplete;
    private final Runnable onShowPrestigeWindow;

    private StellarCoreWidget coreWidget;

    private boolean animationActive = false;
    private int animationTick = 0;
    private int pointsEarned = 0;

    private final Random random = new Random();
    private float flickerIntensity = 0f;
    private float starScale = 1f;
    private float starAlpha = 1f;
    private float windowAlpha = 0f;

    private int[] glitchOffsets = new int[6];
    private boolean[] scanlineHits = new boolean[20];

    public PrestigeAnimationOverlay(int x, int y, int width, int height,
                                    Supplier<IrisMultiblockMachine> machineSupplier,
                                    Runnable onAnimationComplete,
                                    Runnable onShowPrestigeWindow) {
        super(x, y, width, height);
        this.machineSupplier = machineSupplier;
        this.onAnimationComplete = onAnimationComplete;
        this.onShowPrestigeWindow = onShowPrestigeWindow;
    }

    public void setCoreWidget(StellarCoreWidget coreWidget) {
        this.coreWidget = coreWidget;
    }

    public void startAnimation(Stage currentStage, int starColor, int points) {
        animationActive = true;
        animationTick = 0;
        pointsEarned = points;

        starScale = 1f;
        starAlpha = 1f;
        windowAlpha = 0f;
        flickerIntensity = 1f;

        if (coreWidget != null) {
            coreWidget.setPrestigeAnimating(true);
            coreWidget.setPrestigeScale(1f);
            coreWidget.setPrestigeAlpha(1f);
        }

        regenerateGlitchData();

        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.BEACON_DEACTIVATE, 0.5f, 0.8f));
    }

    private void regenerateGlitchData() {
        for (int i = 0; i < glitchOffsets.length; i++) {
            glitchOffsets[i] = random.nextInt(20) - 10;
        }
        for (int i = 0; i < scanlineHits.length; i++) {
            scanlineHits[i] = random.nextFloat() < 0.3f;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();

        if (!animationActive) return;

        animationTick++;

        if (animationTick <= PHASE_FLICKER_DURATION) {
            updateFlickerPhase();
        } else if (animationTick <= PHASE_FLICKER_DURATION + PHASE_SHRINK_DURATION) {
            updateShrinkPhase();
        } else if (animationTick <= PHASE_FLICKER_DURATION + PHASE_SHRINK_DURATION + PHASE_FADE_DURATION) {
            updateFadePhase();
        } else if (animationTick <= TOTAL_ANIMATION_TICKS) {
            updateWindowFadePhase();
        } else {
            completeAnimation();
        }

        if (coreWidget != null) {
            coreWidget.setPrestigeScale(starScale);
            coreWidget.setPrestigeAlpha(starAlpha);
        }

        if (animationTick <= PHASE_FLICKER_DURATION && animationTick % 3 == 0) {
            regenerateGlitchData();
        }
    }

    private void updateFlickerPhase() {
        float progress = (float) animationTick / PHASE_FLICKER_DURATION;
        flickerIntensity = 1f - progress * 0.3f;

        if (random.nextFloat() < 0.15f) {
            float pitch = 0.8f + random.nextFloat() * 0.4f;
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.REDSTONE_TORCH_BURNOUT, pitch, 0.3f));
        }
    }

    private void updateShrinkPhase() {
        int phaseProgress = animationTick - PHASE_FLICKER_DURATION;
        float progress = (float) phaseProgress / PHASE_SHRINK_DURATION;

        float easedProgress = 1f - (1f - progress) * (1f - progress);
        starScale = 1f - easedProgress * 0.9f;

        flickerIntensity = (1f - progress) * 0.5f;

        if (random.nextFloat() < 0.05f) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.FIRE_EXTINGUISH, 0.7f, 0.2f));
        }
    }

    private void updateFadePhase() {
        int phaseProgress = animationTick - PHASE_FLICKER_DURATION - PHASE_SHRINK_DURATION;
        float progress = (float) phaseProgress / PHASE_FADE_DURATION;

        starScale = 0.1f - progress * 0.1f;

        float easedProgress = progress * progress;
        starAlpha = 1f - easedProgress;

        flickerIntensity = 0f;

        if (phaseProgress == PHASE_FADE_DURATION - 10) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.SOUL_ESCAPE, 1.0f, 0.5f));
        }
    }

    private void updateWindowFadePhase() {
        int phaseProgress = animationTick - PHASE_FLICKER_DURATION - PHASE_SHRINK_DURATION - PHASE_FADE_DURATION;
        float progress = (float) phaseProgress / PHASE_WINDOW_FADE_IN;

        windowAlpha = 1f - (1f - progress) * (1f - progress);

        starScale = 0f;
        starAlpha = 0f;

        if (phaseProgress == 1) {
            onShowPrestigeWindow.run();
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2f, 0.8f));
        }
    }

    private void completeAnimation() {
        animationActive = false;

        if (coreWidget != null) {
            coreWidget.setPrestigeAnimating(false);
        }

        onAnimationComplete.run();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);

        if (!animationActive) return;

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        if (flickerIntensity > 0) {
            drawFlickerEffect(graphics, x, y, w, h);
        }

        drawVignette(graphics, x, y, w, h);
    }

    private void drawFlickerEffect(GuiGraphics graphics, int x, int y, int w, int h) {
        for (int i = 0; i < scanlineHits.length; i++) {
            if (scanlineHits[i] && random.nextFloat() < flickerIntensity) {
                int scanY = y + (h * i / scanlineHits.length);
                int scanH = h / scanlineHits.length;
                int offset = glitchOffsets[i % glitchOffsets.length];

                int redAlpha = (int) (30 * flickerIntensity);
                int cyanAlpha = (int) (30 * flickerIntensity);

                graphics.fill(x + offset - 2, scanY, x + w + offset - 2, scanY + scanH,
                        (redAlpha << 24) | 0xFF0000);
                graphics.fill(x - offset + 2, scanY, x + w - offset + 2, scanY + scanH,
                        (cyanAlpha << 24) | 0x00FFFF);
            }
        }

        if (random.nextFloat() < flickerIntensity * 0.3f) {
            int flashAlpha = (int) (40 * flickerIntensity * random.nextFloat());
            graphics.fill(x, y, x + w, y + h, (flashAlpha << 24) | 0xFFFFFF);
        }

        if (flickerIntensity > 0.5f) {
            drawStaticNoise(graphics, x, y, w, h, flickerIntensity);
        }

        for (int row = 0; row < h; row += 3) {
            if (random.nextFloat() < flickerIntensity * 0.1f) {
                int lineAlpha = (int) (20 * flickerIntensity);
                graphics.fill(x, y + row, x + w, y + row + 1, (lineAlpha << 24) | 0x000000);
            }
        }
    }

    private void drawStaticNoise(GuiGraphics graphics, int x, int y, int w, int h, float intensity) {
        int noiseCount = (int) (50 * intensity);
        for (int i = 0; i < noiseCount; i++) {
            int nx = x + random.nextInt(w);
            int ny = y + random.nextInt(h);
            int size = 1 + random.nextInt(3);
            int gray = random.nextInt(256);
            int alpha = (int) (100 * intensity);
            int color = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
            graphics.fill(nx, ny, nx + size, ny + size, color);
        }
    }

    private void drawVignette(GuiGraphics graphics, int x, int y, int w, int h) {
        float vignetteStrength = 0.3f + flickerIntensity * 0.3f;
        int edgeAlpha = (int) (100 * vignetteStrength);

        int edgeHeight = h / 6;
        for (int row = 0; row < edgeHeight; row++) {
            float progress = (float) row / edgeHeight;
            int rowAlpha = (int) (edgeAlpha * (1f - progress));
            int rowColor = rowAlpha << 24;
            graphics.fill(x, y + row, x + w, y + row + 1, rowColor);
            graphics.fill(x, y + h - 1 - row, x + w, y + h - row, rowColor);
        }
    }

    public boolean isAnimationActive() {
        return animationActive;
    }

    public float getWindowAlpha() {
        return windowAlpha;
    }

    public int getAnimationTick() {
        return animationTick;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public float getStarScale() {
        return starScale;
    }

    public float getStarAlpha() {
        return starAlpha;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine == null) return;

        if (machine.isPrestigeAnimationActive() && !animationActive) {
            writeUpdateInfo(UPDATE_ID_ANIMATION_STATE, buf -> {
                buf.writeBoolean(true);
                buf.writeEnum(machine.getStage());
                buf.writeInt(machine.getCustomStarColor());
                buf.writeInt(machine.getLastPrestigePointsEarned());
            });
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == UPDATE_ID_ANIMATION_STATE) {
            boolean shouldStart = buffer.readBoolean();
            if (shouldStart && !animationActive) {
                Stage stage = buffer.readEnum(Stage.class);
                int color = buffer.readInt();
                int points = buffer.readInt();
                startAnimation(stage, color, points);
            }
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }
}
