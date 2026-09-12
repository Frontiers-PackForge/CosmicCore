package com.ghostipedia.cosmiccore.common.rate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

final class RateCalculatorReport {

    private static final int MAX_MACHINES = 256;

    private RateCalculatorReport() {}

    static CompoundTag create(String dimension, ListTag machines, Map<Long, CompoundTag> baselines, long elapsed,
                              int selected, int unloaded, boolean partial) {
        Map<Key, Row> rows = new LinkedHashMap<>();
        ListTag snapshots = new ListTag();
        ListTag unsupportedCapabilities = new ListTag();
        int loaded = 0, unknown = 0;
        double configuredInputEUt = 0, configuredOutputEUt = 0;
        boolean reportPartial = partial, truncated = false;
        for (Tag element : machines) {
            if (!(element instanceof CompoundTag machine)) continue;
            loaded++;
            if (!hasKnownActivity(machine)) unknown++;
            configuredInputEUt += machine.getLong("inputEUt");
            configuredOutputEUt += machine.getLong("outputEUt");
            CompoundTag baseline = baselines.get(machine.getLong("pos"));
            boolean validBaseline = baseline != null &&
                    baseline.getString("machine").equals(machine.getString("machine"));
            boolean machinePartial = machine.getBoolean("stale");
            if (!machine.contains("activity")) machinePartial |= !validBaseline ||
                    machine.getLong("observedInUncertainty") != baseline.getLong("observedInUncertainty") ||
                    machine.getLong("observedOutUncertainty") != baseline.getLong("observedOutUncertainty");
            reportPartial |= machinePartial || machine.getString("configuration").equals("partial");
            if (machine.contains("activity", Tag.TAG_COMPOUND)) {
                CompoundTag activity = machine.getCompound("activity");
                reportPartial |= activity.getBoolean("partialIn") || activity.getBoolean("partialOut");
            }
            for (Tag capability : machine.getList("unsupportedCapabilities", Tag.TAG_STRING)) {
                if (!unsupportedCapabilities.contains(capability)) unsupportedCapabilities.add(capability.copy());
            }
            for (MachineRate rate : rates(machine, baseline, validBaseline, elapsed).values()) {
                reportPartial |= !rate.expectedKnown;
                rows.computeIfAbsent(rate.key, Row::new).add(rate, machine);
            }
            if (snapshots.size() < MAX_MACHINES) snapshots.add(clientSnapshot(machine));
            else truncated = true;
        }
        ListTag rowTags = new ListTag();
        for (Row row : rows.values().stream().sorted(Comparator
                .comparing((Row row) -> row.key.kind)
                .thenComparing(row -> row.key.id)
                .thenComparing(row -> row.key.icon.toString())
                .thenComparing(row -> row.key.alternatives.toString())).toList()) {
            rowTags.add(row.toTag());
        }
        CompoundTag report = new CompoundTag();
        report.putString("dimension", dimension);
        report.put("machines", snapshots);
        report.put("rows", rowTags);
        report.put("unsupportedCapabilities", unsupportedCapabilities);
        report.putLong("elapsedTicks", Math.max(0, elapsed));
        report.putInt("selected", selected);
        report.putInt("loaded", loaded);
        report.putInt("unknown", unknown);
        report.putInt("unloaded", unloaded);
        report.putDouble("configuredInputEUt", configuredInputEUt);
        report.putDouble("configuredOutputEUt", configuredOutputEUt);
        report.putDouble("configuredEUt", configuredInputEUt - configuredOutputEUt);
        report.putBoolean("partial", reportPartial);
        report.putBoolean("truncated", truncated);
        report.putInt("machineCount", snapshots.size());
        report.putInt("rowCount", rowTags.size());
        return truncated ? RateCalculatorReportTransport.unavailable(report) : report;
    }

    private static CompoundTag clientSnapshot(CompoundTag machine) {
        CompoundTag snapshot = machine.copy();
        snapshot.remove("activity");
        snapshot.remove("observedIn");
        snapshot.remove("observedOut");
        if (!snapshot.getList("capacityProfiles", Tag.TAG_COMPOUND).isEmpty()) {
            snapshot.remove("configuredIn");
            snapshot.remove("configuredOut");
        }
        return snapshot;
    }

    private static boolean hasKnownActivity(CompoundTag machine) {
        if (!machine.getString("recipe").isEmpty() && machine.getInt("duration") > 0) return true;
        CompoundTag activity = machine.getCompound("activity");
        for (Tag element : activity.getList("lanes", Tag.TAG_COMPOUND)) {
            CompoundTag lane = (CompoundTag) element;
            if (!lane.getString("recipe").isEmpty() && lane.getLong("duration") > 0 &&
                    (lane.getBoolean("open") || lane.getLong("completed") > 0))
                return true;
        }
        if (activity.getLong("elapsed") <= 0) return false;
        for (Tag element : activity.getList("resources", Tag.TAG_COMPOUND)) {
            CompoundTag resource = (CompoundTag) element;
            if (!resource.getString("kind").equals("state") &&
                    (resource.getLong("in") > 0 || resource.getLong("out") > 0))
                return true;
        }
        return false;
    }

    private static Map<Key, MachineRate> rates(CompoundTag machine, CompoundTag baseline, boolean validBaseline,
                                               long elapsed) {
        if (machine.contains("activity", Tag.TAG_COMPOUND)) return activityRates(machine);
        Map<Key, MachineRate> result = new LinkedHashMap<>();
        configured(result, machine.getList("configuredIn", Tag.TAG_COMPOUND), machine, true);
        configured(result, machine.getList("configuredOut", Tag.TAG_COMPOUND), machine, false);
        for (String direction : new String[] { "observedIn", "observedOut" }) {
            for (Tag element : machine.getList(direction, Tag.TAG_COMPOUND)) {
                CompoundTag resource = (CompoundTag) element;
                result.computeIfAbsent(Key.of(resource), ignored -> new MachineRate(resource));
            }
            if (validBaseline) for (Tag element : baseline.getList(direction, Tag.TAG_COMPOUND)) {
                CompoundTag resource = (CompoundTag) element;
                result.computeIfAbsent(Key.of(resource), ignored -> new MachineRate(resource));
            }
        }
        observed(result, machine.getList("observedIn", Tag.TAG_COMPOUND),
                validBaseline ? baseline.getList("observedIn", Tag.TAG_COMPOUND) : new ListTag(), true, elapsed,
                validBaseline,
                !validBaseline || machine.getLong("observedInUncertainty") !=
                        baseline.getLong("observedInUncertainty"),
                machine.getList("configuredIn", Tag.TAG_COMPOUND));
        observed(result, machine.getList("observedOut", Tag.TAG_COMPOUND),
                validBaseline ? baseline.getList("observedOut", Tag.TAG_COMPOUND) : new ListTag(), false, elapsed,
                validBaseline,
                !validBaseline || machine.getLong("observedOutUncertainty") !=
                        baseline.getLong("observedOutUncertainty"),
                machine.getList("configuredOut", Tag.TAG_COMPOUND));
        return result;
    }

    private static Map<Key, MachineRate> activityRates(CompoundTag machine) {
        Map<Key, MachineRate> result = new LinkedHashMap<>();
        configured(result, machine.getList("configuredIn", Tag.TAG_COMPOUND), machine, true);
        configured(result, machine.getList("configuredOut", Tag.TAG_COMPOUND), machine, false);
        CompoundTag activity = machine.getCompound("activity");
        if (machine.getList("configuredIn", Tag.TAG_COMPOUND).isEmpty() &&
                machine.getList("configuredOut", Tag.TAG_COMPOUND).isEmpty()) {
            for (Tag element : activity.getList("lanes", Tag.TAG_COMPOUND)) {
                CompoundTag lane = (CompoundTag) element;
                for (boolean input : new boolean[] { true, false }) {
                    for (Tag quantity : lane.getList(input ? "ratedIn" : "ratedOut", Tag.TAG_COMPOUND)) {
                        CompoundTag resource = (CompoundTag) quantity;
                        if (resource.getLong("duration") <= 0) continue;
                        MachineRate rate = result.computeIfAbsent(Key.of(resource),
                                ignored -> new MachineRate(resource));
                        double capacity = resource.getLong("amount") / (double) resource.getLong("duration");
                        if (input) {
                            rate.configuredIn += capacity;
                            rate.configuredInRelevant = true;
                        } else {
                            rate.configuredOut += capacity;
                            rate.configuredOutRelevant = true;
                        }
                        rate.configuredKnown = true;
                        rate.recordedCapacity = true;
                    }
                }
            }
        }
        long elapsed = activity.getLong("elapsed");
        boolean mature = elapsed > 0;
        boolean inputPartial = activity.getBoolean("partialIn");
        boolean outputPartial = activity.getBoolean("partialOut");
        String reason = activityReason(activity);
        for (Tag element : activity.getList("resources", Tag.TAG_COMPOUND)) {
            if (!(element instanceof CompoundTag resource) || resource.getString("kind").equals("state")) continue;
            MachineRate rate = result.computeIfAbsent(Key.of(resource), ignored -> new MachineRate(resource));
            boolean learning = resource.getBoolean("learning");
            if (elapsed > 0) {
                rate.observedIn = resource.getLong("in") / (double) elapsed;
                rate.observedOut = resource.getLong("out") / (double) elapsed;
                if (resource.getBoolean("cycleRateAvailable")) {
                    double input = resource.getDouble("cycleInRate"), output = resource.getDouble("cycleOutRate");
                    if (resource.contains("cycleInRate") && Double.isFinite(input) && input >= 0)
                        rate.observedIn = input;
                    if (resource.contains("cycleOutRate") && Double.isFinite(output) && output >= 0)
                        rate.observedOut = output;
                }
            }
            rate.observedInAvailable |= elapsed > 0 &&
                    (resource.getLong("in") > 0 || resource.getBoolean("inputEvidence"));
            rate.observedOutAvailable |= elapsed > 0 &&
                    (resource.getLong("out") > 0 || resource.getBoolean("outputEvidence"));
            rate.inputLearning |= (resource.contains("inputLearning") ? resource.getBoolean("inputLearning") :
                    learning) && rate.observedInAvailable;
            rate.outputLearning |= (resource.contains("outputLearning") ? resource.getBoolean("outputLearning") :
                    learning) && rate.observedOutAvailable;
            rate.reason = reason;
        }
        for (MachineRate rate : result.values()) {
            rate.inputLearning |= elapsed == 0 || rate.configuredInRelevant && !rate.observedInAvailable;
            rate.outputLearning |= elapsed == 0 || rate.configuredOutRelevant && !rate.observedOutAvailable;
            rate.observedInKnown = mature && !inputPartial && !rate.inputLearning;
            rate.observedOutKnown = mature && !outputPartial && !rate.outputLearning;
            rate.observedInAvailable |= rate.observedInKnown;
            rate.observedOutAvailable |= rate.observedOutKnown;
        }
        return result;
    }

    private static String activityReason(CompoundTag activity) {
        for (Tag element : activity.getList("lanes", Tag.TAG_COMPOUND)) if (element instanceof CompoundTag lane) {
            String reason = lane.getString("reason");
            if (!reason.isEmpty() && !reason.equals("unknown")) return reason;
        }
        return "unknown";
    }

    private static void configured(Map<Key, MachineRate> rates, ListTag resources, CompoundTag machine, boolean input) {
        for (Tag element : resources) if (element instanceof CompoundTag resource) {
            MachineRate rate = rates.computeIfAbsent(Key.of(resource), ignored -> new MachineRate(resource));
            rate.configuredKnown |= resource.getBoolean("known");
            if (input) rate.configuredInRelevant = true;
            else rate.configuredOutRelevant = true;
            rate.unresolved |= !resource.getBoolean("known");
            rate.expectedKnown &= !resource.contains("expectedKnown") || resource.getBoolean("expectedKnown");
            if (input) rate.configuredIn += configuredRate(resource, machine);
            else rate.configuredOut += configuredRate(resource, machine);
        }
    }

    private static void observed(Map<Key, MachineRate> rates, ListTag current, ListTag baseline, boolean input,
                                 long elapsed, boolean validBaseline, boolean partial, ListTag configured) {
        Map<Key, Long> currentAmounts = amounts(current), baselineAmounts = amounts(baseline);
        boolean directionKnown = validBaseline && elapsed > 0 && !partial && current.stream()
                .filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast)
                .allMatch(resource -> resource.getBoolean("known"));
        for (MachineRate rate : rates.values()) {
            boolean relevant = input ? rate.configuredInRelevant : rate.configuredOutRelevant;
            relevant |= currentAmounts.containsKey(rate.key) || baselineAmounts.containsKey(rate.key) ||
                    matchesAlternative(rate.key, configured);
            if (input) rate.observedInKnown |= directionKnown || !relevant && !partial;
            else rate.observedOutKnown |= directionKnown || !relevant && !partial;
            if (input) rate.observedInAvailable |= directionKnown && relevant;
            else rate.observedOutAvailable |= directionKnown && relevant;
        }
        for (Tag element : current) if (element instanceof CompoundTag resource) {
            Key key = Key.of(resource);
            MachineRate rate = rates.computeIfAbsent(key, ignored -> new MachineRate(resource));
            rate.unresolved |= !resource.getBoolean("known");
            if (!validBaseline || elapsed <= 0 || !resource.getBoolean("known")) continue;
            double delta = Math.max(0L,
                    subtract(currentAmounts.getOrDefault(key, 0L), baselineAmounts.getOrDefault(key, 0L))) /
                    (double) elapsed;
            if (input) rate.observedIn = delta;
            else rate.observedOut = delta;
            if (input) rate.observedInAvailable = true;
            else rate.observedOutAvailable = true;
        }
    }

    private static boolean matchesAlternative(Key key, ListTag configured) {
        if (key.alternatives.size() > 0) return false;
        for (Tag element : configured) if (element instanceof CompoundTag resource &&
                resource.getString("kind").equals(key.kind)) {
                    for (Tag alternative : resource.getList("alternatives", Tag.TAG_COMPOUND)) {
                        CompoundTag choice = (CompoundTag) alternative;
                        Tag choiceIcon = choice.get("icon");
                        if (choice.getString("id").equals(key.id) && choiceIcon != null && choiceIcon.equals(key.icon))
                            return true;
                    }
                }
        return false;
    }

    private static Map<Key, Long> amounts(ListTag resources) {
        Map<Key, Long> result = new LinkedHashMap<>();
        for (Tag element : resources) if (element instanceof CompoundTag resource) {
            result.merge(Key.of(resource), resource.getLong("amount"), RateCalculatorReport::add);
        }
        return result;
    }

    private static long add(long left, long right) {
        if (right > 0 && left > Long.MAX_VALUE - right) return Long.MAX_VALUE;
        if (right < 0 && left < Long.MIN_VALUE - right) return Long.MIN_VALUE;
        return left + right;
    }

    private static long subtract(long left, long right) {
        if (right > 0 && left < Long.MIN_VALUE + right) return Long.MIN_VALUE;
        if (right < 0 && left > Long.MAX_VALUE + right) return Long.MAX_VALUE;
        return left - right;
    }

    private static double configuredRate(CompoundTag resource, CompoundTag machine) {
        int chance = resource.getInt("chance"), maxChance = resource.getInt("maxChance");
        if (maxChance <= 0) return 0;
        double amount = resource.getLong("amount") * (chance / (double) maxChance);
        if (chance < maxChance) amount *= Math.max(1, machine.getInt("totalRuns"));
        return resource.getBoolean("perTick") ? amount :
                machine.getInt("duration") > 0 ? amount / machine.getInt("duration") : 0;
    }

    private record Key(String kind, String id, Tag icon, ListTag alternatives) {

        static Key of(CompoundTag resource) {
            Tag icon = resource.get("icon");
            return new Key(resource.getString("kind"), resource.getString("id"),
                    icon == null ? new CompoundTag() : icon.copy(),
                    resource.getList("alternatives", Tag.TAG_COMPOUND).copy());
        }
    }

    private static final class MachineRate {

        private final Key key;
        private boolean configuredKnown, expectedKnown = true, observedInKnown, observedOutKnown, unresolved,
                configuredInRelevant, configuredOutRelevant;
        private boolean observedInAvailable, observedOutAvailable;
        private boolean inputLearning, outputLearning;
        private boolean recordedCapacity;
        private String reason = "unknown";
        private double configuredIn, configuredOut, observedIn, observedOut;

        private MachineRate(CompoundTag resource) {
            key = Key.of(resource);
        }
    }

    private static final class Row {

        private final Key key;
        private final ListTag contributors = new ListTag();
        private boolean configuredKnown, expectedKnown = true, observedInKnown = true, observedOutKnown = true,
                unresolved;
        private boolean observedInAvailable, observedOutAvailable;
        private double configuredIn, configuredOut, observedIn, observedOut;

        private Row(Key key) {
            this.key = key;
        }

        private void add(MachineRate rate, CompoundTag machine) {
            configuredKnown |= rate.configuredKnown;
            expectedKnown &= rate.expectedKnown;
            observedInKnown &= rate.observedInKnown;
            observedOutKnown &= rate.observedOutKnown;
            observedInAvailable |= rate.observedInAvailable;
            observedOutAvailable |= rate.observedOutAvailable;
            unresolved |= rate.unresolved;
            configuredIn += rate.configuredIn;
            configuredOut += rate.configuredOut;
            observedIn += rate.observedIn;
            observedOut += rate.observedOut;
            CompoundTag contributor = new CompoundTag();
            contributor.putLong("pos", machine.getLong("pos"));
            ListTag positions = new ListTag();
            positions.add(LongTag.valueOf(machine.getLong("pos")));
            contributor.put("positions", positions);
            contributor.putString("machine", machine.getString("machine"));
            contributor.putString("recipe", machine.getString("recipe"));
            contributor.putLong("voltage", machine.getLong("voltage"));
            contributor.putLong("amperage", machine.getLong("amperage"));
            contributor.putInt("duration", machine.getInt("duration"));
            contributor.putDouble("configuredIn", rate.configuredIn);
            contributor.putDouble("configuredOut", rate.configuredOut);
            contributor.putDouble("observedIn", rate.observedIn);
            contributor.putDouble("observedOut", rate.observedOut);
            contributor.putDouble("configuredNet", rate.configuredOut - rate.configuredIn);
            contributor.putDouble("observedNet", rate.observedOut - rate.observedIn);
            contributor.putBoolean("configuredKnown", rate.configuredKnown && !rate.unresolved);
            contributor.putBoolean("expectedKnown", rate.expectedKnown);
            contributor.putBoolean("observedInKnown", rate.observedInKnown);
            contributor.putBoolean("observedOutKnown", rate.observedOutKnown);
            contributor.putBoolean("observedKnown", rate.observedInKnown && rate.observedOutKnown);
            contributor.putBoolean("observedInAvailable", rate.observedInAvailable);
            contributor.putBoolean("observedOutAvailable", rate.observedOutAvailable);
            contributor.putBoolean("observedAvailable", rate.observedInAvailable && rate.observedOutAvailable);
            contributor.putBoolean("learning", rate.inputLearning || rate.outputLearning);
            contributor.putBoolean("recordedCapacity", rate.recordedCapacity);
            CompoundTag activity = machine.getCompound("activity");
            contributor.putLong("activityElapsed", activity.getLong("elapsed"));
            contributor.putLong("activityHorizon", activity.getLong("horizon"));
            contributor.putBoolean("activityFresh", activity.getBoolean("fresh"));
            long completed = 0;
            for (Tag element : activity.getList("lanes", Tag.TAG_COMPOUND)) {
                CompoundTag lane = (CompoundTag) element;
                completed += lane.getLong("completed");
                if (contributor.getString("recipe").isEmpty())
                    contributor.putString("recipe", lane.getString("recipe"));
            }
            contributor.putLong("activityCompleted", completed);
            contributor.putString("reason", rate.reason);
            contributors.add(contributor);
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("kind", key.kind);
            tag.putString("id", key.id);
            tag.put("icon", key.icon.copy());
            tag.put("alternatives", key.alternatives.copy());
            tag.putBoolean("known", !unresolved);
            tag.putBoolean("configuredKnown", configuredKnown && !unresolved);
            tag.putBoolean("expectedKnown", expectedKnown);
            tag.putBoolean("observedInKnown", observedInKnown);
            tag.putBoolean("observedOutKnown", observedOutKnown);
            tag.putBoolean("observedKnown", observedInKnown && observedOutKnown);
            tag.putBoolean("observedInAvailable", observedInAvailable);
            tag.putBoolean("observedOutAvailable", observedOutAvailable);
            tag.putBoolean("observedAvailable", observedInAvailable && observedOutAvailable);
            tag.putDouble("configuredIn", configuredIn);
            tag.putDouble("configuredOut", configuredOut);
            tag.putDouble("observedIn", observedIn);
            tag.putDouble("observedOut", observedOut);
            tag.putDouble("configuredNet", configuredOut - configuredIn);
            tag.putDouble("observedNet", observedOut - observedIn);
            tag.putBoolean("unresolvedNet", unresolved);
            tag.putBoolean("learning", !observedInKnown || !observedOutKnown);
            ListTag orderedContributors = new ListTag();
            contributors.stream().map(CompoundTag.class::cast)
                    .sorted(Comparator.comparingLong((CompoundTag contributor) -> contributor.getLong("pos"))
                            .thenComparing(contributor -> contributor.getString("machine")))
                    .forEach(orderedContributors::add);
            tag.put("contributors", orderedContributors);
            return tag;
        }
    }
}
