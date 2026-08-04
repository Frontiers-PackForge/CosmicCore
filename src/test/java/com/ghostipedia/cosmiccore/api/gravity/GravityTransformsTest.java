package com.ghostipedia.cosmiccore.api.gravity;

import com.ghostipedia.cosmiccore.client.gravity.CardinalGravityRotation;
import com.ghostipedia.cosmiccore.common.gravity.DirectedGravityKernel;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GravityTransformsTest {

    private static final double EPSILON = 1.0E-12;

    @Test
    void localDownMapsToEveryGravityDirection() {
        Vec3 localDown = new Vec3(0.0, -1.0, 0.0);
        for (Direction down : Direction.values()) {
            Vec3 worldDown = GravityTransforms.localToWorld(localDown, down);
            assertVectorEquals(Vec3.atLowerCornerOf(down.getNormal()), worldDown);
        }
    }

    @Test
    void vectorsRoundTripForEveryGravityDirection() {
        Vec3[] vectors = {
                new Vec3(0.0, 0.0, 0.0),
                new Vec3(1.25, -3.5, 8.75),
                new Vec3(-17.0, 0.125, 2.0)
        };
        for (Direction down : Direction.values()) {
            for (Vec3 vector : vectors) {
                Vec3 local = GravityTransforms.worldToLocal(vector, down);
                assertVectorEquals(vector, GravityTransforms.localToWorld(local, down));
                assertEquals(vector.lengthSqr(), local.lengthSqr(), EPSILON);
            }
        }
    }

    @Test
    void directionsRoundTripForEveryGravityDirection() {
        for (Direction down : Direction.values()) {
            for (Direction direction : Direction.values()) {
                Direction local = GravityTransforms.worldToLocal(direction, down);
                assertEquals(direction, GravityTransforms.localToWorld(local, down));
            }
        }
    }

    @Test
    void boxesRoundTripAroundArbitraryPivot() {
        AABB box = new AABB(-2.25, 4.5, 7.75, 1.0, 6.25, 11.5);
        Vec3 pivot = new Vec3(0.75, -3.0, 9.25);
        for (Direction down : Direction.values()) {
            AABB local = GravityTransforms.worldToLocal(box, down, pivot);
            assertBoxEquals(box, GravityTransforms.localToWorld(local, down, pivot));
        }
    }

    @Test
    void basisRemainsRightHandedForEveryGravityDirection() {
        Vec3 localX = new Vec3(1.0, 0.0, 0.0);
        Vec3 localY = new Vec3(0.0, 1.0, 0.0);
        Vec3 localZ = new Vec3(0.0, 0.0, 1.0);
        for (Direction down : Direction.values()) {
            Vec3 worldX = GravityTransforms.localToWorld(localX, down);
            Vec3 worldY = GravityTransforms.localToWorld(localY, down);
            Vec3 worldZ = GravityTransforms.localToWorld(localZ, down);
            assertVectorEquals(worldZ, worldX.cross(worldY));
        }
    }

    @Test
    void clientQuaternionsMatchTheAuthoritativeBasis() {
        Vec3[] axes = {
                new Vec3(1.0, 0.0, 0.0),
                new Vec3(0.0, 1.0, 0.0),
                new Vec3(0.0, 0.0, 1.0)
        };
        for (Direction down : Direction.values()) {
            for (Vec3 axis : axes) {
                Vec3 expected = GravityTransforms.localToWorld(axis, down);
                Vec3 actual = CardinalGravityRotation.rotate(CardinalGravityRotation.forDown(down), axis);
                assertEquals(expected.x, actual.x, 1.0E-6);
                assertEquals(expected.y, actual.y, 1.0E-6);
                assertEquals(expected.z, actual.z, 1.0E-6);
            }
        }
    }

    @Test
    void remappedLookPreservesWorldForwardAcrossEveryFramePair() {
        List<GravityFrame> frames = new ArrayList<>();
        frames.add(GravityFrame.NORMAL);
        for (Direction down : Direction.values()) {
            frames.add(new GravityFrame(
                    GravityMode.DIRECTED,
                    down,
                    1.0,
                    ResourceLocation.fromNamespaceAndPath("cosmiccore", "test/" + down.getSerializedName()),
                    0,
                    20,
                    0.0,
                    0L));
        }
        float[][] rotations = {
                { 0.0F, 0.0F },
                { 35.0F, -42.0F },
                { -167.0F, 73.0F },
                { 91.0F, 89.999F },
                { -91.0F, -90.0F }
        };
        for (GravityFrame source : frames) {
            for (GravityFrame target : frames) {
                for (float[] rotation : rotations) {
                    DirectedGravityKernel.LookRotation remapped = DirectedGravityKernel.remapLook(
                            source, target, rotation[0], rotation[1]);
                    Vec3 expected = GravityTransforms.localToWorld(
                            localView(rotation[0], rotation[1]), orientationDown(source));
                    Vec3 actual = GravityTransforms.localToWorld(
                            localView(remapped.yaw(), remapped.pitch()), orientationDown(target));
                    assertEquals(expected.x, actual.x, 1.0E-6);
                    assertEquals(expected.y, actual.y, 1.0E-6);
                    assertEquals(expected.z, actual.z, 1.0E-6);
                }
            }
        }
    }

    private static Vec3 localView(float yaw, float pitch) {
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

    private static void assertVectorEquals(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }

    private static void assertBoxEquals(AABB expected, AABB actual) {
        assertEquals(expected.minX, actual.minX, EPSILON);
        assertEquals(expected.minY, actual.minY, EPSILON);
        assertEquals(expected.minZ, actual.minZ, EPSILON);
        assertEquals(expected.maxX, actual.maxX, EPSILON);
        assertEquals(expected.maxY, actual.maxY, EPSILON);
        assertEquals(expected.maxZ, actual.maxZ, EPSILON);
    }
}
