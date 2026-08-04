package com.ghostipedia.cosmiccore.client.gravity;

import net.minecraft.world.phys.Vec3;

import com.spacegravity.spacegravity.ZeroGravityOrientation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FreeDriftPresentationAnglesTest {

    private static final double EPSILON = 2.0E-4;
    private static final float ANGLE_EPSILON = 0.02F;

    @Test
    void preservesAContinuousEulerBranchAcrossTheVerticalPole() {
        var state = new FreeDriftPresentationAngles.ContinuityState();
        float previousPitch = Float.POSITIVE_INFINITY;
        float previousYaw = 35.0F;
        float previousRoll = 25.0F;

        for (float pitch : new float[] { -88.0F, -89.0F, -90.0F, -91.0F, -92.0F }) {
            ZeroGravityOrientation.OrientationData orientation = orientation(35.0F, pitch, 25.0F);
            ZeroGravityOrientation.CameraAngles original = ZeroGravityOrientation.toCameraAngles(orientation);
            ZeroGravityOrientation.CameraAngles stabilized = FreeDriftPresentationAngles.stabilize(orientation,
                    original, state);

            assertTrue(stabilized.pitch() < previousPitch);
            assertEquals(previousYaw, stabilized.yaw(), ANGLE_EPSILON);
            assertEquals(previousRoll, stabilized.roll(), ANGLE_EPSILON);
            assertOrientationEquals(orientation, orientation(
                    stabilized.yaw(), stabilized.pitch(), stabilized.roll()));
            previousPitch = stabilized.pitch();
            previousYaw = stabilized.yaw();
            previousRoll = stabilized.roll();
        }
    }

    @Test
    void preservesAContinuousEulerBranchAcrossTheOppositePole() {
        var state = new FreeDriftPresentationAngles.ContinuityState();
        float previousPitch = Float.NEGATIVE_INFINITY;

        for (float pitch : new float[] { 88.0F, 89.0F, 90.0F, 91.0F, 92.0F }) {
            ZeroGravityOrientation.OrientationData orientation = orientation(-63.0F, pitch, -41.0F);
            ZeroGravityOrientation.CameraAngles stabilized = FreeDriftPresentationAngles.stabilize(
                    orientation, ZeroGravityOrientation.toCameraAngles(orientation), state);

            assertTrue(stabilized.pitch() > previousPitch);
            assertEquals(-63.0F, stabilized.yaw(), ANGLE_EPSILON);
            assertEquals(-41.0F, stabilized.roll(), ANGLE_EPSILON);
            assertOrientationEquals(orientation, orientation(
                    stabilized.yaw(), stabilized.pitch(), stabilized.roll()));
            previousPitch = stabilized.pitch();
        }
    }

    @Test
    void rejectsAnUnstableYawSampleAtTheExactPole() {
        var state = new FreeDriftPresentationAngles.ContinuityState();
        ZeroGravityOrientation.OrientationData before = orientation(42.0F, -89.8F, 17.0F);
        FreeDriftPresentationAngles.stabilize(before, ZeroGravityOrientation.toCameraAngles(before), state);

        ZeroGravityOrientation.OrientationData pole = orientation(42.0F, -90.0F, 17.0F);
        ZeroGravityOrientation.CameraAngles stabilized = FreeDriftPresentationAngles.stabilize(
                pole, new ZeroGravityOrientation.CameraAngles(-137.0F, -90.0F, -162.0F), state);

        assertEquals(42.0F, stabilized.yaw(), ANGLE_EPSILON);
        assertEquals(-90.0F, stabilized.pitch(), ANGLE_EPSILON);
        assertEquals(17.0F, stabilized.roll(), ANGLE_EPSILON);
        assertOrientationEquals(pole, orientation(stabilized.yaw(), stabilized.pitch(), stabilized.roll()));
    }

    @Test
    void exitsPoleHysteresisWithoutChangingEulerBranches() {
        var state = new FreeDriftPresentationAngles.ContinuityState();

        for (float pitch : new float[] { -89.8F, -90.0F, -90.2F, -91.0F }) {
            ZeroGravityOrientation.OrientationData orientation = orientation(-28.0F, pitch, -33.0F);
            ZeroGravityOrientation.CameraAngles stabilized = FreeDriftPresentationAngles.stabilize(
                    orientation, ZeroGravityOrientation.toCameraAngles(orientation), state);

            assertEquals(-28.0F, stabilized.yaw(), ANGLE_EPSILON);
            assertEquals(pitch, stabilized.pitch(), ANGLE_EPSILON);
            assertEquals(-33.0F, stabilized.roll(), ANGLE_EPSILON);
            assertOrientationEquals(orientation, orientation(
                    stabilized.yaw(), stabilized.pitch(), stabilized.roll()));
        }
    }

    private static ZeroGravityOrientation.OrientationData orientation(float yaw, float pitch, float roll) {
        Vec3 forward = Vec3.directionFromRotation(pitch, yaw).normalize();
        Vec3 horizontalForward = Vec3.directionFromRotation(0.0F, yaw).normalize();
        Vec3 right = new Vec3(0.0, 1.0, 0.0).cross(horizontalForward).normalize();
        Vec3 baseUp = forward.cross(right).normalize();
        Vec3 up = rotate(baseUp, forward, Math.toRadians(roll)).normalize();
        return new ZeroGravityOrientation.OrientationData(forward, up);
    }

    private static Vec3 rotate(Vec3 vector, Vec3 axis, double angle) {
        double cosine = Math.cos(angle);
        double sine = Math.sin(angle);
        return vector.scale(cosine)
                .add(axis.cross(vector).scale(sine))
                .add(axis.scale(axis.dot(vector) * (1.0 - cosine)));
    }

    private static void assertOrientationEquals(
                                                ZeroGravityOrientation.OrientationData expected,
                                                ZeroGravityOrientation.OrientationData actual) {
        assertTrue(
                expected.forward().distanceTo(actual.forward()) <= EPSILON,
                () -> expected.forward() + " != " + actual.forward());
        assertTrue(expected.up().distanceTo(actual.up()) <= EPSILON, () -> expected.up() + " != " + actual.up());
    }
}
