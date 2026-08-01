package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import appeng.api.networking.IManagedGridNode;
import org.jetbrains.annotations.NotNull;

final class ManagedGridNodeState implements INBTSerializable<CompoundTag> {

    private final IManagedGridNode node;

    ManagedGridNodeState(IManagedGridNode node) {
        this.node = node;
    }

    IManagedGridNode node() {
        return node;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        node.saveToNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider registries, @NotNull CompoundTag tag) {
        node.loadFromNBT(tag);
    }
}
