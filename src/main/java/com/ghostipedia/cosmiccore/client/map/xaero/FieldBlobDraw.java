package com.ghostipedia.cosmiccore.client.map.xaero;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.integration.map.ButtonState;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class FieldBlobDraw {

    private FieldBlobDraw() {}

    public static final String ORE_VEINS_LAYER = "ore_veins";
    public static final int ZONE_MIN_PX = 20;
    public static final double ZONE_SCALE_MULT = 1.6;

    private static final int Z_MINIMAP = 0;
    private static final int Z_WORLDMAP = 200;
    private static final float MINIMAP_RING_INNER = 0.85f;
    private static final double TAU = Math.PI * 2.0;
    private static final int ZONE_SEGMENTS = 30;
    private static final int DEPLETED_RGB = 0x808080;

    private static final double ZONE_BASE_W = 0.80;
    private static final float ZONE_INNER_RING = 0.9f;
    private static final float ZONE_FILL_ALPHA = 0.18f;
    private static final float ZONE_FILL_ALPHA_DEPLETED = 0.10f;
    private static final float ZONE_EDGE_ALPHA = 0.85f;
    private static final float ZONE_EDGE_ALPHA_DEPLETED = 0.50f;

    private static final float SIN45 = 0.70710677f;
    private static final int HATCH_STEP_DIVISIONS = 4;
    private static final float HATCH_THICKNESS = 1.4f;
    private static final float HATCH_GRAY = 0.18f;
    private static final float HATCH_ALPHA = 0.75f;

    private static final Set<String> LOGGED = ConcurrentHashMap.newKeySet();
    private static final Map<Long, BlobShape> SHAPES = new HashMap<>();
    private static BufferBuilder zoneBatch;
    private static BufferBuilder minimapBatch;

    public static float zoneBlockRadius(byte tier, int fieldRadius) {
        float factor = switch (tier) {
            case 0 -> 1.5f;
            case 1 -> 1.3f;
            case 2 -> 1.1f;
            default -> 0.95f;
        };
        return fieldRadius * factor;
    }

    public static float zonePixelRadius(float blockRadius, double mapScale) {
        double px = blockRadius * mapScale * ZONE_SCALE_MULT;
        return (float) Math.max(ZONE_MIN_PX, px);
    }

    public static long shapeSeed(int x, int z) {
        return ((long) x * 0x9E3779B1L) ^ ((long) z * 0x85EBCA77L);
    }

    public static void beginZoneBatch() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        zoneBatch = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    }

    public static void addZone(Matrix4f matrix, float radius, int colorRGB, long shapeSeed, boolean depleted) {
        addZoneAt(matrix, radius, colorRGB, shapeSeed, depleted, Z_WORLDMAP);
    }

    public static void beginMinimapBatch() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        minimapBatch = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_COLOR);
    }

    public static void addMinimapBlob(Matrix4f matrix, float radius, int colorRGB, long shapeSeed, boolean depleted,
                                      double transformPs, double transformPc) {
        BufferBuilder buf = minimapBatch;
        if (buf == null) return;

        BlobShape shape = shape(shapeSeed);
        int rgb = depleted ? DEPLETED_RGB : colorRGB;
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        for (int i = 0; i < ZONE_SEGMENTS; i++) {
            int j = (i + 1) % ZONE_SEGMENTS;
            float x1 = rotatedX(shape.x()[i], shape.z()[i], radius, transformPs, transformPc);
            float z1 = rotatedZ(shape.x()[i], shape.z()[i], radius, transformPs, transformPc);
            float x2 = rotatedX(shape.x()[j], shape.z()[j], radius, transformPs, transformPc);
            float z2 = rotatedZ(shape.x()[j], shape.z()[j], radius, transformPs, transformPc);
            tri(buf, matrix, x1, z1, x1 * MINIMAP_RING_INNER, z1 * MINIMAP_RING_INNER,
                    x2, z2, Z_MINIMAP, r, g, b, 1f);
            tri(buf, matrix, x1 * MINIMAP_RING_INNER, z1 * MINIMAP_RING_INNER,
                    x2 * MINIMAP_RING_INNER, z2 * MINIMAP_RING_INNER,
                    x2, z2, Z_MINIMAP, r, g, b, 1f);
        }
    }

    public static void endMinimapBatch() {
        BufferBuilder buf = minimapBatch;
        minimapBatch = null;
        if (buf == null) return;
        MeshData mesh = buf.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }
    }

    private static void addZoneAt(Matrix4f matrix, float radius, int colorRGB, long shapeSeed, boolean depleted,
                                  int drawZ) {
        BufferBuilder buf = zoneBatch;
        if (buf == null) return;

        BlobShape shape = shape(shapeSeed);

        int rgb = depleted ? DEPLETED_RGB : colorRGB;
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        float fillAlpha = depleted ? ZONE_FILL_ALPHA_DEPLETED : ZONE_FILL_ALPHA;
        float edgeAlpha = depleted ? ZONE_EDGE_ALPHA_DEPLETED : ZONE_EDGE_ALPHA;

        for (int i = 0; i < ZONE_SEGMENTS; i++) {
            int j = (i + 1) % ZONE_SEGMENTS;
            tri(buf, matrix, 0, 0, shape.x()[i] * radius, shape.z()[i] * radius,
                    shape.x()[j] * radius, shape.z()[j] * radius, drawZ, r, g, b, fillAlpha);
        }
        if (depleted) {
            addHatch(buf, matrix, shape, radius, drawZ);
        }
        for (int i = 0; i < ZONE_SEGMENTS; i++) {
            int j = (i + 1) % ZONE_SEGMENTS;
            float x1 = shape.x()[i] * radius;
            float z1 = shape.z()[i] * radius;
            float x2 = shape.x()[j] * radius;
            float z2 = shape.z()[j] * radius;
            tri(buf, matrix, x1, z1, x1 * ZONE_INNER_RING, z1 * ZONE_INNER_RING, x2, z2, drawZ,
                    r, g, b, edgeAlpha);
            tri(buf, matrix, x1 * ZONE_INNER_RING, z1 * ZONE_INNER_RING, x2 * ZONE_INNER_RING,
                    z2 * ZONE_INNER_RING, x2, z2, drawZ, r, g, b, edgeAlpha);
        }
    }

    public static void endZoneBatch() {
        BufferBuilder buf = zoneBatch;
        zoneBatch = null;
        if (buf != null) {
            MeshData mesh = buf.build();
            if (mesh != null) {
                BufferUploader.drawWithShader(mesh);
            }
        }
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    /** Diagonal hatching, clipped to the zone polygon so it never spills past the outline. */
    private static void addHatch(BufferBuilder buf, Matrix4f matrix, BlobShape shape, float radius, int drawZ) {
        float[] rx = new float[ZONE_SEGMENTS];
        float[] rz = new float[ZONE_SEGMENTS];
        float maxR = 0;
        for (int i = 0; i < ZONE_SEGMENTS; i++) {
            rx[i] = shape.x()[i] * radius;
            rz[i] = shape.z()[i] * radius;
            float d = (float) Math.hypot(rx[i], rz[i]);
            if (d > maxR) maxR = d;
        }
        float step = maxR / HATCH_STEP_DIVISIONS;
        if (step < 1) return;
        float ox = -SIN45 * HATCH_THICKNESS;
        float oz = SIN45 * HATCH_THICKNESS;
        float[] ts = new float[ZONE_SEGMENTS];
        for (float c = -maxR; c <= maxR; c += step) {
            float px = -SIN45 * c;
            float pz = SIN45 * c;
            int count = 0;
            for (int i = 0; i < ZONE_SEGMENTS; i++) {
                int j = (i + 1) % ZONE_SEGMENTS;
                float t = hatchClip(px, pz, rx[i], rz[i], rx[j], rz[j]);
                if (!Float.isNaN(t)) ts[count++] = t;
            }
            Arrays.sort(ts, 0, count);
            for (int k = 0; k + 1 < count; k += 2) {
                float ax = px + SIN45 * ts[k];
                float az = pz + SIN45 * ts[k];
                float bx = px + SIN45 * ts[k + 1];
                float bz = pz + SIN45 * ts[k + 1];
                tri(buf, matrix, ax - ox, az - oz, ax + ox, az + oz, bx + ox, bz + oz, drawZ,
                        HATCH_GRAY, HATCH_GRAY, HATCH_GRAY, HATCH_ALPHA);
                tri(buf, matrix, ax - ox, az - oz, bx + ox, bz + oz, bx - ox, bz - oz, drawZ,
                        HATCH_GRAY, HATCH_GRAY, HATCH_GRAY, HATCH_ALPHA);
            }
        }
    }

    private static float hatchClip(float px, float pz, float ax, float az, float bx, float bz) {
        float ex = bx - ax;
        float ez = bz - az;
        float det = SIN45 * (ex - ez);
        if (Math.abs(det) < 1e-6f) return Float.NaN;
        float apx = ax - px;
        float apz = az - pz;
        float s = SIN45 * (apz - apx) / det;
        if (s < 0f || s > 1f) return Float.NaN;
        return (-apx * ez + ex * apz) / det;
    }

    private static void tri(BufferBuilder buf, Matrix4f matrix, float x1, float z1, float x2, float z2,
                            float x3, float z3, int drawZ, float r, float g, float b, float a) {
        buf.addVertex(matrix, x1, z1, drawZ).setColor(r, g, b, a);
        buf.addVertex(matrix, x2, z2, drawZ).setColor(r, g, b, a);
        buf.addVertex(matrix, x3, z3, drawZ).setColor(r, g, b, a);
    }

    public static void ensureLayerDefaultOn() {
        if (!LOGGED.add("seed:" + ORE_VEINS_LAYER)) return;
        try {
            if (!ButtonState.isEnabled(ORE_VEINS_LAYER)) {
                ButtonState.toggleButton(ORE_VEINS_LAYER);
            }
        } catch (Exception e) {
            CosmicCore.LOGGER.debug("[FieldMap] could not seed ore_veins layer default", e);
        }
    }

    public static void clearShapeCache() {
        SHAPES.clear();
    }

    private static BlobShape shape(long seed) {
        return SHAPES.computeIfAbsent(seed, FieldBlobDraw::createShape);
    }

    private static BlobShape createShape(long seed) {
        float[] x = new float[ZONE_SEGMENTS];
        float[] z = new float[ZONE_SEGMENTS];
        double p1 = phase(seed, 1);
        double p2 = phase(seed, 2);
        double p3 = phase(seed, 3);
        for (int i = 0; i < ZONE_SEGMENTS; i++) {
            double a = (double) i / ZONE_SEGMENTS * TAU;
            double w = ZONE_BASE_W + 0.13 * Math.sin(a * 2 + p1) + 0.07 * Math.sin(a * 3 + p2) +
                    0.05 * Math.sin(a * 5 + p3);
            x[i] = (float) (Math.cos(a) * w);
            z[i] = (float) (Math.sin(a) * w);
        }
        return new BlobShape(x, z);
    }

    private static float rotatedX(float x, float z, float radius, double transformPs, double transformPc) {
        return (float) (radius * (transformPs * x - transformPc * z));
    }

    private static float rotatedZ(float x, float z, float radius, double transformPs, double transformPc) {
        return (float) (radius * (transformPc * x + transformPs * z));
    }

    private static double phase(long seed, int salt) {
        long h = (seed + salt * 0x9E3779B97F4A7C15L) * 0xC2B2AE3D27D4EB4FL;
        h ^= (h >>> 32);
        return (h & 0xFFFFFFL) / (double) 0x1000000 * TAU;
    }

    private record BlobShape(float[] x, float[] z) {}
}
