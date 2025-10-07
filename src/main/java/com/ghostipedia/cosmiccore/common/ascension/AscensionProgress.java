package com.ghostipedia.cosmiccore.common.ascension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class AscensionProgress implements IAscensionProgress{

    private final EnumMap<AscensionConsumables, Long> balance = new EnumMap<>(AscensionConsumables.class);
    private final Map<ResourceLocation, Integer> ranks = new HashMap<>();
    private final Set<ResourceLocation> dimensions = new HashSet<>();

    @Override public long getCurrency(AscensionConsumables currency){ return balance.getOrDefault(currency, 0L); }
    @Override public void addCurrency(AscensionConsumables currency,long amount){ balance.merge(currency, amount, Long::sum); }
    @Override public boolean canPurchase(AscensionConsumables currency,long amount){
        long cur = getCurrency(currency); if (cur < amount) return false; balance.put(currency, cur - amount); return true;
    }

    @Override public int getRankTier(ResourceLocation upgradeLoc) { return ranks.getOrDefault(upgradeLoc, 0); }
    @Override public void setRankTier(ResourceLocation id,int rank){ ranks.put(id, rank); }
    @Override public boolean hasUpgrade(ResourceLocation id){ return getRankTier(id) > 0; }

    @Override public Set<ResourceLocation> unlockedDims(){ return dimensions; }
    @Override public EnumMap<AscensionConsumables, Long> all(){ return new EnumMap<>(balance); }

    @Override public CompoundTag save(){
        CompoundTag tag = new CompoundTag();
        CompoundTag balance = new CompoundTag();
        for (var e : this.balance.entrySet()) balance.putLong(e.getKey().name(), e.getValue());
        tag.put("balance", balance);
        CompoundTag rank = new CompoundTag();
        for (var e : ranks.entrySet()) rank.putInt(e.getKey().toString(), e.getValue());
        tag.put("ranks", rank);
        ListTag dimList = new ListTag();
        for (var rl : dimensions) dimList.add(StringTag.valueOf(rl.toString()));
        tag.put("dimensions", dimList);
        return tag;
    }

    @Override public void load(CompoundTag tag){
        balance.clear(); ranks.clear(); dimensions.clear();
        CompoundTag balance = tag.getCompound("balance");
        for (String k : balance.getAllKeys()) this.balance.put(AscensionConsumables.valueOf(k), balance.getLong(k));
        CompoundTag ranks = tag.getCompound("ranks");
        for (String k : ranks.getAllKeys()) this.ranks.put(new ResourceLocation(k), ranks.getInt(k));
        ListTag dimList = tag.getList("dimensions", Tag.TAG_STRING);
        for (int i=0;i<dimList.size();i++) dimensions.add(new ResourceLocation(dimList.getString(i)));
    }
}
