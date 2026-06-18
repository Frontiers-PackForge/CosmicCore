package com.ghostipedia.cosmiccore.api.data.souls;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoulNetwork implements INBTSerializable<CompoundTag> {

    @Setter
    private Runnable dirtyCallback;

    private final Map<SoulType, Integer> contents = new ConcurrentHashMap<>();

    public SoulNetwork() {}

    public SoulStack add(SoulStack stack, int throughput, int capacity, boolean simulate) {
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

    public SoulStack syphon(SoulStack stack, boolean simulate) {
        var currentSoulContent = this.contents.getOrDefault(stack.type(), 0);
        int amountToSyphon = Math.min(stack.amount(), currentSoulContent);

        if (!simulate && amountToSyphon > 0) {
            this.contents.put(stack.type(), currentSoulContent - amountToSyphon);
            if (dirtyCallback != null) dirtyCallback.run();
        }

        return stack.withAmount(amountToSyphon);
    }

    public void reset() {
        this.contents.clear();
        if (dirtyCallback != null) dirtyCallback.run();
    }

    public List<SoulStack> getContents() {
        return contents.entrySet().stream()
                .map(kvp -> new SoulStack(kvp.getKey(), kvp.getValue()))
                .toList();
    }

    @Override
    public String toString() {
        return "SoulNetwork{contents=" + contents + '}';
    }

    @Override
    public CompoundTag serializeNBT() {
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
    public void deserializeNBT(CompoundTag compoundTag) {
        this.contents.clear();
        ListTag listTag = compoundTag.getList("contents", Tag.TAG_COMPOUND);
        for (Tag t : listTag) {
            var contentTag = (CompoundTag) t;
            this.contents.put(SoulType.byName(contentTag.getString("type")), contentTag.getInt("amount"));
        }
    }
}
