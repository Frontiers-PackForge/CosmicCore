package com.ghostipedia.cosmiccore.client.compat.ftbquests;

import com.ghostipedia.cosmiccore.client.CosmicCoreClient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

public final class DeedQuestReadyRenderer {

    private DeedQuestReadyRenderer() {}

    public static boolean draw(GuiGraphics graphics, int x, int y, int width, int height, float time) {
        ShaderInstance shader = CosmicCoreClient.getMirrorDiskShader();
        if (shader == null || width <= 1 || height <= 1) return false;
        int margin = Math.max(3, Math.min(width, height) / 5);
        float left = x - margin;
        float top = y - margin;
        float right = x + width + margin;
        float bottom = y + height + margin;
        shader.safeGetUniform("DiskTime").set((time * 0.42F) % (16.0F * Mth.PI));
        shader.safeGetUniform("InnerR").set(0.54F);
        shader.safeGetUniform("PixelGrid").set(Math.max(18.0F, Math.min(width, height) * 1.35F));
        shader.safeGetUniform("Alpha").set(0.72F);
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix = graphics.pose().last().pose();
        buffer.addVertex(matrix, left, bottom, 0).setUv(0, 1);
        buffer.addVertex(matrix, right, bottom, 0).setUv(1, 1);
        buffer.addVertex(matrix, right, top, 0).setUv(1, 0);
        buffer.addVertex(matrix, left, top, 0).setUv(0, 0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        DeedQuestRenderState.restore();
        return true;
    }
}
