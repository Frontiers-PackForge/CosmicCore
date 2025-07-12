package com.ghostipedia.cosmiccore.api.data.wireless;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;

public class WirelessEnergySavedData extends SavedData {

    public static class WirelessEnergyData {

        public BigInteger energyStored;
        public BigInteger energyCapacity;
        public boolean isActive;
        public Map<BlockPos, Long> energyInput;
        public Map<BlockPos, Long> energyOutput;
        public Map<BlockPos, Long> energyBuffered;
        public Map<BlockPos, Long> passiveDrain;

        public WirelessEnergyData() {
            this(BigInteger.ZERO, BigInteger.valueOf(-1), false);
        }

        public WirelessEnergyData(BigInteger energyStored, BigInteger energyCapacity) {
            this(energyStored, energyCapacity, false);
        }

        public WirelessEnergyData(BigInteger energyStored, BigInteger energyCapacity, boolean isActive) {
            this.energyStored = energyStored;
            this.energyCapacity = energyCapacity;
            this.isActive = isActive;
            this.energyInput = new HashMap<>();
            this.energyOutput = new HashMap<>();
            this.energyBuffered = new HashMap<>();
            this.passiveDrain = new HashMap<>();
        }

        public static WirelessEnergyData fromNBT(CompoundTag nbt) {
            var stored = new BigInteger(nbt.getByteArray("energyStored"));
            var capacity = new BigInteger(nbt.getByteArray("energyCapacity"));
            var active = nbt.getBoolean("isActive");
            return new WirelessEnergyData(stored, capacity, active);
        }

        public CompoundTag toNBT() {
            var tag = new CompoundTag();
            tag.putByteArray("energyStored", energyStored.toByteArray());
            tag.putByteArray("energyCapacity", energyCapacity.toByteArray());
            tag.putBoolean("isActive", isActive);
            return tag;
        }
    };

    // FIXME why is this attributed to GT?
    private static final String DATA_NAME = "gtceu_wireless_energy";
    // FIXME this key is kind of weird. Search & replace mistake?
    private static final String GlobalEnergyNBTTag = "gtceu_wireless_energy_MapNBTTag";
    public static final HashMap<UUID, WirelessEnergyData> GlobalWirelessEnergy = new HashMap<>(20, 0.9f);

    public static WirelessEnergySavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage()
                .computeIfAbsent(WirelessEnergySavedData::new, WirelessEnergySavedData::new, DATA_NAME);
    }

    private WirelessEnergySavedData() {}

    private WirelessEnergySavedData(CompoundTag nbt) {
        var list = nbt.getList(GlobalEnergyNBTTag, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            var uuid = UUID.fromString(tag.getString("uuid"));
            var data = tag.getCompound("energyData");
            GlobalWirelessEnergy.put(uuid, WirelessEnergyData.fromNBT(data));
        }
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag nbt) {
        var wirelessEnergyList = new ListTag();
        for (var entry : GlobalWirelessEnergy.entrySet()) {
            if (entry.getKey() == null) continue;
            var tag = new CompoundTag();
            tag.putString("uuid", entry.getKey().toString());
            tag.put("energyData", entry.getValue().toNBT());
            wirelessEnergyList.add(tag);
        }
        nbt.put(GlobalEnergyNBTTag, wirelessEnergyList);
        return nbt;
    }

    public BigInteger getEnergyStored(UUID uuid) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return data.energyStored;
    }

    public BigInteger getTotalNetworkEnergyStored(UUID uuid) {
        return this.getEnergyStored(uuid).add(this.getEnergyBuffered(uuid));
    }

    public BigInteger getTotalNetworkEnergyStoredExceptLocalBuffer(UUID uuid, BlockPos localBufferPos) {
        return this.getEnergyStored(uuid).add(this.getEnergyBufferedExceptLocal(uuid, localBufferPos));
    }

    public BigInteger getEnergyCapacity(UUID uuid) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return data.energyCapacity;
    }

    public boolean isActive(UUID uuid) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return data.isActive;
    }

    public long getEnergyInput(UUID uuid) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return data.energyInput.values().stream().mapToLong(Long::longValue).sum();
    }

    public void setEnergyInput(UUID uuid, BlockPos blockPos, long input) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        data.energyInput.put(blockPos, input);
        setDirty();
    }

    public void removeEnergyInput(UUID uuid, BlockPos blockPos) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        data.energyInput.remove(blockPos);
        setDirty();
    }

    public long getEnergyOutput(UUID uuid) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return data.energyOutput.values().stream().mapToLong(Long::longValue).sum();
    }

    public void setEnergyOutput(UUID uuid, BlockPos blockPos, long input) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        data.energyOutput.put(blockPos, input);
        setDirty();
    }

    public void removeEnergyOutput(UUID uuid, BlockPos blockPos) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        data.energyOutput.remove(blockPos);
        setDirty();
    }

    public BigInteger getEnergyBuffered(UUID uuid) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        var sum = BigInteger.ZERO;
        for (var value : data.energyBuffered.values()) sum = sum.add(BigInteger.valueOf(value));
        return sum;
    }

    public BigInteger getEnergyBufferedExceptLocal(UUID uuid, BlockPos localBufferPos) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        var sum = BigInteger.ZERO;
        for (var entry : data.energyBuffered.entrySet()) {
            if (!entry.getKey().equals(localBufferPos))
                sum = sum.add(BigInteger.valueOf(entry.getValue()));
        } ;
        return sum;
    }

    public void setEnergyBuffered(UUID uuid, BlockPos blockPos, long input) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        data.energyBuffered.put(blockPos, input);
        setDirty();
    }

    public void removeEnergyBuffered(UUID uuid, BlockPos blockPos) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        data.energyBuffered.remove(blockPos);
        setDirty();
    }

    public long getPassiveDrain(UUID uuid) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return data.passiveDrain.values().stream().mapToLong(Long::longValue).sum();
    }

    public void setPassiveDrain(UUID uuid, BlockPos blockPos, long input) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        data.passiveDrain.put(blockPos, input);
        setDirty();
    }

    public void removePassiveDrain(UUID uuid, BlockPos blockPos) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        data.passiveDrain.remove(blockPos);
        setDirty();
    }

    private boolean compareLocations(Tuple<String, BlockPos> location1, Tuple<String, BlockPos> location2) {
        if (location1 == null || location2 == null) return false;
        if (!location1.getA().equals(location2.getA())) return false;
        if (!location1.getB().equals(location2.getB())) return false;
        return true;
    }

    /**
     * Add EU to the users global energy. You can enter a negative number to subtract it.
     * If the value goes below 0, it will return the EU amount and no operation will be performed.
     * If the value goes above the {@link WirelessEnergyData} capacity, it will return the energy that was not added to
     * the network.
     * If the operation is successful, return value will be 0.
     * BigIntegers have a much slower operation than long/int. You should call these methods as infrequently as possible
     * and bulk store values to add to the global map
     * 
     * @param uuid UUID of the owner of the network
     * @param EU   The energy to add to the network of the owner
     * @return The amount of EU left after the operation
     */
    public BigInteger addEUToGlobalWirelessEnergy(UUID uuid, BigInteger EU) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        var totalEU = data.energyStored.add(EU);
        if (totalEU.signum() >= 0) {
            if (totalEU.compareTo(data.energyCapacity) > 0) {
                var leftover = totalEU.subtract(data.energyCapacity);
                data.energyStored = data.energyCapacity;
                GlobalWirelessEnergy.put(uuid, data);
                setDirty();
                return leftover;
            }
            data.energyStored = totalEU;
            GlobalWirelessEnergy.put(uuid, data);
            setDirty();
            return BigInteger.ZERO;
        }
        return EU;
    }

    public int addEUToGlobalWirelessEnergy(UUID uuid, int energy) {
        return addEUToGlobalWirelessEnergy(uuid, BigInteger.valueOf(energy)).intValue();
    }

    public long addEUToGlobalWirelessEnergy(UUID uuid, long energy) {
        return addEUToGlobalWirelessEnergy(uuid, BigInteger.valueOf(energy)).longValue();
    }

    public void setEnergy(UUID uuid, BigInteger energy) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        data.energyStored = energy;
        setDirty();
    }

    public void setEnergy(UUID uuid, long energy) {
        setEnergy(uuid, BigInteger.valueOf(energy));
    }

    public void setEnergy(UUID uuid, int energy) {
        setEnergy(uuid, BigInteger.valueOf(energy));
    }

    public void setCapacity(UUID uuid, BigInteger capacity) {
        var data = GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        data.energyCapacity = capacity;
        var isOvercharged = capacity.compareTo(data.energyStored) < 0;
        if (isOvercharged) data.energyStored = capacity;
        setDirty();
    }

    public void setCapacity(UUID uuid, long capacity) {
        setCapacity(uuid, BigInteger.valueOf(capacity));
    }

    public void setCapacity(UUID uuid, int capacity) {
        setCapacity(uuid, BigInteger.valueOf(capacity));
    }

    public void clearWirelessEnergy(UUID uuid) {
        GlobalWirelessEnergy.put(uuid, new WirelessEnergyData());
    }

    public void clearGlobalWirelessEnergy() {
        GlobalWirelessEnergy.clear();
    }

    public void setActive(UUID uuid, boolean isActive) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        var data = GlobalWirelessEnergy.get(uuid);
        data.isActive = isActive;
        setDirty();
    }
}
