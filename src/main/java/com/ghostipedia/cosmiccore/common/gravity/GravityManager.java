package com.ghostipedia.cosmiccore.common.gravity;

import com.ghostipedia.cosmiccore.api.gravity.GravityFrame;
import com.ghostipedia.cosmiccore.api.gravity.GravityMode;
import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public final class GravityManager {

    private GravityManager() {}

    public static GravityFrame getFrame(Player player) {
        GravityFrame frame = Objects.requireNonNull(player)
                .getExistingDataOrNull(CosmicAttachmentTypes.GRAVITY_FRAME);
        return frame == null ? GravityFrame.NORMAL : frame;
    }

    public static boolean setFrame(ServerPlayer player, GravityFrame requestedFrame) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(requestedFrame);
        if (requestedFrame.mode() == GravityMode.DIRECTED && !DirectedGravityKernel.canActivate(player)) return false;
        GravityFrame currentFrame = getFrame(player);
        if (currentFrame.sameTarget(requestedFrame)) return true;
        if (!canReplace(currentFrame, requestedFrame)) return false;

        long nextRevision = currentFrame.revision() == Long.MAX_VALUE ? Long.MAX_VALUE : currentFrame.revision() + 1L;
        GravityFrame nextFrame = requestedFrame.withRevision(nextRevision);
        Vec3 targetPosition = findSafeTargetPosition(player, currentFrame, nextFrame);
        if (targetPosition == null) return false;
        boolean frameBasisChanged = currentFrame.mode() != nextFrame.mode() || currentFrame.down() != nextFrame.down();
        boolean orientationChanged = orientationDown(currentFrame) != orientationDown(nextFrame);
        DirectedGravityKernel.LookRotation look = orientationChanged ?
                DirectedGravityKernel.remapLook(
                        currentFrame, nextFrame, player.getYRot(), player.getXRot()) :
                new DirectedGravityKernel.LookRotation(player.getYRot(), player.getXRot());

        player.setData(CosmicAttachmentTypes.GRAVITY_FRAME, nextFrame);
        if (frameBasisChanged) resetFallState(player);
        if (!targetPosition.equals(player.position()) || orientationChanged) {
            player.connection.teleport(
                    targetPosition.x,
                    targetPosition.y,
                    targetPosition.z,
                    look.yaw(),
                    look.pitch());
        } else {
            player.refreshDimensions();
        }
        runtime(player).markDimensionsApplied(nextFrame, DirectedGravityKernel.isActive(player));
        return true;
    }

    public static boolean reset(ServerPlayer player) {
        boolean reset = setFrame(player, GravityFrame.NORMAL);
        if (reset) {
            GravityRuntimeState runtime = runtime(player);
            runtime.reset();
            GravityFrame frame = getFrame(player);
            runtime.markDimensionsApplied(frame, DirectedGravityKernel.isActive(player));
        }
        return reset;
    }

    public static boolean isNormal(Player player) {
        return getFrame(player).mode() == GravityMode.NORMAL;
    }

    public static boolean isFreeDrift(Player player) {
        return getFrame(player).mode() == GravityMode.FREE_DRIFT;
    }

    public static GravityRuntimeState runtime(Player player) {
        return Objects.requireNonNull(player).getData(CosmicAttachmentTypes.GRAVITY_RUNTIME);
    }

    static void resetFallState(Player player) {
        player.resetFallDistance();
        player.resetCurrentImpulseContext();
    }

    private static Vec3 findSafeTargetPosition(ServerPlayer player, GravityFrame currentFrame,
                                               GravityFrame targetFrame) {
        if (targetFrame.mode() != GravityMode.DIRECTED && currentFrame.mode() != GravityMode.DIRECTED) {
            return player.position();
        }

        Vec3 position = player.position();
        if (isCollisionFree(player, targetFrame, position)) return position;

        Direction currentDown = currentFrame.mode() == GravityMode.DIRECTED ? currentFrame.down() : Direction.DOWN;
        Direction targetDown = targetFrame.mode() == GravityMode.DIRECTED ? targetFrame.down() : Direction.DOWN;
        Vec3 currentUp = Vec3.atLowerCornerOf(currentDown.getOpposite().getNormal());
        Vec3 targetUp = Vec3.atLowerCornerOf(targetDown.getOpposite().getNormal());
        double maximumShift = Math.max(
                player.getBbWidth() * 0.5 + 0.5,
                player.getDimensions(player.getPose()).height() + 0.5);
        int steps = Math.max(1, (int) Math.ceil(maximumShift / 0.05));
        for (int step = 1; step <= steps; step++) {
            double distance = Math.min(maximumShift, step * 0.05);
            Vec3 awayFromCurrent = position.add(currentUp.scale(distance));
            if (isCollisionFree(player, targetFrame, awayFromCurrent)) return awayFromCurrent;
            Vec3 alongTargetUp = position.add(targetUp.scale(distance));
            if (isCollisionFree(player, targetFrame, alongTargetUp)) return alongTargetUp;
        }
        return null;
    }

    private static boolean isCollisionFree(ServerPlayer player, GravityFrame frame, Vec3 position) {
        Direction down = frame.mode() == GravityMode.DIRECTED ? frame.down() : Direction.DOWN;
        AABB box = DirectedGravityKernel.makeBoundingBox(player.getDimensions(player.getPose()), position, down)
                .deflate(1.0E-7);
        return player.level().noCollision(player, box);
    }

    private static boolean canReplace(GravityFrame currentFrame, GravityFrame requestedFrame) {
        if (requestedFrame.mode() == GravityMode.NORMAL || currentFrame.mode() == GravityMode.NORMAL ||
                currentFrame.sourceId().equals(requestedFrame.sourceId())) {
            return true;
        }
        if (requestedFrame.priority() != currentFrame.priority()) {
            return requestedFrame.priority() > currentFrame.priority();
        }
        return requestedFrame.sourceId().toString().compareTo(currentFrame.sourceId().toString()) < 0;
    }

    private static Direction orientationDown(GravityFrame frame) {
        return frame.mode() == GravityMode.DIRECTED ? frame.down() : Direction.DOWN;
    }
}
