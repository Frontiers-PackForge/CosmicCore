package com.ghostipedia.cosmiccore.common.data.worldgen.abyss;

public final class AbyssRegions {

    private AbyssRegions() {}

    public static final int ZONES = 4;
    public static final double ZONE_SCALE = 0.007;
    public static final int[] LAYER_EDGES = { -200, -400, -600, -800 };

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

    public static int layerCount() {
        return LAYER_EDGES.length + 1;
    }
}
