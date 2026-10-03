package com.ghostipedia.cosmiccore.client.rate;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class RateCalculatorSnapshotExport {

    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private RateCalculatorSnapshotExport() {}

    static boolean isAvailable(boolean devVisor, boolean held) {
        return devVisor && held;
    }

    public static String toJson(CompoundTag report) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", SCHEMA_VERSION);
        root.addProperty("exportType", "cosmiccore:rate_calculator_frozen_snapshot");
        root.add("units", units());
        root.add("snapshot", snapshot(report));
        return GSON.toJson(root);
    }

    private static JsonObject units() {
        JsonObject units = new JsonObject();
        units.addProperty("time", "ticks");
        units.addProperty("ticksPerSecond", 20);
        units.addProperty("rate", "quantity units per tick");
        units.addProperty("item", "items");
        units.addProperty("fluid", "millibuckets");
        units.addProperty("energy", "EU");
        units.addProperty("charge", "charge units");
        units.addProperty("computation", "CWU");
        units.addProperty("other", "native units named by resource kind");
        return units;
    }

    private static JsonObject snapshot(CompoundTag report) {
        JsonObject snapshot = new JsonObject();
        snapshot.addProperty("dimension", report.getString("dimension"));
        snapshot.addProperty("sessionElapsedTicks", report.getLong("elapsedTicks"));
        snapshot.addProperty("selectedMachineCount", report.getInt("selected"));
        snapshot.addProperty("loadedMachineCount", report.getInt("loaded"));
        snapshot.addProperty("unknownMachineCount", report.getInt("unknown"));
        snapshot.addProperty("unloadedMachineOrChunkCount", report.getInt("unloaded"));
        snapshot.add("coverage", coverage(report));
        JsonObject electrical = new JsonObject();
        electrical.addProperty("configuredInputEuPerTick", report.getDouble("configuredInputEUt"));
        electrical.addProperty("configuredOutputEuPerTick", report.getDouble("configuredOutputEUt"));
        electrical.addProperty("configuredNetConsumptionEuPerTick", report.getDouble("configuredEUt"));
        snapshot.add("electrical", electrical);
        snapshot.add("machines", machines(report));
        snapshot.add("aggregates", aggregates(report));
        return snapshot;
    }

    private static JsonObject coverage(CompoundTag tag) {
        JsonObject coverage = new JsonObject();
        coverage.addProperty("partial", tag.getBoolean("partial"));
        coverage.addProperty("truncated", tag.getBoolean("truncated"));
        coverage.addProperty("recordedQuantitiesUseContributorMeasurementWindows", true);
        coverage.addProperty("recordedRatesMayUseCycleAlignedEvidence", true);
        coverage.add("unsupportedCapabilities", strings(tag, "unsupportedCapabilities"));
        return coverage;
    }

    private static JsonArray machines(CompoundTag report) {
        JsonArray machines = new JsonArray();
        for (Tag element : report.getList("machines", Tag.TAG_COMPOUND)) {
            CompoundTag machine = (CompoundTag) element;
            JsonObject entry = new JsonObject();
            entry.add("position", position(machine.getLong("pos")));
            entry.addProperty("machineId", machine.getString("machine"));
            entry.addProperty("status", machine.getString("status"));
            entry.addProperty("recipeId", machine.getString("recipe"));
            entry.addProperty("effectiveDurationTicks", machine.getInt("duration"));
            entry.addProperty("progressTicks", machine.getInt("progressTicks"));
            entry.addProperty("totalRuns", machine.getInt("totalRuns"));
            entry.addProperty("voltage", machine.getLong("voltage"));
            entry.addProperty("amperage", machine.getLong("amperage"));
            entry.addProperty("inputEuPerTick", machine.getLong("inputEUt"));
            entry.addProperty("outputEuPerTick", machine.getLong("outputEUt"));
            entry.addProperty("overclockTier", machine.getInt("overclockTier"));
            entry.addProperty("minimumOverclockTier", machine.getInt("minimumOverclockTier"));
            entry.addProperty("maximumOverclockTier", machine.getInt("maximumOverclockTier"));
            entry.addProperty("stale", machine.getBoolean("stale"));
            entry.addProperty("configurationCoverage", machine.getString("configuration"));
            entry.addProperty("recordedCoverage", machine.getString("actual"));
            entry.add("unsupportedCapabilities", strings(machine, "unsupportedCapabilities"));
            entry.add("configuredInputs", configuredResources(machine, "configuredIn"));
            entry.add("configuredOutputs", configuredResources(machine, "configuredOut"));
            entry.add("capacityProfiles", capacityProfiles(machine));
            machines.add(entry);
        }
        return machines;
    }

    private static JsonArray capacityProfiles(CompoundTag machine) {
        JsonArray profiles = new JsonArray();
        for (Tag element : machine.getList("capacityProfiles", Tag.TAG_COMPOUND)) {
            CompoundTag profile = (CompoundTag) element;
            JsonObject entry = new JsonObject();
            entry.addProperty("tier", profile.getInt("tier"));
            entry.addProperty("durationTicks", profile.getInt("duration"));
            entry.addProperty("totalRuns", profile.getInt("totalRuns"));
            entry.addProperty("overclockLevel", profile.getInt("ocLevel"));
            entry.addProperty("parallels", profile.getInt("parallels"));
            entry.addProperty("subtickParallels", profile.getInt("subtickParallels"));
            entry.addProperty("batchParallels", profile.getInt("batchParallels"));
            entry.addProperty("voltage", profile.getLong("voltage"));
            entry.addProperty("amperage", profile.getLong("amperage"));
            entry.addProperty("inputEuPerTick", profile.getLong("inputEUt"));
            entry.addProperty("outputEuPerTick", profile.getLong("outputEUt"));
            entry.addProperty("partial", profile.getBoolean("partial"));
            entry.add("inputs", configuredResources(profile, "inputs"));
            entry.add("outputs", configuredResources(profile, "outputs"));
            profiles.add(entry);
        }
        return profiles;
    }

    private static JsonArray configuredResources(CompoundTag source, String key) {
        JsonArray resources = new JsonArray();
        for (Tag element : source.getList(key, Tag.TAG_COMPOUND)) {
            CompoundTag resource = (CompoundTag) element;
            JsonObject entry = identity(resource);
            entry.addProperty("quantity", resource.getLong("amount"));
            entry.addProperty("chance", resource.getInt("chance"));
            entry.addProperty("maxChance", resource.getInt("maxChance"));
            entry.addProperty("perTick", resource.getBoolean("perTick"));
            entry.addProperty("known", resource.getBoolean("known"));
            entry.addProperty("expectedKnown", !resource.contains("expectedKnown") ||
                    resource.getBoolean("expectedKnown"));
            resources.add(entry);
        }
        return resources;
    }

    private static JsonArray aggregates(CompoundTag report) {
        JsonArray aggregates = new JsonArray();
        for (Tag element : report.getList("rows", Tag.TAG_COMPOUND)) {
            CompoundTag row = (CompoundTag) element;
            JsonObject entry = identity(row);
            entry.add("coverage", rateCoverage(row));
            entry.add("recorded", recorded(row));
            entry.add("configured", configured(row));
            JsonArray contributors = new JsonArray();
            for (Tag contribution : row.getList("contributors", Tag.TAG_COMPOUND))
                contributors.add(contributor((CompoundTag) contribution));
            entry.add("contributors", contributors);
            aggregates.add(entry);
        }
        return aggregates;
    }

    private static JsonObject contributor(CompoundTag tag) {
        JsonObject contributor = new JsonObject();
        contributor.add("position", position(tag.getLong("pos")));
        contributor.addProperty("machineId", tag.getString("machine"));
        contributor.addProperty("recipeId", tag.getString("recipe"));
        contributor.addProperty("effectiveDurationTicks", tag.getInt("duration"));
        contributor.addProperty("voltage", tag.getLong("voltage"));
        contributor.addProperty("amperage", tag.getLong("amperage"));
        contributor.addProperty("activityElapsedTicks", tag.getLong("activityElapsed"));
        contributor.addProperty("activityTargetHorizonTicks", tag.getLong("activityHorizon"));
        contributor.addProperty("completedCycles", tag.getLong("activityCompleted"));
        contributor.addProperty("activityFresh", tag.getBoolean("activityFresh"));
        contributor.addProperty("waitingReason", tag.getString("reason"));
        contributor.add("coverage", rateCoverage(tag));
        contributor.add("recorded", recorded(tag));
        contributor.add("configured", configured(tag));
        return contributor;
    }

    private static JsonObject rateCoverage(CompoundTag tag) {
        JsonObject coverage = new JsonObject();
        coverage.addProperty("resourceIdentityKnown", !tag.contains("known") || tag.getBoolean("known"));
        coverage.addProperty("recordedInputKnown", tag.getBoolean("observedInKnown"));
        coverage.addProperty("recordedOutputKnown", tag.getBoolean("observedOutKnown"));
        coverage.addProperty("recordedInputAvailable", tag.getBoolean("observedInAvailable"));
        coverage.addProperty("recordedOutputAvailable", tag.getBoolean("observedOutAvailable"));
        coverage.addProperty("learning", tag.getBoolean("learning"));
        return coverage;
    }

    private static JsonObject recorded(CompoundTag tag) {
        JsonObject recorded = new JsonObject();
        recorded.addProperty("inputQuantity", tag.getLong("recordedIn"));
        recorded.addProperty("outputQuantity", tag.getLong("recordedOut"));
        recorded.addProperty("inputPerTick", rawRate(tag, "In"));
        recorded.addProperty("outputPerTick", rawRate(tag, "Out"));
        recorded.addProperty("netPerTick", rawRate(tag, "Net"));
        return recorded;
    }

    private static double rawRate(CompoundTag tag, String direction) {
        String raw = "rawObserved" + direction;
        return tag.contains(raw) ? tag.getDouble(raw) : tag.getDouble("observed" + direction);
    }

    private static JsonObject configured(CompoundTag tag) {
        JsonObject configured = new JsonObject();
        configured.addProperty("inputPerTick", tag.getDouble("configuredIn"));
        configured.addProperty("outputPerTick", tag.getDouble("configuredOut"));
        configured.addProperty("netPerTick", tag.getDouble("configuredNet"));
        configured.addProperty("known", tag.getBoolean("configuredKnown"));
        configured.addProperty("expectedValueKnown", tag.getBoolean("expectedKnown"));
        return configured;
    }

    private static JsonObject identity(CompoundTag tag) {
        JsonObject identity = new JsonObject();
        String kind = tag.getString("kind");
        identity.addProperty("kind", kind);
        identity.addProperty("id", tag.getString("id"));
        identity.addProperty("quantityUnit", quantityUnit(kind));
        Tag icon = tag.get("icon");
        if (icon != null) identity.addProperty("identityNbt", icon.toString());
        JsonArray alternatives = new JsonArray();
        for (Tag element : tag.getList("alternatives", Tag.TAG_COMPOUND)) {
            CompoundTag alternative = (CompoundTag) element;
            JsonObject entry = new JsonObject();
            entry.addProperty("id", alternative.getString("id"));
            Tag alternativeIcon = alternative.get("icon");
            if (alternativeIcon != null) entry.addProperty("identityNbt", alternativeIcon.toString());
            alternatives.add(entry);
        }
        identity.add("alternatives", alternatives);
        return identity;
    }

    private static String quantityUnit(String kind) {
        return switch (kind) {
            case "item" -> "items";
            case "fluid" -> "millibuckets";
            case "energy" -> "EU";
            case "charge" -> "charge units";
            case "computation" -> "CWU";
            default -> kind + " units";
        };
    }

    private static JsonObject position(long packed) {
        BlockPos position = BlockPos.of(packed);
        JsonObject result = new JsonObject();
        result.addProperty("packedLong", packed);
        result.addProperty("x", position.getX());
        result.addProperty("y", position.getY());
        result.addProperty("z", position.getZ());
        return result;
    }

    private static JsonArray strings(CompoundTag source, String key) {
        JsonArray result = new JsonArray();
        for (Tag value : source.getList(key, Tag.TAG_STRING)) result.add(value.getAsString());
        return result;
    }
}
