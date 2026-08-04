package com.ghostipedia.cosmiccore.common.firmament;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FirmamentTraversalStateTest {

    private static final double EPSILON = 1.0E-12;

    @Test
    void entryAndExitRatesReachTheirEndpoints() {
        assertEquals(1.0, FirmamentTraversalState.advance(0.0, 1.0, 12L), EPSILON);
        assertEquals(0.0, FirmamentTraversalState.advance(1.0, 0.0, 24L), EPSILON);
        assertEquals(0.5, FirmamentTraversalState.advance(0.0, 1.0, 6L), EPSILON);
        assertEquals(0.5, FirmamentTraversalState.advance(1.0, 0.0, 12L), EPSILON);
    }

    @Test
    void predictionIsBoundedToEightTicks() {
        FirmamentTraversalState state = new FirmamentTraversalState(
                0.0,
                1.0,
                FirmamentTraversalState.Phase.ENTERING,
                true,
                true,
                100L);
        double expected = FirmamentTraversalState.advance(0.0, 1.0, 8L);
        assertEquals(expected, state.predictedWeight(108L), EPSILON);
        assertEquals(expected, state.predictedWeight(120L), EPSILON);
        assertEquals(108L, state.predictedTick(120L));
    }

    @Test
    void smootherstepAndPhaseBoundariesAreDeterministic() {
        assertEquals(0.0, FirmamentTraversalState.smootherstep(-1.0), EPSILON);
        assertEquals(0.5, FirmamentTraversalState.smootherstep(0.5), EPSILON);
        assertEquals(1.0, FirmamentTraversalState.smootherstep(2.0), EPSILON);
        assertEquals(
                FirmamentTraversalState.Phase.ENTERING,
                FirmamentTraversalState.phase(0.25, 0.75, false));
        assertEquals(
                FirmamentTraversalState.Phase.EXITING,
                FirmamentTraversalState.phase(0.75, 0.25, false));
        assertEquals(
                FirmamentTraversalState.Phase.RELEASE,
                FirmamentTraversalState.phase(0.0, 0.0, false));
        assertEquals(
                FirmamentTraversalState.Phase.RELEASE,
                FirmamentTraversalState.phase(1.0, 1.0, true));
    }
}
