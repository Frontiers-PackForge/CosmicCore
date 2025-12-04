package com.ghostipedia.cosmiccore.api.data.souls;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SoulNetwork implements INBTSerializable<CompoundTag> {

    @Getter
    private int tier = 0, currentSouls = 0;

    private Map<SoulType, Integer> contents = new HashMap<>();

    public SoulNetwork() {}

    public SoulStack add(SoulStack stack, int throughput, boolean simulate) {
        int currentAmount = this.contents.getOrDefault(stack.type(), 0);

        int amountToAdd = Math.min(stack.amount(), throughput); // Respect throughput
        amountToAdd = Math.min(amountToAdd, getSize() - currentAmount); // Respect network capacity

        if (!simulate) this.contents.put(stack.type(), currentAmount + amountToAdd);

        return stack.withAmount(amountToAdd);
    }

    public SoulStack syphon(SoulStack stack, boolean simulate) {
        var currentSoulContent = this.contents.getOrDefault(stack.type(), 0);
        int amountToSyphon = Math.min(stack.amount(), currentSoulContent);

        if (!simulate && amountToSyphon > 0) this.contents.put(stack.type(), currentSoulContent - amountToSyphon);

        return stack.withAmount(amountToSyphon);
    }

    public List<SoulStack> getContents() {
        return contents.entrySet().stream()
            .map(kvp -> new SoulStack(kvp.getKey(), kvp.getValue()))
            .toList();
    }

    public int getSize() {
        return switch (tier) {
            case 0 -> 1_000;
            case 1 -> 100_000;
            case 3 -> 1_000_000;
            case 4 -> 10_000_000;
            case 5 -> 100_000_000;
            case 6 -> 1_000_000_000;
            default -> Integer.MAX_VALUE;
        };
    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {

    }
}
