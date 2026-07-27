package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.CosmicCoreClient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class MirrorScene {

    private MirrorScene() {}

    private static final ResourceLocation TEX_BAND_A = CosmicCore.id("textures/gui/mirror/band_a.png");
    private static final ResourceLocation TEX_BAND_B = CosmicCore.id("textures/gui/mirror/band_b.png");
    private static final ResourceLocation TEX_RING_SCAR = CosmicCore.id("textures/gui/mirror/ring_scar.png");
    private static final ResourceLocation TEX_CORE = CosmicCore.id("textures/gui/mirror/core.png");
    private static final ResourceLocation TEX_GLOW = CosmicCore.id("textures/gui/mirror/glow.png");
    private static final ResourceLocation TEX_THREAD = CosmicCore.id("textures/gui/mirror/thread.png");
    private static final ResourceLocation TEX_LINE = CosmicCore.id("textures/gui/mirror/line.png");
    private static final ResourceLocation TEX_SOCKET = CosmicCore.id("textures/gui/mirror/socket.png");
    private static final ResourceLocation TEX_HEART = CosmicCore.id("textures/gui/mirror/heart_socket.png");
    private static final ResourceLocation TEX_COIL = CosmicCore.id("textures/gui/mirror/coil.png");
    private static final ResourceLocation TEX_SKEIN = CosmicCore.id("textures/gui/mirror/skein.png");

    private static final float DESIGN_W = 720f;
    private static final float DESIGN_H = 660f;
    private static final float SCALE_BOOST = 1.1f;

    private static final int VOID_COLOR = 0xFF080C18;
    private static final int STAR_COLD = 0xFFC8D4E8;
    private static final int STAR_WARM = 0xFFE8C9A8;
    private static final int DUST_TINT = 0x648A98B0;
    private static final int CHORD_TINT = 0x8C6E7C9C;
    private static final int STRAND_GOLD = 0xFFE8C07A;
    private static final int STRAND_DEEP = 0xFFB08948;
    private static final int FILAMENT_TINT = 0x8CC9B88A;
    private static final int ECHO_LIT = 0xFFF0D9A8;
    private static final int ECHO_HOT = 0xFFFFF3D6;
    private static final int ECHO_DIM = 0xFF8A9BC4;
    private static final int COIL_TINT = 0xFFBF8D50;

    private static final int STAR_COUNT = 110;
    private static final int DUST_COUNT = 16;
    private static final long SCENE_SEED = 20260702L;

    private static final float[][] BANDS = {
            { 235f, 118f, -9f, 0.050f, 0 },
            { 108f, 218f, 17f, -0.034f, 1 },
            { 192f, 150f, -38f, 0.021f, 1 },
    };
    private static final float BAND_TEX_RING = 0.82f;

    private static final int CHORD_COUNT = 12;
    private static final float CHORD_FOLLOW_CHANCE = 0.5f;

    private static final float CORE_RADIUS = 53f;
    private static final float CORE_TEX_RING = 0.52f;
    private static final float CORE_BREATH = 0.012f;

    private static final int ECHO_MAX = 12;
    private static final float ECHO_RADIUS = 7.0f;
    private static final float ECHO_RING_R = 78f;
    private static final float ECHO_RING_START = -Mth.HALF_PI;
    private static final float SOCKET_HALF = 6.5f;
    private static final float HEART_SOCKET_HALF = 11f;

    private static final float SKEIN_RING_R = 42f;
    private static final float SKEIN_START_ANGLE = -1.57f;
    private static final float SKEIN_STEP_ANGLE = 1.05f;
    private static final float SKEIN_HALF = 9f;
    private static final int SKEIN_MAX = MirrorScreen.SKEIN_CAP;
    private static final float STRAND_DIVE_R = 34f;
    private static final float SKEIN_STRAND_WIDTH = 3.0f;
    private static final int SKEIN_STRAND_ALPHA = 130;
    private static final int[] SKEIN_HUES = {
            0xFFC98D50, 0xFF8FB8D0, 0xFFA88BC0, 0xFF9EC49A, 0xFFD0A0A0, 0xFFC6C29E };
    private static final int THREAD_FAINT = 0x66E8C07A;

    private static final float DEPTHS_R = 86f;
    private static final float DEPTHS_PIXEL_GRID = 48f;
    private static final float DEPTHS_ALPHA = 0.9f;
    private static final float DEPTHS_TIME_WRAP = 2513.274f;

    private static final float DISK_HALF_X = 150f;
    private static final float DISK_HALF_Y = 118f;
    private static final float DISK_INNER = 0.45f;
    private static final float DISK_PIXEL_GRID = 52f;
    private static final float DISK_ALPHA = 0.85f;
    private static final float DISK_SPEED = 0.55f;

    private static final int LUA_BAND = 0;

    private static final float STRAND_SWAY = 11f;
    private static final float STRAND_WIDTH = 4.0f;
    private static final float GOLDEN_ANGLE = 2.399963f;
    private static final float CEREMONY_BOND = 0.4f;
    private static final float CEREMONY_RETRACT = 0.15f;

    private static final int VIGNETTE_ALPHA = 150;
    private static final float VIGNETTE_DEPTH = 0.22f;

    private static final float[] PARALLAX = { 0.006f, 0.016f, 0.024f, 0.034f, 0.048f };
    private static final float PARALLAX_STRENGTH = 18f;

    private record Star(float x, float y, float size, float phase, boolean warm) {}

    private record Dust(float bx, float by, float speed, float phase) {}

    private record Chord(int bandA, float thetaA, float followA, int bandB, float thetaB, float followB) {}

    private static final List<Star> STARS = new ArrayList<>();
    private static final List<Dust> DUSTS = new ArrayList<>();
    private static final List<Chord> CHORDS = new ArrayList<>();
    private static final float[][] ECHO_LAYOUT = new float[ECHO_MAX][2];

    static {
        RandomSource r = RandomSource.create(SCENE_SEED);
        for (int i = 0; i < STAR_COUNT; i++) {
            STARS.add(new Star(r.nextFloat(), r.nextFloat(), 1.1f + r.nextFloat() * 1.9f,
                    r.nextFloat() * 6.28f, r.nextFloat() < 0.2f));
        }
        RandomSource rd = RandomSource.create(SCENE_SEED + 99);
        for (int i = 0; i < DUST_COUNT; i++) {
            DUSTS.add(new Dust(rd.nextFloat(), rd.nextFloat(), 2f + rd.nextFloat() * 4f,
                    rd.nextFloat() * 6.28f));
        }
        for (int i = 0; i < CHORD_COUNT; i++) {
            CHORDS.add(new Chord(r.nextInt(3), r.nextFloat() * 6.283f,
                    r.nextFloat() < CHORD_FOLLOW_CHANCE ? 1f : 0f,
                    r.nextInt(3), r.nextFloat() * 6.283f,
                    r.nextFloat() < CHORD_FOLLOW_CHANCE ? 1f : 0f));
        }
        for (int i = 0; i < ECHO_MAX; i++) {
            double a = ECHO_RING_START + i * (Math.PI * 2 / ECHO_MAX);
            ECHO_LAYOUT[i][0] = (float) (Math.cos(a) * ECHO_RING_R);
            ECHO_LAYOUT[i][1] = (float) (Math.sin(a) * ECHO_RING_R);
        }
    }

    public static void render(GuiGraphics g, int width, int height, int mouseX, int mouseY, float time,
                              float zoom, MirrorScreen.DevState state) {
        float cx = width / 2f;
        float cy = height / 2f;
        float px = (mouseX - cx) / cx;
        float py = (mouseY - cy) / cy;
        float scale = sceneScale(width, height) * zoom;

        g.fill(0, 0, width, height, VOID_COLOR);

        PoseStack pose = g.pose();

        pose.pushPose();
        parallax(pose, px, py, 0);
        drawStars(g, width, height, time);
        pose.popPose();

        pose.pushPose();
        pose.translate(cx, cy, 0);
        pose.scale(scale, scale, 1f);
        pose.translate(-cx, -cy, 0);

        pose.pushPose();
        parallax(pose, px, py, 1);
        drawChords(g, cx, cy, time);
        pose.popPose();

        pose.pushPose();
        parallax(pose, px, py, 2);
        for (int b = 0; b < BANDS.length; b++) {
            drawBand(g, cx, cy, b, time, state.scorch);
        }
        pose.popPose();

        pose.pushPose();
        parallax(pose, px, py, 3);
        drawStrands(g, cx, cy, time, state);
        drawCoils(g, cx, cy, time, state);
        pose.popPose();

        pose.pushPose();
        parallax(pose, px, py, 4);
        drawCore(g, cx, cy, time, state);
        pose.popPose();

        pose.popPose();

        if (state.veil > 0.02f) {
            drawVeil(g, width, height, state.veil);
            if (state.ceremonyActive) {
                sceneLayer(pose, cx, cy, scale, px, py, 3);
                drawCeremonyStrand(g, cx, cy, time, state);
                pose.popPose();
            }
        }
        boolean heartHero = (state.skeins >= SKEIN_MAX && Math.min(state.litEchoes, ECHO_MAX) >= ECHO_MAX &&
                !state.heartClaimed) || state.heartBurstTicks > 0;
        if (state.ceremonyActive || state.claimable || state.flashTicks > 0 || state.burstTicks > 0 || heartHero) {
            sceneLayer(pose, cx, cy, scale, px, py, 4);
            if (state.ceremonyActive || state.claimable || state.flashTicks > 0 || state.burstTicks > 0) {
                drawBeacon(g, cx, cy, time, state);
            }
            if (heartHero) {
                drawHeartClaim(g, cx, cy, time, state);
            }
            pose.popPose();
        }

        drawVignette(g, width, height);
    }

    private static void sceneLayer(PoseStack pose, float cx, float cy, float scale, float px, float py,
                                   int layer) {
        pose.pushPose();
        pose.translate(cx, cy, 0);
        pose.scale(scale, scale, 1f);
        pose.translate(-cx, -cy, 0);
        parallax(pose, px, py, layer);
    }

    public static float sceneScale(int width, int height) {
        return Math.min(width / DESIGN_W, height / DESIGN_H) * SCALE_BOOST;
    }

    public static float[] heartScreenPos(int width, int height, double mouseX, double mouseY, float zoom) {
        float cx = width / 2f;
        float cy = height / 2f;
        float scale = sceneScale(width, height) * zoom;
        float px = ((float) mouseX - cx) / cx;
        float py = ((float) mouseY - cy) / cy;
        float shift = PARALLAX[4] * PARALLAX_STRENGTH * scale;
        return new float[] { cx - px * shift, cy - py * shift,
                Math.max(10f, HEART_SOCKET_HALF * 2f * scale) };
    }

    public static float[] echoScreenPos(int index, int width, int height, double mouseX, double mouseY,
                                        float zoom) {
        float cx = width / 2f;
        float cy = height / 2f;
        float scale = sceneScale(width, height) * zoom;
        float px = ((float) mouseX - cx) / cx;
        float py = ((float) mouseY - cy) / cy;
        float shift = PARALLAX[4] * PARALLAX_STRENGTH * scale;
        return new float[] {
                cx + ECHO_LAYOUT[index % ECHO_MAX][0] * scale - px * shift,
                cy + ECHO_LAYOUT[index % ECHO_MAX][1] * scale - py * shift,
                Math.max(8f, ECHO_RADIUS * 2.2f * scale)
        };
    }

    private static void parallax(PoseStack pose, float px, float py, int layer) {
        pose.translate(-px * PARALLAX[layer] * PARALLAX_STRENGTH, -py * PARALLAX[layer] * PARALLAX_STRENGTH, 0);
    }

    private static float[] bandPoint(float cx, float cy, int band, float materialTheta, float time) {
        float[] def = BANDS[band];
        float spin = def[3] * time;
        float tilt = def[2] * Mth.DEG_TO_RAD;
        float t = materialTheta + spin;
        float lx = def[0] * Mth.cos(t);
        float ly = def[1] * Mth.sin(t);
        float x = cx + lx * Mth.cos(tilt) - ly * Mth.sin(tilt);
        float y = cy + lx * Mth.sin(tilt) + ly * Mth.cos(tilt);
        return new float[] { x, y };
    }

    private static void drawStars(GuiGraphics g, int width, int height, float time) {
        BufferBuilder buf = beginTex(TEX_GLOW);
        Matrix4f mat = g.pose().last().pose();
        for (Star s : STARS) {
            float tw = 0.4f + 0.6f * Mth.sin(time * 0.8f + s.phase()) * Mth.sin(time * 0.8f + s.phase());
            texQuad(buf, mat, s.x() * width, s.y() * height, s.size() * 2.2f,
                    s.warm() ? STAR_WARM : STAR_COLD, (int) (tw * 200));
        }
        for (Dust d : DUSTS) {
            float x = (d.bx() * width + time * d.speed()) % (width + 20) - 10;
            float y = d.by() * height + Mth.sin(time * 0.15f + d.phase()) * 14f;
            texQuad(buf, mat, x, y, 2.2f, DUST_TINT, 255);
        }
        flush(buf);
    }

    private static void drawBand(GuiGraphics g, float cx, float cy, int band, float time, boolean scarred) {
        float[] def = BANDS[band];
        float spinDeg = def[3] * time * Mth.RAD_TO_DEG;
        ResourceLocation tex = band == LUA_BAND && scarred ? TEX_RING_SCAR : def[4] == 0 ? TEX_BAND_A : TEX_BAND_B;
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(def[2]));
        pose.scale(1f, def[1] / def[0], 1f);
        pose.mulPose(Axis.ZP.rotationDegrees(spinDeg));
        float half = def[0] / BAND_TEX_RING;
        BufferBuilder buf = beginTex(tex);
        texQuad(buf, pose.last().pose(), 0, 0, half, 0xFFFFFFFF, 255);
        flush(buf);
        pose.popPose();
    }

    private static void drawChords(GuiGraphics g, float cx, float cy, float time) {
        Matrix4f mat = g.pose().last().pose();
        float[][] ends = new float[CHORDS.size() * 2][];
        for (int i = 0; i < CHORDS.size(); i++) {
            Chord c = CHORDS.get(i);
            ends[i * 2] = bandPoint(cx, cy, c.bandA(), c.thetaA(), c.followA() > 0 ? time : 0);
            ends[i * 2 + 1] = bandPoint(cx, cy, c.bandB(), c.thetaB(), c.followB() > 0 ? time : 0);
        }
        BufferBuilder buf = beginTex(TEX_LINE);
        for (int i = 0; i < CHORDS.size(); i++) {
            ribbon(buf, mat, List.of(ends[i * 2], ends[i * 2 + 1]), 1.8f, CHORD_TINT, 255, false);
        }
        flush(buf);

        BufferBuilder knots = beginTex(TEX_GLOW);
        for (float[] p : ends) {
            texQuad(knots, mat, p[0], p[1], 3.2f, CHORD_TINT, 235);
        }
        flush(knots);
    }

    private static List<float[]> strandPath(float cx, float cy, int index, float time, float progress,
                                            float[] tip) {
        int band = index % 3;
        float theta = index * GOLDEN_ANGLE + band * 0.7f;
        float[] anchor = bandPoint(cx, cy, band, theta, time);
        float seed = (index * 0.618f) % 1f;
        float sway = Mth.sin(time * 0.35f + seed * 6.28f) * STRAND_SWAY;
        float c1x = Mth.lerp(0.35f, anchor[0], tip[0]) + sway;
        float c1y = Mth.lerp(0.35f, anchor[1], tip[1]) - sway * 0.5f;
        float c2x = Mth.lerp(0.75f, anchor[0], tip[0]) - sway * 0.6f;
        float c2y = Mth.lerp(0.75f, anchor[1], tip[1]) + sway * 0.4f;
        List<float[]> pts = new ArrayList<>();
        int steps = 24;
        int drawn = Math.max(1, (int) (steps * progress));
        for (int i = 0; i <= drawn; i++) {
            float t = i / (float) steps;
            float u = 1f - t;
            float x = u * u * u * anchor[0] + 3 * u * u * t * c1x + 3 * u * t * t * c2x + t * t * t * tip[0];
            float y = u * u * u * anchor[1] + 3 * u * u * t * c1y + 3 * u * t * t * c2y + t * t * t * tip[1];
            pts.add(new float[] { x, y });
        }
        return pts;
    }

    private static void drawStrands(GuiGraphics g, float cx, float cy, float time, MirrorScreen.DevState state) {
        Matrix4f mat = g.pose().last().pose();
        int lit = Math.min(state.litEchoes, ECHO_MAX);

        List<List<float[]>> paths = new ArrayList<>(lit);
        for (int i = 0; i < lit; i++) {
            paths.add(strandPath(cx, cy, i, time, 1f, strandTip(cx, cy, i)));
        }

        int done = Math.min(state.skeins, SKEIN_MAX);
        BufferBuilder buf = beginTex(TEX_THREAD);
        for (int s = 0; s < done; s++) {
            float[] target = skeinPos(cx, cy, s);
            int alpha = Math.max(90, SKEIN_STRAND_ALPHA - (done - 1 - s) * 12);
            for (int i = 0; i < ECHO_MAX; i++) {
                List<float[]> pts = strandPath(cx, cy, s * ECHO_MAX + i, time, 1f, target);
                ribbon(buf, mat, pts, SKEIN_STRAND_WIDTH, STRAND_GOLD, alpha, true);
            }
        }
        for (List<float[]> pts : paths) {
            ribbon(buf, mat, pts, STRAND_WIDTH + 1.4f, STRAND_DEEP, 190, true);
            ribbon(buf, mat, pts, STRAND_WIDTH, STRAND_GOLD, 255, true);
        }
        flush(buf);

        BufferBuilder glow = beginTex(TEX_GLOW);
        for (List<float[]> pts : paths) {
            float[] anchor = pts.get(0);
            texQuad(glow, mat, anchor[0], anchor[1], 4.6f, STRAND_GOLD, 190);
        }
        flush(glow);
    }

    static float ceremonyEased(MirrorScreen.DevState state) {
        float progress = state.ceremonyWeaveProgress;
        return progress * progress * (3f - 2f * progress);
    }

    private static float[] coilPos(float cx, float cy, int index) {
        float a = 0.6f + index * 1.9f;
        return new float[] { cx + Mth.cos(a) * 272f, cy + Mth.sin(a) * 228f };
    }

    private static void drawCeremonyStrand(GuiGraphics g, float cx, float cy, float time,
                                           MirrorScreen.DevState state) {
        Matrix4f mat = g.pose().last().pose();
        int slot = Math.floorMod(state.ceremonySlot, ECHO_MAX);
        float[] seat = { cx + ECHO_LAYOUT[slot][0], cy + ECHO_LAYOUT[slot][1] };
        float[] spool = coilPos(cx, cy, Math.max(0, state.ceremonyCoil));
        int band = slot % 3;
        float theta = slot * GOLDEN_ANGLE + band * 0.7f;
        float[] anchor = bandPoint(cx, cy, band, theta, time);
        float p = Math.max(0.02f, ceremonyEased(state));

        float h1 = Math.min(1f, p / CEREMONY_BOND);
        float t1 = Mth.clamp((p - CEREMONY_BOND) / CEREMONY_RETRACT, 0f, 1f);
        float h2 = Mth.clamp((p - CEREMONY_BOND) / (1f - CEREMONY_BOND), 0f, 1f);
        float sway = Mth.sin(time * 0.5f) * STRAND_SWAY;

        BufferBuilder buf = beginTex(TEX_THREAD);
        List<float[]> pts1 = null;
        if (h1 - t1 > 0.01f) {
            pts1 = bezierPts(spool, anchor, sway * 0.4f, sway * 0.3f, t1, h1);
            ribbon(buf, mat, pts1, STRAND_WIDTH + 1.4f, STRAND_DEEP, 220, true);
            ribbon(buf, mat, pts1, STRAND_WIDTH, STRAND_GOLD, 255, true);
        }
        List<float[]> pts2 = null;
        if (h2 > 0.01f) {
            pts2 = bezierPts(anchor, seat, sway, sway, 0f, h2);
            ribbon(buf, mat, pts2, STRAND_WIDTH + 1.4f, STRAND_DEEP, 220, true);
            ribbon(buf, mat, pts2, STRAND_WIDTH, STRAND_GOLD, 255, true);
        }
        flush(buf);

        BufferBuilder glow = beginTex(TEX_GLOW);
        if (t1 <= 0.01f && p < CEREMONY_BOND) {
            texQuad(glow, mat, spool[0], spool[1], 5.5f, STRAND_GOLD, 210);
        }
        if (p >= CEREMONY_BOND) {
            float bondFlash = Mth.clamp(1f - (p - CEREMONY_BOND) / 0.1f, 0f, 1f);
            texQuad(glow, mat, anchor[0], anchor[1], 4.6f + 6f * bondFlash, STRAND_GOLD,
                    (int) (200 + 55 * bondFlash));
        }
        List<float[]> active = pts2 != null ? pts2 : pts1;
        if (active != null) {
            int n = active.size();
            for (int t = 1; t <= 3 && n - 1 - t >= 0; t++) {
                float[] pt = active.get(n - 1 - t);
                texQuad(glow, mat, pt[0], pt[1], 6f - t * 1.3f, ECHO_LIT, 120 - t * 30);
            }
            float[] headPt = active.get(n - 1);
            float pulse = 0.6f + 0.4f * Mth.sin(time * 6f);
            texQuad(glow, mat, headPt[0], headPt[1], 13f, ECHO_LIT, (int) (150 * pulse));
            texQuad(glow, mat, headPt[0], headPt[1], 5f, ECHO_HOT, 255);
        }
        flush(glow);
    }

    private static List<float[]> bezierPts(float[] from, float[] to, float swayA, float swayB, float t0,
                                           float t1) {
        float c1x = Mth.lerp(0.35f, from[0], to[0]) + swayA;
        float c1y = Mth.lerp(0.35f, from[1], to[1]) - swayA * 0.6f;
        float c2x = Mth.lerp(0.75f, from[0], to[0]) - swayB * 0.5f;
        float c2y = Mth.lerp(0.75f, from[1], to[1]) + swayB * 0.4f;
        List<float[]> pts = new ArrayList<>();
        int steps = 18;
        for (int i = 0; i <= steps; i++) {
            float t = Mth.lerp(i / (float) steps, t0, t1);
            float u = 1f - t;
            float x = u * u * u * from[0] + 3 * u * u * t * c1x + 3 * u * t * t * c2x + t * t * t * to[0];
            float y = u * u * u * from[1] + 3 * u * u * t * c1y + 3 * u * t * t * c2y + t * t * t * to[1];
            pts.add(new float[] { x, y });
        }
        return pts;
    }

    private static void drawBeacon(GuiGraphics g, float cx, float cy, float time,
                                   MirrorScreen.DevState state) {
        Matrix4f mat = g.pose().last().pose();

        if (state.burstTicks > 0 && state.burstSlot >= 0) {
            float f = 1f - state.burstTicks / 26f;
            float bx = cx + ECHO_LAYOUT[state.burstSlot % ECHO_MAX][0];
            float by = cy + ECHO_LAYOUT[state.burstSlot % ECHO_MAX][1];
            BufferBuilder rings = beginColor();
            ringStroke(rings, mat, bx, by, 6f + f * 52f, 1.2f + 3f * (1f - f), ECHO_HOT,
                    (int) (235 * (1f - f)));
            ringStroke(rings, mat, bx, by, 4f + f * 30f, 1.2f + 2f * (1f - f), STRAND_GOLD,
                    (int) (200 * (1f - f)));
            flush(rings);
            BufferBuilder bglow = beginTex(TEX_GLOW);
            texQuad(bglow, mat, bx, by, 18f * (1f - f * 0.5f), ECHO_HOT, (int) (200 * (1f - f)));
            flush(bglow);
        }

        int slot = Math.floorMod(state.ceremonySlot, ECHO_MAX);
        float ex = cx + ECHO_LAYOUT[slot][0];
        float ey = cy + ECHO_LAYOUT[slot][1];

        if (state.flashTicks > 0) {
            float f = 1f - state.flashTicks / 24f;
            BufferBuilder burst = beginColor();
            ringStroke(burst, mat, ex, ey, 8f + f * 42f, 0.8f + 2.5f * (1f - f), ECHO_HOT,
                    (int) (220 * (1f - f)));
            flush(burst);
        }
        if (!state.claimable && !state.ceremonyActive) return;

        float frac = (time * 0.8f) % 1f;
        float frac2 = (time * 0.8f + 0.5f) % 1f;
        BufferBuilder rings = beginColor();
        ringStroke(rings, mat, ex, ey, 8f + frac * 26f, 1.4f, ECHO_LIT, (int) (170 * (1f - frac)));
        ringStroke(rings, mat, ex, ey, 8f + frac2 * 26f, 1.4f, ECHO_LIT, (int) (170 * (1f - frac2)));
        flush(rings);

        float pulse = 0.5f + 0.5f * Mth.sin(time * 3.2f);
        float boost = state.hoverEcho == slot ? 1.3f : 1f;
        BufferBuilder glow = beginTex(TEX_GLOW);
        texQuad(glow, mat, ex, ey, (16f + 5f * pulse) * boost, ECHO_LIT, (int) (120 + 80 * pulse));
        texQuad(glow, mat, ex, ey, 6.5f * boost, ECHO_HOT, 255);
        flush(glow);

        if (state.ceremonyActive) {
            float hold = state.ceremonyWeaveProgress;
            BufferBuilder lock = beginColor();
            ringStroke(lock, mat, ex, ey, 34f - 25f * hold, 2.2f, ECHO_HOT, (int) (130 + 110 * hold));
            arcStroke(lock, mat, ex, ey, 13f, 3f, -Mth.HALF_PI, hold * Mth.TWO_PI, ECHO_HOT, 255);
            flush(lock);
            BufferBuilder hglow = beginTex(TEX_GLOW);
            texQuad(hglow, mat, ex, ey, 10f + 15f * hold, ECHO_HOT, (int) (90 + 140 * hold));
            flush(hglow);
        }
    }

    private static void drawHeartClaim(GuiGraphics g, float cx, float cy, float time,
                                       MirrorScreen.DevState state) {
        Matrix4f mat = g.pose().last().pose();

        if (state.heartBurstTicks > 0) {
            float span = state.heartClaimed ? 40f : 12f;
            float f = 1f - state.heartBurstTicks / span;
            float reach = state.heartClaimed ? 130f : 40f;
            BufferBuilder burst = beginColor();
            ringStroke(burst, mat, cx, cy, 10f + f * reach, 1.5f + 3f * (1f - f), ECHO_HOT,
                    (int) (235 * (1f - f)));
            if (state.heartClaimed) {
                ringStroke(burst, mat, cx, cy, 6f + f * reach * 0.6f, 1.2f + 2f * (1f - f), STRAND_GOLD,
                        (int) (200 * (1f - f)));
            }
            flush(burst);
        }

        boolean heartClaimable = state.skeins >= SKEIN_MAX && Math.min(state.litEchoes, ECHO_MAX) >= ECHO_MAX &&
                !state.heartClaimed;
        if (!heartClaimable) return;

        float frac = (time * 0.6f) % 1f;
        float frac2 = (time * 0.6f + 0.5f) % 1f;
        BufferBuilder rings = beginColor();
        ringStroke(rings, mat, cx, cy, 12f + frac * 40f, 1.6f, ECHO_LIT, (int) (180 * (1f - frac)));
        ringStroke(rings, mat, cx, cy, 12f + frac2 * 40f, 1.6f, ECHO_LIT, (int) (180 * (1f - frac2)));
        for (int s = 0; s < 3; s++) {
            float start = -Mth.HALF_PI + s * (Mth.TWO_PI / 3f) + 0.12f;
            float sweep = Mth.TWO_PI / 3f - 0.24f;
            boolean done = s < state.heartStage;
            arcStroke(rings, mat, cx, cy, 19f, done ? 3f : 1.6f, start, sweep,
                    done ? ECHO_HOT : ECHO_DIM, done ? 255 : 140);
        }
        flush(rings);

        float pulse = 0.5f + 0.5f * Mth.sin(time * 2.6f);
        BufferBuilder glow = beginTex(TEX_GLOW);
        texQuad(glow, mat, cx, cy, 22f + 7f * pulse, ECHO_LIT, (int) (110 + 80 * pulse));
        texQuad(glow, mat, cx, cy, 9f, ECHO_HOT, 255);
        flush(glow);

        if (state.heartHolding) {
            float hold = Math.min(1f, state.heartHoldTicks / (float) MirrorScreen.HOLD_TICKS);
            BufferBuilder lock = beginColor();
            ringStroke(lock, mat, cx, cy, 48f - 33f * hold, 2.6f, ECHO_HOT, (int) (130 + 110 * hold));
            float start = -Mth.HALF_PI + state.heartStage * (Mth.TWO_PI / 3f) + 0.12f;
            arcStroke(lock, mat, cx, cy, 19f, 3.4f, start, hold * (Mth.TWO_PI / 3f - 0.24f), ECHO_HOT,
                    255);
            flush(lock);
            BufferBuilder hglow = beginTex(TEX_GLOW);
            texQuad(hglow, mat, cx, cy, 16f + 20f * hold, ECHO_HOT, (int) (90 + 150 * hold));
            flush(hglow);
        }
    }

    private static void drawCoils(GuiGraphics g, float cx, float cy, float time, MirrorScreen.DevState state) {
        if (state.coils <= 0) return;
        Matrix4f mat = g.pose().last().pose();
        BufferBuilder buf = beginTex(TEX_COIL);
        for (int i = 0; i < state.coils; i++) {
            float scale = 1f;
            if (state.ceremonyActive && i == state.coils - 1) {
                scale = 1f - Math.min(1f, ceremonyEased(state) / CEREMONY_BOND);
                if (scale <= 0.05f) continue;
            }
            float[] p = coilPos(cx, cy, i);
            int breathe = (int) (215 + 30 * Mth.sin(time * 0.9f + i));
            texQuad(buf, mat, p[0], p[1], 22f * scale, COIL_TINT, breathe);
        }
        flush(buf);
    }

    private static float[] skeinPos(float cx, float cy, int index) {
        float a = SKEIN_START_ANGLE + index * SKEIN_STEP_ANGLE;
        return new float[] { cx + Mth.cos(a) * SKEIN_RING_R, cy + Mth.sin(a) * SKEIN_RING_R };
    }

    private static float[] strandTip(float cx, float cy, int index) {
        double a = ECHO_RING_START + (index % ECHO_MAX) * (Math.PI * 2 / ECHO_MAX);
        return new float[] { cx + (float) (Math.cos(a) * STRAND_DIVE_R),
                cy + (float) (Math.sin(a) * STRAND_DIVE_R) };
    }

    private static void drawSkeins(GuiGraphics g, float cx, float cy, float time, MirrorScreen.DevState state) {
        int done = Math.min(state.skeins, SKEIN_MAX);
        if (done <= 0) return;
        Matrix4f mat = g.pose().last().pose();

        BufferBuilder thread = beginTex(TEX_LINE);
        for (int i = 0; i < done - 1; i++) {
            ribbon(thread, mat, List.of(skeinPos(cx, cy, i), skeinPos(cx, cy, i + 1)), 1.4f, THREAD_FAINT,
                    255, false);
        }
        if (done >= SKEIN_MAX) {
            ribbon(thread, mat, List.of(skeinPos(cx, cy, done - 1), skeinPos(cx, cy, 0)), 1.4f,
                    THREAD_FAINT, 255, false);
            float[] heart = { cx, cy };
            for (int i = 0; i < done; i++) {
                ribbon(thread, mat, List.of(skeinPos(cx, cy, i), heart), 1.4f, THREAD_FAINT, 255, false);
            }
        }
        flush(thread);

        BufferBuilder buf = beginTex(TEX_SKEIN);
        for (int i = 0; i < done; i++) {
            float[] p = skeinPos(cx, cy, i);
            float breathe = 1f + 0.04f * Mth.sin(time * 0.7f + i * 1.3f);
            texQuad(buf, mat, p[0], p[1], SKEIN_HALF * breathe, SKEIN_HUES[i % SKEIN_HUES.length], 245);
        }
        flush(buf);

        BufferBuilder glow = beginTex(TEX_GLOW);
        for (int i = 0; i < done; i++) {
            float[] p = skeinPos(cx, cy, i);
            texQuad(glow, mat, p[0], p[1], SKEIN_HALF * 1.6f, SKEIN_HUES[i % SKEIN_HUES.length], 70);
        }
        flush(glow);

        if (state.skeinBurstTicks > 0) {
            float[] p = skeinPos(cx, cy, done - 1);
            float f = 1f - state.skeinBurstTicks / 20f;
            BufferBuilder burst = beginColor();
            ringStroke(burst, mat, p[0], p[1], 6f + f * 34f, 1.2f + 2f * (1f - f), STRAND_GOLD,
                    (int) (220 * (1f - f)));
            flush(burst);
        }
    }

    private static void drawCore(GuiGraphics g, float cx, float cy, float time, MirrorScreen.DevState state) {
        float breath = 1f + CORE_BREATH * Mth.sin(time * 0.5f);
        Matrix4f mat = g.pose().last().pose();

        drawAccretionDisk(g, cx, cy, time);

        BufferBuilder core = beginTex(TEX_CORE);
        texQuad(core, mat, cx, cy, CORE_RADIUS / CORE_TEX_RING * breath, 0xFFFFFFFF, 255);
        flush(core);

        drawDepths(g, cx, cy, time, state);
        drawSkeins(g, cx, cy, time, state);

        boolean heartActive = state.skeins >= SKEIN_MAX;
        BufferBuilder heart = beginTex(TEX_GLOW);
        if (state.heartClaimed) {
            float pulse = 0.8f + 0.2f * Mth.sin(time * 0.9f);
            texQuad(heart, mat, cx, cy, HEART_SOCKET_HALF * 3f, ECHO_LIT, (int) (130 * pulse));
            texQuad(heart, mat, cx, cy, HEART_SOCKET_HALF * 1.3f, ECHO_HOT, 255);
        } else if (heartActive) {
            float pulse = 0.6f + 0.4f * Mth.sin(time * 1.1f);
            texQuad(heart, mat, cx, cy, HEART_SOCKET_HALF * 2.2f, ECHO_LIT, (int) (90 * pulse));
        }
        flush(heart);
        BufferBuilder heartSocket = beginTex(TEX_HEART);
        texQuad(heartSocket, mat, cx, cy, HEART_SOCKET_HALF, 0xFFFFFFFF, heartActive ? 255 : 205);
        flush(heartSocket);

        int lit = Math.min(state.litEchoes, ECHO_MAX);
        int total = Math.min(state.litEchoes + state.dimEchoes, ECHO_MAX);
        int pending = (state.ceremonyActive || state.claimable) ? 1 : 0;
        int slots = Math.min(Math.max(total, lit + pending), ECHO_MAX);

        boolean finalEra = state.skeins >= SKEIN_MAX;
        BufferBuilder fil = beginTex(TEX_LINE);
        if (finalEra) {
            float[] heartPos = { cx, cy };
            for (int i = 0; i < lit; i++) {
                List<float[]> pts = List.of(
                        new float[] { cx + ECHO_LAYOUT[i][0], cy + ECHO_LAYOUT[i][1] }, heartPos);
                ribbon(fil, mat, pts, 1.1f, FILAMENT_TINT, 255, false);
            }
        } else {
            for (int i = 1; i < lit; i++) {
                float ax = cx + ECHO_LAYOUT[i - 1][0], ay = cy + ECHO_LAYOUT[i - 1][1];
                float bx = cx + ECHO_LAYOUT[i][0], by = cy + ECHO_LAYOUT[i][1];
                float mx = (ax + bx) / 2f + Mth.sin(i * 4.7f) * 2.2f;
                float my = (ay + by) / 2f + Mth.cos(i * 3.1f) * 2.2f;
                List<float[]> pts = List.of(new float[] { ax, ay }, new float[] { mx, my },
                        new float[] { bx, by });
                ribbon(fil, mat, pts, 1.1f, FILAMENT_TINT, 255, false);
            }
        }
        flush(fil);

        BufferBuilder sockets = beginTex(TEX_SOCKET);
        for (int i = 0; i < slots; i++) {
            texQuad(sockets, mat, cx + ECHO_LAYOUT[i][0], cy + ECHO_LAYOUT[i][1], SOCKET_HALF, 0xFFFFFFFF,
                    255);
        }
        flush(sockets);

        BufferBuilder glow = beginTex(TEX_GLOW);
        for (int i = 0; i < total; i++) {
            float ex = cx + ECHO_LAYOUT[i][0];
            float ey = cy + ECHO_LAYOUT[i][1];
            boolean hovered = state.hoverEcho == i;
            if (i < lit) {
                float shimmer = 0.82f + 0.18f * Mth.sin(time * 1.4f + i * 2.1f);
                float boost = hovered ? 1.35f : 1f;
                texQuad(glow, mat, ex, ey, ECHO_RADIUS * 2.4f * boost, ECHO_LIT, (int) (120 * shimmer));
                texQuad(glow, mat, ex, ey, ECHO_RADIUS * 1.1f * boost, ECHO_LIT, (int) (255 * shimmer));
                texQuad(glow, mat, ex, ey, ECHO_RADIUS * 0.55f * boost, ECHO_HOT, 255);
            } else if (hovered) {
                texQuad(glow, mat, ex, ey, 7f, ECHO_DIM, 190);
            }
        }
        flush(glow);
    }

    private static void drawVignette(GuiGraphics g, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_COLOR);
        Matrix4f mat = g.pose().last().pose();
        int d = (int) (Math.min(width, height) * VIGNETTE_DEPTH);
        gradQuadV(buf, mat, 0, 0, width, d, VIGNETTE_ALPHA, 0);
        gradQuadV(buf, mat, 0, height - d, width, height, 0, VIGNETTE_ALPHA);
        gradQuadH(buf, mat, 0, 0, d, height, VIGNETTE_ALPHA, 0);
        gradQuadH(buf, mat, width - d, 0, width, height, 0, VIGNETTE_ALPHA);
        var mesh = buf.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawAccretionDisk(GuiGraphics g, float cx, float cy, float time) {
        ShaderInstance shader = CosmicCoreClient.getMirrorDiskShader();
        if (shader == null) return;
        shader.safeGetUniform("DiskTime").set((time * DISK_SPEED) % (16f * Mth.PI));
        shader.safeGetUniform("InnerR").set(DISK_INNER);
        shader.safeGetUniform("PixelGrid").set(DISK_PIXEL_GRID);
        shader.safeGetUniform("Alpha").set(DISK_ALPHA);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);
        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_TEX);
        Matrix4f mat = g.pose().last().pose();
        buf.addVertex(mat, cx - DISK_HALF_X, cy - DISK_HALF_Y, 0).setUv(0, 0);
        buf.addVertex(mat, cx - DISK_HALF_X, cy + DISK_HALF_Y, 0).setUv(0, 1);
        buf.addVertex(mat, cx + DISK_HALF_X, cy + DISK_HALF_Y, 0).setUv(1, 1);
        buf.addVertex(mat, cx + DISK_HALF_X, cy + DISK_HALF_Y, 0).setUv(1, 1);
        buf.addVertex(mat, cx + DISK_HALF_X, cy - DISK_HALF_Y, 0).setUv(1, 0);
        buf.addVertex(mat, cx - DISK_HALF_X, cy - DISK_HALF_Y, 0).setUv(0, 0);
        flush(buf);
    }

    private static void drawDepths(GuiGraphics g, float cx, float cy, float time,
                                   MirrorScreen.DevState state) {
        float awaken = state.awaken;
        if (awaken < 0.02f) return;
        ShaderInstance shader = CosmicCoreClient.getMirrorDepthsShader();
        if (shader == null) return;
        float heartProgress = state.heartClaimed ? 1f : (state.heartStage +
                (state.heartHolding ? Math.min(1f, state.heartHoldTicks / (float) MirrorScreen.HOLD_TICKS) : 0f)) / 3f;
        float vortex = Math.max(0.22f, heartProgress);
        shader.safeGetUniform("DepthsTime").set(time % DEPTHS_TIME_WRAP);
        shader.safeGetUniform("Awaken").set(awaken);
        shader.safeGetUniform("Vortex").set(vortex);
        shader.safeGetUniform("PixelGrid").set(DEPTHS_PIXEL_GRID);
        shader.safeGetUniform("Alpha").set(DEPTHS_ALPHA);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);
        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_TEX);
        Matrix4f mat = g.pose().last().pose();
        buf.addVertex(mat, cx - DEPTHS_R, cy - DEPTHS_R, 0).setUv(0, 0);
        buf.addVertex(mat, cx - DEPTHS_R, cy + DEPTHS_R, 0).setUv(0, 1);
        buf.addVertex(mat, cx + DEPTHS_R, cy + DEPTHS_R, 0).setUv(1, 1);
        buf.addVertex(mat, cx + DEPTHS_R, cy + DEPTHS_R, 0).setUv(1, 1);
        buf.addVertex(mat, cx + DEPTHS_R, cy - DEPTHS_R, 0).setUv(1, 0);
        buf.addVertex(mat, cx - DEPTHS_R, cy - DEPTHS_R, 0).setUv(0, 0);
        flush(buf);
    }

    private static void drawVeil(GuiGraphics g, int width, int height, float strength) {
        BufferBuilder buf = beginColor();
        int a = (int) (110 * strength);
        gradQuadV(buf, g.pose().last().pose(), 0, 0, width, height, a, a);
        flush(buf);
    }

    private static BufferBuilder beginColor() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        return Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_COLOR);
    }

    private static void ringStroke(BufferBuilder buf, Matrix4f mat, float cx, float cy, float r, float w,
                                   int tint, int alphaIn) {
        int rr = (tint >> 16) & 0xFF, gg = (tint >> 8) & 0xFF, bb = tint & 0xFF;
        int srcA = tint >>> 24;
        int a = srcA == 0 ? alphaIn : Math.min(255, srcA * alphaIn / 255);
        if (a <= 0) return;
        int n = 48;
        float ri = Math.max(0.2f, r - w / 2f);
        float ro = r + w / 2f;
        for (int i = 0; i < n; i++) {
            float a0 = i * Mth.TWO_PI / n;
            float a1 = (i + 1) * Mth.TWO_PI / n;
            float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1), s1 = Mth.sin(a1);
            buf.addVertex(mat, cx + c0 * ri, cy + s0 * ri, 0).setColor(rr, gg, bb, a);
            buf.addVertex(mat, cx + c0 * ro, cy + s0 * ro, 0).setColor(rr, gg, bb, a);
            buf.addVertex(mat, cx + c1 * ro, cy + s1 * ro, 0).setColor(rr, gg, bb, a);
            buf.addVertex(mat, cx + c1 * ro, cy + s1 * ro, 0).setColor(rr, gg, bb, a);
            buf.addVertex(mat, cx + c1 * ri, cy + s1 * ri, 0).setColor(rr, gg, bb, a);
            buf.addVertex(mat, cx + c0 * ri, cy + s0 * ri, 0).setColor(rr, gg, bb, a);
        }
    }

    private static void arcStroke(BufferBuilder buf, Matrix4f mat, float cx, float cy, float r, float w,
                                  float start, float sweep, int tint, int alphaIn) {
        if (sweep <= 0.001f) return;
        int rr = (tint >> 16) & 0xFF, gg = (tint >> 8) & 0xFF, bb = tint & 0xFF;
        int srcA = tint >>> 24;
        int a = srcA == 0 ? alphaIn : Math.min(255, srcA * alphaIn / 255);
        if (a <= 0) return;
        int n = Math.max(2, (int) (48 * sweep / Mth.TWO_PI));
        float ri = Math.max(0.2f, r - w / 2f);
        float ro = r + w / 2f;
        for (int i = 0; i < n; i++) {
            float a0 = start + sweep * i / n;
            float a1 = start + sweep * (i + 1) / n;
            float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1), s1 = Mth.sin(a1);
            buf.addVertex(mat, cx + c0 * ri, cy + s0 * ri, 0).setColor(rr, gg, bb, a);
            buf.addVertex(mat, cx + c0 * ro, cy + s0 * ro, 0).setColor(rr, gg, bb, a);
            buf.addVertex(mat, cx + c1 * ro, cy + s1 * ro, 0).setColor(rr, gg, bb, a);
            buf.addVertex(mat, cx + c1 * ro, cy + s1 * ro, 0).setColor(rr, gg, bb, a);
            buf.addVertex(mat, cx + c1 * ri, cy + s1 * ri, 0).setColor(rr, gg, bb, a);
            buf.addVertex(mat, cx + c0 * ri, cy + s0 * ri, 0).setColor(rr, gg, bb, a);
        }
    }

    private static BufferBuilder beginTex(ResourceLocation tex) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, tex);
        return Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    private static void flush(BufferBuilder buf) {
        var mesh = buf.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void texQuad(BufferBuilder buf, Matrix4f mat, float x, float y, float half, int tint,
                                int alphaIn) {
        int r = (tint >> 16) & 0xFF, g = (tint >> 8) & 0xFF, b = tint & 0xFF;
        int srcA = tint >>> 24;
        int a = srcA == 0 ? alphaIn : Math.min(255, srcA * alphaIn / 255);
        buf.addVertex(mat, x - half, y - half, 0).setUv(0, 0).setColor(r, g, b, a);
        buf.addVertex(mat, x - half, y + half, 0).setUv(0, 1).setColor(r, g, b, a);
        buf.addVertex(mat, x + half, y + half, 0).setUv(1, 1).setColor(r, g, b, a);
        buf.addVertex(mat, x + half, y + half, 0).setUv(1, 1).setColor(r, g, b, a);
        buf.addVertex(mat, x + half, y - half, 0).setUv(1, 0).setColor(r, g, b, a);
        buf.addVertex(mat, x - half, y - half, 0).setUv(0, 0).setColor(r, g, b, a);
    }

    private static void ribbon(BufferBuilder buf, Matrix4f mat, List<float[]> pts, float width, int tint,
                               int alphaIn, boolean taper) {
        if (pts.size() < 2) return;
        int r = (tint >> 16) & 0xFF, g = (tint >> 8) & 0xFF, b = tint & 0xFF;
        int srcA = tint >>> 24;
        int a = srcA == 0 ? alphaIn : Math.min(255, srcA * alphaIn / 255);
        float u = 0;
        for (int i = 0; i < pts.size() - 1; i++) {
            float[] p0 = pts.get(i);
            float[] p1 = pts.get(i + 1);
            float dx = p1[0] - p0[0], dy = p1[1] - p0[1];
            float len = Mth.sqrt(dx * dx + dy * dy);
            if (len < 1.0e-4f) continue;
            float w0 = width / 2f;
            float w1 = width / 2f;
            if (taper) {
                float t0 = i / (float) (pts.size() - 1);
                float t1 = (i + 1) / (float) (pts.size() - 1);
                w0 *= 0.65f + 0.35f * (1f - t0);
                w1 *= 0.65f + 0.35f * (1f - t1);
            }
            float nx = -dy / len, ny = dx / len;
            float u1 = u + len / 64f;
            buf.addVertex(mat, p0[0] + nx * w0, p0[1] + ny * w0, 0).setUv(u, 0).setColor(r, g, b, a);
            buf.addVertex(mat, p0[0] - nx * w0, p0[1] - ny * w0, 0).setUv(u, 1).setColor(r, g, b, a);
            buf.addVertex(mat, p1[0] - nx * w1, p1[1] - ny * w1, 0).setUv(u1, 1).setColor(r, g, b, a);
            buf.addVertex(mat, p1[0] - nx * w1, p1[1] - ny * w1, 0).setUv(u1, 1).setColor(r, g, b, a);
            buf.addVertex(mat, p1[0] + nx * w1, p1[1] + ny * w1, 0).setUv(u1, 0).setColor(r, g, b, a);
            buf.addVertex(mat, p0[0] + nx * w0, p0[1] + ny * w0, 0).setUv(u, 0).setColor(r, g, b, a);
            u = u1;
        }
    }

    private static void gradQuadV(BufferBuilder buf, Matrix4f mat, float x0, float y0, float x1, float y1,
                                  int alphaTop, int alphaBottom) {
        buf.addVertex(mat, x0, y0, 0).setColor(0, 0, 0, alphaTop);
        buf.addVertex(mat, x0, y1, 0).setColor(0, 0, 0, alphaBottom);
        buf.addVertex(mat, x1, y1, 0).setColor(0, 0, 0, alphaBottom);
        buf.addVertex(mat, x1, y1, 0).setColor(0, 0, 0, alphaBottom);
        buf.addVertex(mat, x1, y0, 0).setColor(0, 0, 0, alphaTop);
        buf.addVertex(mat, x0, y0, 0).setColor(0, 0, 0, alphaTop);
    }

    private static void gradQuadH(BufferBuilder buf, Matrix4f mat, float x0, float y0, float x1, float y1,
                                  int alphaLeft, int alphaRight) {
        buf.addVertex(mat, x0, y0, 0).setColor(0, 0, 0, alphaLeft);
        buf.addVertex(mat, x0, y1, 0).setColor(0, 0, 0, alphaLeft);
        buf.addVertex(mat, x1, y1, 0).setColor(0, 0, 0, alphaRight);
        buf.addVertex(mat, x1, y1, 0).setColor(0, 0, 0, alphaRight);
        buf.addVertex(mat, x1, y0, 0).setColor(0, 0, 0, alphaRight);
        buf.addVertex(mat, x0, y0, 0).setColor(0, 0, 0, alphaLeft);
    }
}
