package com.ghostipedia.cosmiccore.common.reflection;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of IReflection - stores all soul/erosion/bargain data for a player.
 */
public class ReflectionData implements IReflection {

    // Currency and capacity
    private int shardBalance = 0;
    private int baseCapacity = 100;
    private int bonusCapacity = 0;

    // Erosion and death tracking
    private int erosion = 0;
    private int deathCount = 0;
    private int highestThresholdSeen = -1;
    private boolean awakened = false;
    private boolean awakeningSequenceCompleted = false;

    private final Set<ResourceLocation> activeBargains = new HashSet<>();
    private final Set<ResourceLocation> defianceScars = new HashSet<>();
    private final Map<String, Integer> commandUsageCount = new HashMap<>();
    private final Map<String, Long> commandLastUseTime = new HashMap<>();
    private final Map<String, Integer> memory = new HashMap<>();

    // ---- Shards (Currency) ----

    @Override
    public int getShardBalance() {
        return shardBalance;
    }

    @Override
    public void setShardBalance(int shards) {
        this.shardBalance = Math.max(0, shards);
    }

    @Override
    public void addShards(int amount) {
        this.shardBalance = Math.max(0, this.shardBalance + amount);
    }

    @Override
    public boolean spendShards(int amount) {
        if (shardBalance >= amount) {
            shardBalance -= amount;
            return true;
        }
        return false;
    }

    // ---- Soul Capacity ----

    @Override
    public int getBaseCapacity() {
        return baseCapacity;
    }

    @Override
    public void setBaseCapacity(int capacity) {
        this.baseCapacity = Math.max(0, capacity);
    }

    @Override
    public int getBonusCapacity() {
        return bonusCapacity;
    }

    @Override
    public void setBonusCapacity(int bonus) {
        this.bonusCapacity = Math.max(0, bonus);
    }

    @Override
    public int getUsedCapacity() {
        // Calculate weight from all active bargains
        int totalWeight = 0;
        for (ResourceLocation bargainId : activeBargains) {
            var bargainOpt = com.ghostipedia.cosmiccore.common.reflection.bargain.BargainRegistry.get(bargainId);
            if (bargainOpt.isPresent()) {
                totalWeight += bargainOpt.get().getWeight();
            }
        }
        return totalWeight;
    }

    // ---- Erosion ----

    @Override
    public int getErosion() {
        return erosion;
    }

    @Override
    public void setErosion(int erosion) {
        this.erosion = Math.max(0, erosion);
    }

    @Override
    public void addErosion(int amount) {
        this.erosion = Math.max(0, this.erosion + amount);
    }

    @Override
    public int getDeathCount() {
        return deathCount;
    }

    @Override
    public void recordDeath() {
        deathCount++;
        addErosion(1);
    }

    // ---- Bargains ----

    @Override
    public Set<ResourceLocation> getActiveBargains() {
        return new HashSet<>(activeBargains);
    }

    @Override
    public boolean hasBargain(ResourceLocation bargainId) {
        return activeBargains.contains(bargainId);
    }

    @Override
    public void acceptBargain(ResourceLocation bargainId) {
        activeBargains.add(bargainId);
    }

    @Override
    public void defy(ResourceLocation bargainId) {
        if (activeBargains.remove(bargainId)) {
            defianceScars.add(bargainId);
        }
    }

    @Override
    public Set<ResourceLocation> getDefianceScars() {
        return new HashSet<>(defianceScars);
    }

    @Override
    public boolean hasDefianceScar(ResourceLocation bargainId) {
        return defianceScars.contains(bargainId);
    }

    @Override
    public void removeScar(ResourceLocation bargainId) {
        defianceScars.remove(bargainId);
    }

    // ---- Threshold Tracking ----

    @Override
    public int getHighestThresholdSeen() {
        return highestThresholdSeen;
    }

    @Override
    public void setHighestThresholdSeen(int threshold) {
        this.highestThresholdSeen = Math.max(this.highestThresholdSeen, threshold);
    }

    // ---- First Encounter ----

    @Override
    public boolean hasAwakened() {
        return awakened;
    }

    @Override
    public void setAwakened(boolean awakened) {
        this.awakened = awakened;
    }

    @Override
    public boolean hasCompletedAwakeningSequence() {
        return awakeningSequenceCompleted;
    }

    @Override
    public void setAwakeningSequenceCompleted(boolean completed) {
        this.awakeningSequenceCompleted = completed;
    }

    // ---- Command Usage Tracking ----

    @Override
    public int getCommandUsageCount(String commandId) {
        return commandUsageCount.getOrDefault(commandId, 0);
    }

    @Override
    public void recordCommandUse(String commandId) {
        commandUsageCount.merge(commandId, 1, Integer::sum);
        commandLastUseTime.put(commandId, System.currentTimeMillis());
    }

    @Override
    public void resetCommandUsage(String commandId) {
        commandUsageCount.remove(commandId);
    }

    @Override
    public long getLastCommandUseTime(String commandId) {
        return commandLastUseTime.getOrDefault(commandId, 0L);
    }

    // ---- Memory / Context ----

    @Override
    public Map<String, Integer> getMemory() {
        return new HashMap<>(memory);
    }

    @Override
    public void rememberEvent(String eventKey) {
        memory.merge(eventKey, 1, Integer::sum);
    }

    @Override
    public int getMemoryCount(String eventKey) {
        return memory.getOrDefault(eventKey, 0);
    }

    // ---- NBT Persistence ----

    public CompoundTag saveTag() {
        CompoundTag root = new CompoundTag();

        // Currency and capacity
        root.putInt("shardBalance", shardBalance);
        root.putInt("baseCapacity", baseCapacity);
        root.putInt("bonusCapacity", bonusCapacity);

        // Core stats
        root.putInt("erosion", erosion);
        root.putInt("deathCount", deathCount);
        root.putInt("highestThresholdSeen", highestThresholdSeen);
        root.putBoolean("awakened", awakened);
        root.putBoolean("awakeningSequenceCompleted", awakeningSequenceCompleted);

        // Active bargains
        ListTag bargainList = new ListTag();
        for (ResourceLocation bargain : activeBargains) {
            bargainList.add(StringTag.valueOf(bargain.toString()));
        }
        root.put("activeBargains", bargainList);

        // Defiance scars
        ListTag scarList = new ListTag();
        for (ResourceLocation scar : defianceScars) {
            scarList.add(StringTag.valueOf(scar.toString()));
        }
        root.put("defianceScars", scarList);

        // Command usage
        CompoundTag cmdUsage = new CompoundTag();
        for (Map.Entry<String, Integer> entry : commandUsageCount.entrySet()) {
            cmdUsage.putInt(entry.getKey(), entry.getValue());
        }
        root.put("commandUsageCount", cmdUsage);

        CompoundTag cmdTime = new CompoundTag();
        for (Map.Entry<String, Long> entry : commandLastUseTime.entrySet()) {
            cmdTime.putLong(entry.getKey(), entry.getValue());
        }
        root.put("commandLastUseTime", cmdTime);

        // Memory
        CompoundTag memoryTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : memory.entrySet()) {
            memoryTag.putInt(entry.getKey(), entry.getValue());
        }
        root.put("memory", memoryTag);

        return root;
    }

    public void loadTag(CompoundTag root) {
        // Currency and capacity (with defaults for backwards compatibility)
        shardBalance = root.getInt("shardBalance");
        baseCapacity = root.contains("baseCapacity") ? root.getInt("baseCapacity") : 100;
        bonusCapacity = root.getInt("bonusCapacity");

        // Core stats
        erosion = root.getInt("erosion");
        deathCount = root.getInt("deathCount");
        highestThresholdSeen = root.getInt("highestThresholdSeen");
        awakened = root.getBoolean("awakened");
        awakeningSequenceCompleted = root.getBoolean("awakeningSequenceCompleted");

        // Active bargains
        activeBargains.clear();
        ListTag bargainList = root.getList("activeBargains", Tag.TAG_STRING);
        for (Tag tag : bargainList) {
            activeBargains.add(new ResourceLocation(tag.getAsString()));
        }

        // Defiance scars
        defianceScars.clear();
        ListTag scarList = root.getList("defianceScars", Tag.TAG_STRING);
        for (Tag tag : scarList) {
            defianceScars.add(new ResourceLocation(tag.getAsString()));
        }

        // Command usage
        commandUsageCount.clear();
        CompoundTag cmdUsage = root.getCompound("commandUsageCount");
        for (String key : cmdUsage.getAllKeys()) {
            commandUsageCount.put(key, cmdUsage.getInt(key));
        }

        commandLastUseTime.clear();
        CompoundTag cmdTime = root.getCompound("commandLastUseTime");
        for (String key : cmdTime.getAllKeys()) {
            commandLastUseTime.put(key, cmdTime.getLong(key));
        }

        // Memory
        memory.clear();
        CompoundTag memoryTag = root.getCompound("memory");
        for (String key : memoryTag.getAllKeys()) {
            memory.put(key, memoryTag.getInt(key));
        }
    }
}
