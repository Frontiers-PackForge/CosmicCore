package com.ghostipedia.cosmiccore.client.firmament;

import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class FirmamentSkyShoalRenderer {

    private static final ResourceLocation CLOUDS = ResourceLocation
            .withDefaultNamespace("textures/environment/clouds.png");
    private static final RenderType SHOAL_TYPE = RenderType.entityTranslucentEmissive(CLOUDS);
    private static final float DOME_RADIUS = 92.0f;
    private static final int COLUMNS = 16;
    private static final int ROWS = 8;
    private static final float TEXTURE_SIZE = 256.0f;
    private static final Shoal[] SHOALS = {
            new Shoal(0.32f, 0.72f, 0.16f, 52.0f, 18.0f, 50, 112, 190, 72, 0, 0),
            new Shoal(1.18f, 0.48f, -0.26f, 42.0f, 14.0f, 72, 148, 208, 64, 64, 32),
            new Shoal(2.08f, 1.02f, 0.34f, 34.0f, 12.0f, 112, 106, 190, 54, 128, 64),
            new Shoal(2.92f, 0.64f, 0.06f, 54.0f, 20.0f, 42, 128, 202, 76, 192, 96),
            new Shoal(3.76f, 0.86f, -0.22f, 46.0f, 16.0f, 86, 122, 202, 58, 32, 160),
            new Shoal(4.68f, 0.54f, 0.28f, 50.0f, 18.0f, 52, 154, 212, 68, 112, 192),
            new Shoal(5.58f, 1.10f, -0.12f, 32.0f, 12.0f, 118, 112, 196, 50, 192, 224)
    };

    private FirmamentSkyShoalRenderer() {}

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !minecraft.level.dimension().equals(FirmamentDimension.KEY)) return;

        var camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = poseStack.last().pose();

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(SHOAL_TYPE);
        for (Shoal shoal : SHOALS) {
            drawShoal(consumer, matrix, camera.x, camera.y, camera.z, shoal);
        }
        buffers.endBatch(SHOAL_TYPE);
        poseStack.popPose();
    }

    private static void drawShoal(VertexConsumer consumer, Matrix4f matrix, double cameraX, double cameraY,
                                  double cameraZ, Shoal shoal) {
        float cosElevation = (float) Math.cos(shoal.elevation());
        Vector3f center = new Vector3f(
                cosElevation * (float) Math.cos(shoal.azimuth()),
                (float) Math.sin(shoal.elevation()),
                cosElevation * (float) Math.sin(shoal.azimuth()));
        Vector3f tangent = new Vector3f(-(float) Math.sin(shoal.azimuth()), 0.0f,
                (float) Math.cos(shoal.azimuth()));
        Vector3f bitangent = new Vector3f(
                -(float) Math.sin(shoal.elevation()) * (float) Math.cos(shoal.azimuth()),
                cosElevation,
                -(float) Math.sin(shoal.elevation()) * (float) Math.sin(shoal.azimuth()));
        float cosRoll = (float) Math.cos(shoal.roll());
        float sinRoll = (float) Math.sin(shoal.roll());
        Vector3f horizontal = new Vector3f(tangent).mul(cosRoll).add(new Vector3f(bitangent).mul(sinRoll));
        Vector3f vertical = new Vector3f(tangent).mul(-sinRoll).add(new Vector3f(bitangent).mul(cosRoll));

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                addCell(consumer, matrix, cameraX, cameraY, cameraZ, shoal, center, horizontal, vertical,
                        column, row);
            }
        }
    }

    private static void addCell(VertexConsumer consumer, Matrix4f matrix, double cameraX, double cameraY,
                                double cameraZ, Shoal shoal, Vector3f center, Vector3f horizontal,
                                Vector3f vertical, int column, int row) {
        float left = -1.0f + 2.0f * column / COLUMNS;
        float right = -1.0f + 2.0f * (column + 1) / COLUMNS;
        float bottom = -1.0f + 2.0f * row / ROWS;
        float top = -1.0f + 2.0f * (row + 1) / ROWS;
        float u0 = (shoal.textureX() + 32.0f * column / COLUMNS) / TEXTURE_SIZE;
        float u1 = (shoal.textureX() + 32.0f * (column + 1) / COLUMNS) / TEXTURE_SIZE;
        float v0 = (shoal.textureY() + 16.0f * row / ROWS) / TEXTURE_SIZE;
        float v1 = (shoal.textureY() + 16.0f * (row + 1) / ROWS) / TEXTURE_SIZE;

        addVertex(consumer, matrix, cameraX, cameraY, cameraZ, shoal, center, horizontal, vertical,
                left, bottom, u0, v1);
        addVertex(consumer, matrix, cameraX, cameraY, cameraZ, shoal, center, horizontal, vertical,
                right, bottom, u1, v1);
        addVertex(consumer, matrix, cameraX, cameraY, cameraZ, shoal, center, horizontal, vertical,
                right, top, u1, v0);
        addVertex(consumer, matrix, cameraX, cameraY, cameraZ, shoal, center, horizontal, vertical,
                left, top, u0, v0);
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f matrix, double cameraX, double cameraY,
                                  double cameraZ, Shoal shoal, Vector3f center, Vector3f horizontal,
                                  Vector3f vertical, float normalizedX, float normalizedY, float u, float v) {
        Vector3f direction = new Vector3f(center).mul(DOME_RADIUS)
                .add(new Vector3f(horizontal).mul(normalizedX * shoal.width() * 0.5f))
                .add(new Vector3f(vertical).mul(normalizedY * shoal.height() * 0.5f))
                .normalize();
        float edge = Math.max(Math.abs(normalizedX), Math.abs(normalizedY));
        float fade = 1.0f - edge * edge * edge;
        int alpha = Math.round(shoal.alpha() * Math.max(0.0f, fade));
        float x = (float) cameraX + direction.x * DOME_RADIUS;
        float y = (float) cameraY + direction.y * DOME_RADIUS;
        float z = (float) cameraZ + direction.z * DOME_RADIUS;
        consumer.addVertex(matrix, x, y, z)
                .setColor(shoal.red(), shoal.green(), shoal.blue(), alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(-direction.x, -direction.y, -direction.z);
    }

    private record Shoal(float azimuth, float elevation, float roll, float width, float height,
                         int red, int green, int blue, int alpha, int textureX, int textureY) {}
}
