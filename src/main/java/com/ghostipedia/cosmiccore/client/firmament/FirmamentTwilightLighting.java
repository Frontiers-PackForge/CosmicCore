package com.ghostipedia.cosmiccore.client.firmament;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import org.joml.Vector3f;

public final class FirmamentTwilightLighting {

    public static final float SOLAR_AZIMUTH = 0.0f;
    public static final float SOLAR_ELEVATION = 0.0f;

    private static final float SUN_NORMALIZATION = Mth.sqrt(1.0f + SOLAR_ELEVATION * SOLAR_ELEVATION);
    private static final float SUN_X = 1.0f / SUN_NORMALIZATION;
    private static final float SUN_Y = SOLAR_ELEVATION / SUN_NORMALIZATION;
    private static final float SHADOW_FLOOR_RED = 0.16f;
    private static final float SHADOW_FLOOR_GREEN = 0.105f;
    private static final float SHADOW_FLOOR_BLUE = 0.17f;

    private FirmamentTwilightLighting() {}

    public static float shade(Direction direction, boolean enabled) {
        return enabled ? shade(direction.getStepX(), direction.getStepY(), direction.getStepZ(), true) : 1.0f;
    }

    public static float shade(float normalX, float normalY, float normalZ, boolean enabled) {
        if (!enabled) return 1.0f;

        float lengthSquared = normalX * normalX + normalY * normalY + normalZ * normalZ;
        if (lengthSquared < 1.0E-6f) return 1.0f;

        float inverseLength = Mth.invSqrt(lengthSquared);
        float x = normalX * inverseLength;
        float y = normalY * inverseLength;
        float z = normalZ * inverseLength;
        float horizontal = Mth.sqrt(x * x + z * z);
        float direct = Math.max(0.0f, x * SUN_X + y * SUN_Y);
        float horizonFill = horizontal * (0.09f + (x + 1.0f) * 0.03f);
        return Mth.clamp(0.54f + horizonFill + Math.max(0.0f, y) * 0.20f + direct * 0.28f,
                0.54f, 1.0f);
    }

    public static void adjustLightmap(Vector3f colors, float skyLight, int blockLightLevel, int skyLightLevel) {
        float blockFraction = Mth.clamp(blockLightLevel / 15.0f, 0.0f, 1.0f);
        float skyFraction = Mth.clamp(skyLightLevel / 15.0f, 0.0f, 1.0f);
        float floorRetention = 1.0f - blockFraction * 0.72f;
        colors.set(
                Math.max(colors.x, SHADOW_FLOOR_RED * floorRetention),
                Math.max(colors.y, SHADOW_FLOOR_GREEN * floorRetention),
                Math.max(colors.z, SHADOW_FLOOR_BLUE * floorRetention));

        float warmMix = (0.055f + 0.105f * Math.max(skyFraction, Mth.clamp(skyLight, 0.0f, 1.0f))) *
                (1.0f - blockFraction * 0.58f);
        float luminance = colors.x * 0.30f + colors.y * 0.59f + colors.z * 0.11f;
        colors.set(
                Mth.lerp(warmMix, colors.x, luminance * 1.16f),
                Mth.lerp(warmMix, colors.y, luminance * 0.91f),
                Mth.lerp(warmMix, colors.z, luminance * 0.74f));
        colors.set(Mth.clamp(colors.x, 0.0f, 1.0f), Mth.clamp(colors.y, 0.0f, 1.0f),
                Mth.clamp(colors.z, 0.0f, 1.0f));
    }
}
