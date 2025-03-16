package com.ghostipedia.cosmiccore.api.data.wireless;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
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
        public Set<String> wirelessDimensions;

        public WirelessEnergyData() {
            this(BigInteger.ZERO, BigInteger.valueOf(-1), false, new HashSet<>());
        }

        public WirelessEnergyData(BigInteger energyStored, BigInteger energyCapacity) {
            this(energyStored, energyCapacity, false, new HashSet<>());
        }

        public WirelessEnergyData(BigInteger energyStored, BigInteger energyCapacity, boolean isActive,
                                  Set<String> wirelessDimensions) {
            this.energyStored = energyStored;
            this.energyCapacity = energyCapacity;
            this.isActive = isActive;
            this.energyInput = new HashMap<>();
            this.energyOutput = new HashMap<>();
            this.energyBuffered = new HashMap<>();
            this.passiveDrain = new HashMap<>();
            this.wirelessDimensions = wirelessDimensions;
        }

        public static WirelessEnergyData fromNBT(CompoundTag nbt) {
            var stored = new BigInteger(nbt.getByteArray("energyStored"));
            var capacity = new BigInteger(nbt.getByteArray("energyCapacity"));
            var active = nbt.getBoolean("isActive");
            var dimensionsListTag = nbt.getList("wirelessDimensions", Tag.TAG_STRING);
            var dimensionsList = new HashSet<String>();
            for (var tag : dimensionsListTag) dimensionsList.add(tag.getAsString());
            return new WirelessEnergyData(stored, capacity, active, dimensionsList);
        }

        public CompoundTag toNBT() {
            var tag = new CompoundTag();
            tag.putByteArray("energyStored", energyStored.toByteArray());
            tag.putByteArray("energyCapacity", energyCapacity.toByteArray());
            tag.putBoolean("isActive", isActive);
            var list = new ListTag();
            for (var dimension : wirelessDimensions) list.add(StringTag.valueOf(dimension));
            tag.put("wirelessDimensions", list);
            return tag;
        }
    };

    private static final String DATA_NAME = "gtceu_wireless_energy";
    private static final String GlobalEnergyNBTTag = "gtceu_wireless_energy_MapNBTTag";
    public static final HashMap<UUID, WirelessEnergyData> GlobalWirelessEnergy = new HashMap<>(20, 0.9f);

    private final ServerLevel serverLevel;

    public static WirelessEnergySavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(
                tag -> new WirelessEnergySavedData(serverLevel, tag),
                () -> new WirelessEnergySavedData(serverLevel), DATA_NAME);
    }

    private WirelessEnergySavedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    private WirelessEnergySavedData(ServerLevel serverLevel, CompoundTag nbt) {
        this(serverLevel);
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
            var tag = new CompoundTag();
            tag.putString("uuid", entry.getKey().toString());
            tag.put("energyData", entry.getValue().toNBT());
            wirelessEnergyList.add(tag);
        }
        nbt.put(GlobalEnergyNBTTag, wirelessEnergyList);
        return nbt;
    }

    public BigInteger getEnergyStored(UUID uuid) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return GlobalWirelessEnergy.get(uuid).energyStored;
    }

    public BigInteger getTotalNetworkEnergyStored(UUID uuid) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return this.getEnergyStored(uuid).add(this.getEnergyBuffered(uuid));
    }

    public BigInteger getTotalNetworkEnergyStoredExceptLocalBuffer(UUID uuid, BlockPos localBufferPos) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return this.getEnergyStored(uuid).add(this.getEnergyBufferedExceptLocal(uuid, localBufferPos));
    }

    public BigInteger getEnergyCapacity(UUID uuid) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return GlobalWirelessEnergy.get(uuid).energyCapacity;
    }

    public boolean isActive(UUID uuid) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return GlobalWirelessEnergy.get(uuid).isActive;
    }

    public long getEnergyInput(UUID uuid) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return GlobalWirelessEnergy.get(uuid).energyInput.values().stream().mapToLong(Long::longValue).sum();
    }

    public void setEnergyInput(UUID uuid, BlockPos blockPos, long input) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        GlobalWirelessEnergy.get(uuid).energyInput.put(blockPos, input);
    }

    public void removeEnergyInput(UUID uuid, BlockPos blockPos) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        GlobalWirelessEnergy.get(uuid).energyInput.remove(blockPos);
    }

    public long getEnergyOutput(UUID uuid) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return GlobalWirelessEnergy.get(uuid).energyOutput.values().stream().mapToLong(Long::longValue).sum();
    }

    public void setEnergyOutput(UUID uuid, BlockPos blockPos, long input) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        GlobalWirelessEnergy.get(uuid).energyOutput.put(blockPos, input);
    }

    public void removeEnergyOutput(UUID uuid, BlockPos blockPos) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        GlobalWirelessEnergy.get(uuid).energyOutput.remove(blockPos);
    }

    public BigInteger getEnergyBuffered(UUID uuid) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        var sum = BigInteger.ZERO;
        for (var value : GlobalWirelessEnergy.get(uuid).energyBuffered.values()) sum.add(BigInteger.valueOf(value));
        return sum;
    }

    public BigInteger getEnergyBufferedExceptLocal(UUID uuid, BlockPos localBufferPos) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        var sum = BigInteger.ZERO;
        for (var entry : GlobalWirelessEnergy.get(uuid).energyBuffered.entrySet()) {
            if (!entry.getKey().equals(localBufferPos))
                sum = sum.add(BigInteger.valueOf(entry.getValue()));
        };
        return sum;
    }

    public void setEnergyBuffered(UUID uuid, BlockPos blockPos, long input) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        GlobalWirelessEnergy.get(uuid).energyBuffered.put(blockPos, input);
    }

    public void removeEnergyBuffered(UUID uuid, BlockPos blockPos) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        GlobalWirelessEnergy.get(uuid).energyBuffered.remove(blockPos);
    }

    public long getPassiveDrain(UUID uuid) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return GlobalWirelessEnergy.get(uuid).passiveDrain.values().stream().mapToLong(Long::longValue).sum();
    }

    public void setPassiveDrain(UUID uuid, BlockPos blockPos, long input) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        GlobalWirelessEnergy.get(uuid).passiveDrain.put(blockPos, input);
    }

    public void removePassiveDrain(UUID uuid, BlockPos blockPos) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        GlobalWirelessEnergy.get(uuid).passiveDrain.remove(blockPos);
    }

    public boolean isWirelessActive(UUID uuid, String dimension) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return GlobalWirelessEnergy.get(uuid).wirelessDimensions.contains(dimension);
    }

    public void addWirelessDimensions(UUID uuid, String dimension) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        GlobalWirelessEnergy.get(uuid).wirelessDimensions.add(dimension);
    }

    public void removeWirelessDimensions(UUID uuid, String dimension) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        GlobalWirelessEnergy.get(uuid).wirelessDimensions.remove(dimension);
    }


    public List<String> getWirelessDimensions(UUID uuid) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        return GlobalWirelessEnergy.get(uuid).wirelessDimensions.stream().toList();
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
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        var energyStore = GlobalWirelessEnergy.get(uuid);
        var totalEU = energyStore.energyStored.add(EU);
        if (totalEU.signum() >= 0) {
            if (totalEU.compareTo(energyStore.energyCapacity) > 0) {
                var leftover = totalEU.subtract(energyStore.energyCapacity);
                energyStore.energyStored = energyStore.energyCapacity;
                GlobalWirelessEnergy.put(uuid, energyStore);
                setDirty();
                return leftover;
            }
            energyStore.energyStored = totalEU;
            GlobalWirelessEnergy.put(uuid, energyStore);
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
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        var data = GlobalWirelessEnergy.get(uuid);
        data.energyStored = energy;
        GlobalWirelessEnergy.put(uuid, data);
        setDirty();
    }

    public void setEnergy(UUID uuid, long energy) {
        setEnergy(uuid, BigInteger.valueOf(energy));
    }

    public void setEnergy(UUID uuid, int energy) {
        setEnergy(uuid, BigInteger.valueOf(energy));
    }

    public void setCapacity(UUID uuid, BigInteger capacity) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
        var data = GlobalWirelessEnergy.get(uuid);
        data.energyCapacity = capacity;
        GlobalWirelessEnergy.put(uuid, data);
        setDirty();
    }

    public void setCapacity(UUID uuid, long capacity) {
        setCapacity(uuid, BigInteger.valueOf(capacity));
    }

    public void setCapacity(UUID uuid, int capacity) {
        GlobalWirelessEnergy.computeIfAbsent(uuid, k -> new WirelessEnergyData());
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
