package com.ghostipedia.cosmiccore.common.airControl;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.function.IntUnaryOperator;

import static com.ghostipedia.cosmiccore.common.airControl.OxygenConfig.*;

public final class OxygenBudget implements INBTSerializable<CompoundTag> {

    private long oxygenTicks = MAX_OXYGEN_TICKS;
    private double fractionalTicks;

    public long getOxygenTicks() {
        return oxygenTicks;
    }

    public void setOxygenTicks(long ticks) {
        setRemaining(ticks);
    }

    public void restoreOxygenTicks(long ticks) {
        if (ticks > 0) setRemaining(oxygenTicks + fractionalTicks + ticks);
    }

    private void setRemaining(double ticks) {
        double remaining = Math.clamp(ticks, 0, MAX_OXYGEN_TICKS);
        oxygenTicks = (long) remaining;
        fractionalTicks = remaining - oxygenTicks;
    }

    public void reset() {
        oxygenTicks = MAX_OXYGEN_TICKS;
        fractionalTicks = 0;
    }

    public boolean tick(double drainPerTick, double recoveryPerTick, IntUnaryOperator supply) {
        double remaining = oxygenTicks + fractionalTicks;
        if (drainPerTick <= 0 && recoveryPerTick > 0) {
            setRemaining(remaining + recoveryPerTick);
            return false;
        }

        double demand = Math.max(0, drainPerTick);
        double refill = Math.min(TANK_TOPUP_TICKS_PER_TICK, MAX_OXYGEN_TICKS - remaining);
        int requested = (int) (demand + refill);
        int supplied = requested > 0 ? Math.clamp(supply.applyAsInt(requested), 0, requested) : 0;
        double next = remaining + supplied - demand;
        setRemaining(next);
        return demand > 0 && next <= 0 && supplied < demand;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("oxygenTicks", oxygenTicks);
        tag.putDouble("fractionalTicks", fractionalTicks);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        reset();
        if (tag.contains("oxygenTicks", Tag.TAG_ANY_NUMERIC)) {
            double fraction = tag.getDouble("fractionalTicks");
            if (!Double.isFinite(fraction) || Math.abs(fraction) >= 1) fraction = 0;
            setRemaining(tag.getLong("oxygenTicks") + fraction);
            return;
        }

        double fullestReserve = -1;
        for (Tag entry : tag.getList("entries", Tag.TAG_COMPOUND)) {
            CompoundTag legacy = (CompoundTag) entry;
            if (!legacy.contains("oxygenTicks", Tag.TAG_ANY_NUMERIC) || legacy.getLong("oxygenTicks") < 0) continue;
            double buffer = legacy.getDouble("regenBuffer");
            double fraction = buffer > 0 ? buffer * 20 : buffer;
            if (!Double.isFinite(fraction) || Math.abs(fraction) >= 1) fraction = 0;
            double remaining = Math.clamp(legacy.getLong("oxygenTicks") + fraction, 0, MAX_OXYGEN_TICKS);
            fullestReserve = Math.max(fullestReserve, remaining);
        }
        if (fullestReserve >= 0) setRemaining(fullestReserve);
    }
}
