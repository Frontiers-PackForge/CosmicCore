package com.ghostipedia.cosmiccore.client.renderer;

import com.ghostipedia.cosmiccore.client.CosmicCoreClient;

import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public class SoulThreadsRenderer {

    public static void render(PoseStack poseStack, int centerX, int centerY, int radius,
                              int erosion, float intensity, int screenWidth, int screenHeight) {
        ShaderInstance shader = CosmicCoreClient.getSoulThreadsShader();
        if (shader == null) return;

        float normalizedErosion = SoulShaderHelper.getNormalizedErosion(erosion);
        if (normalizedErosion < 0.01f) return;

        float normalizedCenterX = (float) centerX / screenWidth;
        float normalizedCenterY = (float) centerY / screenHeight;
        float normalizedRadius = (float) radius / Math.min(screenWidth, screenHeight);

        SoulShaderHelper.setupShader(shader, screenWidth, screenHeight,
                normalizedCenterX, normalizedCenterY,
                SoulShaderHelper.getCoreColor(erosion), SoulShaderHelper.getShellColor(erosion),
                intensity, normalizedRadius, normalizedErosion);

        int drawSize = (int) (radius * 3.5f);
        SoulShaderHelper.drawQuad(poseStack, centerX, centerY, drawSize, screenWidth, screenHeight);
    }
}
