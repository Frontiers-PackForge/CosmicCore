package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
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
        if (!(event.getScreen() instanceof InventoryScreen screen) || !ClientDeedCache.entryUnlocked()) return;
        ImageButton recipeButton = event.getListenersList().stream()
                .filter(ImageButton.class::isInstance)
                .map(ImageButton.class::cast)
                .filter(button -> button.getWidth() == 20 && button.getHeight() == 18 &&
                        button.getY() == screen.height / 2 - 22)
                .min((left, right) -> Integer.compare(
                        Math.abs(left.getX() - screen.getGuiLeft() - 104),
                        Math.abs(right.getX() - screen.getGuiLeft() - 104)))
                .orElse(null);
        if (recipeButton == null) return;
        DeedInventoryButton button = new DeedInventoryButton(
                screen,
                recipeButton,
                Component.translatable("button.cosmiccore.deeds"),
                ignored -> MirrorScreen.open());
        button.setTooltip(Tooltip.create(Component.translatable("button.cosmiccore.deeds.tooltip")));
        event.addListener(button);
    }
}
