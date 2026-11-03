package com.ghostipedia.cosmiccore.common.compat.apothic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public final class SpawnerEntityPolicy {

    private SpawnerEntityPolicy() {}

    public static CompoundTag sanitize(CompoundTag original) {
        boolean reset = containsModdedEntity(original.getCompound("SpawnData").getCompound("entity"));
        for (Tag entry : original.getList("SpawnPotentials", Tag.TAG_COMPOUND)) {
            reset |= containsModdedEntity(((CompoundTag) entry).getCompound("data").getCompound("entity"));
        }
        if (!reset) return original;

        CompoundTag result = original.copy();
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "minecraft:pig");
        CompoundTag data = new CompoundTag();
        data.put("entity", entity);
        result.put("SpawnData", data);
        CompoundTag potential = new CompoundTag();
        potential.putInt("weight", 1);
        potential.put("data", data.copy());
        ListTag potentials = new ListTag();
        potentials.add(potential);
        result.put("SpawnPotentials", potentials);
        return result;
    }

    private static boolean containsModdedEntity(CompoundTag entity) {
        ResourceLocation id = ResourceLocation.tryParse(entity.getString("id"));
        if (id != null && !id.getNamespace().equals("minecraft")) return true;
        for (Tag passenger : entity.getList("Passengers", Tag.TAG_COMPOUND)) {
            if (containsModdedEntity((CompoundTag) passenger)) return true;
        }
        return false;
    }
}
