package com.ghostipedia.cosmiccore.client.murkbloom;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.worldgen.abyss.AbyssRegions;
import com.ghostipedia.cosmiccore.common.murkbloom.MurkbloomServerLogic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class AbyssSightWallRenderer {

    private AbyssSightWallRenderer() {}

    public static final int LID_Y = -92;
    private static final float HALF_EXTENT = 384f;
    private static final int VISIBLE_RANGE = 208;
    private static final float FADE_NEAR = 2f;
    private static final float FADE_FAR = 20f;
    private static final float[] SHEET_OFFSETS = { -12.37f, -7.19f, -3.41f, -0.23f, 3.31f, 7.13f, 12.29f };
    private static final float[] SHEET_ALPHAS = { 0.22f, 0.38f, 0.60f, 0.80f, 0.60f, 0.38f, 0.22f };
    private static final ResourceLocation WHITE_TEX = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final RenderType WALL_TYPE = RenderType.entityTranslucent(WHITE_TEX);

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = pose.last().pose();

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        if (mc.level.dimension().equals(MurkbloomServerLogic.HOLLOW_DIM)) {
            VertexConsumer vc = buffers.getBuffer(WALL_TYPE);
            drawWall(vc, matrix, cam.x, cam.y, cam.z, LID_Y, colorForEdge(cam.y, LID_Y));
            for (int edge : AbyssRegions.LAYER_EDGES) {
                drawWall(vc, matrix, cam.x, cam.y, cam.z, edge, colorForEdge(cam.y, edge));
            }
            buffers.endBatch(WALL_TYPE);
        }

        SonarPulseRenderer.renderWireframe(matrix, buffers, cam, MurkbloomClientState.ticks());
        pose.popPose();
    }

    private static final float WALL_BRIGHTEN = 2.3f;
    private static final float WALL_LIFT = 0.03f;

    private static float[] colorForEdge(double camY, int edgeY) {
        double sampleY = camY > edgeY ? edgeY - 24 : edgeY + 24;
        float[] c = AbyssClientFog.fogColorAt(sampleY);
        c[0] = Math.min(1f, c[0] * WALL_BRIGHTEN + WALL_LIFT);
        c[1] = Math.min(1f, c[1] * WALL_BRIGHTEN + WALL_LIFT);
        c[2] = Math.min(1f, c[2] * WALL_BRIGHTEN + WALL_LIFT);
        return c;
    }

    private static void drawWall(VertexConsumer vc, Matrix4f matrix, double camX, double camY, double camZ,
                                 int edgeY, float[] color) {
        float dist = (float) Math.abs(camY - edgeY);
        if (dist > VISIBLE_RANGE) return;
        float t = Mth.clamp((dist - FADE_NEAR) / (FADE_FAR - FADE_NEAR), 0f, 1f);
        float passFade = t * t * (3f - 2f * t);
        if (passFade <= 0.01f) return;

        int r = (int) (color[0] * 255);
        int g = (int) (color[1] * 255);
        int b = (int) (color[2] * 255);
        float x0 = (float) camX - HALF_EXTENT;
        float x1 = (float) camX + HALF_EXTENT;
        float z0 = (float) camZ - HALF_EXTENT;
        float z1 = (float) camZ + HALF_EXTENT;

        for (int i = 0; i < SHEET_OFFSETS.length; i++) {
            float y = edgeY + SHEET_OFFSETS[i];
            int a = (int) (SHEET_ALPHAS[i] * passFade * 255);
            quadVertex(vc, matrix, x0, y, z0, 0f, 0f, r, g, b, a);
            quadVertex(vc, matrix, x0, y, z1, 0f, 1f, r, g, b, a);
            quadVertex(vc, matrix, x1, y, z1, 1f, 1f, r, g, b, a);
            quadVertex(vc, matrix, x1, y, z0, 1f, 0f, r, g, b, a);
        }
    }

    private static void quadVertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float z,
                                   float u, float v, int r, int g, int b, int a) {
        vc.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0f, 1f, 0f);
    }
}
