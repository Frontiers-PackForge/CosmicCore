package com.ghostipedia.cosmiccore.common.data.worldgen.field;

import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Deterministic ore-field placement. A pure function of (world seed, dimension, position): for any coordinate it
 * computes the same set of field cores, their bundle types, and their fragment layouts, without touching world state
 * or generated blocks. Both the worldgen dispatcher (which places fields) and the survey scanner (which predicts them
 * ahead of generation) call into this class, so the two can never drift apart.
 *
 * <p>
 * Cores are scattered by a deterministic Poisson-disk: a jittered cell grid where a candidate keeps its spot only
 * if no higher-priority neighbour sits within {@link #MIN_DISTANCE}. Each surviving core is assigned a
 * dimension-eligible, rarity-weighted bundle, then expanded into a field: a set of small ore-pocket fragments whose
 * arrangement traces one of four shapes. The shape lives in the arrangement; each pocket itself is a plain small blob
 * (see {@code PocketVeinGenerator}).
 */
public final class OreFieldPlacement {

    private OreFieldPlacement() {}

    public static final int MIN_DISTANCE = 350;
    public static final int DEFAULT_FIELD_RADIUS = 88;

    private static final int CELL_SIZE = MIN_DISTANCE;
    private static final long ANCHOR_SALT = 0x9E37F1DCA7E5B900L;
    private static final long TYPE_SALT = 0xC2B2AE3D27D4EB4FL;
    private static final long DISTRICT_SALT = 0x165667B19E3779F9L;
    private static final double TAU = Math.PI * 2.0;

    public enum Shape {
        BRANCHING,
        CLUSTER,
        STRINGER,
        FRACTURE
    }

    public record FieldProfile(Set<ResourceKey<Level>> dimensions, int weight, Shape shape,
                               int fragmentCount, int fieldRadius) {}

    public record FieldMember(BlockPos center) {}

    public record OreField(BlockPos core, Material bundle, long worldSeed, ResourceKey<Level> dimension) {

        public List<FieldMember> members() {
            return layoutField(this);
        }
    }

    private static final Map<Material, FieldProfile> PROFILES = new LinkedHashMap<>();
    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        PROFILES.clear();

        put(CosmicBundleMaterials.Ferosine, profile(Level.OVERWORLD, Shape.BRANCHING));
        put(CosmicBundleMaterials.Cuprosiva, profile(Level.OVERWORLD, Shape.STRINGER));
        put(CosmicBundleMaterials.Galenite, profile(Level.OVERWORLD, Shape.BRANCHING));
        put(CosmicBundleMaterials.Landisite, profile(Level.OVERWORLD, Shape.BRANCHING));
        put(CosmicBundleMaterials.Redstona, profile(Level.OVERWORLD, Shape.FRACTURE));
        put(CosmicBundleMaterials.Lazuric, profile(Level.OVERWORLD, Shape.CLUSTER));
        put(CosmicBundleMaterials.Carbonic, profile(Level.OVERWORLD, Shape.CLUSTER));
        put(CosmicBundleMaterials.EarthenSalts, profile(Level.OVERWORLD, Shape.CLUSTER));

        put(CosmicBundleMaterials.Pyroltic, profile(Level.NETHER, Shape.FRACTURE));
        put(CosmicBundleMaterials.Quartizine, profile(Level.NETHER, Shape.CLUSTER));
        put(CosmicBundleMaterials.Molybite, profile(Level.NETHER, Shape.STRINGER));
        put(CosmicBundleMaterials.Fahlorium, profile(Level.NETHER, Shape.FRACTURE));
        put(CosmicBundleMaterials.MonaziteSalts, profile(Level.NETHER, Shape.STRINGER));

        put(CosmicBundleMaterials.Agarlite, profile(Level.END, Shape.STRINGER));
        put(CosmicBundleMaterials.CrudeRadionite, profile(Level.END, Shape.FRACTURE));
        put(CosmicBundleMaterials.Vanachrome, profile(Level.END, Shape.BRANCHING));

        initialized = true;
    }

    private static FieldProfile profile(ResourceKey<Level> dimension, Shape shape) {
        int fragments = (shape == Shape.STRINGER || shape == Shape.FRACTURE) ? 39 : 44;
        return new FieldProfile(Set.of(dimension), 100, shape, fragments, DEFAULT_FIELD_RADIUS);
    }

    private static void put(Material bundle, FieldProfile profile) {
        if (bundle != null) PROFILES.put(bundle, profile);
    }

    public static FieldProfile profileFor(Material bundle) {
        init();
        return PROFILES.get(bundle);
    }

    public static Set<Material> bundles() {
        init();
        return PROFILES.keySet();
    }

    /**
     * Every field core within {@code radius} blocks (horizontal) of the given position, in the given dimension.
     */
    public static List<OreField> fieldsNear(long worldSeed, ResourceKey<Level> dimension, int centerX, int centerZ,
                                            int radius) {
        init();
        List<OreField> out = new ArrayList<>();
        int margin = radius + CELL_SIZE;
        int minCellX = Math.floorDiv(centerX - margin, CELL_SIZE);
        int maxCellX = Math.floorDiv(centerX + margin, CELL_SIZE);
        int minCellZ = Math.floorDiv(centerZ - margin, CELL_SIZE);
        int maxCellZ = Math.floorDiv(centerZ + margin, CELL_SIZE);
        long radiusSqr = (long) radius * radius;

        for (int cx = minCellX; cx <= maxCellX; cx++) {
            for (int cz = minCellZ; cz <= maxCellZ; cz++) {
                long[] core = survivingCore(worldSeed, dimension, cx, cz);
                if (core == null) continue;
                int x = (int) core[0];
                int z = (int) core[1];
                long dx = x - centerX;
                long dz = z - centerZ;
                if (dx * dx + dz * dz > radiusSqr) continue;
                Material bundle = assignBundle(worldSeed, dimension, x, z);
                if (bundle == null) continue;
                out.add(new OreField(new BlockPos(x, 0, z), bundle, worldSeed, dimension));
            }
        }
        return out;
    }

    /**
     * The nearest field of a specific bundle (or any bundle if {@code bundle} is null) within {@code maxRadius}.
     */
    public static Optional<OreField> nearestField(long worldSeed, ResourceKey<Level> dimension, Material bundle,
                                                  int x, int z, int maxRadius) {
        List<OreField> fields = fieldsNear(worldSeed, dimension, x, z, maxRadius);
        OreField best = null;
        long bestSqr = Long.MAX_VALUE;
        for (OreField field : fields) {
            if (bundle != null && field.bundle() != bundle) continue;
            long dx = field.core().getX() - x;
            long dz = field.core().getZ() - z;
            long d = dx * dx + dz * dz;
            if (d < bestSqr) {
                bestSqr = d;
                best = field;
            }
        }
        return Optional.ofNullable(best);
    }

    private static long[] candidate(long worldSeed, ResourceKey<Level> dimension, int cx, int cz) {
        RandomSource random = new XoroshiroRandomSource(
                mix(worldSeed, ANCHOR_SALT ^ dimensionSalt(dimension), cx, cz));
        long x = (long) cx * CELL_SIZE + random.nextInt(CELL_SIZE);
        long z = (long) cz * CELL_SIZE + random.nextInt(CELL_SIZE);
        long priority = random.nextLong();
        return new long[] { x, z, priority };
    }

    private static long[] survivingCore(long worldSeed, ResourceKey<Level> dimension, int cx, int cz) {
        long[] self = candidate(worldSeed, dimension, cx, cz);
        long minSqr = (long) MIN_DISTANCE * MIN_DISTANCE;
        for (int nx = cx - 1; nx <= cx + 1; nx++) {
            for (int nz = cz - 1; nz <= cz + 1; nz++) {
                if (nx == cx && nz == cz) continue;
                long[] other = candidate(worldSeed, dimension, nx, nz);
                if (other[2] <= self[2]) continue;
                long dx = other[0] - self[0];
                long dz = other[1] - self[1];
                if (dx * dx + dz * dz < minSqr) return null;
            }
        }
        return self;
    }

    private static Material assignBundle(long worldSeed, ResourceKey<Level> dimension, int x, int z) {
        int total = 0;
        for (FieldProfile profile : PROFILES.values()) {
            if (profile.dimensions().contains(dimension) && profile.weight() > 0) {
                total += profile.weight();
            }
        }
        if (total <= 0) return null;

        RandomSource random = new XoroshiroRandomSource(mix(worldSeed, TYPE_SALT ^ dimensionSalt(dimension), x, z));
        int roll = random.nextInt(total);
        int acc = 0;
        Material last = null;
        for (Map.Entry<Material, FieldProfile> entry : PROFILES.entrySet()) {
            FieldProfile profile = entry.getValue();
            if (!profile.dimensions().contains(dimension) || profile.weight() <= 0) continue;
            last = entry.getKey();
            acc += profile.weight();
            if (roll < acc) return entry.getKey();
        }
        return last;
    }

    private static List<FieldMember> layoutField(OreField field) {
        init();
        FieldProfile profile = PROFILES.get(field.bundle());
        if (profile == null) {
            return List.of(new FieldMember(field.core()));
        }

        int n = profile.fragmentCount();
        float radius = profile.fieldRadius();
        RandomSource r = new XoroshiroRandomSource(
                mix(field.worldSeed(), DISTRICT_SALT ^ dimensionSalt(field.dimension()),
                        field.core().getX(), field.core().getZ()));

        List<int[]> pts = new ArrayList<>();
        switch (profile.shape()) {
            case BRANCHING -> branchingLayout(pts, n, radius, r);
            case CLUSTER -> clusterLayout(pts, n, radius, r);
            case STRINGER -> stringerLayout(pts, n, radius, r);
            case FRACTURE -> fractureLayout(pts, n, radius, r);
        }

        int coreX = field.core().getX();
        int coreZ = field.core().getZ();
        int count = Math.min(n, pts.size());
        List<FieldMember> members = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int[] p = pts.get(i);
            members.add(new FieldMember(new BlockPos(coreX + p[0], 0, coreZ + p[1])));
        }
        return members;
    }

    private static void branchingLayout(List<int[]> pts, int n, float radius, RandomSource r) {
        int core = Math.round(n * 0.15f);
        for (int i = 0; i < core; i++) {
            double a = r.nextDouble() * TAU;
            double d = r.nextDouble() * radius * 0.15;
            add(pts, Math.cos(a) * d, Math.sin(a) * d);
        }
        int rem = n - core;
        int branches = 5;
        int per = Math.max(1, Mth.ceil((float) rem / branches));
        for (int b = 0; b < branches; b++) {
            double ang = (double) b / branches * TAU + (r.nextDouble() - 0.5) * 0.5;
            double x = 0, z = 0;
            double len = radius * (0.9 + r.nextDouble() * 0.25);
            double step = len / per;
            for (int s = 0; s < per; s++) {
                ang += (r.nextDouble() - 0.5) * 0.35;
                x += Math.cos(ang) * step;
                z += Math.sin(ang) * step;
                add(pts, x + jit(r, radius), z + jit(r, radius));
            }
        }
    }

    private static void clusterLayout(List<int[]> pts, int n, float radius, RandomSource r) {
        int k = clamp(Math.round(n / 5f), 4, 9);
        double[][] nodes = new double[k][2];
        for (int i = 0; i < k; i++) {
            double a = r.nextDouble() * TAU;
            double d = (0.1 + r.nextDouble() * 0.55) * radius;
            nodes[i][0] = Math.cos(a) * d;
            nodes[i][1] = Math.sin(a) * d * 0.9;
        }
        int per = Math.max(1, Mth.ceil((float) n / k));
        for (double[] node : nodes) {
            for (int j = 0; j < per; j++) {
                double a = r.nextDouble() * TAU;
                double d = r.nextDouble() * radius * 0.32;
                add(pts, node[0] + Math.cos(a) * d, node[1] + Math.sin(a) * d);
            }
        }
    }

    private static void stringerLayout(List<int[]> pts, int n, float radius, RandomSource r) {
        int core = Math.round(n * 0.32f);
        for (int i = 0; i < core; i++) {
            double a = r.nextDouble() * TAU;
            double d = r.nextDouble() * radius * 0.16;
            add(pts, Math.cos(a) * d, Math.sin(a) * d);
        }
        int rem = n - core;
        int strands = clamp(Math.round(rem / 3f), 6, 12);
        int per = Math.max(1, Mth.ceil((float) rem / strands));
        double inner = radius * 0.16;
        for (int s = 0; s < strands; s++) {
            double ang = (double) s / strands * TAU + (r.nextDouble() - 0.5) * 0.4;
            double len = radius * (0.75 + r.nextDouble() * 0.35);
            for (int j = 0; j < per; j++) {
                double t = (double) (j + 1) / per;
                double d = inner + t * (len - inner);
                double w = ang + (r.nextDouble() - 0.5) * 0.12;
                add(pts, Math.cos(w) * d + jit(r, radius), Math.sin(w) * d + jit(r, radius));
            }
        }
    }

    private static void fractureLayout(List<int[]> pts, int n, float radius, RandomSource r) {
        double ringRadius = radius * 0.55;
        int ring = Math.round(n * 0.5f);
        for (int i = 0; i < ring; i++) {
            double a = (double) i / Math.max(1, ring) * TAU + (r.nextDouble() - 0.5) * 0.25;
            add(pts, Math.cos(a) * ringRadius + jit(r, radius), Math.sin(a) * ringRadius + jit(r, radius));
        }
        int center = Math.round(n * 0.1f);
        for (int i = 0; i < center; i++) {
            double a = r.nextDouble() * TAU;
            double d = r.nextDouble() * radius * 0.14;
            add(pts, Math.cos(a) * d, Math.sin(a) * d);
        }
        int rem = Math.max(0, n - ring - center);
        int cracks = 4;
        int per = Math.max(1, Mth.ceil((float) rem / cracks));
        for (int c = 0; c < cracks; c++) {
            double ang = r.nextDouble() * TAU;
            for (int j = 0; j < per; j++) {
                double d = ringRadius + ((double) (j + 1) / per) * (radius - ringRadius);
                add(pts, Math.cos(ang) * d + jit(r, radius), Math.sin(ang) * d + jit(r, radius));
            }
        }
    }

    private static double jit(RandomSource r, float radius) {
        return (r.nextDouble() - 0.5) * radius * 0.06;
    }

    private static void add(List<int[]> pts, double x, double z) {
        pts.add(new int[] { (int) Math.round(x), (int) Math.round(z) });
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static long dimensionSalt(ResourceKey<Level> dimension) {
        return (long) dimension.location().toString().hashCode() * 0x9E3779B97F4A7C15L;
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
