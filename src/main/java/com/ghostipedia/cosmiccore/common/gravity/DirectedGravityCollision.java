package com.ghostipedia.cosmiccore.common.gravity;

import com.ghostipedia.cosmiccore.api.gravity.GravityTransforms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public final class DirectedGravityCollision {

    private static final double COLLISION_EPSILON = 1.0E-7;
    private static final double SUPPORT_EPSILON = 1.0E-5;
    private static final double EDGE_INCREMENT = 0.05;

    private DirectedGravityCollision() {}

    public static Vec3 collide(Player player, Vec3 movement) {
        if (movement.lengthSqr() == 0.0) return movement;

        Direction down = DirectedGravityKernel.down(player);
        AABB box = player.getBoundingBox();
        Level level = player.level();
        List<VoxelShape> entityColliders = level.getEntityCollisions(player, box.expandTowards(movement));
        List<VoxelShape> colliders = collectColliders(player, level, entityColliders, box.expandTowards(movement));
        Vec3 clipped = collideWithShapes(movement, box, colliders, down);
        DirectedGravityKernel.MovementState state = DirectedGravityKernel.movementState(player, movement, clipped);

        if (player.maxUpStep() <= 0.0F || !(state.onGround() || player.onGround()) || !state.horizontalCollision()) {
            return clipped;
        }

        AABB groundedBox = state.onGround() ?
                box.move(GravityTransforms.localToWorld(new Vec3(0.0, state.actualLocal().y, 0.0), down)) : box;
        Vec3 stepExpansion = GravityTransforms.localToWorld(
                new Vec3(state.requestedLocal().x, player.maxUpStep(), state.requestedLocal().z), down);
        AABB stepSearchBox = groundedBox.expandTowards(stepExpansion);
        if (!state.onGround()) {
            stepSearchBox = stepSearchBox.expandTowards(
                    GravityTransforms.localToWorld(new Vec3(0.0, -SUPPORT_EPSILON, 0.0), down));
        }

        List<VoxelShape> stepEntities = level.getEntityCollisions(player, stepSearchBox);
        List<VoxelShape> stepColliders = collectColliders(player, level, stepEntities, stepSearchBox);
        double[] candidates = collectStepHeights(
                groundedBox,
                stepColliders,
                player.maxUpStep(),
                state.actualLocal().y,
                down);

        for (double candidate : candidates) {
            Vec3 requestedStep = GravityTransforms.localToWorld(
                    new Vec3(state.requestedLocal().x, candidate, state.requestedLocal().z), down);
            Vec3 stepped = collideWithShapes(requestedStep, groundedBox, stepColliders, down);
            Vec3 localStepped = GravityTransforms.worldToLocal(stepped, down);
            if (horizontalDistanceSqr(localStepped) > horizontalDistanceSqr(state.actualLocal())) {
                double distanceToGround = localMinY(box, down) - localMinY(groundedBox, down);
                return stepped.add(GravityTransforms.localToWorld(new Vec3(0.0, -distanceToGround, 0.0), down));
            }
        }

        return clipped;
    }

    public static AABB supportArea(AABB box, Direction down) {
        AABB local = GravityTransforms.worldToLocal(box, down);
        AABB support = new AABB(
                local.minX,
                local.minY - SUPPORT_EPSILON,
                local.minZ,
                local.maxX,
                local.minY,
                local.maxZ);
        return GravityTransforms.localToWorld(support, down);
    }

    public static AABB previousSupportArea(AABB box, Vec3 worldMovement, Direction down) {
        Vec3 localMovement = GravityTransforms.worldToLocal(worldMovement, down);
        Vec3 horizontal = GravityTransforms.localToWorld(new Vec3(localMovement.x, 0.0, localMovement.z), down);
        return supportArea(box, down).move(horizontal.reverse());
    }

    public static BlockPos getOnPos(Player player, float offset) {
        Direction down = DirectedGravityKernel.down(player);
        return BlockPos.containing(player.position().add(
                down.getStepX() * offset,
                down.getStepY() * offset,
                down.getStepZ() * offset));
    }

    public static AABB makeDirectionalEyeBox(Vec3 eyePosition, double width, Direction down) {
        AABB local = AABB.ofSize(Vec3.ZERO, width, COLLISION_EPSILON, width);
        return GravityTransforms.localToWorld(local, down).move(eyePosition);
    }

    public static Vec3 backOffFromEdge(Player player, Vec3 movement, MoverType mover) {
        Direction down = DirectedGravityKernel.down(player);
        Vec3 local = GravityTransforms.worldToLocal(movement, down);
        float maxStep = player.maxUpStep();
        if (local.y > 0.0 || (mover != MoverType.SELF && mover != MoverType.PLAYER) ||
                !player.isShiftKeyDown() || !isAboveGround(player, maxStep, down)) {
            return movement;
        }

        double x = local.x;
        double z = local.z;
        double xIncrement = Math.signum(x) * EDGE_INCREMENT;
        double zIncrement = Math.signum(z) * EDGE_INCREMENT;

        while (x != 0.0 && canFallAtLeast(player, x, 0.0, maxStep, down)) {
            if (Math.abs(x) <= EDGE_INCREMENT) {
                x = 0.0;
            } else {
                x -= xIncrement;
            }
        }

        while (z != 0.0 && canFallAtLeast(player, 0.0, z, maxStep, down)) {
            if (Math.abs(z) <= EDGE_INCREMENT) {
                z = 0.0;
            } else {
                z -= zIncrement;
            }
        }

        while (x != 0.0 && z != 0.0 && canFallAtLeast(player, x, z, maxStep, down)) {
            x = Math.abs(x) <= EDGE_INCREMENT ? 0.0 : x - xIncrement;
            z = Math.abs(z) <= EDGE_INCREMENT ? 0.0 : z - zIncrement;
        }

        return GravityTransforms.localToWorld(new Vec3(x, local.y, z), down);
    }

    public static double localVertical(Player player, Vec3 worldMovement) {
        return DirectedGravityKernel.worldToLocal(player, worldMovement).y;
    }

    private static Vec3 collideWithShapes(Vec3 movement, AABB box, List<VoxelShape> colliders, Direction down) {
        if (colliders.isEmpty()) return movement;

        Vec3 local = GravityTransforms.worldToLocal(movement, down);
        AABB movedBox = box;
        double y = removeTiny(collideAxis(Direction.UP, local.y, movedBox, colliders, down));
        if (y != 0.0) {
            movedBox = movedBox.move(GravityTransforms.localToWorld(new Vec3(0.0, y, 0.0), down));
        }

        boolean zFirst = Math.abs(local.x) < Math.abs(local.z);
        double x = local.x;
        double z = local.z;
        if (zFirst) {
            z = removeTiny(collideAxis(Direction.SOUTH, z, movedBox, colliders, down));
            if (z != 0.0) {
                movedBox = movedBox.move(GravityTransforms.localToWorld(new Vec3(0.0, 0.0, z), down));
            }
        }

        x = removeTiny(collideAxis(Direction.EAST, x, movedBox, colliders, down));
        if (!zFirst && x != 0.0) {
            movedBox = movedBox.move(GravityTransforms.localToWorld(new Vec3(x, 0.0, 0.0), down));
        }

        if (!zFirst) {
            z = removeTiny(collideAxis(Direction.SOUTH, z, movedBox, colliders, down));
        }

        return GravityTransforms.localToWorld(new Vec3(x, y, z), down);
    }

    private static double collideAxis(Direction localDirection, double localMovement, AABB box,
                                      List<VoxelShape> colliders, Direction down) {
        if (localMovement == 0.0) return 0.0;
        Direction worldDirection = GravityTransforms.localToWorld(localDirection, down);
        int sign = worldDirection.getAxisDirection().getStep();
        return Shapes.collide(worldDirection.getAxis(), box, colliders, localMovement * sign) * sign;
    }

    private static List<VoxelShape> collectColliders(Player player, Level level, List<VoxelShape> entityColliders,
                                                     AABB searchBox) {
        List<VoxelShape> colliders = new ArrayList<>(entityColliders.size() + 1);
        colliders.addAll(entityColliders);
        WorldBorder border = level.getWorldBorder();
        if (border.isInsideCloseToBorder(player, searchBox)) {
            colliders.add(border.getCollisionShape());
        }
        for (VoxelShape shape : level.getBlockCollisions(player, searchBox)) {
            colliders.add(shape);
        }
        return colliders;
    }

    private static double[] collectStepHeights(AABB box, List<VoxelShape> colliders, double maxStep,
                                               double skippedHeight, Direction down) {
        TreeSet<Double> candidates = new TreeSet<>();
        Direction localUp = GravityTransforms.localToWorld(Direction.UP, down);
        Direction.Axis axis = localUp.getAxis();
        int sign = localUp.getAxisDirection().getStep();
        double localMinY = localMinY(box, down);

        for (VoxelShape collider : colliders) {
            for (double coordinate : collider.getCoords(axis)) {
                double height = coordinate * sign - localMinY;
                if (height >= 0.0 && height <= maxStep && Math.abs(height - skippedHeight) > COLLISION_EPSILON) {
                    candidates.add(height);
                }
            }
        }
        return candidates.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private static boolean isAboveGround(Player player, float maxStep, Direction down) {
        return player.onGround() || player.fallDistance < maxStep &&
                !canFallAtLeast(player, 0.0, 0.0, maxStep - player.fallDistance, down);
    }

    private static boolean canFallAtLeast(Player player, double localX, double localZ, double distance,
                                          Direction down) {
        Vec3 horizontal = GravityTransforms.localToWorld(new Vec3(localX, 0.0, localZ), down);
        AABB moved = player.getBoundingBox().move(horizontal);
        AABB local = GravityTransforms.worldToLocal(moved, down);
        AABB fallArea = new AABB(
                local.minX,
                local.minY - distance - SUPPORT_EPSILON,
                local.minZ,
                local.maxX,
                local.minY,
                local.maxZ);
        return player.level().noCollision(player, GravityTransforms.localToWorld(fallArea, down));
    }

    private static double localMinY(AABB box, Direction down) {
        return GravityTransforms.worldToLocal(box, down).minY;
    }

    private static double horizontalDistanceSqr(Vec3 localMovement) {
        return localMovement.x * localMovement.x + localMovement.z * localMovement.z;
    }

    private static double removeTiny(double value) {
        return Math.abs(value) < COLLISION_EPSILON ? 0.0 : value;
    }
}
