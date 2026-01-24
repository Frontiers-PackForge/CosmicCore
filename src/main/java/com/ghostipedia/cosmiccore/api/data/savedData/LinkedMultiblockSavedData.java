package com.ghostipedia.cosmiccore.api.data.savedData;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock.LinkRole;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Persists multiblock link relationships across server restarts.
 * Links are keyed by team UUID for access control.
 * <p>
 * Each machine stores its own perspective with EFFECTIVE role
 * (result of negotiation, not declared role).
 */
public class LinkedMultiblockSavedData extends SavedData {

    private static final String DATA_NAME = "cosmiccore_linked_multiblocks";

    // Owner UUID -> (Machine GlobalPos -> Set of LinkEntry)
    private final Map<UUID, Map<GlobalPos, Set<LinkEntry>>> links = new HashMap<>();

    public LinkedMultiblockSavedData() {}

    public LinkedMultiblockSavedData(CompoundTag tag) {
        load(tag);
    }

    // ==================== Access ====================

    /**
     * Get or create the SavedData instance.
     * Stored in overworld to ensure single source of truth.
     */
    public static LinkedMultiblockSavedData getOrCreate(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                LinkedMultiblockSavedData::new,
                LinkedMultiblockSavedData::new,
                DATA_NAME);
    }

    public static LinkedMultiblockSavedData getOrCreate(ServerLevel level) {
        return getOrCreate(level.getServer());
    }

    // ==================== Link Management ====================

    /**
     * Establish a link between two machines with pre-negotiated roles.
     * <p>
     * If a link already exists between these machines, it will be replaced
     * with the new roles.
     * <p>
     * IMPORTANT: Roles should be the result of negotiateRoles(), not raw declared roles.
     *
     * @param owner          Team/player UUID (must match for both machines)
     * @param a              First machine's position
     * @param b              Second machine's position
     * @param aEffectiveRole A's effective role after negotiation
     * @param bEffectiveRole B's effective role after negotiation
     */
    public void link(UUID owner, GlobalPos a, GlobalPos b,
                     LinkRole aEffectiveRole, LinkRole bEffectiveRole) {
        // A's perspective - remove existing then add (to handle role changes)
        Set<LinkEntry> aLinks = links.computeIfAbsent(owner, k -> new HashMap<>())
                .computeIfAbsent(a, k -> new HashSet<>());
        aLinks.remove(new LinkEntry(b, null)); // equals only checks target
        aLinks.add(new LinkEntry(b, aEffectiveRole));

        // B's perspective
        Set<LinkEntry> bLinks = links.get(owner)
                .computeIfAbsent(b, k -> new HashSet<>());
        bLinks.remove(new LinkEntry(a, null)); // equals only checks target
        bLinks.add(new LinkEntry(a, bEffectiveRole));

        setDirty();
    }

    /**
     * Remove a specific link between two machines.
     * Removes both perspectives.
     */
    public void unlink(UUID owner, GlobalPos a, GlobalPos b) {
        Map<GlobalPos, Set<LinkEntry>> ownerLinks = links.get(owner);
        if (ownerLinks == null) return;

        // Remove A -> B
        Set<LinkEntry> aLinks = ownerLinks.get(a);
        if (aLinks != null) {
            aLinks.removeIf(entry -> entry.target().equals(b));
            if (aLinks.isEmpty()) {
                ownerLinks.remove(a);
            }
        }

        // Remove B -> A
        Set<LinkEntry> bLinks = ownerLinks.get(b);
        if (bLinks != null) {
            bLinks.removeIf(entry -> entry.target().equals(a));
            if (bLinks.isEmpty()) {
                ownerLinks.remove(b);
            }
        }

        // Cleanup empty owner entry
        if (ownerLinks.isEmpty()) {
            links.remove(owner);
        }

        setDirty();
    }

    /**
     * Remove all links for a machine (called when multiblock is destroyed).
     * Also removes reverse links from all partners.
     */
    public void removeAllLinks(UUID owner, GlobalPos pos) {
        Map<GlobalPos, Set<LinkEntry>> ownerLinks = links.get(owner);
        if (ownerLinks == null) return;

        // Get partners before removal
        Set<LinkEntry> myLinks = ownerLinks.remove(pos);

        // Remove reverse links from partners
        if (myLinks != null) {
            for (LinkEntry entry : myLinks) {
                Set<LinkEntry> partnerLinks = ownerLinks.get(entry.target());
                if (partnerLinks != null) {
                    partnerLinks.removeIf(e -> e.target().equals(pos));
                    if (partnerLinks.isEmpty()) {
                        ownerLinks.remove(entry.target());
                    }
                }
            }
        }

        if (ownerLinks.isEmpty()) {
            links.remove(owner);
        }

        setDirty();
    }

    // ==================== Queries ====================

    /**
     * Get all links for a machine.
     * Returns an unmodifiable copy to prevent external mutation.
     */
    public Set<LinkEntry> getLinks(UUID owner, GlobalPos pos) {
        Set<LinkEntry> result = links.getOrDefault(owner, Collections.emptyMap())
                .getOrDefault(pos, Collections.emptySet());
        return Collections.unmodifiableSet(new HashSet<>(result));
    }

    /**
     * Get just the partner positions (without role info).
     */
    public Set<GlobalPos> getPartnerPositions(UUID owner, GlobalPos pos) {
        Set<GlobalPos> result = new HashSet<>();
        for (LinkEntry entry : getLinks(owner, pos)) {
            result.add(entry.target());
        }
        return result;
    }

    /**
     * Get the specific link to a partner, if it exists.
     */
    @Nullable
    public LinkEntry getLinkTo(UUID owner, GlobalPos self, GlobalPos partner) {
        for (LinkEntry entry : getLinks(owner, self)) {
            if (entry.target().equals(partner)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Check if this machine can query the partner (based on effective role).
     * PEER and CONTROLLER can query; REMOTE cannot.
     */
    public boolean canQuery(UUID owner, GlobalPos self, GlobalPos partner) {
        LinkEntry link = getLinkTo(owner, self, partner);
        if (link == null) return false;

        return link.effectiveRole() == LinkRole.PEER || link.effectiveRole() == LinkRole.CONTROLLER;
    }

    /**
     * Check if this machine can be queried by the partner (based on effective role).
     * PEER and REMOTE can be queried; CONTROLLER cannot.
     */
    public boolean canBeQueriedBy(UUID owner, GlobalPos self, GlobalPos partner) {
        LinkEntry link = getLinkTo(owner, self, partner);
        if (link == null) return false;

        return link.effectiveRole() == LinkRole.PEER || link.effectiveRole() == LinkRole.REMOTE;
    }

    /**
     * Check if two machines are linked (regardless of role).
     */
    public boolean isLinked(UUID owner, GlobalPos a, GlobalPos b) {
        return getLinkTo(owner, a, b) != null;
    }

    // ==================== Serialization ====================

    @Override
    public CompoundTag save(CompoundTag root) {
        ListTag ownersList = new ListTag();

        for (Map.Entry<UUID, Map<GlobalPos, Set<LinkEntry>>> ownerEntry : links.entrySet()) {
            CompoundTag ownerTag = new CompoundTag();
            ownerTag.putUUID("Owner", ownerEntry.getKey());

            ListTag machinesList = new ListTag();
            for (Map.Entry<GlobalPos, Set<LinkEntry>> machineEntry : ownerEntry.getValue().entrySet()) {
                CompoundTag machineTag = new CompoundTag();

                GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, machineEntry.getKey())
                        .result()
                        .ifPresent(encoded -> machineTag.put("Pos", encoded));

                ListTag linksList = new ListTag();
                for (LinkEntry link : machineEntry.getValue()) {
                    linksList.add(link.save());
                }
                machineTag.put("Links", linksList);

                machinesList.add(machineTag);
            }
            ownerTag.put("Machines", machinesList);

            ownersList.add(ownerTag);
        }

        root.put("Owners", ownersList);
        return root;
    }

    private void load(CompoundTag root) {
        links.clear();

        ListTag ownersList = root.getList("Owners", Tag.TAG_COMPOUND);
        for (int i = 0; i < ownersList.size(); i++) {
            CompoundTag ownerTag = ownersList.getCompound(i);
            UUID owner = ownerTag.getUUID("Owner");

            Map<GlobalPos, Set<LinkEntry>> ownerLinks = new HashMap<>();

            ListTag machinesList = ownerTag.getList("Machines", Tag.TAG_COMPOUND);
            for (int j = 0; j < machinesList.size(); j++) {
                CompoundTag machineTag = machinesList.getCompound(j);

                GlobalPos pos = GlobalPos.CODEC
                        .decode(NbtOps.INSTANCE, machineTag.get("Pos"))
                        .result()
                        .map(pair -> pair.getFirst())
                        .orElse(null);

                if (pos == null) continue;

                Set<LinkEntry> machineLinks = new HashSet<>();
                ListTag linksList = machineTag.getList("Links", Tag.TAG_COMPOUND);
                for (int k = 0; k < linksList.size(); k++) {
                    try {
                        machineLinks.add(LinkEntry.load(linksList.getCompound(k)));
                    } catch (Exception e) {
                        CosmicCore.LOGGER.warn("Failed to load link entry: {}", e.getMessage());
                    }
                }

                if (!machineLinks.isEmpty()) {
                    ownerLinks.put(pos, machineLinks);
                }
            }

            if (!ownerLinks.isEmpty()) {
                links.put(owner, ownerLinks);
            }
        }
    }
}
