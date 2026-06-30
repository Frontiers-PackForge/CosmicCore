package com.ghostipedia.cosmiccore.common.data.worldgen.abyss;

public final class AbyssRegions {

    private AbyssRegions() {}

    public static final int ZONES = 4;
    public static final double ZONE_SCALE = 0.007;
    public static final int[] LAYER_EDGES = { -200, -400, -600, -800 };

    private static final double BLEND_SCALE = 0.07;
    private static final int BLEND_AMOUNT = 13;
    private static final long BLEND_SALT = 0x9E3779B97F4A7C15L;

    public static int zone(long seed, double x, double z) {
        double n = AbyssShape.noise2(seed, x * ZONE_SCALE, z * ZONE_SCALE);
        int zoneIndex = (int) Math.floor(n * ZONES);
        if (zoneIndex < 0) zoneIndex = 0;
        if (zoneIndex >= ZONES) zoneIndex = ZONES - 1;
        return zoneIndex;
    }

    public static int layer(int y) {
        int l = 0;
        for (int edge : LAYER_EDGES) {
            if (y < edge) l++;
        }
        return l;
    }

    public static int layerBlended(long seed, int x, int y, int z) {
        double n = AbyssShape.noise3(seed + BLEND_SALT, x * BLEND_SCALE, y * BLEND_SCALE, z * BLEND_SCALE);
        int jitter = (int) Math.round((n - 0.5) * 2.0 * BLEND_AMOUNT);
        return layer(y + jitter);
    }

    public static int layerCount() {
        return LAYER_EDGES.length + 1;
    }
}
