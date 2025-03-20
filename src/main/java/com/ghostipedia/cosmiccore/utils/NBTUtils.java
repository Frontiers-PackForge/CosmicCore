package com.ghostipedia.cosmiccore.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;

public abstract class NBTUtils {

    public static CompoundTag toNBT(Tuple<String, BlockPos> tuple) {
        var tag = new CompoundTag();
        tag.putString("A", tuple.getA());
        tag.putLong("B", tuple.getB().asLong());
        return tag;
    }

    public static Tuple<String, BlockPos> fromNBT(CompoundTag tag) {
        var A = tag.getString("A");
        var B = BlockPos.of(tag.getLong("B"));
        return new Tuple<>(A, B);
    }

}
