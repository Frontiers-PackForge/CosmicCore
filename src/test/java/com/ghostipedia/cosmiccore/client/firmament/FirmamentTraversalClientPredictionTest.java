package com.ghostipedia.cosmiccore.client.firmament;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FirmamentTraversalClientPredictionTest {

    @Test
    void normalizesPositiveEquivalentRollToTheShortestExitArc() {
        assertEquals(-35.0F, FirmamentTraversalClientPrediction.shortestExitRoll(325.0F, 0.0F));
    }

    @Test
    void normalizesNegativeEquivalentRollToTheShortestExitArc() {
        assertEquals(35.0F, FirmamentTraversalClientPrediction.shortestExitRoll(-325.0F, 0.0F));
    }

    @Test
    void measuresTheResidualRelativeToAnExistingBaseRoll() {
        assertEquals(-30.0F, FirmamentTraversalClientPrediction.shortestExitRoll(342.0F, 12.0F));
    }

    @Test
    void preservesAnAlreadyShortExitArc() {
        assertEquals(42.0F, FirmamentTraversalClientPrediction.shortestExitRoll(50.0F, 8.0F));
    }
}
