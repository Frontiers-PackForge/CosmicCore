package com.ghostipedia.cosmiccore.api.data;

import net.minecraft.core.Direction;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DebugBlockPatternOrientationTest {

    @Test
    void xAndYRotationsReachAllCubeOrientations() {
        Set<DebugBlockPattern.StructureOrientation> pending = new HashSet<>();
        Set<DebugBlockPattern.StructureOrientation> reached = new HashSet<>();
        pending.add(DebugBlockPattern.orientationFor(Direction.WEST));

        while (!pending.isEmpty()) {
            DebugBlockPattern.StructureOrientation orientation = pending.iterator().next();
            pending.remove(orientation);
            if (!reached.add(orientation)) continue;
            pending.add(orientation.rotate(Direction.Axis.X));
            pending.add(orientation.rotate(Direction.Axis.Y));
        }

        assertEquals(24, reached.size());
        for (DebugBlockPattern.StructureOrientation orientation : reached) {
            assertNotNull(DebugBlockPattern.exportOrientationFor(orientation));
        }
    }

    @Test
    void fourQuarterTurnsRestoreTheOriginalOrientation() {
        DebugBlockPattern.StructureOrientation initial = DebugBlockPattern.orientationFor(Direction.WEST);
        for (Direction.Axis axis : new Direction.Axis[] { Direction.Axis.X, Direction.Axis.Y }) {
            DebugBlockPattern.StructureOrientation rotated = initial;
            for (int i = 0; i < 4; i++) {
                rotated = rotated.rotate(axis);
            }
            assertEquals(initial, rotated);
        }
    }
}
