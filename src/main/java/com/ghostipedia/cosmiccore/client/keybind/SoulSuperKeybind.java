package com.ghostipedia.cosmiccore.client.keybind;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionConstants;
import com.ghostipedia.cosmiccore.common.reflection.network.SoulSuperPacket;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SoulSuperKeybind {

    public static final String CATEGORY = "key.categories.cosmiccore.reflection";
    public static KeyMapping SUPER_KEY;
    private static boolean wasKeyDown = false;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        SUPER_KEY = new KeyMapping(
                "key.cosmiccore.reflection.super",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                CATEGORY);
        event.register(SUPER_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ReflectionConstants.ENABLED) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null) {
            wasKeyDown = false;
            return;
        }

        boolean isKeyDown = SUPER_KEY != null && SUPER_KEY.isDown();
        if (isKeyDown && !wasKeyDown) {
            CCoreNetwork.sendToServer(new SoulSuperPacket());
        }
        wasKeyDown = isKeyDown;

        while (SUPER_KEY != null && SUPER_KEY.consumeClick()) {
            // Consumed
        }
    }
}
