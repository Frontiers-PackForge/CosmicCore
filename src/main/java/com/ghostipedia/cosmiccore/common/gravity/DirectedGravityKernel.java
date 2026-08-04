package com.ghostipedia.cosmiccore.common.gravity;

import com.ghostipedia.cosmiccore.api.gravity.GravityApi;
import com.ghostipedia.cosmiccore.api.gravity.GravityFrame;
import com.ghostipedia.cosmiccore.api.gravity.GravityMode;
import com.ghostipedia.cosmiccore.api.gravity.GravityTransforms;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public final class DirectedGravityKernel {

    private DirectedGravityKernel() {}

    public static boolean isDirected(Player player) {
        return GravityApi.getFrame(Objects.requireNonNull(player)).mode() == GravityMode.DIRECTED;
    }

    public static boolean canActivate(Player player) {
        Objects.requireNonNull(player);
        return !player.isPassenger() && !player.isSpectator() && !player.isSleeping() &&
                !player.isDeadOrDying() && !player.isNoGravity() && !player.getAbilities().flying &&
                !player.isFallFlying() &&
                !player.isSwimming() && !player.isInWater() && !player.isInLava() && !player.isInFluidType();
    }

    public static boolean isActive(Player player) {
        GravityFrame frame = frame(player);
        return frame.mode() == GravityMode.DIRECTED && canActivate(player) &&
                !GravityManager.runtime(player).isDirectedSuppressed(frame.revision());
    }

    public static GravityFrame frame(Player player) {
        return GravityApi.getFrame(Objects.requireNonNull(player));
    }

    public static Direction down(Player player) {
        return frame(player).down();
    }

    public static AABB makeBoundingBox(Player player, EntityDimensions dimensions, Vec3 position) {
        return makeBoundingBox(dimensions, position, down(player));
    }

    public static AABB makeBoundingBox(EntityDimensions dimensions, Vec3 position, Direction down) {
        double halfWidth = dimensions.width() * 0.5;
        AABB localBox = new AABB(
                -halfWidth,
                0.0,
                -halfWidth,
                halfWidth,
                dimensions.height(),
                halfWidth);
        return GravityTransforms.localToWorld(localBox, down).move(position);
    }

    public static Vec3 eyeOffset(Player player, float eyeHeight) {
        return GravityTransforms.localToWorld(new Vec3(0.0, eyeHeight, 0.0), down(player));
    }

    public static Vec3 viewVector(Player player, Vec3 localView) {
        return GravityTransforms.localToWorld(localView, down(player));
    }

    public static Vec3 relativeMovement(Player player, Vec3 localMovement) {
        return GravityTransforms.localToWorld(localMovement, down(player));
    }

    public static Vec3 applyGravity(Player player, Vec3 worldMovement, double baseGravity) {
        GravityFrame frame = frame(player);
        Vec3 gravity = Vec3.atLowerCornerOf(frame.down().getNormal()).scale(baseGravity * frame.strength());
        return worldMovement.add(gravity);
    }

    public static Vec3 worldToLocal(Player player, Vec3 worldVector) {
        return GravityTransforms.worldToLocal(worldVector, down(player));
    }

    public static Vec3 localToWorld(Player player, Vec3 localVector) {
        return GravityTransforms.localToWorld(localVector, down(player));
    }

    public static Vec3 removeClippedVelocity(Player player, Vec3 requestedWorldMovement, Vec3 actualWorldMovement,
                                             Vec3 currentWorldVelocity) {
        return removeClippedVelocity(down(player), requestedWorldMovement, actualWorldMovement, currentWorldVelocity);
    }

    static Vec3 removeClippedVelocity(Direction down, Vec3 requestedWorldMovement, Vec3 actualWorldMovement,
                                      Vec3 currentWorldVelocity) {
        Vec3 requested = GravityTransforms.worldToLocal(requestedWorldMovement, down);
        Vec3 actual = GravityTransforms.worldToLocal(actualWorldMovement, down);
        Vec3 current = GravityTransforms.worldToLocal(currentWorldVelocity, down);
        double x = Mth.equal(requested.x, actual.x) ? current.x : 0.0;
        double y = Mth.equal(requested.y, actual.y) ? current.y : 0.0;
        double z = Mth.equal(requested.z, actual.z) ? current.z : 0.0;
        return GravityTransforms.localToWorld(new Vec3(x, y, z), down);
    }

    public static LookRotation remapLook(GravityFrame sourceFrame, GravityFrame targetFrame,
                                         float sourceYaw, float sourcePitch) {
        Direction sourceDown = orientationDown(sourceFrame);
        Direction targetDown = orientationDown(targetFrame);
        Vec3 sourceLocalView = calculateLocalView(sourceYaw, sourcePitch);
        Vec3 worldView = GravityTransforms.localToWorld(sourceLocalView, sourceDown);
        Vec3 targetLocalView = GravityTransforms.worldToLocal(worldView, targetDown).normalize();
        double horizontal = Math.sqrt(targetLocalView.x * targetLocalView.x + targetLocalView.z * targetLocalView.z);
        float targetPitch = (float) Math.toDegrees(Math.atan2(-targetLocalView.y, horizontal));
        float targetYaw;
        if (horizontal > 1.0E-7) {
            targetYaw = (float) Math.toDegrees(Math.atan2(-targetLocalView.x, targetLocalView.z));
        } else {
            Vec3 sourceHeading = calculateLocalView(sourceYaw, 0.0F);
            Vec3 worldHeading = GravityTransforms.localToWorld(sourceHeading, sourceDown);
            Vec3 targetHeading = GravityTransforms.worldToLocal(worldHeading, targetDown);
            double headingHorizontal = Math.sqrt(
                    targetHeading.x * targetHeading.x + targetHeading.z * targetHeading.z);
            targetYaw = headingHorizontal > 1.0E-7 ?
                    (float) Math.toDegrees(Math.atan2(-targetHeading.x, targetHeading.z)) : sourceYaw;
        }
        return new LookRotation(Mth.wrapDegrees(targetYaw), Mth.clamp(targetPitch, -90.0F, 90.0F));
    }

    private static Vec3 calculateLocalView(float yaw, float pitch) {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        double horizontal = Math.cos(pitchRadians);
        return new Vec3(
                -Math.sin(yawRadians) * horizontal,
                -Math.sin(pitchRadians),
                Math.cos(yawRadians) * horizontal);
    }

    private static Direction orientationDown(GravityFrame frame) {
        return frame.mode() == GravityMode.DIRECTED ? frame.down() : Direction.DOWN;
    }

    public static Vec3 scaleSurfaceVelocity(Player player, Vec3 worldVelocity, double horizontalScale) {
        Direction down = down(player);
        Vec3 local = GravityTransforms.worldToLocal(worldVelocity, down);
        return GravityTransforms.localToWorld(
                new Vec3(local.x * horizontalScale, local.y, local.z * horizontalScale), down);
    }

    public static MovementState movementState(Player player, Vec3 requestedWorldMovement, Vec3 actualWorldMovement) {
        Direction down = down(player);
        Vec3 requested = GravityTransforms.worldToLocal(requestedWorldMovement, down);
        Vec3 actual = GravityTransforms.worldToLocal(actualWorldMovement, down);
        boolean xCollision = !Mth.equal(requested.x, actual.x);
        boolean verticalCollision = !Mth.equal(requested.y, actual.y);
        boolean zCollision = !Mth.equal(requested.z, actual.z);
        return new MovementState(
                xCollision || zCollision,
                verticalCollision,
                verticalCollision && requested.y < 0.0,
                requested,
                actual);
    }

    public record MovementState(
                                boolean horizontalCollision,
                                boolean verticalCollision,
                                boolean onGround,
                                Vec3 requestedLocal,
                                Vec3 actualLocal) {}

    public record LookRotation(float yaw, float pitch) {}
}
