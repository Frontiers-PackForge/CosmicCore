package com.ghostipedia.cosmiccore.client.rate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class RateCalculatorSimulation {

    private static final double EPSILON = 1.0E-9;
    private static final int MAX_ITERATIONS = 64;
    private List<Group> groups = List.of();
    private List<CompoundTag> rows = List.of();
    private double electricalEUt;
    private boolean partial;

    void update(CompoundTag report) {
        Map<String, Group> merged = new LinkedHashMap<>();
        for (Tag element : report.getList("machines", Tag.TAG_COMPOUND)) {
            CompoundTag machine = (CompoundTag) element;
            ListTag profiles = profiles(machine);
            int tier = machine.contains("overclockTier") ? machine.getInt("overclockTier") : -1;
            String key = machine.getString("machine") + "\u0000" + machine.getString("recipe") + "\u0000" +
                    tier + "\u0000" + profiles;
            Group group = merged.computeIfAbsent(key,
                    ignored -> new Group(key, machine.getString("machine"), machine.getString("recipe"), tier,
                            profiles));
            group.actualCount++;
            group.positions.add(machine.getLong("pos"));
        }
        for (Group group : merged.values()) group.selectTier(group.currentTier);
        groups = List.copyOf(merged.values());
        rebuild();
    }

    List<CompoundTag> rows() {
        return rows;
    }

    double electricalEUt() {
        return electricalEUt;
    }

    boolean partial() {
        return partial;
    }

    private void rebuild() {
        for (Group group : groups) group.loadRates();
        boolean solved = solveUtilization();
        partial = !solved;
        Map<ResourceKey, Row> result = new LinkedHashMap<>();
        electricalEUt = 0;
        for (Group group : groups) {
            CompoundTag profile = group.profile();
            electricalEUt += (profile.getLong("inputEUt") - profile.getLong("outputEUt")) * group.actualCount *
                    group.utilization;
            for (GroupRate rate : group.rates.values())
                result.computeIfAbsent(rate.key, Row::new).add(group, profile, rate, solved);
        }
        List<CompoundTag> tags = new ArrayList<>(result.size());
        result.values().forEach(row -> tags.add(row.toTag()));
        rows = List.copyOf(tags);
    }

    private boolean solveUtilization() {
        Map<ResourceKey, List<Group>> producers = new LinkedHashMap<>();
        Map<ResourceKey, List<Group>> consumers = new LinkedHashMap<>();
        Map<ResourceKey, Double> fullDemand = new LinkedHashMap<>();
        boolean complete = true;
        for (Group group : groups) {
            group.utilization = 1;
            for (GroupRate rate : group.rates.values()) {
                if (!rate.known || !rate.expectedKnown) complete = false;
                if (rate.output > EPSILON) producers.computeIfAbsent(rate.key, ignored -> new ArrayList<>()).add(group);
                if (rate.input > EPSILON) {
                    consumers.computeIfAbsent(rate.key, ignored -> new ArrayList<>()).add(group);
                    fullDemand.merge(rate.key, rate.input * group.actualCount, Double::sum);
                }
            }
        }
        if (!complete || cyclic(producers, consumers)) return false;
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            Map<ResourceKey, Double> supply = new HashMap<>();
            for (Group group : groups) for (GroupRate rate : group.rates.values())
                if (rate.output > EPSILON) supply.merge(rate.key, rate.output * group.actualCount * group.utilization,
                        Double::sum);
            double[] next = new double[groups.size()];
            double difference = 0;
            for (int index = 0; index < groups.size(); index++) {
                Group group = groups.get(index);
                double utilization = 1;
                for (GroupRate rate : group.rates.values())
                    if (rate.input > EPSILON && producers.containsKey(rate.key)) {
                        double demand = fullDemand.getOrDefault(rate.key, 0d);
                        if (demand > EPSILON) utilization = Math.min(utilization,
                                supply.getOrDefault(rate.key, 0d) / demand);
                    }
                next[index] = Math.clamp(utilization, 0, 1);
                difference = Math.max(difference, Math.abs(group.utilization - next[index]));
            }
            for (int index = 0; index < groups.size(); index++) groups.get(index).utilization = next[index];
            if (difference < EPSILON) return true;
        }
        return false;
    }

    private boolean cyclic(Map<ResourceKey, List<Group>> producers, Map<ResourceKey, List<Group>> consumers) {
        Map<Group, Set<Group>> edges = new LinkedHashMap<>();
        Map<Group, Integer> indegree = new LinkedHashMap<>();
        for (Group group : groups) {
            edges.put(group, new HashSet<>());
            indegree.put(group, 0);
        }
        for (Map.Entry<ResourceKey, List<Group>> entry : producers.entrySet()) {
            List<Group> resourceConsumers = consumers.get(entry.getKey());
            if (resourceConsumers == null) continue;
            for (Group producer : entry.getValue()) for (Group consumer : resourceConsumers)
                if (edges.get(producer).add(consumer)) indegree.merge(consumer, 1, Integer::sum);
        }
        ArrayDeque<Group> ready = new ArrayDeque<>();
        indegree.forEach((group, degree) -> { if (degree == 0) ready.add(group); });
        int visited = 0;
        while (!ready.isEmpty()) {
            Group group = ready.removeFirst();
            visited++;
            for (Group next : edges.get(group)) if (indegree.merge(next, -1, Integer::sum) == 0) ready.add(next);
        }
        return visited != groups.size();
    }

    private static double configuredRate(CompoundTag resource, CompoundTag profile) {
        int chance = resource.getInt("chance"), maxChance = resource.getInt("maxChance");
        if (maxChance <= 0) return 0;
        double amount = resource.getLong("amount") * (chance / (double) maxChance);
        if (chance < maxChance) amount *= Math.max(1, profile.getInt("totalRuns"));
        return resource.getBoolean("perTick") ? amount :
                profile.getInt("duration") > 0 ? amount / profile.getInt("duration") : 0;
    }

    private static ListTag profiles(CompoundTag machine) {
        ListTag profiles = machine.getList("capacityProfiles", Tag.TAG_COMPOUND).copy();
        if (!profiles.isEmpty()) return profiles;
        CompoundTag profile = new CompoundTag();
        profile.putInt("tier", machine.contains("overclockTier") ? machine.getInt("overclockTier") : -1);
        profile.putInt("duration", machine.getInt("duration"));
        profile.putInt("totalRuns", machine.getInt("totalRuns"));
        profile.putInt("parallels", Math.max(1, machine.getInt("totalRuns")));
        profile.putInt("subtickParallels", 1);
        profile.putInt("batchParallels", 1);
        profile.putLong("voltage", machine.getLong("voltage"));
        profile.putLong("amperage", machine.getLong("amperage"));
        profile.putLong("inputEUt", machine.getLong("inputEUt"));
        profile.putLong("outputEUt", machine.getLong("outputEUt"));
        profile.putBoolean("partial", machine.getString("configuration").equals("partial"));
        profile.put("inputs", machine.getList("configuredIn", Tag.TAG_COMPOUND).copy());
        profile.put("outputs", machine.getList("configuredOut", Tag.TAG_COMPOUND).copy());
        profiles.add(profile);
        return profiles;
    }

    static String key(CompoundTag row) {
        return row.getString("kind") + "\u0000" + row.getString("id") + "\u0000" + row.get("icon") + "\u0000" +
                row.getList("alternatives", Tag.TAG_COMPOUND);
    }

    private record ResourceKey(String kind, String id, Tag icon, ListTag alternatives) {

        private static ResourceKey of(CompoundTag resource) {
            Tag icon = resource.get("icon");
            return new ResourceKey(resource.getString("kind"), resource.getString("id"),
                    icon == null ? new CompoundTag() : icon.copy(),
                    resource.getList("alternatives", Tag.TAG_COMPOUND).copy());
        }
    }

    private static final class GroupRate {

        private final ResourceKey key;
        private boolean known = true;
        private boolean expectedKnown = true;
        private double input;
        private double output;

        private GroupRate(ResourceKey key) {
            this.key = key;
        }
    }

    private static final class Group {

        private final String key;
        private final String machine;
        private final String recipe;
        private final int currentTier;
        private final List<CompoundTag> profiles;
        private final List<Long> positions = new ArrayList<>();
        private final Map<ResourceKey, GroupRate> rates = new LinkedHashMap<>();
        private int actualCount;
        private int profileIndex;
        private double utilization = 1;

        private Group(String key, String machine, String recipe, int currentTier, ListTag profiles) {
            this.key = key;
            this.machine = machine;
            this.recipe = recipe;
            this.currentTier = currentTier;
            this.profiles = new ArrayList<>(profiles.size());
            for (Tag profile : profiles) this.profiles.add((CompoundTag) profile.copy());
        }

        private CompoundTag profile() {
            return profiles.get(profileIndex);
        }

        private void selectTier(int tier) {
            for (int index = 0; index < profiles.size(); index++) if (profiles.get(index).getInt("tier") == tier) {
                profileIndex = index;
                return;
            }
            profileIndex = 0;
        }

        private void loadRates() {
            rates.clear();
            add(profile().getList("inputs", Tag.TAG_COMPOUND), true);
            add(profile().getList("outputs", Tag.TAG_COMPOUND), false);
        }

        private void add(ListTag resources, boolean input) {
            for (Tag element : resources) {
                CompoundTag resource = (CompoundTag) element;
                GroupRate rate = rates.computeIfAbsent(ResourceKey.of(resource), GroupRate::new);
                if (input) rate.input += configuredRate(resource, profile());
                else rate.output += configuredRate(resource, profile());
                rate.known &= resource.getBoolean("known");
                rate.expectedKnown &= !resource.contains("expectedKnown") || resource.getBoolean("expectedKnown");
            }
        }
    }

    private final class Row {

        private final ResourceKey key;
        private final ListTag contributors = new ListTag();
        private boolean known = true;
        private boolean expectedKnown = true;
        private double input;
        private double output;
        private double capacityInput;
        private double capacityOutput;

        private Row(ResourceKey key) {
            this.key = key;
        }

        private void add(Group group, CompoundTag profile, GroupRate rate, boolean solved) {
            known &= rate.known && solved;
            expectedKnown &= rate.expectedKnown && solved;
            input += rate.input * group.actualCount * group.utilization;
            output += rate.output * group.actualCount * group.utilization;
            capacityInput += rate.input * group.actualCount;
            capacityOutput += rate.output * group.actualCount;
            CompoundTag contributor = new CompoundTag();
            contributor.putString("groupKey", group.key);
            contributor.putString("machine", group.machine);
            contributor.putString("recipe", group.recipe);
            contributor.putInt("actualCount", group.actualCount);
            contributor.putInt("tier", profile.getInt("tier"));
            contributor.putInt("duration", profile.getInt("duration"));
            contributor.putInt("totalRuns", profile.getInt("totalRuns"));
            contributor.putInt("ocLevel", profile.getInt("ocLevel"));
            contributor.putInt("parallels", profile.getInt("parallels"));
            contributor.putInt("subtickParallels", profile.getInt("subtickParallels"));
            contributor.putInt("batchParallels", profile.getInt("batchParallels"));
            contributor.putLong("voltage", profile.getLong("voltage"));
            contributor.putLong("amperage", profile.getLong("amperage"));
            contributor.putDouble("utilization", group.utilization);
            contributor.putDouble("perMachineIn", rate.input * group.utilization);
            contributor.putDouble("perMachineOut", rate.output * group.utilization);
            contributor.putDouble("configuredIn", rate.input * group.actualCount * group.utilization);
            contributor.putDouble("configuredOut", rate.output * group.actualCount * group.utilization);
            contributor.putDouble("configuredNet", (rate.output - rate.input) * group.actualCount * group.utilization);
            contributor.putDouble("capacityIn", rate.input * group.actualCount);
            contributor.putDouble("capacityOut", rate.output * group.actualCount);
            contributor.putDouble("capacityNet", (rate.output - rate.input) * group.actualCount);
            contributor.putBoolean("configuredKnown", rate.known && solved);
            contributor.putBoolean("capacityKnown", rate.known);
            contributor.putBoolean("expectedKnown", rate.expectedKnown && solved);
            ListTag positions = new ListTag();
            group.positions.forEach(position -> positions.add(LongTag.valueOf(position)));
            contributor.put("positions", positions);
            if (!group.positions.isEmpty()) contributor.putLong("pos", group.positions.get(0));
            contributors.add(contributor);
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("kind", key.kind);
            tag.putString("id", key.id);
            tag.put("icon", key.icon.copy());
            tag.put("alternatives", key.alternatives.copy());
            tag.putBoolean("known", known);
            tag.putBoolean("configuredKnown", known);
            tag.putBoolean("expectedKnown", expectedKnown);
            tag.putDouble("configuredIn", input);
            tag.putDouble("configuredOut", output);
            tag.putDouble("configuredNet", output - input);
            tag.putDouble("capacityIn", capacityInput);
            tag.putDouble("capacityOut", capacityOutput);
            tag.putDouble("capacityNet", capacityOutput - capacityInput);
            tag.putBoolean("capacityKnown", !partial);
            tag.putBoolean("unresolvedNet", !known);
            tag.put("contributors", contributors);
            return tag;
        }
    }
}
