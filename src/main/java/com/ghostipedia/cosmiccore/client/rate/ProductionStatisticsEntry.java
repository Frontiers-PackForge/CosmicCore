package com.ghostipedia.cosmiccore.client.rate;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class ProductionStatisticsEntry {

    private static KeyMapping open;

    private ProductionStatisticsEntry() {}

    public static void register(RegisterKeyMappingsEvent event) {
        open = new KeyMapping("key.cosmiccore.production_statistics", KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.cosmiccore.production_statistics");
        event.register(open);
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS || open == null ||
                !open.matches(event.getKey(), event.getScanCode()))
            return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null && minecraft.player != null) ProductionStatisticsScreen.open();
    }
}
