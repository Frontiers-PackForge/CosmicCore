package com.ghostipedia.cosmiccore.api.data.souls;

import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class SoulNetwork implements INBTSerializable<CompoundTag> {

    @Getter
    private int tier = 0, currentSouls = 0;

    public SoulNetwork() {}

    public int add(int amount, int max) {
        int oldSouls = this.currentSouls;
        if (oldSouls >= max) return 0;
        else {
            int newSouls = Math.min(max, oldSouls + amount);
            this.currentSouls = newSouls;
            return newSouls - oldSouls;
        }
    }

    public int syphon(int amount) {
        if (this.currentSouls >= amount) {
            this.currentSouls -= amount;
            return amount;
        } else return 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {}


}
