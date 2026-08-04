package com.ghostipedia.cosmiccore.api.gravity;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public final class GravityTransforms {

    private GravityTransforms() {}

    public static Vec3 worldToLocal(Vec3 worldVector, Direction down) {
        Objects.requireNonNull(worldVector);
        Basis basis = basis(down);
        return new Vec3(worldVector.dot(basis.x()), worldVector.dot(basis.y()), worldVector.dot(basis.z()));
    }

    public static Vec3 localToWorld(Vec3 localVector, Direction down) {
        Objects.requireNonNull(localVector);
        Basis basis = basis(down);
        return basis.x().scale(localVector.x)
                .add(basis.y().scale(localVector.y))
                .add(basis.z().scale(localVector.z));
    }

    public static Vec3 worldVelocityToLocal(Vec3 worldVelocity, Direction down) {
        return worldToLocal(worldVelocity, down);
    }

    public static Vec3 localVelocityToWorld(Vec3 localVelocity, Direction down) {
        return localToWorld(localVelocity, down);
    }

    public static Direction worldToLocal(Direction worldDirection, Direction down) {
        Objects.requireNonNull(worldDirection);
        Vec3 transformed = worldToLocal(Vec3.atLowerCornerOf(worldDirection.getNormal()), down);
        return Direction.getNearest(transformed.x, transformed.y, transformed.z);
    }

    public static Direction localToWorld(Direction localDirection, Direction down) {
        Objects.requireNonNull(localDirection);
        Vec3 transformed = localToWorld(Vec3.atLowerCornerOf(localDirection.getNormal()), down);
        return Direction.getNearest(transformed.x, transformed.y, transformed.z);
    }

    public static Vec3 worldPositionToLocal(Vec3 worldPosition, Direction down, Vec3 pivot) {
        Objects.requireNonNull(worldPosition);
        Objects.requireNonNull(pivot);
        return pivot.add(worldToLocal(worldPosition.subtract(pivot), down));
    }

    public static Vec3 localPositionToWorld(Vec3 localPosition, Direction down, Vec3 pivot) {
        Objects.requireNonNull(localPosition);
        Objects.requireNonNull(pivot);
        return pivot.add(localToWorld(localPosition.subtract(pivot), down));
    }

    public static AABB worldToLocal(AABB worldBox, Direction down, Vec3 pivot) {
        Objects.requireNonNull(worldBox);
        Vec3 min = worldPositionToLocal(new Vec3(worldBox.minX, worldBox.minY, worldBox.minZ), down, pivot);
        Vec3 max = worldPositionToLocal(new Vec3(worldBox.maxX, worldBox.maxY, worldBox.maxZ), down, pivot);
        return new AABB(min, max);
    }

    public static AABB localToWorld(AABB localBox, Direction down, Vec3 pivot) {
        Objects.requireNonNull(localBox);
        Vec3 min = localPositionToWorld(new Vec3(localBox.minX, localBox.minY, localBox.minZ), down, pivot);
        Vec3 max = localPositionToWorld(new Vec3(localBox.maxX, localBox.maxY, localBox.maxZ), down, pivot);
        return new AABB(min, max);
    }

    public static AABB worldToLocal(AABB worldBox, Direction down) {
        return worldToLocal(worldBox, down, Vec3.ZERO);
    }

    public static AABB localToWorld(AABB localBox, Direction down) {
        return localToWorld(localBox, down, Vec3.ZERO);
    }

    private static Basis basis(Direction down) {
        Objects.requireNonNull(down);
        Vec3 y = Vec3.atLowerCornerOf(down.getOpposite().getNormal());
        Direction forward = down.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.DOWN;
        Vec3 z = Vec3.atLowerCornerOf(forward.getNormal());
        Vec3 x = y.cross(z);
        return new Basis(x, y, z);
    }

    private record Basis(Vec3 x, Vec3 y, Vec3 z) {}
}
