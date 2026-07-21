package com.ghostipedia.cosmiccore.client.compat.ftbquests;

import com.ghostipedia.cosmiccore.common.compat.ftbquests.DependencyLineStyle;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

public final class DependencyLineRenderer {

    private DependencyLineRenderer() {}

    public static void render(PoseStack poseStack, double startX, double startY, double endX, double endY,
                              float halfWidth, int red, int green, int blue, int startAlpha, int endAlpha,
                              DependencyLineStyle style, Tesselator tesselator) {
        float length = (float) Math.hypot(endX - startX, endY - startY);
        if (length <= 0.0F) return;
        float dashLength = switch (style) {
            case SOLID -> length;
            case DASHED -> Math.max(6.0F, halfWidth * 8.0F);
            case DOTTED -> Math.max(2.0F, halfWidth * 2.0F);
            case DEFAULT -> length;
        };
        float gapLength = switch (style) {
            case SOLID, DEFAULT -> 0.0F;
            case DASHED -> Math.max(4.0F, halfWidth * 5.0F);
            case DOTTED -> Math.max(3.0F, halfWidth * 4.0F);
        };

        poseStack.pushPose();
        poseStack.translate(startX, startY, 0.0);
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotation((float) Math.atan2(endY - startY, endX - startX)));
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float cursor = 0.0F;
        while (cursor < length) {
            float segmentEnd = Math.min(length, cursor + dashLength);
            float startRatio = length == 0.0F ? 0.0F : cursor / length;
            float endRatio = length == 0.0F ? 1.0F : segmentEnd / length;
            int segmentStartAlpha = interpolate(startAlpha, endAlpha, startRatio);
            int segmentEndAlpha = interpolate(startAlpha, endAlpha, endRatio);
            int endRed = red * 3 / 4;
            int endGreen = green * 3 / 4;
            int endBlue = blue * 3 / 4;
            int segmentStartRed = interpolate(red, endRed, startRatio);
            int segmentStartGreen = interpolate(green, endGreen, startRatio);
            int segmentStartBlue = interpolate(blue, endBlue, startRatio);
            int segmentEndRed = interpolate(red, endRed, endRatio);
            int segmentEndGreen = interpolate(green, endGreen, endRatio);
            int segmentEndBlue = interpolate(blue, endBlue, endRatio);
            buffer.addVertex(matrix, cursor, -halfWidth, 0.0F)
                    .setColor(segmentStartRed, segmentStartGreen, segmentStartBlue, segmentStartAlpha);
            buffer.addVertex(matrix, cursor, halfWidth, 0.0F)
                    .setColor(segmentStartRed, segmentStartGreen, segmentStartBlue, segmentStartAlpha);
            buffer.addVertex(matrix, segmentEnd, halfWidth, 0.0F)
                    .setColor(segmentEndRed, segmentEndGreen, segmentEndBlue, segmentEndAlpha);
            buffer.addVertex(matrix, segmentEnd, -halfWidth, 0.0F)
                    .setColor(segmentEndRed, segmentEndGreen, segmentEndBlue, segmentEndAlpha);
            cursor = segmentEnd + gapLength;
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        poseStack.popPose();
    }

    private static int interpolate(int start, int end, float ratio) {
        return Math.round(start + (end - start) * ratio);
    }
}
