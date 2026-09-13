package com.ghostipedia.cosmiccore.common.production;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityResource;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;

public record ProductionResource(String kind, String id, CompoundTag icon) {

    private static final long MAX_IDENTITY_BYTES = 16 * 1024;

    public static ProductionResource item(ItemStack stack, HolderLookup.Provider lookup) {
        return fromActivity(ActivityResource.item(stack), lookup);
    }

    public static ProductionResource fluid(FluidStack stack, HolderLookup.Provider lookup) {
        return fromActivity(ActivityResource.fluid(stack), lookup);
    }

    public static ProductionResource value(String kind, String id) {
        return new ProductionResource(kind, id, new CompoundTag());
    }

    private static ProductionResource fromActivity(ActivityResource resource, HolderLookup.Provider lookup) {
        CompoundTag tag = resource.toTag(lookup);
        if (tag.sizeInBytes() > MAX_IDENTITY_BYTES) return null;
        return new ProductionResource(tag.getString("kind"), tag.getString("id"), tag.getCompound("icon"));
    }

    public String key() {
        return kind + '\u0000' + id + '\u0000' + icon;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", kind);
        tag.putString("id", id);
        tag.put("icon", icon.copy());
        return tag;
    }

    public static ProductionResource fromTag(CompoundTag tag) {
        String kind = tag.getString("kind");
        String id = tag.getString("id");
        if (kind.length() > 32 || id.length() > 512 || tag.sizeInBytes() > MAX_IDENTITY_BYTES) return null;
        return new ProductionResource(kind, id, tag.getCompound("icon").copy());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ProductionResource resource && key().equals(resource.key());
    }

    @Override
    public int hashCode() {
        return Objects.hash(key());
    }
}
