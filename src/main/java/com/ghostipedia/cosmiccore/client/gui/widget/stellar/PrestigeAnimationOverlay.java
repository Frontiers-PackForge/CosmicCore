package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class PrestigeAnimationOverlay extends Widget {

    private static final int UPDATE_ID_ANIMATION_STATE = 400;

    private static final int PHASE_DESTABILIZE = 50;
    private static final int PHASE_COLLAPSE = 70;
    private static final int PHASE_SINGULARITY = 15;
    private static final int PHASE_SHOCKWAVE = 50;
    private static final int PHASE_FADE = 15;
    private static final int PHASE_TRANSCENDENCE = 20;
    private static final int TOTAL_TICKS = PHASE_DESTABILIZE + PHASE_COLLAPSE +
            PHASE_SINGULARITY + PHASE_SHOCKWAVE + PHASE_FADE + PHASE_TRANSCENDENCE;

    // Star position within the widget (based on StellarIrisWidget layout)
    private static final int STAR_X = 100;
    private static final int STAR_Y = 80;

    private final Supplier<IrisMultiblockMachine> machineSupplier;
    private final Runnable onAnimationComplete;
    private final Runnable onShowPrestigeWindow;
    private final Random random = new Random();

    private StellarCoreWidget coreWidget;
    private boolean active = false;
    private int tick = 0;
    private int pointsEarned = 0;
    private int starColor = 0xFFCC44;

    private float starScale = 1f;
    private float starAlpha = 1f;
    private float shake = 0f;
    private float shakeX = 0f;
    private float shakeY = 0f;
    private float distortion = 0f;
    private float glitch = 0f;

    private final List<Tendril> tendrils = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Shockwave> shockwaves = new ArrayList<>();
    private final List<Lightning> lightning = new ArrayList<>();
    private final List<Debris> debris = new ArrayList<>();

    private int[] glitchOffsets = new int[12];
    private boolean[] scanlines = new boolean[30];

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
        active = true;
        tick = 0;
        pointsEarned = points;
        this.starColor = starColor != -1 ? starColor : 0xFFCC44;

        starScale = 1f;
        starAlpha = 1f;
        shake = 0f;
        distortion = 0f;
        glitch = 0f;

        tendrils.clear();
        particles.clear();
        shockwaves.clear();
        lightning.clear();
        debris.clear();

        if (coreWidget != null) {
            coreWidget.setPrestigeAnimating(true);
            coreWidget.setPrestigeScale(1f);
            coreWidget.setPrestigeAlpha(1f);
        }

        randomizeGlitch();
        playSound(SoundEvents.WARDEN_HEARTBEAT, 0.5f, 0.8f);
        playSound(SoundEvents.AMBIENT_CAVE.value(), 1.0f, 0.5f);
    }

    private void randomizeGlitch() {
        for (int i = 0; i < glitchOffsets.length; i++) {
            glitchOffsets[i] = random.nextInt(30) - 15;
        }
        for (int i = 0; i < scanlines.length; i++) {
            scanlines[i] = random.nextFloat() < 0.4f;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        if (!active) return;

        tick++;

        if (shake > 0) {
            shakeX = (random.nextFloat() - 0.5f) * 2 * shake;
            shakeY = (random.nextFloat() - 0.5f) * 2 * shake;
        } else {
            shakeX = 0;
            shakeY = 0;
        }

        int t = tick;
        if (t <= PHASE_DESTABILIZE) {
            tickDestabilize(t);
        } else if (t <= PHASE_DESTABILIZE + PHASE_COLLAPSE) {
            tickCollapse(t - PHASE_DESTABILIZE);
        } else if (t <= PHASE_DESTABILIZE + PHASE_COLLAPSE + PHASE_SINGULARITY) {
            tickSingularity(t - PHASE_DESTABILIZE - PHASE_COLLAPSE);
        } else if (t <= PHASE_DESTABILIZE + PHASE_COLLAPSE + PHASE_SINGULARITY + PHASE_SHOCKWAVE) {
            tickShockwave(t - PHASE_DESTABILIZE - PHASE_COLLAPSE - PHASE_SINGULARITY);
        } else if (t <= PHASE_DESTABILIZE + PHASE_COLLAPSE + PHASE_SINGULARITY + PHASE_SHOCKWAVE + PHASE_FADE) {
            tickFade(t - PHASE_DESTABILIZE - PHASE_COLLAPSE - PHASE_SINGULARITY - PHASE_SHOCKWAVE);
        } else if (t <= TOTAL_TICKS) {
            tickTranscendence(
                    t - PHASE_DESTABILIZE - PHASE_COLLAPSE - PHASE_SINGULARITY - PHASE_SHOCKWAVE - PHASE_FADE);
        } else {
            finish();
        }

        tendrils.removeIf(p -> {
            p.tick();
            return p.dead();
        });
        particles.removeIf(p -> {
            p.tick();
            return p.dead();
        });
        shockwaves.removeIf(p -> {
            p.tick();
            return p.dead();
        });
        lightning.removeIf(p -> {
            p.tick();
            return p.dead();
        });
        debris.removeIf(p -> {
            p.tick();
            return p.dead();
        });

        if (coreWidget != null) {
            coreWidget.setPrestigeScale(starScale);
            coreWidget.setPrestigeAlpha(starAlpha);
        }

        if (glitch > 0 && tick % 2 == 0) {
            randomizeGlitch();
        }
    }

    private void tickDestabilize(int t) {
        float p = (float) t / PHASE_DESTABILIZE;
        shake = p * 8f;

        float flicker = Mth.sin(t * 0.8f) * 0.15f + Mth.sin(t * 1.7f) * 0.1f + Mth.sin(t * 3.3f) * 0.05f;
        starScale = 1f + flicker * (1f + p);
        glitch = p * 0.8f;

        if (random.nextFloat() < p * 0.3f) spawnLightning();

        if (t == 1) playSound(SoundEvents.BEACON_DEACTIVATE, 0.5f, 0.6f);
        if (t == 20) playSound(SoundEvents.WARDEN_SONIC_CHARGE, 0.7f, 0.5f);
        if (t == 40) playSound(SoundEvents.WARDEN_SONIC_CHARGE, 0.9f, 0.7f);
        if (random.nextFloat() < 0.1f * p) {
            playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.3f + p * 0.3f, 0.5f + random.nextFloat() * 0.5f);
        }
    }

    private void tickCollapse(int t) {
        float p = (float) t / PHASE_COLLAPSE;
        float eased = p * p * p;

        starScale = 1.2f * (1f - eased * 0.95f);
        distortion = eased * 1.5f;
        shake = 8f * (1f - eased * 0.5f) + eased * 15f * (1f - p);
        glitch = 0.8f * (1f - eased);

        if (t % 3 == 0 && tendrils.size() < 20) spawnTendril();
        if (t % 2 == 0) {
            for (int i = 0; i < 3; i++) spawnParticle();
        }

        if (t == 1) playSound(SoundEvents.WARDEN_SONIC_BOOM, 0.6f, 0.3f);
        if (t == 30) playSound(SoundEvents.WARDEN_SONIC_CHARGE, 1.0f, 0.8f);
        if (t == 60) playSound(SoundEvents.WARDEN_SONIC_CHARGE, 1.0f, 1.2f);
        if (t % 10 == 0) playSound(SoundEvents.WARDEN_HEARTBEAT, 0.4f + p * 0.4f, 0.5f + p * 0.5f);
    }

    private void tickSingularity(int t) {
        float p = (float) t / PHASE_SINGULARITY;
        starScale = 0.05f * (1f - p);
        distortion = 1.5f * (1f - p * 0.5f);
        shake = 20f * (1f - p);

        if (t == 1) {
            playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0f, 0.3f);
            playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.0f, 0.5f);
            playSound(SoundEvents.END_PORTAL_SPAWN, 0.8f, 0.5f);
        }
    }

    private void tickShockwave(int t) {
        float p = (float) t / PHASE_SHOCKWAVE;
        starScale = 0f;
        starAlpha = 0f;

        if (t == 1) {
            spawnShockwave(1.0f, 0xFFFFFFFF);
            playSound(SoundEvents.DRAGON_FIREBALL_EXPLODE, 0.8f, 0.6f);
            spawnDebris(40);
        }
        if (t == 8) spawnShockwave(0.8f, starColor | 0xFF000000);
        if (t == 16) spawnShockwave(0.6f, 0xFF80A0FF);
        if (t == 24) spawnShockwave(0.4f, 0xFF4060FF);

        shake = 12f * (1f - p);
        distortion = 1.0f * (1f - p * p);

        if (t % 5 == 0 && t < 30) spawnDebris(5);
    }

    private void tickFade(int t) {
        float p = (float) t / PHASE_FADE;
        starScale = 0f;
        starAlpha = 0f;
        distortion = 0f;
        shake = 2f * (1f - p);

        if (t == 1) playSound(SoundEvents.SOUL_ESCAPE.value(), 0.8f, 0.5f);
    }

    private void tickTranscendence(int t) {
        shake = 0f;
        distortion = 0f;

        if (t == 1) {
            onShowPrestigeWindow.run();
            playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            playSound(SoundEvents.PLAYER_LEVELUP, 0.8f, 0.8f);
        }
    }

    private void finish() {
        active = false;
        if (coreWidget != null) coreWidget.setPrestigeAnimating(false);
        onAnimationComplete.run();
    }

    private void spawnTendril() {
        float angle = random.nextFloat() * Mth.TWO_PI;
        float dist = 80 + random.nextFloat() * 60;
        tendrils.add(new Tendril(
                STAR_X + Mth.cos(angle) * dist,
                STAR_Y + Mth.sin(angle) * dist,
                STAR_X, STAR_Y, starColor));
    }

    private void spawnParticle() {
        float angle = random.nextFloat() * Mth.TWO_PI;
        float dist = 25 + random.nextFloat() * 50;
        particles.add(new Particle(
                STAR_X + Mth.cos(angle) * dist,
                STAR_Y + Mth.sin(angle) * dist,
                STAR_X, STAR_Y, starColor));
    }

    private void spawnShockwave(float intensity, int color) {
        shockwaves.add(new Shockwave(STAR_X, STAR_Y, intensity, color));
    }

    private void spawnLightning() {
        float a1 = random.nextFloat() * Mth.TWO_PI;
        float a2 = a1 + Mth.PI * (0.3f + random.nextFloat() * 0.4f);
        float dist = 35 + random.nextFloat() * 35;
        lightning.add(new Lightning(
                STAR_X + Mth.cos(a1) * dist, STAR_Y + Mth.sin(a1) * dist,
                STAR_X + Mth.cos(a2) * dist, STAR_Y + Mth.sin(a2) * dist));
    }

    private void spawnDebris(int count) {
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float speed = 3f + random.nextFloat() * 8f;
            debris.add(new Debris(STAR_X, STAR_Y, angle, speed, starColor));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        if (!active) return;

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        graphics.pose().pushPose();
        graphics.pose().translate(shakeX, shakeY, 0);

        if (distortion > 0) drawDistortion(graphics, x, y);

        for (Tendril t : tendrils) t.draw(graphics, x, y);
        for (Particle p : particles) p.draw(graphics, x, y);
        for (Lightning l : lightning) l.draw(graphics, x, y);
        for (Shockwave s : shockwaves) s.draw(graphics, x, y);
        for (Debris d : debris) d.draw(graphics, x, y);

        if (glitch > 0) drawGlitch(graphics, x, y, w, h);
        drawVignette(graphics, x, y, w, h);

        graphics.pose().popPose();
    }

    private void drawDistortion(GuiGraphics graphics, int ox, int oy) {
        int cx = ox + STAR_X;
        int cy = oy + STAR_Y;

        for (int i = 0; i < 24; i++) {
            float angle = i * Mth.TWO_PI / 24;
            float outer = 100;
            float inner = 15 * (1f - distortion * 0.8f);

            int x1 = cx + (int) (Mth.cos(angle) * outer);
            int y1 = cy + (int) (Mth.sin(angle) * outer);
            int x2 = cx + (int) (Mth.cos(angle) * inner);
            int y2 = cy + (int) (Mth.sin(angle) * inner);

            int alpha = (int) (50 * distortion);
            drawLine(graphics, x1, y1, x2, y2, (alpha << 24) | 0x8080FF);
        }

        for (int ring = 0; ring < 5; ring++) {
            float radius = (80 - ring * 14) * (1f - distortion * 0.3f * (5 - ring) / 5f);
            int alpha = (int) (40 * distortion);
            drawRing(graphics, cx, cy, (int) radius, (alpha << 24) | 0x6060C0);
        }
    }

    private void drawGlitch(GuiGraphics graphics, int x, int y, int w, int h) {
        for (int i = 0; i < scanlines.length; i++) {
            if (scanlines[i] && random.nextFloat() < glitch) {
                int sy = y + (h * i / scanlines.length);
                int sh = h / scanlines.length;
                int off = glitchOffsets[i % glitchOffsets.length];
                int a = (int) (50 * glitch);

                graphics.fill(x + off - 3, sy, x + w + off - 3, sy + sh, (a << 24) | 0xFF0000);
                graphics.fill(x - off + 3, sy, x + w - off + 3, sy + sh, (a << 24) | 0x00FFFF);
            }
        }

        if (random.nextFloat() < glitch * 0.4f) {
            int a = (int) (80 * glitch * random.nextFloat());
            graphics.fill(x, y, x + w, y + h, (a << 24) | 0xFFFFFF);
        }

        if (glitch > 0.3f) {
            int count = (int) (100 * glitch);
            for (int i = 0; i < count; i++) {
                int nx = x + random.nextInt(w);
                int ny = y + random.nextInt(h);
                int size = 1 + random.nextInt(3);
                int gray = random.nextInt(256);
                int a = (int) (150 * glitch);
                graphics.fill(nx, ny, nx + size, ny + size, (a << 24) | (gray << 16) | (gray << 8) | gray);
            }
        }

        for (int row = 0; row < h; row += 4) {
            if (random.nextFloat() < glitch * 0.15f) {
                int a = (int) (40 * glitch);
                int off = random.nextInt(20) - 10;
                graphics.fill(x + off, y + row, x + w + off, y + row + 2, (a << 24));
            }
        }
    }

    private void drawVignette(GuiGraphics graphics, int x, int y, int w, int h) {
        float strength = 0.4f + shake * 0.02f + distortion * 0.2f;
        int edgeAlpha = (int) (150 * strength);
        int edgeSize = h / 4;

        for (int row = 0; row < edgeSize; row++) {
            int a = (int) (edgeAlpha * (1f - (float) row / edgeSize));
            graphics.fill(x, y + row, x + w, y + row + 1, a << 24);
            graphics.fill(x, y + h - 1 - row, x + w, y + h - row, a << 24);
        }

        for (int col = 0; col < edgeSize; col++) {
            int a = (int) (edgeAlpha * (1f - (float) col / edgeSize) * 0.5f);
            graphics.fill(x + col, y, x + col + 1, y + h, a << 24);
            graphics.fill(x + w - 1 - col, y, x + w - col, y + h, a << 24);
        }
    }

    private void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            g.fill(x1, y1, x1 + 1, y1 + 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void drawRing(GuiGraphics g, int cx, int cy, int radius, int color) {
        for (int a = 0; a < 360; a += 4) {
            float rad = a * Mth.DEG_TO_RAD;
            int px = cx + (int) (Mth.cos(rad) * radius);
            int py = cy + (int) (Mth.sin(rad) * radius);
            g.fill(px, py, px + 1, py + 1, color);
        }
    }

    private void playSound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    // Particle classes

    private class Tendril {

        float sx, sy, ex, ey;
        int color;
        float[] offsets = new float[8];
        int age = 0;

        Tendril(float sx, float sy, float ex, float ey, int color) {
            this.sx = sx;
            this.sy = sy;
            this.ex = ex;
            this.ey = ey;
            this.color = color;
            for (int i = 0; i < offsets.length; i++) offsets[i] = (random.nextFloat() - 0.5f) * 20;
        }

        void tick() {
            age++;
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] += (random.nextFloat() - 0.5f) * 4;
                offsets[i] *= 0.9f;
            }
        }

        boolean dead() {
            return age >= 40;
        }

        void draw(GuiGraphics g, int ox, int oy) {
            float alpha = (1f - age / 40f) * 0.8f;
            int a = (int) (alpha * 255);
            int c = (a << 24) | (color & 0xFFFFFF);

            for (int i = 0; i < offsets.length - 1; i++) {
                float t1 = (float) i / offsets.length;
                float t2 = (float) (i + 1) / offsets.length;
                float x1 = Mth.lerp(t1, sx, ex) + offsets[i] * (1f - t1);
                float y1 = Mth.lerp(t1, sy, ey) + offsets[i] * 0.5f * (1f - t1);
                float x2 = Mth.lerp(t2, sx, ex) + offsets[i + 1] * (1f - t2);
                float y2 = Mth.lerp(t2, sy, ey) + offsets[i + 1] * 0.5f * (1f - t2);
                drawLine(g, ox + (int) x1, oy + (int) y1, ox + (int) x2, oy + (int) y2, c);
            }
        }
    }

    private class Particle {

        float x, y, tx, ty, speed;
        int color, size, age = 0;

        Particle(float x, float y, float tx, float ty, int color) {
            this.x = x;
            this.y = y;
            this.tx = tx;
            this.ty = ty;
            this.color = color;
            this.speed = 0.05f + random.nextFloat() * 0.1f;
            this.size = 1 + random.nextInt(3);
        }

        void tick() {
            age++;
            float dx = tx - x, dy = ty - y;
            x += dx * speed;
            y += dy * speed;
            speed *= 1.1f;
        }

        boolean dead() {
            float dist = Mth.sqrt((tx - x) * (tx - x) + (ty - y) * (ty - y));
            return age >= 30 || dist < 3;
        }

        void draw(GuiGraphics g, int ox, int oy) {
            int a = (int) ((1f - age / 30f) * 200);
            int c = (a << 24) | (color & 0xFFFFFF);
            g.fill(ox + (int) x - size / 2, oy + (int) y - size / 2,
                    ox + (int) x + size / 2 + 1, oy + (int) y + size / 2 + 1, c);
        }
    }

    private class Shockwave {

        float cx, cy, radius = 0, intensity, speed = 8f;
        int color;

        Shockwave(float cx, float cy, float intensity, int color) {
            this.cx = cx;
            this.cy = cy;
            this.intensity = intensity;
            this.color = color;
        }

        void tick() {
            radius += speed;
            speed *= 0.98f;
        }

        boolean dead() {
            return radius >= 200;
        }

        void draw(GuiGraphics g, int ox, int oy) {
            float p = radius / 200f;
            int a = (int) (intensity * (1f - p) * 255);

            for (int t = 0; t < 3; t++) {
                int r = (int) radius - t * 2;
                int ra = a - t * 30;
                if (r > 0 && ra > 0) {
                    drawRing(g, ox + (int) cx, oy + (int) cy, r, (ra << 24) | (color & 0xFFFFFF));
                }
            }
        }
    }

    private class Lightning {

        float x1, y1, x2, y2;
        float[] offsets = new float[6];
        int age = 0;

        Lightning(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            regen();
        }

        void regen() {
            for (int i = 0; i < offsets.length; i++) offsets[i] = (random.nextFloat() - 0.5f) * 15;
        }

        void tick() {
            age++;
            if (age % 2 == 0) regen();
        }

        boolean dead() {
            return age >= 8;
        }

        void draw(GuiGraphics g, int ox, int oy) {
            float alpha = 1f - age / 8f;
            int a = (int) (alpha * 255);
            int c = (a << 24) | 0xAADDFF;
            int glow = ((a / 2) << 24) | 0x4488FF;

            int segs = offsets.length + 1;
            for (int i = 0; i < segs; i++) {
                float t1 = (float) i / segs, t2 = (float) (i + 1) / segs;
                float px1 = Mth.lerp(t1, x1, x2), py1 = Mth.lerp(t1, y1, y2);
                float px2 = Mth.lerp(t2, x1, x2), py2 = Mth.lerp(t2, y1, y2);

                if (i < offsets.length) {
                    float perpX = -(y2 - y1), perpY = (x2 - x1);
                    float len = Mth.sqrt(perpX * perpX + perpY * perpY);
                    if (len > 0) {
                        perpX /= len;
                        perpY /= len;
                    }
                    px1 += perpX * offsets[i];
                    py1 += perpY * offsets[i];
                }
                if (i + 1 < offsets.length) {
                    float perpX = -(y2 - y1), perpY = (x2 - x1);
                    float len = Mth.sqrt(perpX * perpX + perpY * perpY);
                    if (len > 0) {
                        perpX /= len;
                        perpY /= len;
                    }
                    px2 += perpX * offsets[i + 1];
                    py2 += perpY * offsets[i + 1];
                }

                drawLine(g, ox + (int) px1 - 1, oy + (int) py1, ox + (int) px2 - 1, oy + (int) py2, glow);
                drawLine(g, ox + (int) px1 + 1, oy + (int) py1, ox + (int) px2 + 1, oy + (int) py2, glow);
                drawLine(g, ox + (int) px1, oy + (int) py1, ox + (int) px2, oy + (int) py2, c);
            }
        }
    }

    private class Debris {

        float x, y, vx, vy;
        int color, size, age = 0;

        Debris(float x, float y, float angle, float speed, int baseColor) {
            this.x = x;
            this.y = y;
            this.vx = Mth.cos(angle) * speed;
            this.vy = Mth.sin(angle) * speed;
            int r = ((baseColor >> 16) & 0xFF) + random.nextInt(40) - 20;
            int g = ((baseColor >> 8) & 0xFF) + random.nextInt(40) - 20;
            int b = (baseColor & 0xFF) + random.nextInt(40) - 20;
            this.color = (Mth.clamp(r, 0, 255) << 16) | (Mth.clamp(g, 0, 255) << 8) | Mth.clamp(b, 0, 255);
            this.size = 1 + random.nextInt(4);
        }

        void tick() {
            age++;
            x += vx;
            y += vy;
            vx *= 0.97f;
            vy *= 0.97f;
            vy += 0.05f;
        }

        boolean dead() {
            return age >= 60;
        }

        void draw(GuiGraphics g, int ox, int oy) {
            int a = (int) ((1f - age / 60f) * 200);
            g.fill(ox + (int) x - size / 2, oy + (int) y - size / 2,
                    ox + (int) x + size / 2 + 1, oy + (int) y + size / 2 + 1, (a << 24) | color);
        }
    }

    // Public API

    public boolean isAnimationActive() {
        return active;
    }

    public int getAnimationTick() {
        return tick;
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

        if (machine.isPrestigeAnimationActive() && !active) {
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
    public void readUpdateInfo(int id, RegistryFriendlyByteBuf buffer) {
        if (id == UPDATE_ID_ANIMATION_STATE) {
            boolean shouldStart = buffer.readBoolean();
            if (shouldStart && !active) {
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
