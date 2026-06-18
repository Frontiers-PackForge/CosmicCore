package com.ghostipedia.cosmiccore.common.airControl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class OxygenBudget implements IOxygen {

    private final Map<ResourceLocation, Long> oxygenTicksByDimension = new HashMap<>();
    private final Map<ResourceLocation, Boolean> consumingByDimension = new HashMap<>();
    private final Map<ResourceLocation, Double> regenBufferByDimension = new HashMap<>();

    @Override
    public long getOxygenTicks(ResourceKey<Level> dimension) {
        return oxygenTicksByDimension.getOrDefault(dimension.location(), -1L);
    }

    @Override
    public void setOxygenTicks(ResourceKey<Level> dimension, long ticks) {
        oxygenTicksByDimension.put(dimension.location(), ticks);
    }

    @Override
    public boolean isConsuming(ResourceKey<Level> dimension) {
        return consumingByDimension.getOrDefault(dimension.location(), false);
    }

    @Override
    public void setConsuming(ResourceKey<Level> dimension, boolean consuming) {
        consumingByDimension.put(dimension.location(), consuming);
    }

    @Override
    public double getRegenBuffer(ResourceKey<Level> dimension) {
        return regenBufferByDimension.getOrDefault(dimension.location(), 0.0);
    }

    @Override
    public void setRegenBuffer(ResourceKey<Level> dimension, double buffer) {
        regenBufferByDimension.put(dimension.location(), buffer);
    }

    // ---- NBT persistence ----

    public CompoundTag saveTag() {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();

        for (var entry : oxygenTicksByDimension.entrySet()) {
            ResourceLocation dim = entry.getKey();
            CompoundTag tag = new CompoundTag();
            tag.putString("dimension", dim.toString());
            tag.putLong("oxygenTicks", entry.getValue());
            tag.putBoolean("consuming", consumingByDimension.getOrDefault(dim, false));
            tag.putDouble("regenBuffer", regenBufferByDimension.getOrDefault(dim, 0.0));
            list.add(tag);
        }
        root.put("entries", list);
        return root;
    }

    public void loadTag(CompoundTag root) {
        oxygenTicksByDimension.clear();
        consumingByDimension.clear();
        regenBufferByDimension.clear();

        ListTag list = root.getList("entries", Tag.TAG_COMPOUND);
        for (Tag element : list) {
            CompoundTag tag = (CompoundTag) element;
            ResourceLocation dim = ResourceLocation.fromNamespaceAndPath(tag.getString("dimension"));
            oxygenTicksByDimension.put(dim, tag.getLong("oxygenTicks"));
            consumingByDimension.put(dim, tag.getBoolean("consuming"));
            regenBufferByDimension.put(dim, tag.getDouble("regenBuffer"));
        }
    }
}
