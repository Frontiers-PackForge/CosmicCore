package com.ghostipedia.cosmiccore.common.production;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.UUID;

public final class ProductionBinding implements INBTSerializable<CompoundTag> {

    private UUID pool;

    public UUID pool(MetaMachine machine) {
        if (pool != null) return pool;
        UUID owner = machine.getOwnerUUID();
        if (owner == null) return null;
        if (machine.getOwner() instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(owner);
            if (team != null) owner = team.getTeamId();
        }
        pool = owner;
        return pool;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (pool != null) tag.putUUID("pool", pool);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        pool = tag.hasUUID("pool") ? tag.getUUID("pool") : null;
    }
}
