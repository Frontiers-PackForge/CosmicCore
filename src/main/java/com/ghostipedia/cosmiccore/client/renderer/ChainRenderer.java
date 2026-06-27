package com.ghostipedia.cosmiccore.client.renderer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ChainRenderer {

    private static final int SEGMENTS = 8;
    private static final float GRAVITY = 0.35f;
    private static final float DAMPENING = 0.96f;
    private static final int CONSTRAINT_ITERATIONS = 6;
    private static final float MOUSE_INFLUENCE_RADIUS = 20.0f;
    private static final float MOUSE_PUSH_STRENGTH = 2.0f;
    private static final int BASE_LINK_SIZE = 14;

    private static final ResourceLocation CHAIN_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft",
            "textures/item/chain.png");

    private final List<Chain> chains = new ArrayList<>();

    public void clear() {
        chains.clear();
    }

    public void addChain(float shellX, float shellY, float pinX, float pinY, int[] color) {
        chains.add(new Chain(shellX, shellY, pinX, pinY, color));
    }

    public int getChainCount() {
        return chains.size();
    }

    public Chain getChain(int index) {
        return chains.get(index);
    }

    public void tick(float mouseX, float mouseY) {
        for (Chain chain : chains) {
            chain.simulate(mouseX, mouseY);
        }
    }

    public void updateAnchors(int chainIndex, float shellX, float shellY, float pinX, float pinY) {
        if (chainIndex < 0 || chainIndex >= chains.size()) return;
        Chain chain = chains.get(chainIndex);

        float dShellX = shellX - chain.points[0].x;
        float dShellY = shellY - chain.points[0].y;
        float dPinX = pinX - chain.points[SEGMENTS - 1].x;
        float dPinY = pinY - chain.points[SEGMENTS - 1].y;

        for (int i = 1; i < SEGMENTS - 1; i++) {
            float t = (float) i / (SEGMENTS - 1);
            float dx = dShellX * (1 - t) + dPinX * t;
            float dy = dShellY * (1 - t) + dPinY * t;
            chain.points[i].x += dx;
            chain.points[i].y += dy;
            chain.points[i].prevX += dx;
            chain.points[i].prevY += dy;
        }

        chain.points[0].x = shellX;
        chain.points[0].y = shellY;
        chain.points[0].prevX = shellX;
        chain.points[0].prevY = shellY;
        chain.points[SEGMENTS - 1].x = pinX;
        chain.points[SEGMENTS - 1].y = pinY;
        chain.points[SEGMENTS - 1].prevX = pinX;
        chain.points[SEGMENTS - 1].prevY = pinY;
        chain.recalcRestLength();
    }

    public void render(GuiGraphics graphics, float fadeAlpha, float mouseX, float mouseY, float partialTick) {
        if (chains.isEmpty() || fadeAlpha <= 0.01f) return;

        for (Chain chain : chains) {
            chain.hovered = chain.isNearMouse(mouseX, mouseY, 10.0f);
        }

        graphics.flush();
        Matrix4f matrix = graphics.pose().last().pose();

        RenderSystem.setShaderTexture(0, CHAIN_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        for (Chain chain : chains) {
            float alpha = fadeAlpha * (chain.hovered ? 0.85f : 0.55f);
            if (alpha <= 0.01f) continue;

            RenderSystem.setShaderColor(chain.color[0] / 255f, chain.color[1] / 255f,
                    chain.color[2] / 255f, alpha);

            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                    DefaultVertexFormat.POSITION_TEX);
            emitChainQuads(buffer, matrix, chain, partialTick);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void emitChainQuads(BufferBuilder buffer, Matrix4f matrix, Chain chain, float partialTick) {
        for (int i = 0; i < SEGMENTS - 1; i++) {
            float ax = lerp(chain.points[i].prevX, chain.points[i].x, partialTick);
            float ay = lerp(chain.points[i].prevY, chain.points[i].y, partialTick);
            float bx = lerp(chain.points[i + 1].prevX, chain.points[i + 1].x, partialTick);
            float by = lerp(chain.points[i + 1].prevY, chain.points[i + 1].y, partialTick);

            float midX = (ax + bx) * 0.5f;
            float midY = (ay + by) * 0.5f;
            float dx = bx - ax;
            float dy = by - ay;
            float segDist = (float) Math.sqrt(dx * dx + dy * dy);

            float hs = Math.max(BASE_LINK_SIZE, segDist * 1.15f) * 0.5f;

            float angle = (float) Math.atan2(dy, dx) + (float) (Math.PI / 2);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float c00x = -hs * cos + hs * sin, c00y = -hs * sin - hs * cos;
            float c01x = -hs * cos - hs * sin, c01y = -hs * sin + hs * cos;
            float c11x = hs * cos - hs * sin, c11y = hs * sin + hs * cos;
            float c10x = hs * cos + hs * sin, c10y = hs * sin - hs * cos;

            buffer.addVertex(matrix, midX + c00x, midY + c00y, 0).setUv(0, 0);
            buffer.addVertex(matrix, midX + c01x, midY + c01y, 0).setUv(0, 1);
            buffer.addVertex(matrix, midX + c11x, midY + c11y, 0).setUv(1, 1);
            buffer.addVertex(matrix, midX + c10x, midY + c10y, 0).setUv(1, 0);
        }
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public int getHoveredChain(float mouseX, float mouseY, float threshold) {
        for (int i = 0; i < chains.size(); i++) {
            if (chains.get(i).isNearMouse(mouseX, mouseY, threshold)) {
                return i;
            }
        }
        return -1;
    }

    public static class Chain {

        final Point[] points;
        final int[] color;
        private boolean hovered = false;
        private float restLength;

        Chain(float shellX, float shellY, float pinX, float pinY, int[] color) {
            this.color = color;
            this.points = new Point[SEGMENTS];

            float totalDist = (float) Math.sqrt(
                    (pinX - shellX) * (pinX - shellX) + (pinY - shellY) * (pinY - shellY));

            for (int i = 0; i < SEGMENTS; i++) {
                float t = (float) i / (SEGMENTS - 1);
                float x = shellX + (pinX - shellX) * t;
                float y = shellY + (pinY - shellY) * t;
                float sag = (float) Math.sin(t * Math.PI) * totalDist * 0.15f;
                y += sag;
                points[i] = new Point(x, y);
            }
            recalcRestLength();
        }

        private void recalcRestLength() {
            float dx = points[SEGMENTS - 1].x - points[0].x;
            float dy = points[SEGMENTS - 1].y - points[0].y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            restLength = dist * 1.3f / (SEGMENTS - 1);
        }

        void simulate(float mouseX, float mouseY) {
            float shellX = points[0].x;
            float shellY = points[0].y;
            float pinX = points[SEGMENTS - 1].x;
            float pinY = points[SEGMENTS - 1].y;

            for (int i = 1; i < SEGMENTS - 1; i++) {
                Point p = points[i];
                float vx = (p.x - p.prevX) * DAMPENING;
                float vy = (p.y - p.prevY) * DAMPENING;

                p.prevX = p.x;
                p.prevY = p.y;

                p.x += vx;
                p.y += vy + GRAVITY;

                float dx = p.x - mouseX;
                float dy = p.y - mouseY;
                float distSq = dx * dx + dy * dy;
                float radiusSq = MOUSE_INFLUENCE_RADIUS * MOUSE_INFLUENCE_RADIUS;

                if (distSq < radiusSq && distSq > 0.01f) {
                    float dist = (float) Math.sqrt(distSq);
                    float force = (1.0f - dist / MOUSE_INFLUENCE_RADIUS) * MOUSE_PUSH_STRENGTH;
                    p.x += (dx / dist) * force;
                    p.y += (dy / dist) * force;
                }
            }

            float rest = restLength;
            for (int iter = 0; iter < CONSTRAINT_ITERATIONS; iter++) {
                for (int i = 0; i < SEGMENTS - 1; i++) {
                    Point a = points[i];
                    Point b = points[i + 1];

                    float dx = b.x - a.x;
                    float dy = b.y - a.y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < 0.001f) continue;

                    float diff = (rest - dist) / dist;
                    float offsetX = dx * diff * 0.5f;
                    float offsetY = dy * diff * 0.5f;

                    if (i > 0) {
                        a.x -= offsetX;
                        a.y -= offsetY;
                    }
                    if (i + 1 < SEGMENTS - 1) {
                        b.x += offsetX;
                        b.y += offsetY;
                    }
                }
            }

            points[0].x = shellX;
            points[0].y = shellY;
            points[SEGMENTS - 1].x = pinX;
            points[SEGMENTS - 1].y = pinY;
        }

        public boolean isHovered() {
            return hovered;
        }

        boolean isNearMouse(float mouseX, float mouseY, float threshold) {
            float thresholdSq = threshold * threshold;
            for (int i = 0; i < SEGMENTS - 1; i++) {
                float distSq = pointToSegmentDistSq(mouseX, mouseY,
                        points[i].x, points[i].y,
                        points[i + 1].x, points[i + 1].y);
                if (distSq < thresholdSq) return true;
            }
            return false;
        }

        private float pointToSegmentDistSq(float px, float py, float ax, float ay, float bx, float by) {
            float dx = bx - ax;
            float dy = by - ay;
            float lenSq = dx * dx + dy * dy;

            if (lenSq < 0.001f) {
                float ex = px - ax;
                float ey = py - ay;
                return ex * ex + ey * ey;
            }

            float t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / lenSq));
            float projX = ax + t * dx;
            float projY = ay + t * dy;
            float ex = px - projX;
            float ey = py - projY;
            return ex * ex + ey * ey;
        }
    }

    static class Point {

        float x, y;
        float prevX, prevY;

        Point(float x, float y) {
            this.x = x;
            this.y = y;
            this.prevX = x;
            this.prevY = y;
        }
    }
}
