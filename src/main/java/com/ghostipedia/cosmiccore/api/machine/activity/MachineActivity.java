package com.ghostipedia.cosmiccore.api.machine.activity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class MachineActivity implements INBTSerializable<CompoundTag> {

    public static final int MAX_RESOURCES = 64;
    public static final int MAX_LANES = 16;
    private static final int MAX_BYTES = 131_072;
    private static final int MAX_RESOURCE_BYTES = 4096;
    private ActivityLedger ledger = new ActivityLedger();
    private final ActivityResource[] resources = new ActivityResource[MAX_RESOURCES];
    private final int[] evidence = new int[MAX_RESOURCES];
    private final int[] outputEvidence = new int[MAX_RESOURCES];
    private final boolean[] outsideInputs = new boolean[MAX_RESOURCES];
    private final boolean[] outsideOutputs = new boolean[MAX_RESOURCES];
    private final int[] identitySizes = new int[MAX_RESOURCES];
    private int identityBytes;
    private final Lane[] lanes = new Lane[MAX_LANES];
    private HolderLookup.Provider registries;
    private boolean fresh;
    private long lastWorldTick = Long.MIN_VALUE;
    private long lastPrune;
    private boolean corrupt;

    public void loaded(HolderLookup.Provider lookup) {
        registries = lookup;
        lastWorldTick = Long.MIN_VALUE;
        fresh = false;
        for (Lane lane : lanes) if (lane != null) lane.validatedReason = false;
    }

    public void tick(long worldTick, HolderLookup.Provider lookup) {
        registries = lookup;
        if (worldTick == lastWorldTick) return;
        lastWorldTick = worldTick;
        ledger.advanceTick();
        if (ledger.clock() - lastPrune >= 1200) prune();
    }

    public long clock() {
        return ledger.clock();
    }

    public void begin(int index, String operation, long duration) {
        Lane lane = lane(index);
        if (lane == null) return;
        String id = operation == null || operation.length() > 256 ? "" : operation;
        if (!lane.operation.equals(id)) {
            lane.switched = ledger.clock();
            lane.resetEstimator();
        }
        lane.operation = id;
        lane.duration = Math.max(1, duration);
        lane.open = true;
        lane.started = ledger.clock();
        Arrays.fill(lane.inputs, 0);
        Arrays.fill(lane.outputs, 0);
        fresh = true;
        horizon();
    }

    public void complete(int index) {
        Lane lane = lane(index);
        if (lane == null || !lane.open) return;
        long now = ledger.clock();
        lane.durations[lane.sample % 8] = lane.duration;
        if (lane.completed > 0 && now > lane.lastCompleted)
            lane.intervals[lane.sample % 8] = now - lane.lastCompleted;
        lane.sample = (lane.sample + 1) % 8;
        lane.completed = Math.min(Integer.MAX_VALUE, lane.completed + 1L);
        lane.lastCompleted = now;
        lane.lastOperation = lane.operation;
        lane.lastDuration = lane.duration;
        System.arraycopy(lane.inputs, 0, lane.lastInputs, 0, MAX_RESOURCES);
        System.arraycopy(lane.outputs, 0, lane.lastOutputs, 0, MAX_RESOURCES);
        lane.completeEstimator(now);
        for (int i = 0; i < MAX_RESOURCES; i++) {
            if (lane.inputs[i] > 0) evidence[i] = Math.min(2, evidence[i] + 1);
            if (lane.outputs[i] > 0) outputEvidence[i] = Math.min(2, outputEvidence[i] + 1);
        }
        lane.open = false;
        fresh = true;
        horizon();
    }

    public void interrupt(int index) {
        Lane lane = lane(index);
        if (lane != null) lane.open = false;
    }

    public void failure(int index, String reason) {
        Lane lane = lane(index);
        if (lane == null) return;
        lane.reason = reason;
        lane.validatedReason = true;
    }

    public void state(int index, String status, int progress) {
        Lane lane = lane(index);
        if (lane == null) return;
        lane.status = status;
        lane.progress = progress;
        String reason;
        if (status.equals("working")) {
            reason = "working";
            lane.validatedReason = false;
        } else if (status.equals("suspend")) reason = "disabled";
        else reason = lane.validatedReason ? lane.reason : "unknown";
        recordValue(-1, "state", reason, 1, true);
    }

    public void recordItem(int lane, ItemStack stack, long amount, boolean input) {
        if (stack.isEmpty()) return;
        record(lane, find(resource -> resource.matches(stack), () -> ActivityResource.item(stack), input), amount,
                input);
    }

    public void recordFluid(int lane, FluidStack stack, long amount, boolean input) {
        if (stack.isEmpty()) return;
        record(lane, find(resource -> resource.matches(stack), () -> ActivityResource.fluid(stack), input), amount,
                input);
    }

    public void recordValue(int lane, String kind, String id, long amount, boolean input) {
        record(lane, find(resource -> resource.matches(kind, id), () -> ActivityResource.value(kind, id), input),
                amount, input);
    }

    public void markPartial(boolean input) {
        ledger.markPartial(input);
    }

    private void record(int index, int resource, long amount, boolean input) {
        if (resource < 0 || amount <= 0) return;
        if (!ledger.record(resource, input, amount)) return;
        if (!resources[resource].kind().equals("state")) fresh = true;
        if (index < 0 || index >= MAX_LANES) {
            if (!resources[resource].kind().equals("state")) markOutside(resource, input);
            return;
        }
        Lane lane = lanes[index];
        if (lane == null || !lane.open) {
            if (!resources[resource].kind().equals("state")) markOutside(resource, input);
            return;
        }
        long[] quantities = input ? lane.inputs : lane.outputs;
        if (quantities[resource] > Long.MAX_VALUE - amount) {
            quantities[resource] = Long.MAX_VALUE;
            markPartial(input);
        } else quantities[resource] += amount;
        lane.recordEstimator(resource, amount, input);
    }

    private void markOutside(int resource, boolean input) {
        if (input) outsideInputs[resource] = true;
        else outsideOutputs[resource] = true;
    }

    private int find(Predicate<ActivityResource> match, Supplier<ActivityResource> create, boolean input) {
        int free = -1;
        for (int i = 0; i < resources.length; i++) {
            if (resources[i] == null) {
                if (free == -1) free = i;
            } else if (match.test(resources[i])) return i;
        }
        if (free < 0) {
            prune();
            for (int i = 0; i < resources.length; i++) if (resources[i] == null) {
                free = i;
                break;
            }
        }
        if (free < 0 || registries == null) {
            markPartial(input);
            return -1;
        }
        ActivityResource resource = create.get();
        int size = encodedSize(resource.toTag(registries));
        if (size > MAX_RESOURCE_BYTES || identityBytes + size > 65_536) {
            markPartial(input);
            return -1;
        }
        resources[free] = resource;
        identitySizes[free] = size;
        identityBytes += size;
        evidence[free] = 0;
        outputEvidence[free] = 0;
        return free;
    }

    private Lane lane(int index) {
        if (index < 0 || index >= MAX_LANES) return null;
        if (lanes[index] == null) lanes[index] = new Lane();
        return lanes[index];
    }

    private void horizon() {
        long scale = 1;
        for (Lane lane : lanes) if (lane != null) {
            if (lane.open) scale = Math.max(scale, lane.duration);
            for (long duration : lane.durations) scale = Math.max(scale, duration);
            long[] intervals = Arrays.stream(lane.intervals).filter(value -> value > 0).sorted().toArray();
            if (intervals.length > 0) scale = Math.max(scale, intervals[intervals.length / 2]);
        }
        ledger.setHorizon(scale > Long.MAX_VALUE / 8 ? Long.MAX_VALUE : Math.max(1200, scale * 8));
    }

    private void prune() {
        boolean[] retained = new boolean[MAX_RESOURCES];
        for (int resource : ledger.retainedResources()) retained[resource] = true;
        for (Lane lane : lanes) if (lane != null) for (int i = 0; i < MAX_RESOURCES; i++)
            if (lane.lastInputs[i] > 0 || lane.lastOutputs[i] > 0 ||
                    lane.open && (lane.inputs[i] > 0 || lane.outputs[i] > 0))
                retained[i] = true;
        for (int i = 0; i < MAX_RESOURCES; i++) if (!retained[i]) {
            identityBytes -= identitySizes[i];
            identitySizes[i] = 0;
            resources[i] = null;
            evidence[i] = 0;
            outputEvidence[i] = 0;
            outsideInputs[i] = false;
            outsideOutputs[i] = false;
            for (Lane lane : lanes) if (lane != null) lane.clearEstimatorResource(i);
        }
        lastPrune = ledger.clock();
    }

    public CompoundTag snapshot(HolderLookup.Provider lookup) {
        ActivityLedger.Snapshot snapshot = ledger.snapshot();
        CompoundTag tag = new CompoundTag();
        tag.putLong("elapsed", snapshot.elapsedTicks());
        tag.putLong("horizon", snapshot.horizon());
        tag.putBoolean("fresh", fresh);
        tag.putBoolean("partialIn", corrupt || snapshot.input().partial());
        tag.putBoolean("partialOut", corrupt || snapshot.output().partial());
        tag.putBoolean("coarse", snapshot.input().coarse() || snapshot.output().coarse());
        ListTag totals = new ListTag();
        long[] inputs = new long[MAX_RESOURCES], outputs = new long[MAX_RESOURCES];
        for (ActivityLedger.ResourceTotal total : snapshot.totals()) {
            inputs[total.resource()] = total.input();
            outputs[total.resource()] = total.output();
        }
        for (int i = 0; i < MAX_RESOURCES; i++) {
            ActivityResource resource = resources[i];
            if (resource == null) continue;
            CompoundTag entry = resource.toTag(lookup);
            entry.putLong("in", inputs[i]);
            entry.putLong("out", outputs[i]);
            entry.putBoolean("inputLearning", evidence[i] < 2);
            entry.putBoolean("outputLearning", outputEvidence[i] < 2);
            entry.putBoolean("inputEvidence", evidence[i] > 0 || inputs[i] > 0);
            entry.putBoolean("outputEvidence", outputEvidence[i] > 0 || outputs[i] > 0);
            entry.putBoolean("learning", evidence[i] < 2 && outputEvidence[i] < 2);
            CycleRates rates = cycleRates(i);
            if (rates.inputAvailable) entry.putDouble("cycleInRate", rates.inputRate);
            if (rates.outputAvailable) entry.putDouble("cycleOutRate", rates.outputRate);
            entry.putBoolean("cycleRateAvailable", rates.inputAvailable || rates.outputAvailable);
            totals.add(entry);
        }
        ListTag remembered = new ListTag();
        long completed = 0;
        boolean mixed = false;
        for (int i = 0; i < MAX_LANES; i++) {
            Lane lane = lanes[i];
            if (lane == null) continue;
            completed += lane.completed;
            mixed |= lane.switched >= snapshot.startTick() && lane.switched > 0;
            CompoundTag entry = new CompoundTag();
            entry.putInt("lane", i);
            entry.putString("recipe", lane.open ? lane.operation : lane.lastOperation);
            entry.putLong("duration", lane.open ? lane.duration : lane.lastDuration);
            entry.putString("status", lane.status);
            entry.putString("reason", lane.status.equals("working") ? "working" : lane.status.equals("suspend") ?
                    "disabled" : lane.validatedReason ? lane.reason : "unknown");
            entry.putInt("progress", lane.progress);
            entry.putBoolean("open", lane.open);
            entry.putLong("completed", lane.completed);
            entry.putLong("sinceCompletion", lane.completed > 0 ? ledger.clock() - lane.lastCompleted : -1);
            entry.put("ratedIn", quantities(lane.lastInputs, lane.lastDuration, lookup));
            entry.put("ratedOut", quantities(lane.lastOutputs, lane.lastDuration, lookup));
            remembered.add(entry);
        }
        tag.put("resources", totals);
        tag.put("lanes", remembered);
        tag.putBoolean("learning", completed < 2 || snapshot.elapsedTicks() == 0);
        tag.putBoolean("mixed", mixed);
        return tag;
    }

    private CycleRates cycleRates(int resource) {
        CycleRates rates = new CycleRates();
        for (Lane lane : lanes) if (lane != null) lane.addCycleRates(resource, ledger.clock(), rates);
        rates.inputBlocked |= outsideInputs[resource];
        rates.outputBlocked |= outsideOutputs[resource];
        if (rates.inputBlocked) rates.inputAvailable = false;
        if (rates.outputBlocked) rates.outputAvailable = false;
        return rates;
    }

    private ListTag quantities(long[] quantities, long duration, HolderLookup.Provider lookup) {
        ListTag result = new ListTag();
        if (duration <= 0) return result;
        for (int i = 0; i < MAX_RESOURCES; i++) if (quantities[i] > 0 && resources[i] != null) {
            CompoundTag entry = resources[i].toTag(lookup);
            entry.putLong("amount", quantities[i]);
            entry.putLong("duration", duration);
            result.add(entry);
        }
        return result;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", 1);
        ActivityLedger.State state = ledger.saveState();
        tag.putLong("clock", state.clock());
        tag.putLong("horizon", state.horizon());
        tag.putLong("width", state.bucketWidth());
        tag.putLong("partialInTick", corrupt ? state.clock() : state.inputPartialTick());
        tag.putLong("partialOutTick", corrupt ? state.clock() : state.outputPartialTick());
        tag.putBoolean("coarseIn", state.inputCoarse());
        tag.putBoolean("coarseOut", state.outputCoarse());
        ListTag identities = new ListTag();
        for (int i = 0; i < MAX_RESOURCES; i++) if (resources[i] != null) {
            CompoundTag entry = resources[i].toTag(lookup);
            entry.putInt("slot", i);
            entry.putInt("evidence", evidence[i]);
            entry.putInt("outputEvidence", outputEvidence[i]);
            identities.add(entry);
        }
        tag.put("identities", identities);
        ListTag laneTags = new ListTag();
        for (int i = 0; i < MAX_LANES; i++) if (lanes[i] != null) {
            CompoundTag lane = lanes[i].save();
            lane.putInt("slot", i);
            laneTags.add(lane);
        }
        tag.put("lanes", laneTags);
        ListTag buckets = new ListTag();
        for (ActivityLedger.Bucket bucket : state.buckets()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("start", bucket.start());
            entry.putLong("end", bucket.end());
            entry.putLongArray("in", bucket.inputs());
            entry.putLongArray("out", bucket.outputs());
            buckets.add(entry);
        }
        tag.put("buckets", buckets);
        while (encodedSize(tag) > MAX_BYTES && !buckets.isEmpty()) {
            buckets.remove(0);
            tag.putLong("partialInTick", state.clock());
            tag.putLong("partialOutTick", state.clock());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider lookup, CompoundTag tag) {
        registries = lookup;
        Arrays.fill(resources, null);
        Arrays.fill(evidence, 0);
        Arrays.fill(outputEvidence, 0);
        Arrays.fill(outsideInputs, false);
        Arrays.fill(outsideOutputs, false);
        Arrays.fill(identitySizes, 0);
        identityBytes = 0;
        corrupt = false;
        lastPrune = 0;
        Arrays.fill(lanes, null);
        ledger = new ActivityLedger();
        fresh = false;
        lastWorldTick = Long.MIN_VALUE;
        if (tag.isEmpty()) return;
        if (tag.getInt("schema") != 1 || encodedSize(tag) > MAX_BYTES) {
            corrupt = true;
            return;
        }
        try {
            ListTag identities = tag.getList("identities", Tag.TAG_COMPOUND);
            if (identities.size() > MAX_RESOURCES) throw new IllegalArgumentException();
            for (Tag element : identities) {
                CompoundTag entry = (CompoundTag) element;
                int slot = entry.getInt("slot");
                if (slot < 0 || slot >= MAX_RESOURCES || encodedSize(entry) > MAX_RESOURCE_BYTES + 64)
                    throw new IllegalArgumentException();
                resources[slot] = ActivityResource.fromTag(entry, lookup);
                if (resources[slot] == null || identitySizes[slot] != 0) throw new IllegalArgumentException();
                identitySizes[slot] = encodedSize(resources[slot].toTag(lookup));
                identityBytes += identitySizes[slot];
                if (identityBytes > 65_536) throw new IllegalArgumentException();
                evidence[slot] = Math.clamp(entry.getInt("evidence"), 0, 2);
                outputEvidence[slot] = Math.clamp(entry.getInt("outputEvidence"), 0, 2);
            }
            ListTag laneTags = tag.getList("lanes", Tag.TAG_COMPOUND);
            if (laneTags.size() > MAX_LANES) throw new IllegalArgumentException();
            for (Tag element : laneTags) {
                CompoundTag entry = (CompoundTag) element;
                int slot = entry.getInt("slot");
                if (slot < 0 || slot >= MAX_LANES || lanes[slot] != null) throw new IllegalArgumentException();
                lanes[slot] = Lane.load(entry);
                validateReferences(lanes[slot].inputs, lanes[slot].outputs);
                validateReferences(lanes[slot].lastInputs, lanes[slot].lastOutputs);
            }
            ListTag bucketTags = tag.getList("buckets", Tag.TAG_COMPOUND);
            if (bucketTags.size() > 128) throw new IllegalArgumentException();
            List<ActivityLedger.Bucket> buckets = new ArrayList<>();
            long previousEnd = -1;
            for (Tag element : bucketTags) {
                CompoundTag entry = (CompoundTag) element;
                long[] in = entry.getLongArray("in"), out = entry.getLongArray("out");
                if (in.length > MAX_RESOURCES || out.length > MAX_RESOURCES) throw new IllegalArgumentException();
                validateReferences(in, out);
                if (entry.getLong("start") <= previousEnd) throw new IllegalArgumentException();
                previousEnd = entry.getLong("end");
                buckets.add(new ActivityLedger.Bucket(entry.getLong("start"), entry.getLong("end"), in, out));
            }
            ledger = ActivityLedger.restore(new ActivityLedger.State(tag.getLong("clock"), tag.getLong("horizon"),
                    tag.getLong("width"), tag.getLong("partialInTick"), tag.getLong("partialOutTick"),
                    tag.getBoolean("coarseIn"), tag.getBoolean("coarseOut"), buckets));
        } catch (RuntimeException exception) {
            ledger = new ActivityLedger();
            Arrays.fill(resources, null);
            Arrays.fill(lanes, null);
            corrupt = true;
        }
    }

    private static int encodedSize(CompoundTag tag) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            NbtIo.write(tag, new DataOutputStream(bytes));
            return bytes.size();
        } catch (IOException | RuntimeException exception) {
            return Integer.MAX_VALUE;
        }
    }

    private void validateReferences(long[] inputs, long[] outputs) {
        for (int i = 0; i < Math.max(inputs.length, outputs.length); i++)
            if (resources[i] == null && (i < inputs.length && inputs[i] != 0 || i < outputs.length && outputs[i] != 0))
                throw new IllegalArgumentException();
    }

    private static final class Lane {

        private String operation = "", lastOperation = "", status = "idle", reason = "unknown";
        private boolean open, validatedReason;
        private long duration = 1, lastDuration, started, lastCompleted, completed, switched;
        private int sample, progress;
        private final long[] durations = new long[8], intervals = new long[8];
        private final long[] inputs = new long[MAX_RESOURCES], outputs = new long[MAX_RESOURCES];
        private final long[] lastInputs = new long[MAX_RESOURCES], lastOutputs = new long[MAX_RESOURCES];
        private final long[] estimatorInputs = new long[MAX_RESOURCES], estimatorOutputs = new long[MAX_RESOURCES];
        private final boolean[] estimatorInputTouched = new boolean[MAX_RESOURCES],
                estimatorOutputTouched = new boolean[MAX_RESOURCES];
        private final long[][] estimatorInputSamples = new long[8][MAX_RESOURCES];
        private final long[][] estimatorOutputSamples = new long[8][MAX_RESOURCES];
        private final long[] estimatorIntervals = new long[8];
        private long estimatorLastCompletion;
        private int estimatorSamples, estimatorNext;
        private boolean estimatorBoundary;

        private void resetEstimator() {
            Arrays.fill(estimatorInputs, 0);
            Arrays.fill(estimatorOutputs, 0);
            Arrays.fill(estimatorInputTouched, false);
            Arrays.fill(estimatorOutputTouched, false);
            for (long[] sample : estimatorInputSamples) Arrays.fill(sample, 0);
            for (long[] sample : estimatorOutputSamples) Arrays.fill(sample, 0);
            Arrays.fill(estimatorIntervals, 0);
            estimatorLastCompletion = 0;
            estimatorSamples = 0;
            estimatorNext = 0;
            estimatorBoundary = false;
        }

        private void recordEstimator(int resource, long amount, boolean input) {
            long[] totals = input ? estimatorInputs : estimatorOutputs;
            boolean[] touched = input ? estimatorInputTouched : estimatorOutputTouched;
            touched[resource] = true;
            totals[resource] = totals[resource] > Long.MAX_VALUE - amount ? Long.MAX_VALUE : totals[resource] + amount;
        }

        private void completeEstimator(long now) {
            if (!estimatorBoundary) {
                estimatorBoundary = true;
                estimatorLastCompletion = now;
                Arrays.fill(estimatorInputs, 0);
                Arrays.fill(estimatorOutputs, 0);
                return;
            }
            long interval = now - estimatorLastCompletion;
            if (interval <= 0) return;
            estimatorIntervals[estimatorNext] = interval;
            System.arraycopy(estimatorInputs, 0, estimatorInputSamples[estimatorNext], 0, MAX_RESOURCES);
            System.arraycopy(estimatorOutputs, 0, estimatorOutputSamples[estimatorNext], 0, MAX_RESOURCES);
            Arrays.fill(estimatorInputs, 0);
            Arrays.fill(estimatorOutputs, 0);
            estimatorLastCompletion = now;
            estimatorNext = (estimatorNext + 1) % estimatorIntervals.length;
            estimatorSamples = Math.min(estimatorIntervals.length, estimatorSamples + 1);
        }

        private void clearEstimatorResource(int resource) {
            estimatorInputs[resource] = 0;
            estimatorOutputs[resource] = 0;
            estimatorInputTouched[resource] = false;
            estimatorOutputTouched[resource] = false;
            for (long[] sample : estimatorInputSamples) sample[resource] = 0;
            for (long[] sample : estimatorOutputSamples) sample[resource] = 0;
        }

        private void addCycleRates(int resource, long now, CycleRates rates) {
            addCycleRate(resource, now, true, rates);
            addCycleRate(resource, now, false, rates);
        }

        private void addCycleRate(int resource, long now, boolean input, CycleRates rates) {
            boolean touched = input ? estimatorInputTouched[resource] : estimatorOutputTouched[resource];
            if (!touched) return;
            if (estimatorSamples < 2 || !estimatorBoundary) {
                if (input) rates.inputBlocked = true;
                else rates.outputBlocked = true;
                return;
            }
            long quantities = 0, intervals = 0;
            long[][] samples = input ? estimatorInputSamples : estimatorOutputSamples;
            for (int i = 0; i < estimatorSamples; i++) {
                int slot = Math.floorMod(estimatorNext - 1 - i, estimatorIntervals.length);
                intervals = addBounded(intervals, estimatorIntervals[slot]);
                quantities = addBounded(quantities, samples[slot][resource]);
            }
            if (intervals <= 0) return;
            long cadence = Math.max(1, intervals / estimatorSamples);
            long overdue = Math.max(0, now - estimatorLastCompletion - cadence);
            double rate = quantities / (double) addBounded(intervals, overdue);
            if (input) {
                rates.inputAvailable = true;
                rates.inputRate += rate;
            } else {
                rates.outputAvailable = true;
                rates.outputRate += rate;
            }
        }

        private static long addBounded(long left, long right) {
            return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("operation", operation);
            tag.putString("lastOperation", lastOperation);
            tag.putBoolean("open", open);
            tag.putLong("duration", duration);
            tag.putLong("lastDuration", lastDuration);
            tag.putLong("started", started);
            tag.putLong("lastCompleted", lastCompleted);
            tag.putLong("completed", completed);
            tag.putLong("switched", switched);
            tag.putInt("sample", sample);
            tag.putLongArray("durations", durations);
            tag.putLongArray("intervals", intervals);
            tag.putLongArray("in", inputs);
            tag.putLongArray("out", outputs);
            tag.putLongArray("lastIn", lastInputs);
            tag.putLongArray("lastOut", lastOutputs);
            return tag;
        }

        private static Lane load(CompoundTag tag) {
            Lane lane = new Lane();
            lane.operation = tag.getString("operation");
            lane.lastOperation = tag.getString("lastOperation");
            if (lane.operation.length() > 256 || lane.lastOperation.length() > 256)
                throw new IllegalArgumentException();
            lane.open = tag.getBoolean("open");
            lane.duration = Math.max(1, tag.getLong("duration"));
            lane.lastDuration = Math.max(0, tag.getLong("lastDuration"));
            lane.started = Math.max(0, tag.getLong("started"));
            lane.lastCompleted = Math.max(0, tag.getLong("lastCompleted"));
            lane.completed = Math.max(0, tag.getLong("completed"));
            lane.switched = Math.max(0, tag.getLong("switched"));
            lane.sample = Math.floorMod(tag.getInt("sample"), 8);
            copy(tag, "durations", lane.durations);
            copy(tag, "intervals", lane.intervals);
            copy(tag, "in", lane.inputs);
            copy(tag, "out", lane.outputs);
            copy(tag, "lastIn", lane.lastInputs);
            copy(tag, "lastOut", lane.lastOutputs);
            return lane;
        }

        private static void copy(CompoundTag tag, String key, long[] target) {
            long[] values = tag.getLongArray(key);
            if (values.length > target.length || Arrays.stream(values).anyMatch(value -> value < 0))
                throw new IllegalArgumentException();
            System.arraycopy(values, 0, target, 0, values.length);
        }
    }

    private static final class CycleRates {

        private double inputRate, outputRate;
        private boolean inputAvailable, outputAvailable, inputBlocked, outputBlocked;
    }
}
