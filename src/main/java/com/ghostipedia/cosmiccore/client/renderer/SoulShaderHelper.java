package com.ghostipedia.cosmiccore.client.renderer;

import com.ghostipedia.cosmiccore.common.reflection.ReflectionConstants;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public final class SoulShaderHelper {

    public static float getNormalizedErosion(int erosion) {
        return Math.min(1.0f, erosion / 1000.0f);
    }

    public static float[] getCoreColor(int erosion) {
        int tier = ReflectionConstants.getSoulColorTier(erosion);
        return switch (tier) {
            case 0 -> new float[] { 1.0f, 0.95f, 0.80f };
            case 1 -> new float[] { 0.85f, 0.90f, 1.0f };
            case 2 -> new float[] { 0.70f, 0.55f, 1.0f };
            case 3 -> new float[] { 0.90f, 0.30f, 0.60f };
            case 4 -> new float[] { 0.80f, 0.15f, 0.15f };
            case 5 -> new float[] { 0.50f, 0.05f, 0.10f };
            default -> new float[] { 0.20f, 0.02f, 0.15f };
        };
    }

    public static float[] getShellColor(int erosion) {
        int tier = ReflectionConstants.getSoulColorTier(erosion);
        return switch (tier) {
            case 0 -> new float[] { 0.88f, 0.88f, 0.94f };
            case 1 -> new float[] { 0.75f, 0.80f, 0.92f };
            case 2 -> new float[] { 0.60f, 0.55f, 0.80f };
            case 3 -> new float[] { 0.55f, 0.35f, 0.55f };
            case 4 -> new float[] { 0.40f, 0.20f, 0.25f };
            case 5 -> new float[] { 0.25f, 0.12f, 0.15f };
            default -> new float[] { 0.12f, 0.06f, 0.10f };
        };
    }

    public static void setupShader(ShaderInstance shader, int screenWidth, int screenHeight,
                                   float centerX, float centerY, float[] coreColor, float[] shellColor,
                                   float intensity, float radius, float erosion) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> shader);

        if (shader.GAME_TIME != null) shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
        if (shader.SCREEN_SIZE != null) shader.SCREEN_SIZE.set((float) screenWidth, (float) screenHeight);

        setUniform(shader, "Center", centerX, centerY);
        setUniform(shader, "CoreColor", coreColor[0], coreColor[1], coreColor[2]);
        setUniform(shader, "ShellColor", shellColor[0], shellColor[1], shellColor[2]);
        setUniform(shader, "Intensity", intensity);
        setUniform(shader, "Radius", radius);
        setUniform(shader, "Erosion", erosion);
    }

    public static void drawQuad(PoseStack poseStack, int centerX, int centerY, int drawSize,
                                int screenWidth, int screenHeight) {
        int x1 = centerX - drawSize;
        int y1 = centerY - drawSize;
        int x2 = centerX + drawSize;
        int y2 = centerY + drawSize;

        float u1 = (float) x1 / screenWidth;
        float v1 = (float) y1 / screenHeight;
        float u2 = (float) x2 / screenWidth;
        float v2 = (float) y2 / screenHeight;

        Matrix4f matrix = poseStack.last().pose();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, x1, y2, 0).uv(u1, v2).endVertex();
        buffer.vertex(matrix, x2, y2, 0).uv(u2, v2).endVertex();
        buffer.vertex(matrix, x2, y1, 0).uv(u2, v1).endVertex();
        buffer.vertex(matrix, x1, y1, 0).uv(u1, v1).endVertex();
        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    public static void setUniform(ShaderInstance shader, String name, float value) {
        var uniform = shader.getUniform(name);
        if (uniform != null) uniform.set(value);
    }

    public static void setUniform(ShaderInstance shader, String name, float x, float y) {
        var uniform = shader.getUniform(name);
        if (uniform != null) uniform.set(x, y);
    }

    public static void setUniform(ShaderInstance shader, String name, float x, float y, float z) {
        var uniform = shader.getUniform(name);
        if (uniform != null) uniform.set(x, y, z);
    }

    private SoulShaderHelper() {}
}
