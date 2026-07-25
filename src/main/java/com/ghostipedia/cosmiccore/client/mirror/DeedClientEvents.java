package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class DeedClientEvents {

    private DeedClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;
        for (ClientDeedCache.ClientPresentation presentation : ClientDeedCache.presentations()) {
            if (presentation.forced() || presentation.live()) {
                MirrorScreen.openPresentation(presentation);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen) || !ClientDeedCache.entryUnlocked()) return;
        Button button = Button
                .builder(Component.translatable("button.cosmiccore.deeds"), ignored -> MirrorScreen.open())
                .bounds(event.getScreen().width / 2 + 92, event.getScreen().height / 2 - 83, 48, 20)
                .build();
        button.setTooltip(Tooltip.create(Component.translatable("button.cosmiccore.deeds.tooltip")));
        event.addListener(button);
    }
}
