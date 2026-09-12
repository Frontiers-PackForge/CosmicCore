package com.ghostipedia.cosmiccore.api.machine.activity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ActivityLedger {

    public static final int MAX_RESOURCES = 64;
    public static final int MAX_BUCKETS = 128;
    public static final long MIN_HORIZON = 1_200L;
    private static final long[] EMPTY_TOTALS = new long[0];

    private final ArrayList<MutableBucket> buckets = new ArrayList<>();
    private long clock;
    private long horizon = MIN_HORIZON;
    private long bucketWidth = 1L;
    private long inputPartialTick = -1L;
    private long outputPartialTick = -1L;
    private boolean inputCoarse;
    private boolean outputCoarse;

    public long clock() {
        return clock;
    }

    public void advanceTick() {
        if (clock == Long.MAX_VALUE) {
            markPartial(true);
            markPartial(false);
            return;
        }
        clock++;
    }

    public void setHorizon(long ticks) {
        long requested = Math.max(MIN_HORIZON, ticks < 0L ? MIN_HORIZON : ticks);
        if (requested < horizon) bucketWidth = 1L;
        horizon = requested;
        expire();
    }

    public long horizon() {
        return horizon;
    }

    public boolean record(int resource, boolean input, long amount) {
        if (resource < 0 || resource >= MAX_RESOURCES || amount <= 0L) {
            markPartial(input);
            return false;
        }
        expire();
        MutableBucket bucket = bucketFor(clock);
        if (!bucket.add(input, resource, amount, clock)) {
            markPartial(input);
            return false;
        }
        compact();
        return true;
    }

    public void markPartial(boolean input) {
        if (input) inputPartialTick = clock;
        else outputPartialTick = clock;
    }

    public int[] retainedResources() {
        expire();
        boolean[] retained = new boolean[MAX_RESOURCES];
        for (MutableBucket bucket : buckets) {
            for (int resource = 0; resource < MAX_RESOURCES; resource++)
                retained[resource] |= value(bucket.inputs, resource) != 0L || value(bucket.outputs, resource) != 0L;
        }
        int count = 0;
        for (boolean value : retained) if (value) count++;
        int[] result = new int[count];
        for (int resource = 0, index = 0; resource < MAX_RESOURCES; resource++)
            if (retained[resource]) result[index++] = resource;
        return result;
    }

    public Snapshot snapshot() {
        expire();
        long[] inputs = new long[MAX_RESOURCES];
        long[] outputs = new long[MAX_RESOURCES];
        boolean inputsPartial = inputPartialTick >= cutoff();
        boolean outputsPartial = outputPartialTick >= cutoff();
        for (MutableBucket bucket : buckets) {
            for (int resource = 0; resource < MAX_RESOURCES; resource++) {
                long bucketInput = value(bucket.inputs, resource);
                long bucketOutput = value(bucket.outputs, resource);
                if (Long.MAX_VALUE - inputs[resource] < bucketInput) inputsPartial = true;
                else inputs[resource] += bucketInput;
                if (Long.MAX_VALUE - outputs[resource] < bucketOutput) outputsPartial = true;
                else outputs[resource] += bucketOutput;
            }
        }
        long start = buckets.isEmpty() ? clock : buckets.getFirst().start;
        long end = clock;
        long elapsed = buckets.isEmpty() ? 0L : elapsed(start, end);
        boolean coarse = !buckets.isEmpty() && (bucketWidth > 1L || start < cutoff());
        ArrayList<ResourceTotal> totals = new ArrayList<>();
        for (int resource = 0; resource < MAX_RESOURCES; resource++)
            if (inputs[resource] != 0L || outputs[resource] != 0L)
                totals.add(new ResourceTotal(resource, inputs[resource], outputs[resource]));
        DirectionMetadata input = new DirectionMetadata(inputsPartial, coarse || inputCoarse, start, end);
        DirectionMetadata output = new DirectionMetadata(outputsPartial, coarse || outputCoarse, start, end);
        return new Snapshot(start, end, elapsed, horizon, List.copyOf(totals), input, output);
    }

    public State saveState() {
        ArrayList<Bucket> saved = new ArrayList<>(buckets.size());
        for (MutableBucket bucket : buckets) saved.add(bucket.freeze());
        return new State(clock, horizon, bucketWidth, inputPartialTick, outputPartialTick, inputCoarse, outputCoarse,
                saved);
    }

    public static ActivityLedger restore(State state) {
        ActivityLedger ledger = new ActivityLedger();
        if (state == null) return ledger;
        ledger.clock = Math.max(0L, state.clock());
        ledger.horizon = Math.max(MIN_HORIZON, state.horizon());
        ledger.bucketWidth = validWidth(state.bucketWidth()) ? state.bucketWidth() : 1L;
        ledger.inputPartialTick = validPartialTick(state.inputPartialTick(), ledger.clock);
        ledger.outputPartialTick = validPartialTick(state.outputPartialTick(), ledger.clock);
        ledger.inputCoarse = state.inputCoarse();
        ledger.outputCoarse = state.outputCoarse();
        for (Bucket bucket : state.buckets()) {
            if (bucket == null || bucket.start() < 0L || bucket.end() < bucket.start() ||
                    bucket.end() > ledger.clock || bucket.inputs().length > MAX_RESOURCES ||
                    bucket.outputs().length > MAX_RESOURCES) {
                ledger.markPartial(true);
                ledger.markPartial(false);
                continue;
            }
            if (!nonNegative(bucket.inputs()) || !nonNegative(bucket.outputs())) {
                ledger.markPartial(true);
                ledger.markPartial(false);
                continue;
            }
            ledger.buckets.add(new MutableBucket(bucket));
        }
        ledger.buckets.sort(Comparator.comparingLong(bucket -> bucket.start));
        if (ledger.buckets.size() > MAX_BUCKETS) {
            ledger.markPartial(true);
            ledger.markPartial(false);
            while (ledger.buckets.size() > MAX_BUCKETS) ledger.buckets.removeFirst();
        }
        ledger.expire();
        return ledger;
    }

    private MutableBucket bucketFor(long tick) {
        long start = bucketStart(tick, bucketWidth);
        if (!buckets.isEmpty() && (buckets.getLast().start == start || buckets.getLast().end == tick))
            return buckets.getLast();
        MutableBucket bucket = new MutableBucket(start, tick);
        buckets.add(bucket);
        return bucket;
    }

    private void compact() {
        while (buckets.size() > MAX_BUCKETS) {
            long next = bucketWidth > Long.MAX_VALUE / 2L ? Long.MAX_VALUE : bucketWidth * 2L;
            if (next == bucketWidth) {
                markPartial(true);
                markPartial(false);
                buckets.removeFirst();
                continue;
            }
            rebin(next);
        }
    }

    private void rebin(long width) {
        ArrayList<MutableBucket> rebinned = new ArrayList<>(buckets.size());
        for (MutableBucket source : buckets) {
            long start = bucketStart(source.start, width);
            MutableBucket target;
            if (rebinned.isEmpty() || rebinned.getLast().start != start) {
                if (!rebinned.isEmpty() && start <= rebinned.getLast().end) start = source.start;
                target = new MutableBucket(start, source.end);
                rebinned.add(target);
            } else target = rebinned.getLast();
            for (int resource = 0; resource < MAX_RESOURCES; resource++) {
                if (!target.add(true, resource, value(source.inputs, resource), source.end)) markPartial(true);
                if (!target.add(false, resource, value(source.outputs, resource), source.end)) markPartial(false);
            }
            target.end = Math.max(target.end, source.end);
        }
        buckets.clear();
        buckets.addAll(rebinned);
        bucketWidth = width;
        inputCoarse = true;
        outputCoarse = true;
        expire();
    }

    private void expire() {
        long cutoff = cutoff();
        while (!buckets.isEmpty() && buckets.getFirst().end < cutoff) buckets.removeFirst();
    }

    private long cutoff() {
        return clock >= horizon ? clock - horizon + 1L : 0L;
    }

    private static long value(long[] totals, int resource) {
        return resource < totals.length ? totals[resource] : 0L;
    }

    private static long bucketStart(long tick, long width) {
        return width == Long.MAX_VALUE ? 0L : tick - tick % width;
    }

    private static long elapsed(long start, long end) {
        return end < start ? 0L : end - start == Long.MAX_VALUE ? Long.MAX_VALUE : end - start + 1L;
    }

    private static boolean validWidth(long width) {
        return width > 0L && (width & (width - 1L)) == 0L;
    }

    private static long validPartialTick(long tick, long clock) {
        return tick >= -1L && tick <= clock ? tick : clock;
    }

    private static boolean nonNegative(long[] values) {
        for (long value : values) if (value < 0L) return false;
        return true;
    }

    public record ResourceTotal(int resource, long input, long output) {}

    public record DirectionMetadata(boolean partial, boolean coarse, long coverageStart, long coverageEnd) {}

    public record Snapshot(long startTick, long endTick, long elapsedTicks, long horizon, List<ResourceTotal> totals,
                           DirectionMetadata input, DirectionMetadata output) {}

    public record State(long clock, long horizon, long bucketWidth, long inputPartialTick, long outputPartialTick,
                        boolean inputCoarse, boolean outputCoarse, List<Bucket> buckets) {

        public State {
            buckets = buckets == null ? List.of() : List.copyOf(buckets);
        }
    }

    public record Bucket(long start, long end, long[] inputs, long[] outputs) {

        public Bucket {
            inputs = inputs == null ? EMPTY_TOTALS.clone() : inputs.clone();
            outputs = outputs == null ? EMPTY_TOTALS.clone() : outputs.clone();
        }

        @Override
        public long[] inputs() {
            return inputs.clone();
        }

        @Override
        public long[] outputs() {
            return outputs.clone();
        }
    }

    private static final class MutableBucket {

        private final long start;
        private long end;
        private long[] inputs;
        private long[] outputs;

        private MutableBucket(long start, long end) {
            this.start = start;
            this.end = end;
            this.inputs = EMPTY_TOTALS;
            this.outputs = EMPTY_TOTALS;
        }

        private MutableBucket(Bucket bucket) {
            this.start = bucket.start();
            this.end = bucket.end();
            this.inputs = bucket.inputs();
            this.outputs = bucket.outputs();
        }

        private Bucket freeze() {
            return new Bucket(start, end, inputs, outputs);
        }

        private boolean add(boolean input, int resource, long amount, long tick) {
            if (amount == 0L) return true;
            long[] totals = input ? inputs : outputs;
            if (resource >= totals.length) {
                int length = Math.min(MAX_RESOURCES, Math.max(resource + 1, Math.max(4, totals.length * 2)));
                totals = java.util.Arrays.copyOf(totals, length);
                if (input) inputs = totals;
                else outputs = totals;
            }
            if (Long.MAX_VALUE - totals[resource] < amount) return false;
            totals[resource] += amount;
            end = Math.max(end, tick);
            return true;
        }
    }
}
