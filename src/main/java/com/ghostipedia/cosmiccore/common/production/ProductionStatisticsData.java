package com.ghostipedia.cosmiccore.common.production;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public final class ProductionStatisticsData extends SavedData {

    public static final String DEFAULT_SORT_MODE = "default";

    public static final long[] WINDOWS = { 100, 1_200, 12_000, 72_000, 720_000, 1_800_000, 3_600_000,
            7_200_000, 18_000_000, 36_000_000, 54_000_000, 72_000_000 };
    private static final String NAME = "cosmiccore_production_statistics";
    private static final int BUCKETS = 240;
    private final Map<UUID, Pool> pools = new HashMap<>();
    private long clock;
    private boolean initialized;

    public static ProductionStatisticsData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ProductionStatisticsData::new, ProductionStatisticsData::load), NAME);
    }

    public void initialize() {
        if (initialized) return;
        initialized = true;
        setDirty();
    }

    public void ensurePool(UUID pool) {
        if (!initialized || pool == null) return;
        if (pools.putIfAbsent(pool, new Pool(clock)) == null) setDirty();
    }

    public boolean initialized() {
        return initialized;
    }

    public long clock() {
        return clock;
    }

    public void tick() {
        if (!initialized) return;
        clock++;
        setDirty();
    }

    public void record(UUID pool, String dimension, ProductionResource resource, BigDecimal amount, boolean input) {
        if (!initialized || pool == null || resource == null || amount == null || amount.signum() <= 0) return;
        pools.computeIfAbsent(pool, ignored -> new Pool(clock)).record(dimension, resource, clock, amount, input);
        setDirty();
    }

    public void markPartial(UUID pool, String dimension, String kind, boolean input) {
        if (!initialized || pool == null || !trackedKind(kind)) return;
        pools.computeIfAbsent(pool, ignored -> new Pool(clock)).markPartial(dimension, kind, clock, input);
        setDirty();
    }

    public Query query(UUID poolId, String dimension, String kind, int window, int page, List<String> selected) {
        return query(poolId, dimension, kind, "", window, page, selected, DEFAULT_SORT_MODE);
    }

    public Query query(UUID poolId, String dimension, String kind, String search, int window, int page,
                       List<String> selected) {
        return query(poolId, dimension, kind, search, window, page, selected, DEFAULT_SORT_MODE);
    }

    public Query query(UUID poolId, String dimension, String kind, String search, int window, int page,
                       List<String> selected, String sortMode) {
        return query(poolId, dimension, kind, search, window, page, selected, sortMode, false);
    }

    public Query query(UUID poolId, String dimension, String kind, String search, int window, int page,
                       List<String> selected, String sortMode, boolean reverse) {
        String normalizedSort = normalizeSortMode(sortMode);
        Pool pool = pools.get(poolId);
        if (pool == null)
            return new Query(clock, 0, 0, 0, 0, normalizedSort, reverse, List.of(), List.of(), false, false, false);
        return pool.query(clock, dimension, kind, search, window, Math.max(0, Math.min(100_000, page)), selected,
                normalizedSort, reverse);
    }

    public static String normalizeSortMode(String sortMode) {
        return switch (sortMode) {
            case "default", "id", "produced", "consumed" -> sortMode;
            case "special" -> DEFAULT_SORT_MODE;
            default -> DEFAULT_SORT_MODE;
        };
    }

    public Set<String> dimensions(UUID poolId) {
        Pool pool = pools.get(poolId);
        if (pool == null) return Set.of();
        TreeSet<String> result = new TreeSet<>();
        pool.records.values().forEach(record -> result.add(record.dimension));
        pool.gaps.forEach(gap -> {
            if (!gap.dimension.isEmpty()) result.add(gap.dimension);
        });
        return result;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        tag.putBoolean("initialized", initialized);
        tag.putLong("clock", clock);
        ListTag poolTags = new ListTag();
        pools.forEach((id, pool) -> {
            CompoundTag entry = pool.save();
            entry.putUUID("id", id);
            poolTags.add(entry);
        });
        tag.put("pools", poolTags);
        return tag;
    }

    static ProductionStatisticsData load(CompoundTag tag, HolderLookup.Provider provider) {
        ProductionStatisticsData data = new ProductionStatisticsData();
        data.initialized = tag.getBoolean("initialized");
        data.clock = Math.max(0, tag.getLong("clock"));
        ListTag pools = tag.getList("pools", Tag.TAG_COMPOUND);
        for (int i = 0; i < pools.size(); i++) {
            CompoundTag entry = pools.getCompound(i);
            if (entry.hasUUID("id")) data.pools.put(entry.getUUID("id"), Pool.load(entry, data.clock));
            else CosmicCore.LOGGER.warn("Production statistics skipped a saved pool without a valid UUID");
        }
        return data;
    }

    public record Row(ProductionResource resource, String input, String output) {}

    public record Point(String resource, long start, long end, String input, String output) {}

    public record Query(long clock, long coveredTicks, long lifetimeTicks, long energyStarted, long energyCoveredTicks,
                        String sortMode, boolean reverse, List<Row> rows, List<Point> graph, boolean more,
                        boolean partialInput,
                        boolean partialOutput) {}

    private static final class Pool {

        private long started;
        private long energyStarted;
        private final Map<String, Record> records = new HashMap<>();
        private final List<Gap> gaps = new ArrayList<>();

        Pool(long started) {
            this.started = Math.max(0, started);
            this.energyStarted = this.started;
        }

        void record(String dimension, ProductionResource resource, long clock, BigDecimal amount, boolean input) {
            String key = dimension + '\u0000' + resource.key();
            records.computeIfAbsent(key, ignored -> new Record(dimension, resource)).record(clock, amount, input);
        }

        void markPartial(String dimension, String kind, long clock, boolean input) {
            Gap gap = gaps.stream().filter(value -> value.dimension.equals(dimension) && value.kind.equals(kind) &&
                    value.input == input).findFirst().orElseGet(() -> {
                        Gap created = new Gap(dimension, kind, input);
                        gaps.add(created);
                        return created;
                    });
            gap.mark(clock);
        }

        Query query(long now, String dimension, String kind, String search, int window, int page,
                    List<String> selected, String sortMode, boolean reverse) {
            int wi = Math.max(-1, Math.min(WINDOWS.length - 1, window));
            long kindStarted = kind.equals("energy") ? Math.max(started, energyStarted) : started;
            long representedStart = wi < 0 ? kindStarted : representedStart(kindStarted, now, wi);
            long covered = Math.max(0, now - representedStart + 1);
            long lifetime = Math.max(0, now - kindStarted + 1);
            long energyFloor = Math.max(started, energyStarted);
            long energyRepresentedStart = wi < 0 ? energyFloor : representedStart(energyFloor, now, wi);
            long energyCovered = Math.max(0, now - energyRepresentedStart + 1);
            Map<String, Aggregate> aggregate = new HashMap<>();
            for (Record record : records.values()) {
                if (!dimension.isEmpty() && !dimension.equals(record.dimension)) continue;
                if (!kind.isEmpty() && !kind.equals(record.resource.kind())) continue;
                if (!search.isEmpty() && !record.resource.id().toLowerCase(Locale.ROOT).contains(search)) continue;
                Aggregate value = aggregate.computeIfAbsent(record.resource.key(),
                        ignored -> new Aggregate(record.resource));
                value.add(record, representedStart, wi);
            }
            List<Aggregate> sorted = aggregate.values().stream()
                    .sorted(comparator(sortMode, reverse))
                    .toList();
            int from = Math.min(sorted.size(), page * 50);
            int to = Math.min(sorted.size(), from + 50);
            List<Row> rows = sorted.subList(from, to).stream()
                    .map(value -> new Row(value.resource, text(value.input), text(value.output))).toList();
            List<Point> graph = graph(records.values(), dimension, kind, now, wi, representedStart,
                    energyRepresentedStart, selected);
            boolean partialInput = gaps.stream().anyMatch(gap -> gap.matches(dimension, kind, representedStart, wi,
                    true));
            boolean partialOutput = gaps.stream().anyMatch(gap -> gap.matches(dimension, kind, representedStart, wi,
                    false));
            return new Query(now, covered, lifetime, energyStarted, energyCovered, sortMode, reverse, rows, graph,
                    to < sorted.size(), partialInput, partialOutput);
        }

        private static Comparator<Aggregate> comparator(String sortMode, boolean reverse) {
            Comparator<Aggregate> comparator = switch (sortMode) {
                case "id" -> Comparator.comparing((Aggregate value) -> value.resource.id())
                        .thenComparing(value -> value.resource.key());
                case "produced" -> Comparator.comparing((Aggregate value) -> value.output).reversed()
                        .thenComparing(value -> value.resource.key());
                case "consumed" -> Comparator.comparing((Aggregate value) -> value.input).reversed()
                        .thenComparing(value -> value.resource.key());
                default -> Comparator.comparingInt((Aggregate value) -> specialRank(value.resource))
                        .thenComparing((left, right) -> specialRank(left.resource) < 4 ? 0 :
                                right.output.add(right.input).compareTo(left.output.add(left.input)))
                        .thenComparing(value -> value.resource.key());
            };
            return reverse ? comparator.reversed() : comparator;
        }

        private static int specialRank(ProductionResource resource) {
            if (resource.kind().equals("energy") && resource.id().equals("gtceu:eu")) return 0;
            if (resource.kind().equals("soul") && resource.id().equals("anima")) return 1;
            if (resource.kind().equals("soul") && resource.id().equals("spiritus")) return 2;
            if (resource.kind().equals("ember") && resource.id().equals("embers:ember")) return 3;
            return 4;
        }

        private static List<Point> graph(Iterable<Record> records, String dimension, String kind, long now, int wi,
                                         long representedStart, long energyRepresentedStart, List<String> selected) {
            if (wi < 0 || selected.isEmpty()) return List.of();
            long bucket = bucketTicks(wi);
            Map<String, Long> starts = new HashMap<>();
            Map<String, Map<Long, BigDecimal[]>> values = new HashMap<>();
            for (String resource : selected) {
                long startFloor = resource.startsWith("energy\u0000") ? energyRepresentedStart : representedStart;
                starts.put(resource, startFloor);
                long graphStart = startFloor / bucket * bucket;
                Map<Long, BigDecimal[]> samples = values.computeIfAbsent(resource, ignored -> new HashMap<>());
                for (long start = graphStart; start <= now; start += bucket)
                    samples.put(start, new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
            }
            for (Record record : records) {
                if (!dimension.isEmpty() && !dimension.equals(record.dimension)) continue;
                if (!kind.isEmpty() && !kind.equals(record.resource.kind())) continue;
                Map<Long, BigDecimal[]> resource = values.get(record.resource.key());
                if (resource == null) continue;
                for (Sample sample : record.windows[wi]) if (sample.end >= representedStart && sample.start <= now) {
                    BigDecimal[] sums = resource.get(sample.start);
                    if (sums != null) {
                        sums[0] = sums[0].add(sample.input);
                        sums[1] = sums[1].add(sample.output);
                    }
                }
            }
            List<Point> result = new ArrayList<>();
            values.forEach((resource, samples) -> samples.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> result.add(new Point(resource, Math.max(starts.get(resource), entry.getKey()),
                            Math.min(now, entry.getKey() + bucket - 1), text(entry.getValue()[0]),
                            text(entry.getValue()[1])))));
            return result;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putLong("started", started);
            tag.putLong("energyStarted", energyStarted);
            ListTag recordsTag = new ListTag();
            records.values().forEach(record -> recordsTag.add(record.save()));
            tag.put("records", recordsTag);
            ListTag gapsTag = new ListTag();
            gaps.forEach(gap -> gapsTag.add(gap.save()));
            tag.put("gaps", gapsTag);
            return tag;
        }

        static Pool load(CompoundTag tag, long now) {
            Pool pool = new Pool(tag.contains("started") ? tag.getLong("started") : now);
            pool.energyStarted = tag.contains("energyStarted") ?
                    Math.max(pool.started, Math.min(now, Math.max(0, tag.getLong("energyStarted")))) : now;
            ListTag gapTags = tag.getList("gaps", Tag.TAG_COMPOUND);
            for (int i = 0; i < gapTags.size(); i++) {
                Gap gap = Gap.load(gapTags.getCompound(i));
                if (gap != null) pool.gaps.add(gap);
            }
            ListTag recordTags = tag.getList("records", Tag.TAG_COMPOUND);
            for (int i = 0; i < recordTags.size(); i++) {
                CompoundTag recordTag = recordTags.getCompound(i);
                Record record = Record.load(recordTag);
                if (record != null) pool.records.put(record.dimension + '\u0000' + record.resource.key(), record);
                else {
                    pool.absorbInvalid(recordTag);
                    CosmicCore.LOGGER.warn(
                            "Production statistics retained a visible coverage gap for an invalid saved record");
                }
            }
            return pool;
        }

        private void absorbInvalid(CompoundTag tag) {
            String kind = tag.getString("kind");
            if (!trackedKind(kind)) return;
            String dimension = tag.getString("dimension");
            for (boolean input : new boolean[] { true, false }) {
                Gap gap = new Gap(dimension, kind, input);
                gap.lifetime = true;
                ListTag windows = tag.getList("windows", Tag.TAG_LIST);
                for (int wi = 0; wi < Math.min(windows.size(), WINDOWS.length); wi++) {
                    ListTag samples = windows.getList(wi);
                    for (int i = Math.max(0, samples.size() - BUCKETS - 1); i < samples.size(); i++)
                        gap.buckets[wi].add(samples.getCompound(i).getLong("s"));
                }
                gaps.add(gap);
            }
        }
    }

    private static final class Aggregate {

        final ProductionResource resource;
        BigDecimal input = BigDecimal.ZERO;
        BigDecimal output = BigDecimal.ZERO;

        Aggregate(ProductionResource resource) {
            this.resource = resource;
        }

        void add(Record record, long representedStart, int window) {
            if (window < 0) {
                input = input.add(record.input);
                output = output.add(record.output);
            } else for (Sample sample : record.windows[window]) if (sample.end >= representedStart) {
                input = input.add(sample.input);
                output = output.add(sample.output);
            }
        }
    }

    private static final class Record {

        final String dimension;
        final ProductionResource resource;
        BigDecimal input = BigDecimal.ZERO;
        BigDecimal output = BigDecimal.ZERO;
        final List<Sample>[] windows;

        @SuppressWarnings("unchecked")
        Record(String dimension, ProductionResource resource) {
            this.dimension = dimension;
            this.resource = resource;
            windows = new List[WINDOWS.length];
            for (int i = 0; i < windows.length; i++) windows[i] = new ArrayList<>();
        }

        void record(long clock, BigDecimal amount, boolean consumed) {
            if (consumed) input = input.add(amount);
            else output = output.add(amount);
            for (int i = 0; i < windows.length; i++) {
                long size = bucketTicks(i);
                long start = clock / size * size;
                List<Sample> samples = windows[i];
                Sample sample = samples.isEmpty() ? null : samples.getLast();
                if (sample == null || sample.start != start) {
                    sample = new Sample(start, start + size - 1, BigDecimal.ZERO, BigDecimal.ZERO);
                    samples.add(sample);
                }
                if (consumed) sample.input = sample.input.add(amount);
                else sample.output = sample.output.add(amount);
                while (samples.size() > BUCKETS + 1) samples.removeFirst();
            }
        }

        CompoundTag save() {
            CompoundTag tag = resource.toTag();
            tag.putString("dimension", dimension);
            tag.putString("input", text(input));
            tag.putString("output", text(output));
            ListTag all = new ListTag();
            for (List<Sample> window : windows) {
                ListTag list = new ListTag();
                window.forEach(sample -> list.add(sample.save()));
                all.add(list);
            }
            tag.put("windows", all);
            return tag;
        }

        static Record load(CompoundTag tag) {
            ProductionResource resource = ProductionResource.fromTag(tag);
            String dimension = tag.getString("dimension");
            if (resource == null || dimension.length() > 512) return null;
            Record record = new Record(dimension, resource);
            try {
                record.input = positive(tag.getString("input"));
                record.output = positive(tag.getString("output"));
                ListTag all = tag.getList("windows", Tag.TAG_LIST);
                for (int i = 0; i < Math.min(all.size(), record.windows.length); i++) {
                    ListTag list = all.getList(i);
                    for (int j = Math.max(0, list.size() - BUCKETS - 1); j < list.size(); j++)
                        record.windows[i].add(Sample.load(list.getCompound(j)));
                }
                return record;
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }

    private static final class Sample {

        final long start;
        final long end;
        BigDecimal input;
        BigDecimal output;

        Sample(long start, long end, BigDecimal input, BigDecimal output) {
            this.start = start;
            this.end = end;
            this.input = input;
            this.output = output;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putLong("s", start);
            tag.putLong("e", end);
            tag.putString("i", text(input));
            tag.putString("o", text(output));
            return tag;
        }

        static Sample load(CompoundTag tag) {
            long start = tag.getLong("s");
            long end = tag.getLong("e");
            if (start < 0 || end < start) throw new IllegalArgumentException();
            return new Sample(start, end, positive(tag.getString("i")), positive(tag.getString("o")));
        }
    }

    private static final class Gap {

        final String dimension;
        final String kind;
        final boolean input;
        boolean lifetime;
        final Set<Long>[] buckets;

        @SuppressWarnings("unchecked")
        Gap(String dimension, String kind, boolean input) {
            this.dimension = dimension;
            this.kind = kind;
            this.input = input;
            buckets = new Set[WINDOWS.length];
            for (int i = 0; i < buckets.length; i++) buckets[i] = new HashSet<>();
        }

        void mark(long clock) {
            lifetime = true;
            for (int i = 0; i < buckets.length; i++) {
                long size = bucketTicks(i);
                buckets[i].add(clock / size * size);
                long oldest = clock - WINDOWS[i] - size;
                buckets[i].removeIf(start -> start < oldest);
            }
        }

        boolean matches(String dimension, String kind, long representedStart, int window, boolean input) {
            if (this.input != input || !dimension.isEmpty() && !dimension.equals(this.dimension) ||
                    !kind.isEmpty() && !kind.equals(this.kind))
                return false;
            return window < 0 ? lifetime :
                    buckets[window].stream().anyMatch(start -> start + bucketTicks(window) - 1 >= representedStart);
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("dimension", dimension);
            tag.putString("kind", kind);
            tag.putBoolean("input", input);
            tag.putBoolean("lifetime", lifetime);
            ListTag all = new ListTag();
            for (Set<Long> window : buckets) {
                ListTag list = new ListTag();
                window.stream().sorted().forEach(start -> {
                    CompoundTag entry = new CompoundTag();
                    entry.putLong("s", start);
                    list.add(entry);
                });
                all.add(list);
            }
            tag.put("windows", all);
            return tag;
        }

        static Gap load(CompoundTag tag) {
            String dimension = tag.getString("dimension");
            String kind = tag.getString("kind");
            if (dimension.length() > 512 || !trackedKind(kind)) return null;
            Gap gap = new Gap(dimension, kind, tag.getBoolean("input"));
            gap.lifetime = tag.getBoolean("lifetime");
            ListTag all = tag.getList("windows", Tag.TAG_LIST);
            for (int i = 0; i < Math.min(all.size(), gap.buckets.length); i++) {
                ListTag list = all.getList(i);
                for (int j = Math.max(0, list.size() - BUCKETS - 1); j < list.size(); j++) {
                    long start = list.getCompound(j).getLong("s");
                    if (start >= 0) gap.buckets[i].add(start);
                }
            }
            return gap;
        }
    }

    private static long representedStart(long poolStart, long now, int window) {
        long cutoff = Math.max(poolStart, now - WINDOWS[window] + 1);
        long bucket = bucketTicks(window);
        return Math.max(poolStart, cutoff / bucket * bucket);
    }

    private static long bucketTicks(int window) {
        return Math.max(1, (WINDOWS[window] + BUCKETS - 1) / BUCKETS);
    }

    private static BigDecimal positive(String value) {
        BigDecimal decimal = new BigDecimal(value);
        if (decimal.signum() < 0) throw new IllegalArgumentException();
        return decimal;
    }

    private static String text(BigDecimal value) {
        if (value.signum() == 0) return "0";
        return value.stripTrailingZeros().toPlainString();
    }

    private static boolean trackedKind(String kind) {
        return kind.equals("item") || kind.equals("fluid") || kind.equals("ember") || kind.equals("soul") ||
                kind.equals("energy");
    }

    int sampleCountForTest() {
        return pools.values().stream().mapToInt(pool -> pool.records.values().stream()
                .mapToInt(record -> java.util.Arrays.stream(record.windows).mapToInt(List::size).sum()).sum() +
                pool.gaps.stream().mapToInt(gap -> java.util.Arrays.stream(gap.buckets).mapToInt(Set::size).sum())
                        .sum())
                .sum();
    }
}
