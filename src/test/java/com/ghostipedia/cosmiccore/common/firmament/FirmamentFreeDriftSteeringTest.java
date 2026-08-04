package com.ghostipedia.cosmiccore.common.firmament;

import net.minecraft.world.phys.Vec3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirmamentFreeDriftSteeringTest {

    private static final double EPSILON = 1.0E-9;

    @Test
    void preservesUncommandedDrift() {
        Vec3 shaped = FirmamentFreeDriftSteering.shapeDelta(
                new Vec3(0.7, -0.2, 0.4), Vec3.ZERO, false, 1.0);

        assertEquals(Vec3.ZERO, shaped);
    }

    @Test
    void convergesAlignedMovementTowardCruiseSpeed() {
        Vec3 shaped = FirmamentFreeDriftSteering.shapeDelta(
                new Vec3(0.0, 0.0, 0.4), new Vec3(0.0, 0.0, 1.0), false, 1.0);

        assertEquals(0.0, shaped.x, EPSILON);
        assertEquals(0.0, shaped.y, EPSILON);
        assertEquals(0.09, shaped.z, EPSILON);
    }

    @Test
    void softensTheFirstInputTick() {
        Vec3 shaped = FirmamentFreeDriftSteering.shapeDelta(
                Vec3.ZERO, new Vec3(0.0, 0.0, 1.0), false, 0.2);

        assertEquals(0.018, shaped.length(), EPSILON);
    }

    @Test
    void turnsMomentumAlongAContinuousCurve() {
        Vec3 velocity = new Vec3(0.4, 0.0, 0.0);
        Vec3 shaped = FirmamentFreeDriftSteering.shapeDelta(
                velocity, new Vec3(0.0, 0.0, 1.0), false, 1.0);
        Vec3 candidate = velocity.add(shaped);

        assertTrue(shaped.x < 0.0);
        assertTrue(shaped.z > 0.0);
        assertTrue(candidate.x > 0.0);
        assertTrue(candidate.z > 0.0);
    }

    @Test
    void boundsHighSpeedTurnCorrection() {
        Vec3 velocity = new Vec3(8.0, 0.0, 0.0);
        Vec3 shaped = FirmamentFreeDriftSteering.shapeDelta(
                velocity, new Vec3(0.0, 0.0, 1.0), false, 1.0);

        assertTrue(shaped.length() <= 0.09 + EPSILON);
    }

    @Test
    void forwardAtTheVerticalPoleUsesTheExactCameraAxis() {
        var input = new com.spacegravity.spacegravity.ZeroGravityInputState(
                1.0f,
                0.0f,
                0.0f,
                false,
                0.0f,
                1.0f,
                0.0f,
                0.0f,
                0.0f,
                -1.0f);
        Vec3 command = FirmamentFreeDriftSteering.commandFromInput(input);

        assertEquals(0.0, command.x, EPSILON);
        assertEquals(1.0, command.y, EPSILON);
        assertEquals(0.0, command.z, EPSILON);
    }

    @Test
    void sixAxisInputUsesAnOrthonormalCameraFrame() {
        var input = new com.spacegravity.spacegravity.ZeroGravityInputState(
                1.0f,
                1.0f,
                1.0f,
                false,
                0.0f,
                1.0f,
                0.0f,
                0.0f,
                0.0f,
                -1.0f);
        Vec3 command = FirmamentFreeDriftSteering.commandFromInput(input);

        assertEquals(1.0, command.length(), EPSILON);
        assertTrue(command.x > 0.0);
        assertTrue(command.y > 0.0);
        assertTrue(command.z < 0.0);
    }

    @Test
    void strafeInputPreservesCameraRelativeHandedness() {
        var leftInput = new com.spacegravity.spacegravity.ZeroGravityInputState(
                0.0f,
                1.0f,
                0.0f,
                false,
                0.0f,
                0.0f,
                1.0f,
                0.0f,
                1.0f,
                0.0f);
        var rightInput = new com.spacegravity.spacegravity.ZeroGravityInputState(
                0.0f,
                -1.0f,
                0.0f,
                false,
                0.0f,
                0.0f,
                1.0f,
                0.0f,
                1.0f,
                0.0f);

        Vec3 leftCommand = FirmamentFreeDriftSteering.commandFromInput(leftInput);
        Vec3 rightCommand = FirmamentFreeDriftSteering.commandFromInput(rightInput);

        assertEquals(1.0, leftCommand.x, EPSILON);
        assertEquals(0.0, leftCommand.y, EPSILON);
        assertEquals(0.0, leftCommand.z, EPSILON);
        assertEquals(-1.0, rightCommand.x, EPSILON);
        assertEquals(0.0, rightCommand.y, EPSILON);
        assertEquals(0.0, rightCommand.z, EPSILON);
    }
}
