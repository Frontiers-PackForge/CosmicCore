package com.ghostipedia.cosmiccore.client.firmament;

import net.minecraft.core.Direction;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirmamentTwilightLightingTest {

    @Test
    void shadesTerrainFromTheFixedEasternSun() {
        float east = FirmamentTwilightLighting.shade(Direction.EAST, true);
        float north = FirmamentTwilightLighting.shade(Direction.NORTH, true);
        float west = FirmamentTwilightLighting.shade(Direction.WEST, true);
        float up = FirmamentTwilightLighting.shade(Direction.UP, true);
        float down = FirmamentTwilightLighting.shade(Direction.DOWN, true);

        assertTrue(east > up);
        assertTrue(up > north);
        assertTrue(north > west);
        assertTrue(west > down);
    }

    @Test
    void leavesUnshadedGeometryAtFullBrightness() {
        assertEquals(1.0f, FirmamentTwilightLighting.shade(Direction.WEST, false));
        assertEquals(1.0f, FirmamentTwilightLighting.shade(0.25f, 0.75f, -0.5f, false));
    }

    @Test
    void givesOccludedTerrainAWarmReadableFloor() {
        Vector3f colors = new Vector3f(0.01f, 0.01f, 0.01f);

        FirmamentTwilightLighting.adjustLightmap(colors, 0.0f, 0, 0);

        assertTrue(colors.x > 0.14f);
        assertTrue(colors.y > 0.08f);
        assertTrue(colors.z > 0.12f);
        assertTrue(colors.x > colors.y);
        assertTrue(colors.z > colors.y);
    }

    @Test
    void doesNotCrushBrightBlockLight() {
        Vector3f colors = new Vector3f(1.0f, 0.82f, 0.58f);

        FirmamentTwilightLighting.adjustLightmap(colors, 0.0f, 15, 0);

        assertTrue(colors.x > 0.9f);
        assertTrue(colors.y > 0.7f);
        assertTrue(colors.z > 0.5f);
    }
}
