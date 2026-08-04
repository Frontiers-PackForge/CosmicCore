package com.ghostipedia.cosmiccore.common.gravity;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.gravity.GravityFrame;
import com.ghostipedia.cosmiccore.api.gravity.GravityMode;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class GravityLifecycle {

    private GravityLifecycle() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        GravityFrame frame = GravityManager.getFrame(player);
        GravityRuntimeState runtime = GravityManager.runtime(player);
        boolean canActivate = DirectedGravityKernel.canActivate(player);
        if (frame.mode() == GravityMode.DIRECTED && !canActivate) {
            if (player instanceof ServerPlayer serverPlayer && GravityManager.reset(serverPlayer)) return;
            runtime.suppressDirected(frame.revision());
            if (runtime.directedActive()) {
                GravityManager.resetFallState(player);
                if (player.level().isClientSide) {
                    DirectedGravityKernel.LookRotation look = DirectedGravityKernel.remapLook(
                            frame, GravityFrame.NORMAL, player.getYRot(), player.getXRot());
                    player.absRotateTo(look.yaw(), look.pitch());
                    player.refreshDimensions();
                    runtime.markDimensionsApplied(frame, false);
                }
            }
            return;
        }

        if (frame.mode() == GravityMode.DIRECTED && runtime.isDirectedSuppressed(frame.revision())) {
            if (player instanceof ServerPlayer serverPlayer) GravityManager.reset(serverPlayer);
            return;
        }

        boolean active = DirectedGravityKernel.isActive(player);
        if (!runtime.needsDimensionRefresh(frame, active)) return;

        if (runtime.frameBasisChanged(frame) || runtime.directedActive() != active) {
            GravityManager.resetFallState(player);
        }
        player.refreshDimensions();
        runtime.markDimensionsApplied(frame, active);
    }
}
