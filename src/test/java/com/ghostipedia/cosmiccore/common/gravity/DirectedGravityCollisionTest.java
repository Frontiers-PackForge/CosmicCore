package com.ghostipedia.cosmiccore.common.gravity;

import com.ghostipedia.cosmiccore.api.gravity.GravityTransforms;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectedGravityCollisionTest {

    private static final double EPSILON = 1.0E-12;

    @Test
    void directionalEyeBoxUsesLocalUpAsItsThinAxis() {
        Vec3 eyePosition = new Vec3(3.25, -7.5, 11.75);
        double width = 0.48;
        for (Direction down : Direction.values()) {
            AABB box = DirectedGravityCollision.makeDirectionalEyeBox(eyePosition, width, down);
            assertEquals(eyePosition.x, box.getCenter().x, EPSILON);
            assertEquals(eyePosition.y, box.getCenter().y, EPSILON);
            assertEquals(eyePosition.z, box.getCenter().z, EPSILON);
            for (Direction.Axis axis : Direction.Axis.values()) {
                double expected = axis == down.getAxis() ? 1.0E-7 : width;
                assertEquals(expected, size(box, axis), EPSILON);
            }
        }
    }

    @Test
    void collisionRemovesEveryClippedLocalVelocityAxis() {
        Vec3 requestedLocal = new Vec3(0.25, -0.5, 0.75);
        Vec3 actualLocal = new Vec3(0.25, 0.0, 0.0);
        Vec3 velocityLocal = new Vec3(1.0, -2.0, 3.0);
        Vec3 expectedLocal = new Vec3(1.0, 0.0, 0.0);
        for (Direction down : Direction.values()) {
            Vec3 result = DirectedGravityKernel.removeClippedVelocity(
                    down,
                    GravityTransforms.localToWorld(requestedLocal, down),
                    GravityTransforms.localToWorld(actualLocal, down),
                    GravityTransforms.localToWorld(velocityLocal, down));
            Vec3 resultLocal = GravityTransforms.worldToLocal(result, down);
            assertEquals(expectedLocal.x, resultLocal.x, EPSILON);
            assertEquals(expectedLocal.y, resultLocal.y, EPSILON);
            assertEquals(expectedLocal.z, resultLocal.z, EPSILON);
        }
    }

    private static double size(AABB box, Direction.Axis axis) {
        return switch (axis) {
            case X -> box.getXsize();
            case Y -> box.getYsize();
            case Z -> box.getZsize();
        };
    }
}
