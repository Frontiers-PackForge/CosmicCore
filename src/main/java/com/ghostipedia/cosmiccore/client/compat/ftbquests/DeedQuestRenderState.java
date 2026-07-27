package com.ghostipedia.cosmiccore.client.compat.ftbquests;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.systems.RenderSystem;

final class DeedQuestRenderState {

    private DeedQuestRenderState() {}

    static void restore() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
    }
}
