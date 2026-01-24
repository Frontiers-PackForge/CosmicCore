package com.ghostipedia.cosmiccore.api.data.savedData;

import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock.LinkRole;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

import java.util.Objects;

/**
 * Represents one end of a link from this machine's perspective.
 * Stores the target position and this machine's EFFECTIVE role
 * (after negotiation, not declared role).
 */
public final class LinkEntry {

    private static final String TAG_TARGET = "Target";
    private static final String TAG_ROLE = "Role";

    private final GlobalPos target;
    private final LinkRole effectiveRole;

    public LinkEntry(GlobalPos target, LinkRole effectiveRole) {
        this.target = target;
        this.effectiveRole = effectiveRole;
    }

    public GlobalPos target() {
        return target;
    }

    public LinkRole effectiveRole() {
        return effectiveRole;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, target)
                .result()
                .ifPresent(encoded -> tag.put(TAG_TARGET, encoded));
        tag.putString(TAG_ROLE, effectiveRole.name());
        return tag;
    }

    public static LinkEntry load(CompoundTag tag) {
        GlobalPos target = GlobalPos.CODEC
                .decode(NbtOps.INSTANCE, tag.get(TAG_TARGET))
                .result()
                .map(pair -> pair.getFirst())
                .orElseThrow(() -> new IllegalStateException("Invalid LinkEntry: missing target"));

        LinkRole role = LinkRole.valueOf(tag.getString(TAG_ROLE));
        return new LinkEntry(target, role);
    }

    /**
     * Equality is based ONLY on target, not role.
     * This ensures only one link per target in a Set, and re-linking
     * with a different role will replace the existing entry.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkEntry linkEntry = (LinkEntry) o;
        return Objects.equals(target, linkEntry.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target);
    }

    @Override
    public String toString() {
        return "LinkEntry{target=" + target + ", role=" + effectiveRole + "}";
    }
}
