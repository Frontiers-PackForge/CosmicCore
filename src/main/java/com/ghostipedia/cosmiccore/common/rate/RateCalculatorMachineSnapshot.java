package com.ghostipedia.cosmiccore.common.rate;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class RateCalculatorMachineSnapshot {

    public static final int SCHEMA = 1;

    private final ResourceKey<Level> dimension;
    private final long position;
    private String machineId = "";
    private String status = "unsupported";
    private String recipeId = "";
    private int duration;
    private int progressTicks;
    private int totalRuns = 1;
    private long voltage;
    private long amperage;
    private long inputEUt;
    private long outputEUt;
    private int overclockTier = -1;
    private int minimumOverclockTier = -1;
    private int maximumOverclockTier = -1;
    private boolean stale;
    private boolean configurationPartial;
    private boolean observedPartial;
    private boolean observationStarted;
    private long observedInUncertainty;
    private long observedOutUncertainty;
    private final List<String> unsupportedCapabilities = new ArrayList<>();
    private final List<RateCalculatorResource> configuredIn = new ArrayList<>();
    private final List<RateCalculatorResource> configuredOut = new ArrayList<>();
    private final List<RateCalculatorResource> observedIn = new ArrayList<>();
    private final List<RateCalculatorResource> observedOut = new ArrayList<>();
    private final List<RateCalculatorCapacityProfile> capacityProfiles = new ArrayList<>();
    private static final int MAX_OBSERVED_IDENTITIES = 512;

    public RateCalculatorMachineSnapshot(ResourceKey<Level> dimension, long position) {
        this.dimension = dimension;
        this.position = position;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public long position() {
        return position;
    }

    // mmmmmm, gross, but whatever, go my param spam.
    void configure(String machineId, String status, String recipeId, int duration, int progressTicks, int totalRuns,
                   long voltage, long amperage, long inputEUt, long outputEUt, int overclockTier,
                   int minimumOverclockTier, int maximumOverclockTier, List<RateCalculatorResource> inputs,
                   List<RateCalculatorResource> outputs, List<String> unsupportedCapabilities,
                   List<RateCalculatorCapacityProfile> capacityProfiles,
                   boolean configurationPartial) {
        this.machineId = machineId;
        this.status = status;
        this.recipeId = recipeId;
        this.duration = duration;
        this.progressTicks = progressTicks;
        this.totalRuns = totalRuns;
        this.voltage = voltage;
        this.amperage = amperage;
        this.inputEUt = inputEUt;
        this.outputEUt = outputEUt;
        this.overclockTier = overclockTier;
        this.minimumOverclockTier = minimumOverclockTier;
        this.maximumOverclockTier = maximumOverclockTier;
        this.configurationPartial = configurationPartial;
        this.unsupportedCapabilities.clear();
        this.unsupportedCapabilities.addAll(unsupportedCapabilities);
        configuredIn.clear();
        configuredIn.addAll(inputs);
        configuredOut.clear();
        configuredOut.addAll(outputs);
        this.capacityProfiles.clear();
        this.capacityProfiles.addAll(capacityProfiles);
    }

    void configureWithoutRecipe(String machineId, String status, int progressTicks) {
        this.machineId = machineId;
        this.status = status;
        this.progressTicks = progressTicks;
    }

    void addObserved(boolean input, List<RateCalculatorResource> resources) {
        observationStarted = true;
        List<RateCalculatorResource> target = input ? observedIn : observedOut;
        for (RateCalculatorResource resource : resources) {
            if (resource.known()) {
                int index = indexOfIdentity(target, resource);
                if (index >= 0) target.set(index, target.get(index).add(resource.amount()));
                else if (target.size() < MAX_OBSERVED_IDENTITIES) target.add(resource);
                else markUncertain(input);
            } else {
                markUncertain(input);
            }
        }
    }

    // This sounds way cooler than it actually is. Yay Equality Slop.
    private static int indexOfIdentity(List<RateCalculatorResource> resources, RateCalculatorResource target) {
        for (int index = 0; index < resources.size(); index++) {
            RateCalculatorResource resource = resources.get(index);
            if (resource.kind().equals(target.kind()) && resource.id().equals(target.id()) &&
                    resource.icon().equals(target.icon()) && resource.chance() == target.chance() &&
                    resource.maxChance() == target.maxChance() && resource.perTick() == target.perTick() &&
                    resource.known() == target.known())
                return index;
        }
        return -1;
    }

    public void invalidate() {
        stale = true;
    }

    void markFresh() {
        stale = false;
    }

    void markObservedPartial() {
        observationStarted = true;
        markUncertain(false);
    }

    void markInputPartial() {
        observationStarted = true;
        markUncertain(true);
    }

    private void markUncertain(boolean input) {
        observedPartial = true;
        if (input) observedInUncertainty++;
        else observedOutUncertainty++;
    }

    // The Monolith Tag, in all its horrible glory lmfao
    public CompoundTag toTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", SCHEMA);
        tag.putString("dimension", dimension.location().toString());
        tag.putLong("pos", position);
        tag.putString("machine", machineId);
        tag.putString("status", status);
        tag.putString("recipe", recipeId);
        tag.putInt("duration", duration);
        tag.putInt("progressTicks", progressTicks);
        tag.putInt("totalRuns", totalRuns);
        tag.putLong("voltage", voltage);
        tag.putLong("amperage", amperage);
        tag.putLong("inputEUt", inputEUt);
        tag.putLong("outputEUt", outputEUt);
        tag.putInt("overclockTier", overclockTier);
        tag.putInt("minimumOverclockTier", minimumOverclockTier);
        tag.putInt("maximumOverclockTier", maximumOverclockTier);
        tag.putBoolean("stale", stale);
        tag.putString("configuration", configurationPartial ? "partial" : "exact");
        ListTag unsupported = new ListTag();
        unsupportedCapabilities.forEach(value -> unsupported.add(net.minecraft.nbt.StringTag.valueOf(value)));
        tag.put("unsupportedCapabilities", unsupported);
        tag.putString("actual", !observationStarted ? "unknown" : observedPartial ? "partial" : "exact");
        tag.putLong("observedInUncertainty", observedInUncertainty);
        tag.putLong("observedOutUncertainty", observedOutUncertainty);
        tag.put("configuredIn", write(configuredIn, registries));
        tag.put("configuredOut", write(configuredOut, registries));
        tag.put("observedIn", write(observedIn, registries));
        tag.put("observedOut", write(observedOut, registries));
        ListTag profiles = new ListTag();
        capacityProfiles.forEach(profile -> profiles.add(profile.toTag(registries)));
        tag.put("capacityProfiles", profiles);
        return tag;
    }

    private static ListTag write(List<RateCalculatorResource> resources, HolderLookup.Provider registries) {
        ListTag result = new ListTag();
        for (RateCalculatorResource resource : resources) result.add(resource.toTag(registries));
        return result;
    }
}
