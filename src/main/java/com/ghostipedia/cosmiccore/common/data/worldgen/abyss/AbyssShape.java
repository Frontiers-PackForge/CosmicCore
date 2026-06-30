package com.ghostipedia.cosmiccore.common.data.worldgen.abyss;

import java.util.List;

public final class AbyssShape {

    private AbyssShape() {}

    public static final double ROUGHNESS = 0.4;
    public static final double ROUGHNESS_SCALE = 0.08;
    public static final double BLEED = 19.0;

    public static double density(long seed, double x, double y, double z, List<AbyssPlacement.Member> members) {
        double d = Double.POSITIVE_INFINITY;
        for (int i = 0; i < members.size(); i++) {
            AbyssPlacement.Member m = members.get(i);
            double r = m.reach();
            if (x < m.x() - r || x > m.x() + r) continue;
            if (z < m.z() - r || z > m.z() + r) continue;
            double yr = m.yReach() + ROUGHNESS * m.size();
            if (y < m.y() - yr || y > m.y() + yr) continue;
            double nx = (x - m.x()) / m.size();
            double ny = (y - m.y()) / (m.size() * m.squash());
            double nz = (z - m.z()) / m.size();
            double base = Math.sqrt(nx * nx + ny * ny + nz * nz) - 1.0;
            double disp = ROUGHNESS *
                    (noise3(seed, x * ROUGHNESS_SCALE, y * ROUGHNESS_SCALE, z * ROUGHNESS_SCALE) * 2.0 - 1.0);
            double sd = (base - disp) * m.size();
            d = smin(d, sd, BLEED);
        }
        return d;
    }

    public static double smin(double a, double b, double k) {
        if (k <= 0) return Math.min(a, b);
        double h = Math.max(k - Math.abs(a - b), 0.0) / k;
        return Math.min(a, b) - h * h * k * 0.25;
    }

    public static double noise2(long seed, double x, double z) {
        return noise3(seed, x, 11.7, z);
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double vh(long seed, int i, int j, int k) {
        long h = seed * 0x9E3779B97F4A7C15L;
        h ^= (long) i * 0xC2B2AE3D27D4EB4FL;
        h ^= (long) j * 0x165667B19E3779F9L;
        h ^= (long) k * 0xFF51AFD7ED558CCDL;
        h = (h ^ (h >>> 31)) * 0x94D049BB133111EBL;
        h ^= (h >>> 29);
        return (h >>> 11) * 0x1.0p-53;
    }

    public static double noise3(long seed, double x, double y, double z) {
        int xi = (int) Math.floor(x), yi = (int) Math.floor(y), zi = (int) Math.floor(z);
        double xf = x - xi, yf = y - yi, zf = z - zi;
        double u = fade(xf), v = fade(yf), w = fade(zf);
        double c000 = vh(seed, xi, yi, zi), c100 = vh(seed, xi + 1, yi, zi),
                c010 = vh(seed, xi, yi + 1, zi), c110 = vh(seed, xi + 1, yi + 1, zi),
                c001 = vh(seed, xi, yi, zi + 1), c101 = vh(seed, xi + 1, yi, zi + 1),
                c011 = vh(seed, xi, yi + 1, zi + 1), c111 = vh(seed, xi + 1, yi + 1, zi + 1);
        double x00 = lerp(c000, c100, u), x10 = lerp(c010, c110, u),
                x01 = lerp(c001, c101, u), x11 = lerp(c011, c111, u);
        return lerp(lerp(x00, x10, v), lerp(x01, x11, v), w);
    }
}
