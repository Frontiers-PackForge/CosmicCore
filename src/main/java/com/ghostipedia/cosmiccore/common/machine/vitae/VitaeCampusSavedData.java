package com.ghostipedia.cosmiccore.common.machine.vitae;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class VitaeCampusSavedData extends SavedData {

    public static final int LINK_RANGE = 64;
    private static final String DATA_NAME = CosmicCore.MOD_ID + "_vitae_campus_data";

    private final Map<GlobalPos, Core> cores = new HashMap<>();
    private final Map<GlobalPos, GlobalPos> moduleCores = new HashMap<>();

    public static VitaeCampusSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(VitaeCampusSavedData::new, VitaeCampusSavedData::load), DATA_NAME);
    }

    public CoreResult registerCore(UUID owner, GlobalPos position, int altarLevel, int resourceLimit) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(position);
        Core existing = cores.get(position);
        if (existing != null && !existing.owner.equals(owner)) return CoreResult.OWNER_MISMATCH;
        if (existing == null) {
            cores.put(position, new Core(owner, altarLevel, resourceLimit));
        } else {
            existing.altarLevel = altarLevel;
            existing.resourceLimit = resourceLimit;
        }
        setDirty();
        return existing == null ? CoreResult.REGISTERED : CoreResult.UPDATED;
    }

    public void setCoreOperational(GlobalPos position, boolean operational) {
        Core core = cores.get(position);
        if (core == null || core.operational == operational) return;
        core.operational = operational;
        setDirty();
    }

    public boolean removeCore(GlobalPos position) {
        if (cores.remove(position) == null) return false;
        moduleCores.entrySet().removeIf(entry -> entry.getValue().equals(position));
        setDirty();
        return true;
    }

    public LinkResult link(UUID owner, GlobalPos corePosition, GlobalPos modulePosition) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(corePosition);
        Objects.requireNonNull(modulePosition);
        Core core = cores.get(corePosition);
        if (core == null) return LinkResult.CORE_MISSING;
        if (!core.owner.equals(owner)) return LinkResult.OWNER_MISMATCH;
        if (!corePosition.dimension().equals(modulePosition.dimension())) return LinkResult.DIFFERENT_DIMENSION;
        if (corePosition.equals(modulePosition)) return LinkResult.SELF_LINK;
        if (corePosition.pos().distSqr(modulePosition.pos()) > LINK_RANGE * LINK_RANGE) return LinkResult.OUT_OF_RANGE;
        GlobalPos existing = moduleCores.get(modulePosition);
        moduleCores.put(modulePosition, corePosition);
        setDirty();
        if (existing == null) return LinkResult.LINKED;
        return existing.equals(corePosition) ? LinkResult.UPDATED : LinkResult.RELINKED;
    }

    public boolean unlink(GlobalPos modulePosition) {
        if (moduleCores.remove(modulePosition) == null) return false;
        setDirty();
        return true;
    }

    public Optional<GlobalPos> coreFor(GlobalPos modulePosition) {
        return Optional.ofNullable(moduleCores.get(modulePosition));
    }

    public Optional<Access> accessFor(GlobalPos modulePosition) {
        GlobalPos corePosition = moduleCores.get(modulePosition);
        Core core = corePosition == null ? null : cores.get(corePosition);
        if (core == null || !core.operational) return Optional.empty();
        return Optional.of(new Access(corePosition, core.owner, core.altarLevel, core.resourceLimit));
    }

    public Optional<Access> accessForAnyCore(GlobalPos corePosition) {
        Core core = cores.get(corePosition);
        if (core == null || !core.operational) return Optional.empty();
        return Optional.of(new Access(corePosition, core.owner, core.altarLevel, core.resourceLimit));
    }

    public static boolean withinPerCraftLimit(int limit, Collection<SoulStack> requested) {
        if (limit <= 0) return false;
        long anima = 0;
        long spiritus = 0;
        for (SoulStack stack : requested) {
            if (stack == null || stack.amount() <= 0) continue;
            if (stack.type() == SoulType.Anima) anima += stack.amount();
            else if (stack.type() == SoulType.Spiritus) spiritus += stack.amount();
            else return false;
            if (anima > limit || spiritus > limit) return false;
        }
        return true;
    }

    public static int altarLevelForCasingCounts(int hv, int ev, int iv) {
        if (hv == 28 && ev == 0 && iv == 0) return 4;
        if (ev == 28 && hv == 0 && iv == 0) return 5;
        if (iv == 28 && hv == 0 && ev == 0) return 6;
        return 0;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag root, @NotNull HolderLookup.Provider provider) {
        ListTag coreTags = new ListTag();
        for (var entry : cores.entrySet()) {
            CompoundTag tag = new CompoundTag();
            putPosition(tag, "position", entry.getKey());
            tag.putUUID("owner", entry.getValue().owner);
            tag.putInt("altarLevel", entry.getValue().altarLevel);
            tag.putInt("resourceLimit", entry.getValue().resourceLimit);
            coreTags.add(tag);
        }
        root.put("cores", coreTags);
        ListTag linkTags = new ListTag();
        for (var entry : moduleCores.entrySet()) {
            if (!cores.containsKey(entry.getValue())) continue;
            CompoundTag tag = new CompoundTag();
            putPosition(tag, "module", entry.getKey());
            putPosition(tag, "core", entry.getValue());
            linkTags.add(tag);
        }
        root.put("links", linkTags);
        return root;
    }

    static VitaeCampusSavedData load(CompoundTag root, HolderLookup.Provider provider) {
        VitaeCampusSavedData data = new VitaeCampusSavedData();
        ListTag coreTags = root.getList("cores", Tag.TAG_COMPOUND);
        for (int index = 0; index < coreTags.size(); index++) {
            CompoundTag tag = coreTags.getCompound(index);
            GlobalPos position = getPosition(tag, "position");
            if (position == null || !tag.hasUUID("owner")) continue;
            data.cores.put(position, new Core(tag.getUUID("owner"),
                    Math.clamp(tag.getInt("altarLevel"), 4, 6), Math.max(0, tag.getInt("resourceLimit"))));
        }
        ListTag linkTags = root.getList("links", Tag.TAG_COMPOUND);
        for (int index = 0; index < linkTags.size(); index++) {
            CompoundTag tag = linkTags.getCompound(index);
            GlobalPos module = getPosition(tag, "module");
            GlobalPos core = getPosition(tag, "core");
            if (module == null || core == null || !data.cores.containsKey(core) ||
                    !module.dimension().equals(core.dimension()))
                continue;
            data.moduleCores.putIfAbsent(module, core);
        }
        return data;
    }

    private static void putPosition(CompoundTag tag, String key, GlobalPos position) {
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, position).result().ifPresent(value -> tag.put(key, value));
    }

    private static @Nullable GlobalPos getPosition(CompoundTag tag, String key) {
        if (!tag.contains(key)) return null;
        return GlobalPos.CODEC.decode(NbtOps.INSTANCE, tag.get(key)).result()
                .map(result -> result.getFirst()).orElse(null);
    }

    public enum CoreResult {
        REGISTERED,
        UPDATED,
        OWNER_MISMATCH
    }

    public enum LinkResult {
        LINKED,
        UPDATED,
        RELINKED,
        CORE_MISSING,
        OWNER_MISMATCH,
        DIFFERENT_DIMENSION,
        SELF_LINK,
        OUT_OF_RANGE
    }

    public record Access(GlobalPos core, UUID owner, int altarLevel, int resourceLimit) {}

    private static final class Core {

        private final UUID owner;
        private int altarLevel;
        private int resourceLimit;
        private boolean operational;

        private Core(UUID owner, int altarLevel, int resourceLimit) {
            this.owner = owner;
            this.altarLevel = altarLevel;
            this.resourceLimit = resourceLimit;
        }
    }
}
