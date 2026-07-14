package com.ghostipedia.cosmiccore.common.block.crop;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SoulGourdClient {

    private static final int STEM_COLOR = 0xD13B3B;

    private SoulGourdClient() {}

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> STEM_COLOR,
                CosmicCrops.SOUL_GOURD_CROP.get(),
                CosmicCrops.SOUL_GOURD_ATTACHED_STEM.get(), CosmicCrops.SOUL_GOURD_BLOOM.get());
    }
}
