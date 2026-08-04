package com.ghostipedia.cosmiccore.client.firmament;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.gravity.GravityApi;
import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;
import com.ghostipedia.cosmiccore.common.firmament.FirmamentTraversalForces;
import com.ghostipedia.cosmiccore.common.firmament.FirmamentTraversalState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class FirmamentTraversalClientPrediction {

    private static LocalPlayer trackedPlayer;
    private static float baseRoll;
    private static float lastManagedRoll;
    private static float exitRoll;
    private static double exitRollStart = Double.NaN;
    private static boolean managedRoll;

    private FirmamentTraversalClientPrediction() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        if (!player.level().dimension().equals(FirmamentDimension.KEY) || player.isSpectator() ||
                player.getAbilities().flying || player.isPassenger()) {
            return;
        }
        FirmamentTraversalState state = player.getData(CosmicAttachmentTypes.FIRMAMENT_TRAVERSAL_STATE);
        if (state.phase() == FirmamentTraversalState.Phase.INACTIVE) return;
        long localTick = player.level().getGameTime();
        double weight = state.predictedWeight(localTick);
        long phaseTick = state.predictedTick(localTick);
        boolean residualGravity = state.residualGravity() && player.isNoGravity();
        FirmamentTraversalForces.apply(player, weight, phaseTick, residualGravity);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void captureBaseRoll(ViewportEvent.ComputeCameraAngles event) {
        baseRoll = event.getRoll();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void blendExitRoll(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || event.getCamera().getEntity() != player) {
            resetRollState(null);
            return;
        }
        if (trackedPlayer != player) resetRollState(player);

        FirmamentTraversalState state = player.getData(CosmicAttachmentTypes.FIRMAMENT_TRAVERSAL_STATE);
        boolean runtimeEnabled = player.isNoGravity();
        boolean ownedRuntime = runtimeEnabled && state.managedFreeDrift() && GravityApi.isFreeDrift(player);
        float currentManagedRoll = shortestExitRoll(event.getRoll(), baseRoll);
        double now = player.tickCount + event.getPartialTick();

        if (ownedRuntime) {
            lastManagedRoll = currentManagedRoll;
            managedRoll = true;
            exitRollStart = Double.NaN;
            return;
        }
        if (runtimeEnabled) {
            if (state.phase() == FirmamentTraversalState.Phase.INACTIVE) {
                managedRoll = false;
                exitRollStart = Double.NaN;
            } else if (managedRoll) {
                lastManagedRoll = currentManagedRoll;
            }
            return;
        }
        if (managedRoll) {
            exitRoll = lastManagedRoll;
            exitRollStart = now;
            managedRoll = false;
        }
        if (Double.isNaN(exitRollStart)) return;

        double progress = Math.clamp(
                (now - exitRollStart) / FirmamentTraversalState.EXIT_ROLL_TICKS,
                0.0,
                1.0);
        if (progress >= 1.0) {
            exitRollStart = Double.NaN;
            exitRoll = 0.0f;
            return;
        }
        float residualRoll = (float) (exitRoll * (1.0 - FirmamentTraversalState.smootherstep(progress)));
        event.setRoll(event.getRoll() + residualRoll);
    }

    private static void resetRollState(LocalPlayer player) {
        trackedPlayer = player;
        lastManagedRoll = 0.0f;
        exitRoll = 0.0f;
        exitRollStart = Double.NaN;
        managedRoll = false;
    }

    static float shortestExitRoll(float presentedRoll, float baseRoll) {
        return Mth.wrapDegrees(presentedRoll - baseRoll);
    }
}
