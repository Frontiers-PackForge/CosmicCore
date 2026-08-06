package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.client.gui.MajorInfoPanelRenderer;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class DeedHudOverlay implements LayeredDraw.Layer {

    private static final Component FIRST = Component.translatable("cosmiccore.deeds.banner.first");
    private static final Component SECOND = Component.translatable("cosmiccore.deeds.banner.second");
    private static final Component PROMPT = Component.translatable("cosmiccore.deeds.banner.prompt");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui ||
                minecraft.gui.getDebugOverlay().showDebugScreen()) {
            return;
        }
        boolean pending = false;
        boolean patientZero = false;
        for (ResourceLocation id : ClientDeedCache.pending()) {
            if (!id.equals(DeedRegistry.THE_ADDRESS.id())) {
                pending = true;
                patientZero |= id.equals(DeedRegistry.NETHER_PERMIT.id());
            }
        }
        if (!pending) return;

        Component key = MirrorScreen.OPEN == null ? Component.empty() : MirrorScreen.OPEN.getTranslatedKeyMessage();
        Component control = Component.translatable(patientZero ?
                "cosmiccore.deeds.banner.force_control" : "cosmiccore.deeds.banner.control", key);

        int textWidth = Math.max(minecraft.font.width(FIRST),
                Math.max(minecraft.font.width(SECOND),
                        Math.max(minecraft.font.width(PROMPT), minecraft.font.width(control))));
        float scale = Math.min(1f, (guiGraphics.guiWidth() - 40f) / (textWidth + 46f));
        int bannerWidth = textWidth + 46;
        int bannerHeight = 78;

        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(guiGraphics.guiWidth() / 2f, 8, 0);
        pose.scale(scale, scale, 1f);
        int left = -bannerWidth / 2;
        MajorInfoPanelRenderer.draw(guiGraphics, left, 0, bannerWidth, bannerHeight);
        guiGraphics.drawCenteredString(minecraft.font, FIRST, 0, 13, 0xFFD4D8E2);
        guiGraphics.drawCenteredString(minecraft.font, SECOND, 0, 27, 0xFFE8D7B4);
        guiGraphics.drawCenteredString(minecraft.font, PROMPT, 0, 43, 0xFFF0C86E);
        guiGraphics.drawCenteredString(minecraft.font, control, 0, 58, 0xFFB9C3D7);
        pose.popPose();
    }
}
