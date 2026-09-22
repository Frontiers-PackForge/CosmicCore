package com.ghostipedia.cosmiccore.common.machine.foundry;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class FoundryCampusSavedData extends SavedData {

    private static final String NAME = "cosmiccore_hephaestus_campuses";
    private static final Comparator<Member> MEMBER_ORDER = Comparator.comparingLong(Member::ordinal)
            .thenComparing(member -> member.position().dimension().location().toString())
            .thenComparingLong(member -> member.position().pos().asLong());
    private final Map<GlobalPos, Campus> campuses = new HashMap<>();
    private final Map<GlobalPos, GlobalPos> furnaceCores = new HashMap<>();
    private final Map<GlobalPos, Boolean> operationalCores = new HashMap<>();

    public static FoundryCampusSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(FoundryCampusSavedData::new, FoundryCampusSavedData::load), NAME);
    }

    public CoreResult registerCore(UUID owner, GlobalPos core, FoundryTier tier, FoundryRenderAnchor sourceAnchor) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(core);
        Objects.requireNonNull(tier);
        Objects.requireNonNull(sourceAnchor);
        Campus current = campuses.get(core);
        if (current == null) {
            campuses.put(core, new Campus(owner, tier, sourceAnchor));
            setDirty();
            return CoreResult.REGISTERED;
        } else {
            if (!current.owner.equals(owner)) return CoreResult.OWNER_MISMATCH;
            current.tier = tier;
            current.sourceAnchor = sourceAnchor;
            current.pyroflux = Math.min(current.pyroflux, FoundryPyrofluxPolicy.capacity(tier));
        }
        setDirty();
        return CoreResult.UPDATED;
    }

    public LinkResult link(UUID owner, GlobalPos core, GlobalPos furnace, ResourceLocation furnaceType,
                           FoundryRenderAnchor receivingAnchor) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(core);
        Objects.requireNonNull(furnace);
        Objects.requireNonNull(furnaceType);
        Objects.requireNonNull(receivingAnchor);
        Campus campus = campuses.get(core);
        if (campus == null) return LinkResult.CORE_MISSING;
        if (!campus.owner.equals(owner)) return LinkResult.OWNER_MISMATCH;
        if (!core.dimension().equals(furnace.dimension())) return LinkResult.DIFFERENT_DIMENSION;
        if (core.equals(furnace)) return LinkResult.SELF_LINK;
        if (core.pos().distSqr(furnace.pos()) > FoundryPyrofluxPolicy.LINK_RANGE *
                FoundryPyrofluxPolicy.LINK_RANGE)
            return LinkResult.OUT_OF_RANGE;
        GlobalPos existingCore = furnaceCores.get(furnace);
        if (existingCore != null && !existingCore.equals(core)) return LinkResult.ALREADY_LINKED;
        Member existing = campus.members.get(furnace);
        if (existing != null) {
            campus.members.put(furnace,
                    new Member(furnace, furnaceType, receivingAnchor, existing.ordinal));
            setDirty();
            return LinkResult.UPDATED;
        }
        if (campus.members.size() >= campus.tier.capacity()) return LinkResult.CAPACITY_REACHED;
        campus.members.put(furnace,
                new Member(furnace, furnaceType, receivingAnchor, campus.nextOrdinal++));
        furnaceCores.put(furnace, core);
        setDirty();
        return LinkResult.LINKED;
    }

    public boolean unlink(GlobalPos core, GlobalPos furnace) {
        Campus campus = campuses.get(core);
        if (campus == null || campus.members.remove(furnace) == null) return false;
        releaseReservation(campus, furnace);
        furnaceCores.remove(furnace, core);
        setDirty();
        return true;
    }

    public boolean removeCore(GlobalPos core) {
        Campus removed = campuses.remove(core);
        if (removed == null) return false;
        operationalCores.remove(core);
        removed.members.keySet().forEach(furnace -> furnaceCores.remove(furnace, core));
        setDirty();
        return true;
    }

    public Optional<GlobalPos> coreFor(GlobalPos furnace) {
        return Optional.ofNullable(furnaceCores.get(furnace));
    }

    public List<GlobalPos> coresIn(net.minecraft.resources.ResourceKey<Level> dimension, ChunkPos chunk) {
        return campuses.keySet().stream()
                .filter(core -> core.dimension().equals(dimension) && new ChunkPos(core.pos()).equals(chunk))
                .sorted(FoundryCampusSavedData::comparePositions)
                .toList();
    }

    public boolean isActive(GlobalPos core, GlobalPos furnace) {
        return memberships(core).stream().anyMatch(member -> member.position.equals(furnace) && member.active);
    }

    public boolean isActive(GlobalPos furnace) {
        GlobalPos core = furnaceCores.get(furnace);
        return core != null && isActive(core, furnace);
    }

    public boolean canOperate(GlobalPos furnace, FoundryFurnaceEndpoint endpoint) {
        GlobalPos core = furnaceCores.get(furnace);
        if (core == null || endpoint == null || !endpoint.isFoundryStructureFormed() || !isActive(core, furnace)) {
            return false;
        }
        Campus campus = campuses.get(core);
        Member member = campus == null ? null : campus.members.get(furnace);
        return member != null && operationalCores.getOrDefault(core, false) &&
                campus.owner.equals(endpoint.foundryOwner()) &&
                member.type.equals(endpoint.foundryFurnaceType());
    }

    public void setCoreOperational(GlobalPos core, boolean operational, long inputVoltage) {
        Campus campus = campuses.get(core);
        if (campus == null) return;
        Boolean previousState = operationalCores.put(core, operational);
        boolean stateChanged = previousState == null || previousState != operational;
        long boundedVoltage = Math.max(0, inputVoltage);
        boolean voltageChanged = campus.inputVoltage != boundedVoltage;
        campus.inputVoltage = boundedVoltage;
        if (stateChanged || voltageChanged) setDirty();
    }

    public long inputVoltage(GlobalPos core) {
        Campus campus = campuses.get(core);
        return campus == null ? 0 : campus.inputVoltage;
    }

    public long inputVoltageFor(GlobalPos furnace) {
        GlobalPos core = furnaceCores.get(furnace);
        return core == null ? 0 : inputVoltage(core);
    }

    public boolean coreOperationalFor(GlobalPos furnace) {
        GlobalPos core = furnaceCores.get(furnace);
        return core != null && operationalCores.getOrDefault(core, false);
    }

    public long storedPyroflux(GlobalPos core) {
        Campus campus = campuses.get(core);
        return campus == null ? 0 : campus.pyroflux;
    }

    public long storedPyrofluxFor(GlobalPos furnace) {
        GlobalPos core = furnaceCores.get(furnace);
        return core == null ? 0 : storedPyroflux(core);
    }

    public long pyrofluxCapacity(GlobalPos core) {
        Campus campus = campuses.get(core);
        return campus == null ? 0 : FoundryPyrofluxPolicy.capacity(campus.tier);
    }

    public long addPyroflux(GlobalPos core, long amount) {
        Campus campus = campuses.get(core);
        if (campus == null || amount <= 0) return 0;
        long capacity = FoundryPyrofluxPolicy.capacity(campus.tier);
        long accepted = Math.min(amount, Math.max(0, capacity - campus.pyroflux));
        if (accepted > 0) {
            campus.pyroflux += accepted;
            setDirty();
        }
        return accepted;
    }

    public boolean canReservePyroflux(GlobalPos furnace, long demand) {
        GlobalPos core = furnaceCores.get(furnace);
        Campus campus = core == null ? null : campuses.get(core);
        if (campus == null || demand < 0 || !operationalCores.getOrDefault(core, false) ||
                !isActive(core, furnace))
            return false;
        Reservation existing = campus.reservations.get(furnace);
        return existing != null ? existing.total == demand : campus.pyroflux >= demand;
    }

    public boolean reservePyroflux(GlobalPos furnace, long demand) {
        GlobalPos core = furnaceCores.get(furnace);
        Campus campus = core == null ? null : campuses.get(core);
        if (campus == null || !canReservePyroflux(furnace, demand)) return false;
        Reservation existing = campus.reservations.get(furnace);
        if (existing != null) return existing.total == demand;
        campus.pyroflux -= demand;
        campus.reservations.put(furnace, new Reservation(demand, demand));
        setDirty();
        return true;
    }

    public boolean restorePyrofluxReservation(GlobalPos furnace, long totalDemand, int progress, int duration) {
        GlobalPos core = furnaceCores.get(furnace);
        Campus campus = core == null ? null : campuses.get(core);
        if (campus == null || !operationalCores.getOrDefault(core, false) || !isActive(core, furnace)) return false;
        Reservation existing = campus.reservations.get(furnace);
        if (existing != null) return existing.total == totalDemand;
        long remaining = Math.max(0,
                totalDemand - FoundryPyrofluxPolicy.consumedAtProgress(totalDemand, progress, duration));
        if (campus.pyroflux < remaining) return false;
        campus.pyroflux -= remaining;
        campus.reservations.put(furnace, new Reservation(totalDemand, remaining));
        setDirty();
        return true;
    }

    public boolean consumePyrofluxForProgress(GlobalPos furnace, int progress, int duration) {
        GlobalPos core = furnaceCores.get(furnace);
        Campus campus = core == null ? null : campuses.get(core);
        Reservation reservation = campus == null ? null : campus.reservations.get(furnace);
        if (reservation == null || !operationalCores.getOrDefault(core, false) || !isActive(core, furnace)) {
            return false;
        }
        long charge = FoundryPyrofluxPolicy.chargeForNextTick(reservation.total, progress, duration);
        if (reservation.remaining < charge) return false;
        campus.reservations.put(furnace, new Reservation(reservation.total, reservation.remaining - charge));
        setDirty();
        return true;
    }

    public boolean hasReservation(GlobalPos furnace) {
        GlobalPos core = furnaceCores.get(furnace);
        Campus campus = core == null ? null : campuses.get(core);
        return campus != null && campus.reservations.containsKey(furnace);
    }

    public void completeReservation(GlobalPos furnace) {
        GlobalPos core = furnaceCores.get(furnace);
        Campus campus = core == null ? null : campuses.get(core);
        if (campus != null && campus.reservations.remove(furnace) != null) setDirty();
    }

    public void releaseReservation(GlobalPos furnace) {
        GlobalPos core = furnaceCores.get(furnace);
        Campus campus = core == null ? null : campuses.get(core);
        if (campus != null && releaseReservation(campus, furnace)) setDirty();
    }

    public long allocatedPyroflux(GlobalPos core) {
        Campus campus = campuses.get(core);
        return campus == null ? 0 : campus.reservations.values().stream().mapToLong(Reservation::remaining).sum();
    }

    public long demandedPyroflux(GlobalPos core) {
        Campus campus = campuses.get(core);
        return campus == null ? 0 : campus.reservations.values().stream().mapToLong(Reservation::total).sum();
    }

    public List<Membership> memberships(GlobalPos core) {
        Campus campus = campuses.get(core);
        if (campus == null) return List.of();
        List<Member> ordered = campus.members.values().stream().sorted(MEMBER_ORDER).toList();
        List<Membership> result = new ArrayList<>(ordered.size());
        for (int index = 0; index < ordered.size(); index++) {
            Member member = ordered.get(index);
            result.add(new Membership(member.position, member.type, member.receivingAnchor, member.ordinal,
                    index < campus.tier.capacity()));
        }
        return List.copyOf(result);
    }

    public Optional<FoundryCampusSnapshot> snapshot(
                                                    GlobalPos core,
                                                    Function<GlobalPos, @Nullable FoundryFurnaceEndpoint> resolver) {
        Campus campus = campuses.get(core);
        if (campus == null) return Optional.empty();
        List<FoundryCampusSnapshot.Furnace> furnaces = new ArrayList<>();
        for (Membership membership : memberships(core)) {
            FoundryFurnaceEndpoint endpoint = resolver.apply(membership.position);
            boolean valid = endpoint != null && endpoint.isFoundryStructureFormed() &&
                    campus.owner.equals(endpoint.foundryOwner()) &&
                    membership.type.equals(endpoint.foundryFurnaceType());
            FoundryRenderAnchor anchor = valid ? endpoint.foundryReceivingAnchor() : membership.receivingAnchor;
            FoundryCampusSnapshot.Activity activity = !valid ? FoundryCampusSnapshot.Activity.UNAVAILABLE :
                    membership.active && endpoint.isFoundryWorking() ? FoundryCampusSnapshot.Activity.WORKING :
                            FoundryCampusSnapshot.Activity.IDLE;
            furnaces.add(new FoundryCampusSnapshot.Furnace(
                    membership.position,
                    membership.type,
                    anchor,
                    membership.active ? FoundryCampusSnapshot.Selection.ACTIVE :
                            FoundryCampusSnapshot.Selection.DORMANT,
                    activity));
        }
        return Optional.of(new FoundryCampusSnapshot(core, campus.tier, campus.sourceAnchor, furnaces));
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag root, @NotNull HolderLookup.Provider provider) {
        ListTag campusTags = new ListTag();
        campuses.entrySet().stream().sorted(Map.Entry.comparingByKey(FoundryCampusSavedData::comparePositions))
                .forEach(entry -> {
                    CompoundTag tag = new CompoundTag();
                    putPosition(tag, "core", entry.getKey());
                    Campus campus = entry.getValue();
                    tag.putUUID("owner", campus.owner);
                    tag.putInt("tier", campus.tier.level());
                    tag.put("sourceAnchor", campus.sourceAnchor.save());
                    tag.putLong("nextOrdinal", campus.nextOrdinal);
                    tag.putLong("pyroflux", campus.pyroflux);
                    tag.putLong("inputVoltage", campus.inputVoltage);
                    ListTag memberTags = new ListTag();
                    campus.members.values().stream().sorted(MEMBER_ORDER).forEach(member -> {
                        CompoundTag memberTag = new CompoundTag();
                        putPosition(memberTag, "position", member.position);
                        memberTag.putString("type", member.type.toString());
                        memberTag.put("receivingAnchor", member.receivingAnchor.save());
                        memberTag.putLong("ordinal", member.ordinal);
                        memberTags.add(memberTag);
                    });
                    tag.put("members", memberTags);
                    ListTag reservationTags = new ListTag();
                    campus.reservations.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey(FoundryCampusSavedData::comparePositions))
                            .forEach(reservationEntry -> {
                                CompoundTag reservationTag = new CompoundTag();
                                putPosition(reservationTag, "furnace", reservationEntry.getKey());
                                reservationTag.putLong("total", reservationEntry.getValue().total);
                                reservationTag.putLong("remaining", reservationEntry.getValue().remaining);
                                reservationTags.add(reservationTag);
                            });
                    tag.put("reservations", reservationTags);
                    campusTags.add(tag);
                });
        root.put("campuses", campusTags);
        return root;
    }

    static FoundryCampusSavedData load(CompoundTag root, HolderLookup.Provider provider) {
        FoundryCampusSavedData data = new FoundryCampusSavedData();
        ListTag campusTags = root.getList("campuses", Tag.TAG_COMPOUND);
        for (int i = 0; i < campusTags.size(); i++) {
            CompoundTag tag = campusTags.getCompound(i);
            GlobalPos core = getPosition(tag, "core");
            if (core == null || !tag.hasUUID("owner") || data.campuses.containsKey(core)) continue;
            Campus campus = new Campus(tag.getUUID("owner"), FoundryTier.fromLevel(tag.getInt("tier")),
                    FoundryRenderAnchor.load(tag.getCompound("sourceAnchor")));
            ListTag memberTags = tag.getList("members", Tag.TAG_COMPOUND);
            for (int memberIndex = 0; memberIndex < memberTags.size(); memberIndex++) {
                CompoundTag memberTag = memberTags.getCompound(memberIndex);
                GlobalPos position = getPosition(memberTag, "position");
                ResourceLocation type = ResourceLocation.tryParse(memberTag.getString("type"));
                if (position == null || type == null || !core.dimension().equals(position.dimension()) ||
                        data.furnaceCores.containsKey(position))
                    continue;
                long ordinal = Math.max(0, memberTag.getLong("ordinal"));
                Member member = new Member(position, type,
                        FoundryRenderAnchor.load(memberTag.getCompound("receivingAnchor")), ordinal);
                campus.members.put(position, member);
                data.furnaceCores.put(position, core);
                campus.nextOrdinal = Math.max(campus.nextOrdinal, ordinal + 1);
            }
            campus.nextOrdinal = Math.max(campus.nextOrdinal, Math.max(0, tag.getLong("nextOrdinal")));
            campus.pyroflux = Math.clamp(tag.getLong("pyroflux"), 0,
                    FoundryPyrofluxPolicy.capacity(campus.tier));
            campus.inputVoltage = Math.max(0, tag.getLong("inputVoltage"));
            ListTag reservationTags = tag.getList("reservations", Tag.TAG_COMPOUND);
            for (int reservationIndex = 0; reservationIndex < reservationTags.size(); reservationIndex++) {
                CompoundTag reservationTag = reservationTags.getCompound(reservationIndex);
                GlobalPos furnace = getPosition(reservationTag, "furnace");
                if (furnace == null || !campus.members.containsKey(furnace)) continue;
                long total = Math.max(0, reservationTag.getLong("total"));
                long remaining = Math.clamp(reservationTag.getLong("remaining"), 0, total);
                campus.reservations.put(furnace, new Reservation(total, remaining));
            }
            data.campuses.put(core, campus);
        }
        return data;
    }

    private static void putPosition(CompoundTag tag, String key, GlobalPos position) {
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, position).result().ifPresent(encoded -> tag.put(key, encoded));
    }

    @Nullable
    private static GlobalPos getPosition(CompoundTag tag, String key) {
        if (!tag.contains(key)) return null;
        return GlobalPos.CODEC.decode(NbtOps.INSTANCE, tag.get(key)).result().map(result -> result.getFirst())
                .orElse(null);
    }

    private static int comparePositions(GlobalPos left, GlobalPos right) {
        int dimension = left.dimension().location().toString().compareTo(right.dimension().location().toString());
        return dimension != 0 ? dimension : Long.compare(left.pos().asLong(), right.pos().asLong());
    }

    public enum LinkResult {
        LINKED,
        UPDATED,
        CORE_MISSING,
        OWNER_MISMATCH,
        DIFFERENT_DIMENSION,
        SELF_LINK,
        ALREADY_LINKED,
        CAPACITY_REACHED,
        OUT_OF_RANGE
    }

    public enum CoreResult {
        REGISTERED,
        UPDATED,
        OWNER_MISMATCH
    }

    public record Membership(GlobalPos position, ResourceLocation type, FoundryRenderAnchor receivingAnchor,
                             long ordinal, boolean active) {}

    private static final class Campus {

        private UUID owner;
        private FoundryTier tier;
        private FoundryRenderAnchor sourceAnchor;
        private long nextOrdinal;
        private long pyroflux;
        private long inputVoltage;
        private final Map<GlobalPos, Member> members = new HashMap<>();
        private final Map<GlobalPos, Reservation> reservations = new HashMap<>();

        private Campus(UUID owner, FoundryTier tier, FoundryRenderAnchor sourceAnchor) {
            this.owner = owner;
            this.tier = tier;
            this.sourceAnchor = sourceAnchor;
        }
    }

    private record Member(GlobalPos position, ResourceLocation type, FoundryRenderAnchor receivingAnchor,
                          long ordinal) {}

    private static boolean releaseReservation(Campus campus, GlobalPos furnace) {
        Reservation reservation = campus.reservations.remove(furnace);
        if (reservation == null) return false;
        long capacity = FoundryPyrofluxPolicy.capacity(campus.tier);
        campus.pyroflux = Math.min(capacity, campus.pyroflux + reservation.remaining);
        return true;
    }

    private record Reservation(long total, long remaining) {}
}
