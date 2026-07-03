package com.ghostipedia.cosmiccore.common.mirror.deed;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DeedLedger extends SavedData {

    private static final String DATA_NAME = "cosmiccore_deeds";

    public record WovenEcho(ResourceLocation deedId, UUID weaver, int claimIndex, long gameTime,
                            @Nullable GlobalPos position) {}

    public static final class TeamDeeds {

        final LinkedHashMap<ResourceLocation, WovenEcho> woven = new LinkedHashMap<>();
        final LinkedHashSet<ResourceLocation> pending = new LinkedHashSet<>();
    }

    private final Map<String, TeamDeeds> teams = new HashMap<>();

    public static DeedLedger get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(DeedLedger::new, DeedLedger::load), DATA_NAME);
    }

    public DeedLedger() {}

    private static DeedLedger load(CompoundTag tag, HolderLookup.Provider provider) {
        DeedLedger ledger = new DeedLedger();
        ListTag teamsTag = tag.getList("teams", Tag.TAG_COMPOUND);
        for (int i = 0; i < teamsTag.size(); i++) {
            CompoundTag teamTag = teamsTag.getCompound(i);
            TeamDeeds deeds = new TeamDeeds();
            ListTag wovenTag = teamTag.getList("woven", Tag.TAG_COMPOUND);
            for (int j = 0; j < wovenTag.size(); j++) {
                CompoundTag echoTag = wovenTag.getCompound(j);
                ResourceLocation id = ResourceLocation.parse(echoTag.getString("id"));
                GlobalPos pos = null;
                if (echoTag.contains("dim")) {
                    pos = GlobalPos.of(
                            ResourceKey.create(Registries.DIMENSION,
                                    ResourceLocation.parse(echoTag.getString("dim"))),
                            BlockPos.of(echoTag.getLong("pos")));
                }
                deeds.woven.put(id, new WovenEcho(id, echoTag.getUUID("weaver"), echoTag.getInt("index"),
                        echoTag.getLong("time"), pos));
            }
            ListTag pendingTag = teamTag.getList("pending", Tag.TAG_STRING);
            for (int j = 0; j < pendingTag.size(); j++) {
                deeds.pending.add(ResourceLocation.parse(pendingTag.getString(j)));
            }
            ledger.teams.put(teamTag.getString("key"), deeds);
        }
        return ledger;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        ListTag teamsTag = new ListTag();
        for (Map.Entry<String, TeamDeeds> entry : teams.entrySet()) {
            CompoundTag teamTag = new CompoundTag();
            teamTag.putString("key", entry.getKey());
            ListTag wovenTag = new ListTag();
            for (WovenEcho echo : entry.getValue().woven.values()) {
                CompoundTag echoTag = new CompoundTag();
                echoTag.putString("id", echo.deedId().toString());
                echoTag.putUUID("weaver", echo.weaver());
                echoTag.putInt("index", echo.claimIndex());
                echoTag.putLong("time", echo.gameTime());
                if (echo.position() != null) {
                    echoTag.putString("dim", echo.position().dimension().location().toString());
                    echoTag.putLong("pos", echo.position().pos().asLong());
                }
                wovenTag.add(echoTag);
            }
            teamTag.put("woven", wovenTag);
            ListTag pendingTag = new ListTag();
            for (ResourceLocation id : entry.getValue().pending) {
                pendingTag.add(StringTag.valueOf(id.toString()));
            }
            teamTag.put("pending", pendingTag);
            teamsTag.add(teamTag);
        }
        tag.put("teams", teamsTag);
        return tag;
    }

    private TeamDeeds team(String key) {
        return teams.computeIfAbsent(key, k -> new TeamDeeds());
    }

    public boolean grantCoil(String teamKey, ResourceLocation deedId) {
        TeamDeeds deeds = team(teamKey);
        if (deeds.woven.containsKey(deedId)) return false;
        boolean added = deeds.pending.add(deedId);
        if (added) setDirty();
        return added;
    }

    @Nullable
    public WovenEcho weave(String teamKey, ResourceLocation deedId, UUID weaver, long gameTime,
                           @Nullable GlobalPos position) {
        TeamDeeds deeds = team(teamKey);
        if (deeds.woven.containsKey(deedId)) return null;
        deeds.pending.remove(deedId);
        WovenEcho echo = new WovenEcho(deedId, weaver, deeds.woven.size(), gameTime, position);
        deeds.woven.put(deedId, echo);
        setDirty();
        return echo;
    }

    public boolean isWoven(String teamKey, ResourceLocation deedId) {
        TeamDeeds deeds = teams.get(teamKey);
        return deeds != null && deeds.woven.containsKey(deedId);
    }

    public List<WovenEcho> wovenOf(String teamKey) {
        TeamDeeds deeds = teams.get(teamKey);
        return deeds == null ? List.of() : new ArrayList<>(deeds.woven.values());
    }

    public Set<ResourceLocation> pendingOf(String teamKey) {
        TeamDeeds deeds = teams.get(teamKey);
        return deeds == null ? Set.of() : new LinkedHashSet<>(deeds.pending);
    }

    public void reset(String teamKey) {
        if (teams.remove(teamKey) != null) {
            setDirty();
        }
    }
}
