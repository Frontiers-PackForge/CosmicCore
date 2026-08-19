package com.ghostipedia.cosmiccore.common.compat.create;

import net.minecraft.world.phys.Vec3;

public final class VerticalChainGeometry {

    private VerticalChainGeometry() {}

    public static boolean requiresCustomStats(Vec3 offset) {
        double horizontalDistance = offset.multiply(1, 0, 1).length();
        return horizontalDistance <= 0.01 || Math.abs(offset.y) / horizontalDistance > 1.19;
    }

    public static boolean isSteepVisual(Vec3 offset) {
        double horizontalDistance = offset.multiply(1, 0, 1).length();
        return horizontalDistance < 0.01 || Math.abs(offset.y) / horizontalDistance > 1.0;
    }

    public static Vec3 stableCross(Vec3 direction, Vec3 axis) {
        Vec3 result = direction.cross(axis);
        return result.lengthSqr() < 0.001 ? direction.cross(new Vec3(1, 0, 0)) : result;
    }

    public static Vec3 unrestrictedHorizontalProjection(Vec3 horizontalProjection) {
        return horizontalProjection.length() < 1.6 ? new Vec3(100, 0, 0) : horizontalProjection.scale(100);
    }
}
