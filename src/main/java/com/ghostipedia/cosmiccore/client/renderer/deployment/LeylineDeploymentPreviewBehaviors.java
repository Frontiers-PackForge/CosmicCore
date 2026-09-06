package com.ghostipedia.cosmiccore.client.renderer.deployment;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.transmission.PowerTowerChainClient;
import com.ghostipedia.cosmiccore.common.data.CosmicMachines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class LeylineDeploymentPreviewBehaviors {

    private static final Map<ResourceLocation, Preview> PREVIEWS = new HashMap<>();

    private LeylineDeploymentPreviewBehaviors() {}

    public static void register(ResourceLocation machine, Preview preview) {
        if (PREVIEWS.putIfAbsent(Objects.requireNonNull(machine), Objects.requireNonNull(preview)) != null)
            throw new IllegalArgumentException("Duplicate leyline preview behavior: " + machine);
    }

    public static void draw(ResourceLocation machine, RenderLevelStageEvent event, BlockPos controller,
                            Direction facing) {
        var preview = PREVIEWS.get(machine);
        if (preview != null) preview.draw(event, controller, facing);
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> register(CosmicMachines.POWER_TOWER.getId(), PowerTowerChainClient::draw));
    }

    @FunctionalInterface
    public interface Preview {

        void draw(RenderLevelStageEvent event, BlockPos controller, Direction facing);
    }
}
