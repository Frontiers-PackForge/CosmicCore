package com.ghostipedia.cosmiccore.client.renderer;

import com.ghostipedia.cosmiccore.client.CosmicCoreClient;

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
public class BackgroundRenderer {

    public enum BackgroundType {
        VOID,   // Mystical ethereal void
        GALAXY  // Deep space galaxy with nebulae
    }

    public static void render(PoseStack poseStack, BackgroundType type, float fadeAlpha,
                              int screenWidth, int screenHeight) {
        Matrix4f matrix = poseStack.last().pose();

        // Black overlay first so world fades to black during transitions
        int blackAlpha = (int) (fadeAlpha * 255);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder blackBuffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR);
        blackBuffer.addVertex(matrix, 0, screenHeight, 0).setColor(0, 0, 0, blackAlpha);
        blackBuffer.addVertex(matrix, screenWidth, screenHeight, 0).setColor(0, 0, 0, blackAlpha);
        blackBuffer.addVertex(matrix, screenWidth, 0, 0).setColor(0, 0, 0, blackAlpha);
        blackBuffer.addVertex(matrix, 0, 0, 0).setColor(0, 0, 0, blackAlpha);
        BufferUploader.drawWithShader(blackBuffer.buildOrThrow());

        ShaderInstance shader = type == BackgroundType.VOID ? CosmicCoreClient.getVoidBgShader() :
                CosmicCoreClient.getGalaxyBgShader();

        if (shader == null) {
            RenderSystem.disableBlend();
            return;
        }

        if (fadeAlpha < 0.01f) {
            RenderSystem.disableBlend();
            return;
        }

        RenderSystem.setShader(() -> shader);

        if (shader.GAME_TIME != null) {
            shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }
        if (shader.SCREEN_SIZE != null) {
            shader.SCREEN_SIZE.set((float) screenWidth, (float) screenHeight);
        }

        setUniformSafe(shader, "Intensity", fadeAlpha);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, 0, screenHeight, 0).setUv(0, 1);
        buffer.addVertex(matrix, screenWidth, screenHeight, 0).setUv(1, 1);
        buffer.addVertex(matrix, screenWidth, 0, 0).setUv(1, 0);
        buffer.addVertex(matrix, 0, 0, 0).setUv(0, 0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    private static void renderFallback(PoseStack poseStack, int screenWidth, int screenHeight) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, 0, screenHeight, 0).setColor(0, 0, 0, 255);
        buffer.addVertex(matrix, screenWidth, screenHeight, 0).setColor(0, 0, 0, 255);
        buffer.addVertex(matrix, screenWidth, 0, 0).setColor(0, 0, 0, 255);
        buffer.addVertex(matrix, 0, 0, 0).setColor(0, 0, 0, 255);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.disableBlend();
    }

    private static void setUniformSafe(ShaderInstance shader, String name, float value) {
        var uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(value);
        }
    }
}
