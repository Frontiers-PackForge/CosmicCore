package com.ghostipedia.cosmiccore.api.data.souls;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

public class SoulNetwork implements INBTSerializable<CompoundTag> {

    @Getter
    private int tier = 0;

    @Setter
    private Runnable dirtyCallback;

    private final Map<SoulType, Integer> contents = new ConcurrentHashMap<>();

    public SoulNetwork() {}

    public SoulStack add(SoulStack stack, int throughput, boolean simulate) {
        int currentAmount = this.contents.getOrDefault(stack.type(), 0);

        // TODO check with ghosti if we should do a total volume or a volume per type
        int totalAmount = this.contents.values().stream().mapToInt(Integer::intValue).sum();

        int amountToAdd = Math.min(stack.amount(), throughput); // Respect throughput
        amountToAdd = Math.min(amountToAdd, getSize() - totalAmount); // Respect network capacity
        amountToAdd = Math.max(0, amountToAdd); // Ensure we don't add a negative amount

        if (!simulate && amountToAdd > 0) {
            this.contents.put(stack.type(), currentAmount + amountToAdd);
            if (dirtyCallback != null) dirtyCallback.run();
        }

        System.out.println(this);

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

    public void setTier(int tier) {
        this.tier = tier;
        if (dirtyCallback != null) dirtyCallback.run();
    }

    public int getSize() {
        return switch (tier) {
            case 0 -> 10_000;
            case 1 -> 50_000;
            case 2 -> 150_000;
            case 4 -> 1_000_000;
            case 5 -> 10_000_000;
            case 6 -> 100_000_000;
            default -> Integer.MAX_VALUE;
        };
    }

    @Override
    public String toString() {
        return "SoulNetwork{" +
                "tier=" + tier +
                ", contents=" + contents + '}';
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putInt("tier", this.tier);
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
        this.tier = compoundTag.getInt("tier");
        this.contents.clear();
        ListTag listTag = compoundTag.getList("contents", Tag.TAG_COMPOUND);
        for (Tag t : listTag) {
            var contentTag = (CompoundTag) t;
            this.contents.put(SoulType.byName(contentTag.getString("type")), contentTag.getInt("amount"));
        }
    }
}
