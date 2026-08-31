package com.ghostipedia.cosmiccore.api.data.souls;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import lombok.Setter;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToIntFunction;

public class SoulNetwork implements INBTSerializable<CompoundTag> {

    @Setter
    private Runnable dirtyCallback;

    private final Map<SoulType, Integer> contents = new ConcurrentHashMap<>();

    public SoulNetwork() {}

    public synchronized SoulStack add(SoulStack stack, int throughput, int capacity, boolean simulate) {
        int currentAmount = this.contents.getOrDefault(stack.type(), 0);

        int amountToAdd = Math.min(stack.amount(), throughput); // Respect throughput
        amountToAdd = Math.min(amountToAdd, capacity - currentAmount); // Respect network capacity
        amountToAdd = Math.max(0, amountToAdd); // Ensure we don't add a negative amount

        if (!simulate && amountToAdd > 0) {
            this.contents.put(stack.type(), currentAmount + amountToAdd);
            if (dirtyCallback != null) dirtyCallback.run();
        }

        return stack.withAmount(amountToAdd);
    }

    public synchronized SoulStack syphon(SoulStack stack, boolean simulate) {
        var currentSoulContent = this.contents.getOrDefault(stack.type(), 0);
        int amountToSyphon = Math.min(stack.amount(), currentSoulContent);

        if (!simulate && amountToSyphon > 0) {
            this.contents.put(stack.type(), currentSoulContent - amountToSyphon);
            if (dirtyCallback != null) dirtyCallback.run();
        }

        return stack.withAmount(amountToSyphon);
    }

    public synchronized boolean insertAll(Collection<SoulStack> stacks, ToIntFunction<SoulType> throughput,
                                          ToIntFunction<SoulType> capacity, boolean simulate) {
        var totals = aggregate(stacks);
        for (var entry : totals.entrySet()) {
            var type = entry.getKey();
            var amount = entry.getValue();
            if (amount > throughput.applyAsInt(type)) return false;
            if ((long) this.contents.getOrDefault(type, 0) + amount > capacity.applyAsInt(type)) return false;
        }
        if (!simulate && !totals.isEmpty()) {
            totals.forEach((type, amount) -> this.contents.merge(type, amount, Math::addExact));
            if (dirtyCallback != null) dirtyCallback.run();
        }
        return true;
    }

    public synchronized boolean extractAll(Collection<SoulStack> stacks, ToIntFunction<SoulType> throughput,
                                           boolean simulate) {
        var totals = aggregate(stacks);
        for (var entry : totals.entrySet()) {
            var type = entry.getKey();
            var amount = entry.getValue();
            if (amount > throughput.applyAsInt(type)) return false;
            if (amount > this.contents.getOrDefault(type, 0)) return false;
        }
        if (!simulate && !totals.isEmpty()) {
            totals.forEach((type, amount) -> this.contents.compute(type, (ignored, current) -> {
                var remaining = current - amount;
                return remaining == 0 ? null : remaining;
            }));
            if (dirtyCallback != null) dirtyCallback.run();
        }
        return true;
    }

    private static EnumMap<SoulType, Integer> aggregate(Collection<SoulStack> stacks) {
        var totals = new EnumMap<SoulType, Integer>(SoulType.class);
        for (var stack : stacks) {
            if (stack == null || stack.isEmpty()) continue;
            totals.merge(stack.type(), stack.amount(), Math::addExact);
        }
        return totals;
    }

    public synchronized int getAmount(SoulType type) {
        return this.contents.getOrDefault(type, 0);
    }

    public synchronized void reset() {
        this.contents.clear();
        if (dirtyCallback != null) dirtyCallback.run();
    }

    public synchronized List<SoulStack> getContents() {
        return contents.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(kvp -> new SoulStack(kvp.getKey(), kvp.getValue()))
                .toList();
    }

    @Override
    public String toString() {
        return "SoulNetwork{contents=" + contents + '}';
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        var listTag = new ListTag();
        this.contents.forEach((soulType, amount) -> {
            var contentTag = new CompoundTag();
            contentTag.putString("type", soulType.getSerializedName());
            contentTag.putInt("amount", amount);
            listTag.add(contentTag);
        });
        tag.put("contents", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        this.contents.clear();
        ListTag listTag = compoundTag.getList("contents", Tag.TAG_COMPOUND);
        for (Tag t : listTag) {
            var contentTag = (CompoundTag) t;
            var type = SoulType.byName(contentTag.getString("type"));
            var amount = contentTag.getInt("amount");
            if (type != null && amount > 0) this.contents.put(type, amount);
        }
    }
}
