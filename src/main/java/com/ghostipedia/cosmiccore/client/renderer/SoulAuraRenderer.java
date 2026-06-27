package com.ghostipedia.cosmiccore.client.renderer;

import com.ghostipedia.cosmiccore.client.CosmicCoreClient;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionConstants;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class SoulAuraRenderer {

    public static void render(PoseStack poseStack, int centerX, int centerY, int radius,
                              int erosion, float intensity, int screenWidth, int screenHeight) {
        ShaderInstance shader = CosmicCoreClient.getSoulAuraShader();
        if (shader == null) return;

        float[] color = getAuraColor(erosion);

        float normalizedCenterX = (float) centerX / screenWidth;
        float normalizedCenterY = (float) centerY / screenHeight;
        float normalizedRadius = (float) radius / Math.min(screenWidth, screenHeight);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> shader);

        if (shader.GAME_TIME != null) {
            shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }
        if (shader.SCREEN_SIZE != null) {
            shader.SCREEN_SIZE.set((float) screenWidth, (float) screenHeight);
        }

        setUniformSafe(shader, "Center", normalizedCenterX, normalizedCenterY);
        setUniformSafe(shader, "BaseColor", color[0], color[1], color[2]);
        setUniformSafe(shader, "Intensity", intensity);
        setUniformSafe(shader, "Radius", normalizedRadius);

        int auraSize = (int) (radius * 3.0f);
        int x1 = centerX - auraSize;
        int y1 = centerY - auraSize;
        int x2 = centerX + auraSize;
        int y2 = centerY + auraSize;

        float u1 = (float) x1 / screenWidth;
        float v1 = (float) y1 / screenHeight;
        float u2 = (float) x2 / screenWidth;
        float v2 = (float) y2 / screenHeight;

        Matrix4f matrix = poseStack.last().pose();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, x1, y2, 0).setUv(u1, v2);
        buffer.addVertex(matrix, x2, y2, 0).setUv(u2, v2);
        buffer.addVertex(matrix, x2, y1, 0).setUv(u2, v1);
        buffer.addVertex(matrix, x1, y1, 0).setUv(u1, v1);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    // Aura colors are complementary to soul orb colors
    private static float[] getAuraColor(int erosion) {
        int tier = ReflectionConstants.getSoulColorTier(erosion);

        return switch (tier) {
            case 0 -> new float[] { 1.0f, 0.85f, 0.45f };
            case 1 -> new float[] { 1.0f, 0.60f, 0.30f };
            case 2 -> new float[] { 0.70f, 0.90f, 0.30f };
            case 3 -> new float[] { 0.30f, 0.85f, 0.50f };
            case 4 -> new float[] { 0.25f, 0.75f, 0.75f };
            case 5 -> new float[] { 0.15f, 0.45f, 0.45f };
            default -> new float[] { 0.10f, 0.25f, 0.25f };
        };
    }

    private static void setUniformSafe(ShaderInstance shader, String name, float value) {
        var uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(value);
        }
    }

    private static void setUniformSafe(ShaderInstance shader, String name, float x, float y) {
        var uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y);
        }
    }

    private static void setUniformSafe(ShaderInstance shader, String name, float x, float y, float z) {
        var uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(x, y, z);
        }
    }
}
