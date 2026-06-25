package com.ghostipedia.cosmiccore.client.keybind;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.CelesteDashHandler;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementHandler;
import com.ghostipedia.cosmiccore.common.reflection.network.DashPacket;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class QuakeMovementKeybinds {

    public static final String CATEGORY = "key.categories.cosmiccore.movement";
    public static KeyMapping DASH;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        DASH = new KeyMapping(
                "key.cosmiccore.movement.dash",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                CATEGORY);
        event.register(DASH);
    }

    public static boolean isDashKeyDown() {
        if (DASH == null) return false;
        InputConstants.Key key = DASH.getKey();
        if (key.getType() != InputConstants.Type.KEYSYM || key.getValue() < 0) return false;
        long window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, key.getValue());
    }

    @EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class KeyHandler {

        private static boolean wasKeyDown = false;

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null || mc.screen != null) {
                wasKeyDown = false;
                return;
            }

            if (!QuakeMovementHandler.getClientHasQuakeMovement()) {
                wasKeyDown = false;
                return;
            }

            boolean isKeyDown = isDashKeyDown();
            if (isKeyDown && !wasKeyDown) {
                if (CelesteDashHandler.tryDash(player)) {
                    CCoreNetwork.sendToServer(new DashPacket(
                            player.getXRot(),
                            player.getYRot(),
                            player.zza,
                            player.xxa));
                }
            }
            wasKeyDown = isKeyDown;

            while (DASH != null && DASH.consumeClick()) {
                // Consumed
            }
        }
    }
}
