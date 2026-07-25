package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class DeedHudOverlay implements LayeredDraw.Layer {

    private static final ResourceLocation THREAD = CosmicCore.id("textures/gui/mirror/thread.png");
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
        float scale = Math.min(1f, (guiGraphics.guiWidth() - 40f) / (textWidth + 32f));
        int bannerWidth = textWidth + 32;
        int bannerHeight = 60;

        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(guiGraphics.guiWidth() / 2f, 8, 0);
        pose.scale(scale, scale, 1f);
        int left = -bannerWidth / 2;
        guiGraphics.fill(left, 0, left + bannerWidth, bannerHeight, 0xC0080C18);
        for (int x = left; x < left + bannerWidth; x += 8) {
            int width = Math.min(8, left + bannerWidth - x);
            guiGraphics.blit(THREAD, x, 0, 0, 0, width, 2, 8, 2);
            guiGraphics.blit(THREAD, x, bannerHeight - 2, 0, 0, width, 2, 8, 2);
        }
        guiGraphics.drawCenteredString(minecraft.font, FIRST, 0, 7, 0xFFD4D8E2);
        guiGraphics.drawCenteredString(minecraft.font, SECOND, 0, 19, 0xFFE8D7B4);
        guiGraphics.drawCenteredString(minecraft.font, PROMPT, 0, 33, 0xFFF0C86E);
        guiGraphics.drawCenteredString(minecraft.font, control, 0, 46, 0xFFB9C3D7);
        pose.popPose();
    }
}
