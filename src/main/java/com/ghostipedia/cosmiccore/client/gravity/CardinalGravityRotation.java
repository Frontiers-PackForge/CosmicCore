package com.ghostipedia.cosmiccore.client.gravity;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class CardinalGravityRotation {

    private static final float HALF_SQRT_TWO = (float) (Math.sqrt(2.0) * 0.5);

    private CardinalGravityRotation() {}

    public static Quaternionf forDown(Direction down) {
        return switch (down) {
            case DOWN -> new Quaternionf();
            case UP -> new Quaternionf(0.0F, 0.0F, 1.0F, 0.0F);
            case NORTH -> new Quaternionf(HALF_SQRT_TWO, 0.0F, 0.0F, HALF_SQRT_TWO);
            case SOUTH -> new Quaternionf(0.0F, HALF_SQRT_TWO, -HALF_SQRT_TWO, 0.0F);
            case WEST -> new Quaternionf(0.5F, 0.5F, -0.5F, 0.5F);
            case EAST -> new Quaternionf(0.5F, -0.5F, 0.5F, 0.5F);
        };
    }

    public static Vec3 rotate(Quaternionfc rotation, Vec3 vector) {
        Vector3f transformed = rotation.transform(
                (float) vector.x, (float) vector.y, (float) vector.z, new Vector3f());
        return new Vec3(transformed.x, transformed.y, transformed.z);
    }
}
