package com.ghostipedia.cosmiccore.client.dev;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.murkbloom.MurkbloomClientState;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.MurkbloomDevImmunityPacket;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class MurkbloomDevControls {

    private MurkbloomDevControls() {}

    private static final String[] STIR_NAMES = { "DORMANT", "RIPPLE", "STIRRING", "RISING", "TAKEN" };
    public static KeyMapping CYCLE;
    public static KeyMapping IMMUNITY;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        CYCLE = new KeyMapping(
                "key.cosmiccore.murkbloom_dev_stir",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                AbyssDevView.CATEGORY);
        event.register(CYCLE);
        IMMUNITY = new KeyMapping(
                "key.cosmiccore.murkbloom_dev_immunity",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                AbyssDevView.CATEGORY);
        event.register(IMMUNITY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (!AbyssDevView.allowed()) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) return;

        if (IMMUNITY != null && IMMUNITY.matches(event.getKey(), event.getScanCode())) {
            CCoreNetwork.sendToServer(new MurkbloomDevImmunityPacket());
            return;
        }
        if (CYCLE == null || !CYCLE.matches(event.getKey(), event.getScanCode())) return;

        boolean shift = (event.getModifiers() & GLFW.GLFW_MOD_SHIFT) != 0;
        if (shift) {
            MurkbloomClientState.flinch(1.5f);
            mc.player.displayClientMessage(Component.translatable("cosmiccore.dev.murkbloom.flinch"), true);
            return;
        }
        int next = (MurkbloomClientState.stir() + 1) % STIR_NAMES.length;
        MurkbloomClientState.setStir(next);
        mc.player.displayClientMessage(Component.translatable("cosmiccore.dev.murkbloom.stir", next,
                STIR_NAMES[next]), true);
    }
}
