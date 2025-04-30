package com.ghostipedia.cosmiccore.api.data.savedData;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UniqueMultiblockData {

    @Getter
    public static class UniqueMultiblockId {

        private final String multiblockType;
        private final String multiblockDimension;

        protected UniqueMultiblockId(String multiblockType, String multiblockDimension) {
            this.multiblockType = multiblockType;
            this.multiblockDimension = multiblockDimension;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UniqueMultiblockId that = (UniqueMultiblockId) o;
            return Objects.equals(multiblockType, that.multiblockType) &&
                    Objects.equals(multiblockDimension, that.multiblockDimension);
        }

        @Override
        public int hashCode() {
            int result = 17; // Some arbitrary prime number
            result = 31 * result + multiblockType.hashCode();
            result = 31 * result + multiblockDimension.hashCode();
            return result;
        }
    }

    private static final String MULTIBLOCK_TYPE = "multiblockType";
    private static final String MULTIBLOCK_DIMENSION = "multiblockDimension";
    private static final String MULTIBLOCK_POS = "multiblockPos";

    // Map a tuple of "Multiblock Type" and "Dimension Name" to a "BlockPos"
    public Map<UniqueMultiblockId, BlockPos> data;

    public UniqueMultiblockData() {
        this.data = new HashMap<>();
    }

    public static UniqueMultiblockData fromTag(ListTag tag) {
        var result = new UniqueMultiblockData();
        for (int i = 0; i < tag.size(); ++i) {
            CompoundTag entry = tag.getCompound(i);
            var type = entry.getString(MULTIBLOCK_TYPE);
            var dimension = entry.getString(MULTIBLOCK_DIMENSION);
            var pos = BlockPos.of(entry.getLong(MULTIBLOCK_POS));
            result.data.put(new UniqueMultiblockId(type, dimension), pos);
        }
        return result;
    }

    public ListTag toTag() {
        var uniqueMultiblockData = new ListTag();
        for (var entry : data.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            var entryTag = new CompoundTag();
            entryTag.putString(MULTIBLOCK_TYPE, entry.getKey().getMultiblockType());
            entryTag.putString(MULTIBLOCK_DIMENSION, entry.getKey().getMultiblockDimension());
            entryTag.putLong(MULTIBLOCK_POS, entry.getValue().asLong());
            uniqueMultiblockData.add(entryTag);
        }
        return uniqueMultiblockData;
    }

    public boolean hasData(String multiblockType, String dimension) {
        return data.containsKey(new UniqueMultiblockId(multiblockType, dimension));
    }

    public boolean isUnique(String multiblockType, String dimension, BlockPos pos) {
        var key = new UniqueMultiblockId(multiblockType, dimension);
        if (!data.containsKey(key)) return true;
        else return data.get(key).equals(pos);
    }

    public void addMultiblock(String multiblockType, String dimension, BlockPos pos) {
        data.put(new UniqueMultiblockId(multiblockType, dimension), pos);
    }

    public void removeMultiblock(String multiblockType, String dimension, BlockPos pos) {
        var key = new UniqueMultiblockId(multiblockType, dimension);
        if (!hasData(multiblockType, dimension)) return;
        if (data.get(key).equals(pos)) data.remove(key);
    }
}
