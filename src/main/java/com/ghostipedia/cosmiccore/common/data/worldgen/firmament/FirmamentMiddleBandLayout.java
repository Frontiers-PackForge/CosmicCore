package com.ghostipedia.cosmiccore.common.data.worldgen.firmament;

import net.minecraft.util.Mth;

public final class FirmamentMiddleBandLayout {

    private static final long BRIDGE_SALT = 0x5748C3A8D291E60DL;
    private static final long WIND_LAYOUT_SEED = 0x243F6A8885A308D3L;
    private static final long WIND_ANGLE_SALT = 0x6A09E667F3BCC909L;
    private static final long WIND_WARP_SALT = 0xBB67AE8584CAA73BL;
    private static final double WIND_SCALE = 1.0 / 448.0;
    private static final double WIND_WARP_SCALE = 1.0 / 224.0;
    private static final double WIND_BAND_SCALE = 1.0 / 92.0;
    private static final double WIND_MIN_Y = 96.0;
    private static final double WIND_FULL_MIN_Y = 112.0;
    private static final double WIND_FULL_MAX_Y = 136.0;
    private static final double WIND_MAX_Y = 152.0;

    private FirmamentMiddleBandLayout() {}

    public static boolean isBridgeChunk(long seed, int chunkX, int chunkZ) {
        double priority = unitHash(seed ^ BRIDGE_SALT, chunkX, chunkZ);
        for (int offsetX = -2; offsetX <= 2; offsetX++) {
            for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
                if (offsetX == 0 && offsetZ == 0) continue;
                if (unitHash(seed ^ BRIDGE_SALT, chunkX + offsetX, chunkZ + offsetZ) <= priority) return false;
            }
        }
        return true;
    }

    public static WindCorridor sampleWind(double blockX, double blockZ) {
        double regional = valueNoise(WIND_LAYOUT_SEED ^ WIND_ANGLE_SALT, blockX * WIND_SCALE,
                blockZ * WIND_SCALE);
        double angle = 0.38 + regional * 0.46;
        double normalX = Math.cos(angle);
        double normalZ = Math.sin(angle);
        double warp = valueNoise(WIND_LAYOUT_SEED ^ WIND_WARP_SALT, blockX * WIND_WARP_SCALE,
                blockZ * WIND_WARP_SCALE);
        double phase = (blockX * normalX + blockZ * normalZ) * WIND_BAND_SCALE + warp * 1.7;
        double distance = Math.abs(Math.sin(phase));
        double strength = Mth.clamp((0.24 - distance) / 0.24, 0.0, 1.0);
        return new WindCorridor(strength, -normalZ, normalX);
    }

    public static WindCorridor sampleWind(double blockX, double blockY, double blockZ) {
        WindCorridor horizontal = sampleWind(blockX, blockZ);
        double lowerEnvelope = Mth.clamp((blockY - WIND_MIN_Y) / (WIND_FULL_MIN_Y - WIND_MIN_Y), 0.0, 1.0);
        double upperEnvelope = Mth.clamp((WIND_MAX_Y - blockY) / (WIND_MAX_Y - WIND_FULL_MAX_Y), 0.0, 1.0);
        return new WindCorridor(horizontal.strength() * Math.min(lowerEnvelope, upperEnvelope),
                horizontal.directionX(), horizontal.directionZ());
    }

    public static long mix(long seed, int x, int z) {
        long mixed = seed ^ ((long) x * 0x9E3779B97F4A7C15L) ^ ((long) z * 0xC2B2AE3D27D4EB4FL);
        mixed ^= mixed >>> 30;
        mixed *= 0xBF58476D1CE4E5B9L;
        mixed ^= mixed >>> 27;
        mixed *= 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }

    private static double valueNoise(long seed, double x, double z) {
        int floorX = Mth.floor(x);
        int floorZ = Mth.floor(z);
        double fractionX = smooth(x - floorX);
        double fractionZ = smooth(z - floorZ);
        double x0 = Mth.lerp(fractionX, signedHash(seed, floorX, floorZ), signedHash(seed, floorX + 1, floorZ));
        double x1 = Mth.lerp(fractionX, signedHash(seed, floorX, floorZ + 1),
                signedHash(seed, floorX + 1, floorZ + 1));
        return Mth.lerp(fractionZ, x0, x1);
    }

    private static double smooth(double value) {
        return value * value * (3.0 - 2.0 * value);
    }

    private static double signedHash(long seed, int x, int z) {
        return unitHash(seed, x, z) * 2.0 - 1.0;
    }

    private static double unitHash(long seed, int x, int z) {
        return (mix(seed, x, z) >>> 11) * 0x1.0p-53;
    }

    public record WindCorridor(double strength, double directionX, double directionZ) {}
}
