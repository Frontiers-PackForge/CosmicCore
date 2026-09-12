package com.ghostipedia.cosmiccore.client.rate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

final class RateCalculatorSmoothing {

    private static final double RESPONSE_MILLIS = 5000;
    private final Map<Key, Estimate> estimates = new HashMap<>();
    private long updatedAt;
    private String dimension;

    CompoundTag update(CompoundTag report, long now) {
        String nextDimension = report.getString("dimension");
        if (!nextDimension.equals(dimension)) {
            estimates.clear();
            dimension = nextDimension;
            updatedAt = now;
        }
        double weight = -Math.expm1(-Math.max(0, now - updatedAt) / RESPONSE_MILLIS);
        updatedAt = Math.max(updatedAt, now);
        CompoundTag display = report.copy();
        Map<Key, Estimate> next = new HashMap<>();
        for (Tag element : display.getList("rows", Tag.TAG_COMPOUND)) {
            CompoundTag row = (CompoundTag) element;
            String resource = RateCalculatorSimulation.key(row);
            rememberRaw(row);
            var contributors = row.getList("contributors", Tag.TAG_COMPOUND);
            if (contributors.isEmpty()) {
                smooth(row, new Key(resource, "", 0), weight, next);
            } else {
                double input = 0, output = 0;
                for (Tag contribution : contributors) {
                    CompoundTag contributor = (CompoundTag) contribution;
                    rememberRaw(contributor);
                    smooth(contributor, new Key(resource, contributor.getString("machine"),
                            contributor.getLong("pos")), weight, next);
                    input += contributor.getDouble("observedIn");
                    output += contributor.getDouble("observedOut");
                }
                setRates(row, input, output);
            }
        }
        estimates.clear();
        estimates.putAll(next);
        return display;
    }

    private void smooth(CompoundTag tag, Key key, double weight, Map<Key, Estimate> next) {
        boolean inputAvailable = tag.getBoolean("observedInAvailable");
        boolean outputAvailable = tag.getBoolean("observedOutAvailable");
        double input = inputAvailable ? tag.getDouble("observedIn") : 0;
        double output = outputAvailable ? tag.getDouble("observedOut") : 0;
        Estimate previous = estimates.get(key);
        if (previous != null) {
            if (inputAvailable && previous.inputAvailable) input = previous.input + weight * (input - previous.input);
            if (outputAvailable && previous.outputAvailable)
                output = previous.output + weight * (output - previous.output);
        }
        if (inputAvailable || outputAvailable)
            next.put(key, new Estimate(input, output, inputAvailable, outputAvailable));
        setRates(tag, input, output);
    }

    private static void rememberRaw(CompoundTag tag) {
        tag.putDouble("rawObservedIn", tag.getDouble("observedIn"));
        tag.putDouble("rawObservedOut", tag.getDouble("observedOut"));
        tag.putDouble("rawObservedNet", tag.getDouble("observedNet"));
    }

    private static void setRates(CompoundTag tag, double input, double output) {
        tag.putDouble("observedIn", input);
        tag.putDouble("observedOut", output);
        tag.putDouble("observedNet", output - input);
    }

    private record Key(String resource, String machine, long position) {}

    private record Estimate(double input, double output, boolean inputAvailable, boolean outputAvailable) {}
}
