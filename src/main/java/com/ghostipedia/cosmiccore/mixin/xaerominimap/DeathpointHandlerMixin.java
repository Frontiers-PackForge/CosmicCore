package com.ghostipedia.cosmiccore.mixin.xaerominimap;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.DeathpointHandler;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;

@Mixin(value = DeathpointHandler.class, remap = false)
public abstract class DeathpointHandlerMixin {

    private static final long DUPLICATE_WINDOW_MS = 2_000L;

    @Shadow
    @Final
    private MinimapSession session;

    @Inject(
            method = "createDeathpoint(Lnet/minecraft/world/entity/player/Player;Lxaero/hud/minimap/world/MinimapWorld;Z)V",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$skipDuplicateDeathpoint(Player player, MinimapWorld world, boolean temporary,
                                                    CallbackInfo ci) {
        WaypointSet waypointSet = world.getCurrentWaypointSet();
        if (waypointSet == null) return;

        double dimensionDivision = session.getDimensionHelper().getDimensionDivision(world);
        int x = Mth.floor(Mth.floor(player.getX()) * dimensionDivision);
        int y = Mth.floor(player.getY());
        int z = Mth.floor(Mth.floor(player.getZ()) * dimensionDivision);
        long oldestDuplicate = System.currentTimeMillis() - DUPLICATE_WINDOW_MS;

        for (Waypoint waypoint : waypointSet.getWaypoints()) {
            if (waypoint.getPurpose() == WaypointPurpose.DEATH && waypoint.getX() == x && waypoint.getY() == y &&
                    waypoint.getZ() == z && waypoint.getCreatedAt() >= oldestDuplicate) {
                ci.cancel();
                return;
            }
        }
    }
}
