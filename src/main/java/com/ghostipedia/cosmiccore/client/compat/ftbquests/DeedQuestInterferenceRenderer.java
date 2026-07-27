package com.ghostipedia.cosmiccore.client.compat.ftbquests;

import com.ghostipedia.cosmiccore.client.CosmicCoreClient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

public final class DeedQuestInterferenceRenderer {

    private DeedQuestInterferenceRenderer() {}

    public static boolean draw(GuiGraphics graphics, int x, int y, int width, int height, float time, float seed,
                               float intensity) {
        ShaderInstance shader = CosmicCoreClient.getDeedInterferenceShader();
        if (shader == null || width <= 1 || height <= 1) return false;
        shader.safeGetUniform("GlitchTime").set(time);
        shader.safeGetUniform("Seed").set(seed);
        shader.safeGetUniform("Intensity").set(intensity);
        shader.safeGetUniform("Aspect").set(width / (float) height);
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix = graphics.pose().last().pose();
        buffer.addVertex(matrix, x, y + height, 0).setUv(0, 1);
        buffer.addVertex(matrix, x + width, y + height, 0).setUv(1, 1);
        buffer.addVertex(matrix, x + width, y, 0).setUv(1, 0);
        buffer.addVertex(matrix, x, y, 0).setUv(0, 0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        DeedQuestRenderState.restore();
        return true;
    }
}
