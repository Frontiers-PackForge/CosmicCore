package com.ghostipedia.cosmiccore.integration.emi.warmer;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class EmiWarmerEvents {

    private static Boolean emiPresent;

    private EmiWarmerEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        run();
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        run();
    }

    private static void run() {
        if (emiPresent == null) {
            emiPresent = ModList.get().isLoaded("emi");
        }
        if (!emiPresent) return;
        EmiSizeWarmer.tick();
    }
}
