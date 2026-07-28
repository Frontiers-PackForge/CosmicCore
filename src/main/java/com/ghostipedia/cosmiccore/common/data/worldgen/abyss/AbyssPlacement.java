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
    public static final int BAND_TOP = -95;
    public static final int SPARSE_BAND_BOTTOM = -780;
    public static final int SHELF_COUNT = 11;
    public static final double VERTICAL_DROP = 73.0;
    public static final double SWITCHBACK_SWAY = 87.0;
    public static final double LANE_OFFSET = 30.0;
    public static final int LOBES_PER_SHELF = 3;
    public static final double MAIN_LOBE_RADIUS = 38.0;
    public static final double CHILD_RADIUS_PERCENT = 0.72;
    public static final double LOBE_SPREAD = 40.0;
    public static final double SHELF_SQUASH = 0.47;
    public static final int CONNECTORS_PER_DROP = 2;
    public static final double CONNECTOR_RADIUS = 12.0;
    public static final int BRAIDED_TRANSITIONS = 3;
    public static final double BRAID_BOW = 36.0;
    public static final int SHOULDER_PODS = 9;
    public static final double SHOULDER_RADIUS = 23.0;
    public static final double SHOULDER_OFFSET = 71.0;

    public static final double SPARSE_SPACING = 26.0;
    public static final double SPARSE_SIZE = 12.0;
    public static final double SPARSE_AMOUNT = 0.45;

    private static final double TAU = Math.PI * 2.0;
    private static final double MIN_DISTANCE = LANDMASS_SPACING * 0.92;
    private static final long ANCHOR_SALT = 0x9E37F1DCA7E5B900L;
    private static final long LAYOUT_SALT = 0xC2B2AE3D27D4EB4FL;
    private static final long STATION_SALT = 0xD6E8FEB86659FD93L;
    private static final long LOBE_SALT = 0xA5A3564E27F8862BL;
    private static final long SHOULDER_SALT = 0x8D58AC26AFE12E47L;
    private static final long SPARSE_SALT = 0x5BD1A2C3D4E5F601L;

    private record Station(double x, double y, double z) {}

    public record Member(double x, double y, double z, double size, double squash, boolean island, boolean sparse) {

        public double reach() {
            return size * 1.9;
        }

        public double yReach() {
            return size * squash * 1.9;
        }
    }

    public static int shelfCount() {
        return SHELF_COUNT;
    }

    public static int maxHorizontalReach() {
        double maxRadial = SWITCHBACK_SWAY * 0.67;
        double stationX = SWITCHBACK_SWAY * 0.45 + LANE_OFFSET + maxRadial * 0.42;
        double stationZ = maxRadial + SWITCHBACK_SWAY * 0.09;
        double stationReach = Math.hypot(stationX, stationZ);
        double childReach = LOBE_SPREAD * 0.70 + MAIN_LOBE_RADIUS * CHILD_RADIUS_PERCENT * 1.12 * 1.9;
        double routeReach = BRAID_BOW + CONNECTOR_RADIUS * 1.9;
        double shoulderReach = SHOULDER_OFFSET + SWITCHBACK_SWAY * 0.36 +
                SHOULDER_RADIUS * 1.12 * 1.9;
        return (int) Math.ceil(stationReach + Math.max(childReach, Math.max(routeReach, shoulderReach)));
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
        long layoutSeed = mix(seed, LAYOUT_SALT ^ dimensionSalt(dim), coreX, coreZ);
        List<Station> stations = new ArrayList<>(SHELF_COUNT);
        for (int shelf = 0; shelf < SHELF_COUNT; shelf++) {
            RandomSource stationRandom = random(layoutSeed, STATION_SALT, shelf, 0);
            double laneSign = Math.floorDiv(shelf, 2) % 2 == 0 ? -1.0 : 1.0;
            double angle = shelf * 0.56 + stationRandom.nextDouble() * 0.32;
            double radial = SWITCHBACK_SWAY * (0.42 + stationRandom.nextDouble() * 0.25);
            double x = coreX + laneSign * (SWITCHBACK_SWAY * 0.45 + LANE_OFFSET) +
                    Math.cos(angle) * radial * 0.42;
            double z = coreZ + Math.sin(angle) * radial +
                    (stationRandom.nextDouble() - 0.5) * SWITCHBACK_SWAY * 0.18;
            double y = BAND_TOP - shelf * VERTICAL_DROP;
            Station station = new Station(x, y, z);
            stations.add(station);
            addShelf(layoutSeed, shelf, station, out);
        }
        addRoutes(stations, out);
        for (int pod = 0; pod < SHOULDER_PODS; pod++) {
            RandomSource r = random(layoutSeed, SHOULDER_SALT, pod, 0);
            Station station = stations.get(r.nextInt(stations.size()));
            double angle = r.nextDouble() * TAU;
            double distance = SHOULDER_OFFSET + r.nextDouble() * SWITCHBACK_SWAY * 0.36;
            double x = station.x() + Math.cos(angle) * distance;
            double y = station.y() + (r.nextDouble() - 0.5) * VERTICAL_DROP * 0.38;
            double z = station.z() + Math.sin(angle) * distance;
            double size = SHOULDER_RADIUS * (0.82 + r.nextDouble() * 0.30);
            double squash = 0.76 + r.nextDouble() * 0.18;
            out.add(new Member(x, y, z, size, squash, false, false));
        }
    }

    private static void addShelf(long layoutSeed, int shelf, Station station, List<Member> out) {
        RandomSource r = random(layoutSeed, LOBE_SALT, shelf, 0);
        double mainY = station.y() + (r.nextDouble() - 0.5) * MAIN_LOBE_RADIUS * 0.16;
        out.add(new Member(station.x(), mainY, station.z(), MAIN_LOBE_RADIUS, SHELF_SQUASH, true, false));
        double phase = r.nextDouble() * TAU;
        int children = LOBES_PER_SHELF - 1;
        for (int child = 0; child < children; child++) {
            double angle = phase + (double) child / children * TAU + (r.nextDouble() - 0.5) * 0.32;
            double distance = LOBE_SPREAD * (0.35 + r.nextDouble() * 0.35);
            double size = MAIN_LOBE_RADIUS * CHILD_RADIUS_PERCENT * (0.88 + r.nextDouble() * 0.24);
            double x = station.x() + Math.cos(angle) * distance;
            double y = station.y() + (r.nextDouble() - 0.5) * size * 0.16;
            double z = station.z() + Math.sin(angle) * distance;
            out.add(new Member(x, y, z, size, SHELF_SQUASH, false, false));
        }
    }

    private static void addRoutes(List<Station> stations, List<Member> out) {
        boolean[] braided = new boolean[stations.size() - 1];
        int braidCount = Math.min(BRAIDED_TRANSITIONS, braided.length);
        for (int braid = 0; braid < braidCount; braid++) {
            braided[(braid + 1) * braided.length / (braidCount + 1)] = true;
        }
        for (int station = 0; station < stations.size() - 1; station++) {
            Station start = stations.get(station);
            Station end = stations.get(station + 1);
            addRoute(start, end, 0.0, out);
            if (braided[station]) addRoute(start, end, BRAID_BOW, out);
        }
    }

    private static void addRoute(Station start, Station end, double bow, List<Member> out) {
        double dx = end.x() - start.x();
        double dz = end.z() - start.z();
        double length = Math.hypot(dx, dz);
        double perpendicularX = length == 0.0 ? 0.0 : -dz / length;
        double perpendicularZ = length == 0.0 ? 0.0 : dx / length;
        for (int connector = 1; connector <= CONNECTORS_PER_DROP; connector++) {
            double progress = (double) connector / (CONNECTORS_PER_DROP + 1);
            double arc = Math.sin(Math.PI * progress) * bow;
            double x = start.x() + dx * progress + perpendicularX * arc;
            double y = start.y() + (end.y() - start.y()) * progress;
            double z = start.z() + dz * progress + perpendicularZ * arc;
            out.add(new Member(x, y, z, CONNECTOR_RADIUS, 0.82, false, false));
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
                double by = SPARSE_BAND_BOTTOM + r.nextDouble() * (BAND_TOP - SPARSE_BAND_BOTTOM);
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

    private static RandomSource random(long seed, long salt, long a, long b) {
        return new XoroshiroRandomSource(mix(seed, salt, a, b));
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
