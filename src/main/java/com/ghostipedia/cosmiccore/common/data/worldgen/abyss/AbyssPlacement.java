package com.ghostipedia.cosmiccore.common.data.worldgen.abyss;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import java.util.ArrayList;
import java.util.List;

public final class AbyssPlacement {

    private AbyssPlacement() {}

    public static final double LANDMASS_SPACING = 175.0;
    public static final double STAIRWELL_SWAY = 57.0;
    public static final int BAND_TOP = -95;
    public static final int BAND_BOTTOM = -780;
    public static final double STEP_DROP = 75.0;
    public static final double ISLAND_SIZE = 40.0;
    public static final double ISLAND_FLATTEN = 0.62;
    public static final int BOULDERS_PER_ISLAND = 9;
    public static final double BOULDER_SIZE = 8.0;
    public static final double BOULDER_SPREAD = 42.0;

    public static final double SPARSE_SPACING = 26.0;
    public static final double SPARSE_SIZE = 12.0;
    public static final double SPARSE_AMOUNT = 0.45;

    private static final double TAU = Math.PI * 2.0;
    private static final double MIN_DISTANCE = LANDMASS_SPACING * 0.92;
    private static final long ANCHOR_SALT = 0x9E37F1DCA7E5B900L;
    private static final long LAYOUT_SALT = 0xC2B2AE3D27D4EB4FL;
    private static final long SPARSE_SALT = 0x5BD1A2C3D4E5F601L;

    public record Member(double x, double y, double z, double size, double squash, boolean island, boolean sparse) {

        public double reach() {
            return size * 1.9;
        }

        public double yReach() {
            return size * squash * 1.9;
        }
    }

    public static int shelfCount() {
        return Math.max(1, (int) Math.floor((double) (BAND_TOP - BAND_BOTTOM) / STEP_DROP));
    }

    public static int maxHorizontalReach() {
        double maxStagger = STAIRWELL_SWAY * (0.5 + (shelfCount() - 1) * 0.10 + 0.6);
        double maxIsland = ISLAND_SIZE * 1.27 * 1.9;
        double maxBoulder = BOULDER_SPREAD + BOULDER_SIZE * 1.45 * 1.9;
        return (int) Math.ceil(maxStagger + Math.max(maxIsland, maxBoulder));
    }

    public static List<Member> membersNear(long seed, ResourceKey<Level> dim, int centerX, int centerZ) {
        List<Member> out = new ArrayList<>();
        int landRadius = maxHorizontalReach() + 16;
        for (long[] core : coresNear(seed, dim, centerX, centerZ, landRadius)) {
            layout(seed, dim, (int) core[0], (int) core[1], out);
        }
        int sparseRadius = (int) Math.ceil(SPARSE_SIZE * 1.45 * 1.9) + 16;
        sparseNear(seed, dim, centerX, centerZ, sparseRadius, out);
        return out;
    }

    private static void layout(long seed, ResourceKey<Level> dim, int coreX, int coreZ, List<Member> out) {
        RandomSource r = new XoroshiroRandomSource(mix(seed, LAYOUT_SALT ^ dimensionSalt(dim), coreX, coreZ));
        int shelves = shelfCount();
        double ang = r.nextDouble() * TAU;
        for (int s = 0; s < shelves; s++) {
            ang += 0.7 + (r.nextDouble() - 0.5) * 0.7;
            double rad = STAIRWELL_SWAY * (0.5 + s * 0.10 + r.nextDouble() * 0.6);
            double sx = coreX + Math.cos(ang) * rad;
            double sz = coreZ + Math.sin(ang) * rad;
            double sy = BAND_TOP - s * STEP_DROP + (r.nextDouble() - 0.5) * STEP_DROP * 0.35;
            if (sy < BAND_BOTTOM) break;
            double size = ISLAND_SIZE * (0.72 + r.nextDouble() * 0.55);
            out.add(new Member(sx, sy, sz, size, ISLAND_FLATTEN, true, false));
            for (int j = 0; j < BOULDERS_PER_ISLAND; j++) {
                double a = r.nextDouble() * TAU;
                double d = r.nextDouble() * BOULDER_SPREAD;
                double bx = sx + Math.cos(a) * d;
                double bz = sz + Math.sin(a) * d;
                double by = sy + (r.nextDouble() - 0.5) * BOULDER_SPREAD * 0.85;
                double bsize = BOULDER_SIZE * (0.6 + r.nextDouble() * 0.85);
                double bsq = 0.8 + r.nextDouble() * 0.45;
                out.add(new Member(bx, by, bz, bsize, bsq, false, false));
            }
        }
    }

    private static void sparseNear(long seed, ResourceKey<Level> dim, int centerX, int centerZ, int radius,
                                   List<Member> out) {
        int cell = (int) SPARSE_SPACING;
        int margin = radius + cell;
        int minCellX = Math.floorDiv(centerX - margin, cell);
        int maxCellX = Math.floorDiv(centerX + margin, cell);
        int minCellZ = Math.floorDiv(centerZ - margin, cell);
        int maxCellZ = Math.floorDiv(centerZ + margin, cell);
        long radiusSqr = (long) radius * radius;
        for (int cx = minCellX; cx <= maxCellX; cx++) {
            for (int cz = minCellZ; cz <= maxCellZ; cz++) {
                RandomSource r = new XoroshiroRandomSource(mix(seed, SPARSE_SALT ^ dimensionSalt(dim), cx, cz));
                if (r.nextDouble() > SPARSE_AMOUNT) continue;
                double bx = (double) cx * cell + r.nextDouble() * cell;
                double bz = (double) cz * cell + r.nextDouble() * cell;
                double dx = bx - centerX;
                double dz = bz - centerZ;
                if (dx * dx + dz * dz > radiusSqr) continue;
                double by = BAND_BOTTOM + r.nextDouble() * (BAND_TOP - BAND_BOTTOM);
                double size = SPARSE_SIZE * (0.5 + r.nextDouble() * 0.9);
                double squash = 0.7 + r.nextDouble() * 0.5;
                out.add(new Member(bx, by, bz, size, squash, false, true));
            }
        }
    }

    private static List<long[]> coresNear(long seed, ResourceKey<Level> dim, int centerX, int centerZ, int radius) {
        List<long[]> out = new ArrayList<>();
        int cell = (int) LANDMASS_SPACING;
        int margin = radius + cell;
        int minCellX = Math.floorDiv(centerX - margin, cell);
        int maxCellX = Math.floorDiv(centerX + margin, cell);
        int minCellZ = Math.floorDiv(centerZ - margin, cell);
        int maxCellZ = Math.floorDiv(centerZ + margin, cell);
        long radiusSqr = (long) radius * radius;
        for (int cx = minCellX; cx <= maxCellX; cx++) {
            for (int cz = minCellZ; cz <= maxCellZ; cz++) {
                long[] core = survivingCore(seed, dim, cx, cz);
                if (core == null) continue;
                long dx = core[0] - centerX;
                long dz = core[1] - centerZ;
                if (dx * dx + dz * dz > radiusSqr) continue;
                out.add(core);
            }
        }
        return out;
    }

    private static long[] candidate(long seed, ResourceKey<Level> dim, int cx, int cz) {
        RandomSource r = new XoroshiroRandomSource(mix(seed, ANCHOR_SALT ^ dimensionSalt(dim), cx, cz));
        int cell = (int) LANDMASS_SPACING;
        long x = (long) cx * cell + r.nextInt(cell);
        long z = (long) cz * cell + r.nextInt(cell);
        long pri = r.nextLong();
        return new long[] { x, z, pri };
    }

    private static long[] survivingCore(long seed, ResourceKey<Level> dim, int cx, int cz) {
        long[] self = candidate(seed, dim, cx, cz);
        long minSqr = (long) (MIN_DISTANCE * MIN_DISTANCE);
        for (int nx = cx - 1; nx <= cx + 1; nx++) {
            for (int nz = cz - 1; nz <= cz + 1; nz++) {
                if (nx == cx && nz == cz) continue;
                long[] o = candidate(seed, dim, nx, nz);
                if (o[2] <= self[2]) continue;
                long dx = o[0] - self[0];
                long dz = o[1] - self[1];
                if (dx * dx + dz * dz < minSqr) return null;
            }
        }
        return self;
    }

    private static long dimensionSalt(ResourceKey<Level> dim) {
        return (long) dim.location().toString().hashCode() * 0x9E3779B97F4A7C15L;
    }

    private static long mix(long seed, long salt, long a, long b) {
        long h = seed * 0x9E3779B97F4A7C15L + salt;
        h ^= a * 0xC2B2AE3D27D4EB4FL;
        h = Long.rotateLeft(h, 31);
        h ^= b * 0x165667B19E3779F9L;
        h = (h ^ (h >>> 33)) * 0xFF51AFD7ED558CCDL;
        h = (h ^ (h >>> 33)) * 0xC4CEB9FE1A85EC53L;
        return h ^ (h >>> 33);
    }
}
