package com.ghostipedia.cosmiccore.common.reflection;

import com.ghostipedia.cosmiccore.common.reflection.soul.SoulShape;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of IReflection - stores all soul/erosion/bargain data for a player.
 */
public class ReflectionData implements IReflection, INBTSerializable<CompoundTag> {

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

    // Void resistance tracking
    private int voidSaveCount = 0;

    // Soul Shape
    private SoulShape soulShape = SoulShape.UNSHAPED;

    // Soul Super state
    private long superCooldownStart = 0;
    private long superActiveUntil = 0;

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

    // ---- Void Save Tracking ----

    @Override
    public int getVoidSaveCount() {
        return voidSaveCount;
    }

    @Override
    public void incrementVoidSaveCount() {
        voidSaveCount++;
    }

    @Override
    public void resetVoidSaveCount() {
        voidSaveCount = 0;
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

    // ---- Soul Shape ----

    @Override
    public SoulShape getSoulShape() {
        return soulShape;
    }

    @Override
    public void setSoulShape(SoulShape shape) {
        this.soulShape = shape != null ? shape : SoulShape.UNSHAPED;
    }

    // ---- Soul Super ----

    @Override
    public long getSuperCooldownStart() {
        return superCooldownStart;
    }

    @Override
    public void setSuperCooldownStart(long gameTime) {
        this.superCooldownStart = gameTime;
    }

    @Override
    public long getSuperActiveUntil() {
        return superActiveUntil;
    }

    @Override
    public void setSuperActiveUntil(long gameTime) {
        this.superActiveUntil = gameTime;
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
        root.putInt("voidSaveCount", voidSaveCount);
        root.putString("soulShape", soulShape.getId());
        root.putLong("superCooldownStart", superCooldownStart);
        root.putLong("superActiveUntil", superActiveUntil);

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

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = saveTag();
        return tag != null ? tag : new CompoundTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt != null) {
            loadTag(nbt);
        }
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
        voidSaveCount = root.getInt("voidSaveCount");
        soulShape = root.contains("soulShape") ? SoulShape.fromId(root.getString("soulShape")) : SoulShape.UNSHAPED;
        superCooldownStart = root.getLong("superCooldownStart");
        superActiveUntil = root.getLong("superActiveUntil");

        // Active bargains
        activeBargains.clear();
        ListTag bargainList = root.getList("activeBargains", Tag.TAG_STRING);
        for (Tag tag : bargainList) {
            activeBargains.add(ResourceLocation.parse(tag.getAsString()));
        }

        // Defiance scars
        defianceScars.clear();
        ListTag scarList = root.getList("defianceScars", Tag.TAG_STRING);
        for (Tag tag : scarList) {
            defianceScars.add(ResourceLocation.parse(tag.getAsString()));
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
