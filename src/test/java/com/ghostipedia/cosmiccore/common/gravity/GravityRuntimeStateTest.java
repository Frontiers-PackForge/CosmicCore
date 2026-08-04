package com.ghostipedia.cosmiccore.common.gravity;

import com.ghostipedia.cosmiccore.api.gravity.GravityFrame;
import com.ghostipedia.cosmiccore.api.gravity.GravityMode;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravityRuntimeStateTest {

    @Test
    void presentationStateRemainsLatchedUntilTheFrameIsApplied() {
        GravityRuntimeState runtime = new GravityRuntimeState();
        GravityFrame east = new GravityFrame(
                GravityMode.DIRECTED,
                Direction.EAST,
                1.0,
                ResourceLocation.fromNamespaceAndPath("cosmiccore", "test/east"),
                0,
                14,
                0.0,
                3L);
        runtime.markDimensionsApplied(east, true);
        runtime.setTargetFrame(GravityFrame.NORMAL);

        assertTrue(runtime.directedActive());
        assertEquals(GravityMode.DIRECTED, runtime.appliedMode());
        assertEquals(Direction.EAST, runtime.appliedDown());
        assertEquals(14, runtime.appliedTransitionTicks());

        runtime.markDimensionsApplied(GravityFrame.NORMAL, false);

        assertFalse(runtime.directedActive());
        assertEquals(GravityMode.NORMAL, runtime.appliedMode());
        assertEquals(Direction.DOWN, runtime.appliedDown());
        assertEquals(0, runtime.appliedTransitionTicks());
    }
}
