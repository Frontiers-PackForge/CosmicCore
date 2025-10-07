package com.ghostipedia.cosmiccore.common.ascension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Set;

public interface IAscensionProgress {


    long getCurrency(AscensionConsumables currency);
    void addCurrency(AscensionConsumables currency, long amount);
    boolean canPurchase(AscensionConsumables currency, long amount);

    int getRankTier(ResourceLocation upgradeLoc);
    void setRankTier(ResourceLocation upgradeLoc, int rank);

    boolean hasUpgrade(ResourceLocation upgradeLoc);

    Set<ResourceLocation> unlockedDims();

    CompoundTag save();
    void load(CompoundTag tag);

    EnumMap<AscensionConsumables, Long> all();

}
