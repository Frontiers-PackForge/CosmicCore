package com.ghostipedia.cosmiccore.common.abyss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class AbyssBudget implements IAbyssTimer {

    private final Map<ResourceLocation, Long> timeRemaining = new HashMap<>();
    private final Map<ResourceLocation, Boolean> decay = new HashMap<>();
    private final Map<ResourceLocation, Double> cleanse = new HashMap<>();

    // spotless: off
    @Override
    public long getRemainingTicks(ResourceKey<Level> dimension) {
        return timeRemaining.getOrDefault(dimension.location(), -1L);
    }

    @Override
    public void setRemainingTicks(ResourceKey<Level> dimension, long ticks) {
        timeRemaining.put(dimension.location(), ticks);
    }

    @Override
    public boolean isDecaying(ResourceKey<Level> dimension) {
        return decay.getOrDefault(dimension.location(), false);
    }

    @Override
    public void setDecaying(ResourceKey<Level> dimension, boolean decaying) {
        decay.put(dimension.location(), decaying);
    }

    @Override
    public double getCleanse(ResourceKey<Level> dimension) {
        return cleanse.getOrDefault(dimension.location(), 0d);
    }

    @Override
    public void setCleanse(ResourceKey<Level> dimension, double amount) {
        cleanse.put(dimension.location(), amount);
    }
    // spotless: on

    public CompoundTag tagSave() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();

        for (var i : timeRemaining.entrySet()) {
            CompoundTag abyssTagData = new CompoundTag();
            abyssTagData.putString("dimension", i.getKey().toString());
            abyssTagData.putLong("ticks", i.getValue());
            abyssTagData.putBoolean("decaying", decay.getOrDefault(i.getKey(), false));
            abyssTagData.putDouble("cleanse", cleanse.getOrDefault(i.getKey(), 0d));
            listTag.add(abyssTagData);
        }
        tag.put("entries", listTag);
        return tag;
    }

    public void tagLoad(CompoundTag tag) {
        timeRemaining.clear();
        decay.clear();
        cleanse.clear();
        ListTag listTag = tag.getList("entries", ListTag.TAG_COMPOUND);

        for (Tag tagDat : listTag) {
            CompoundTag compoundTag = (CompoundTag) tagDat;
            var resLoc = new ResourceLocation(compoundTag.getString("dimension"));
            timeRemaining.put(resLoc, compoundTag.getLong("ticks"));
            decay.put(resLoc, compoundTag.getBoolean("decaying"));
            cleanse.put(resLoc, compoundTag.getDouble("cleanse"));
        }
    }
}
