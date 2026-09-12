package com.ghostipedia.cosmiccore.common.rate;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;

record RateCalculatorCapacityProfile(int tier, int duration, int totalRuns, int ocLevel, int parallels,
                                     int subtickParallels, int batchParallels, long voltage, long amperage,
                                     long inputEUt, long outputEUt, boolean partial,
                                     List<RateCalculatorResource> inputs, List<RateCalculatorResource> outputs) {

    RateCalculatorCapacityProfile {
        inputs = List.copyOf(inputs);
        outputs = List.copyOf(outputs);
    }

    CompoundTag toTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("tier", tier);
        tag.putInt("duration", duration);
        tag.putInt("totalRuns", totalRuns);
        tag.putInt("ocLevel", ocLevel);
        tag.putInt("parallels", parallels);
        tag.putInt("subtickParallels", subtickParallels);
        tag.putInt("batchParallels", batchParallels);
        tag.putLong("voltage", voltage);
        tag.putLong("amperage", amperage);
        tag.putLong("inputEUt", inputEUt);
        tag.putLong("outputEUt", outputEUt);
        tag.putBoolean("partial", partial);
        tag.put("inputs", write(inputs, registries));
        tag.put("outputs", write(outputs, registries));
        return tag;
    }

    private static ListTag write(List<RateCalculatorResource> resources, HolderLookup.Provider registries) {
        ListTag result = new ListTag();
        resources.forEach(resource -> result.add(resource.toTag(registries)));
        return result;
    }
}
