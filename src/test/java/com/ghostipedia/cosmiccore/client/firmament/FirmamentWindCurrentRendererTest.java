package com.ghostipedia.cosmiccore.client.firmament;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirmamentWindCurrentRendererTest {

    @Test
    void keepsSubtickPrecisionInsideTheBoundedAnimationClock() {
        assertEquals(12.025F, FirmamentWindCurrentRenderer.currentTime(240L, 0.5F), 1.0E-6F);
    }

    @Test
    void repeatsAtTheShaderAnimationPeriod() {
        assertEquals(
                FirmamentWindCurrentRenderer.currentTime(0L, 0.25F),
                FirmamentWindCurrentRenderer.currentTime(1_280L, 0.25F),
                1.0E-6F);
        assertEquals(
                FirmamentWindCurrentRenderer.currentTime(1_279L, 0.75F),
                FirmamentWindCurrentRenderer.currentTime(2_559L, 0.75F),
                1.0E-6F);
    }

    @Test
    void separatesQuietReadableAndHeroStormTiers() {
        assertEquals(0.0F, FirmamentWindCurrentRenderer.stormProminence(0.30F));
        float quiet = FirmamentWindCurrentRenderer.stormProminence(0.60F);
        float readable = FirmamentWindCurrentRenderer.stormProminence(0.86F);
        float hero = FirmamentWindCurrentRenderer.stormProminence(0.98F);
        assertTrue(quiet >= 0.18F && quiet <= 0.34F);
        assertTrue(readable >= 0.52F && readable <= 0.72F);
        assertTrue(hero >= 0.88F && hero <= 1.0F);
        assertTrue(quiet < readable && readable < hero);
    }
}
