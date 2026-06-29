package com.ghostipedia.cosmiccore.client.dev;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class AbyssDevView {

    private AbyssDevView() {}

    public static final String CATEGORY = "key.categories.cosmiccore";
    public static KeyMapping TOGGLE;
    public static boolean stripFog = false;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        TOGGLE = new KeyMapping(
                "key.cosmiccore.abyss_dev_view",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY);
        event.register(TOGGLE);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (TOGGLE == null || !TOGGLE.matches(event.getKey(), event.getScanCode())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        stripFog = !stripFog;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("Abyss dev view (fog strip): " + (stripFog ? "ON" : "OFF")), true);
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!stripFog || event.getType() != FogType.WATER) return;
        event.setNearPlaneDistance(-8.0f);
        event.setFarPlaneDistance(2048.0f);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!stripFog) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.isUnderWater()) return;
        event.setRed(0.5f);
        event.setGreen(0.6f);
        event.setBlue(0.7f);
    }
}
