package com.ghostipedia.cosmiccore.api.capability;

import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;

import net.minecraft.core.GlobalPos;

import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Interface for multiblocks that support cross-dimensional linking.
 * Extends GTCEu's IDataStickInteractable for datastick-based linking.
 * <p>
 * SECURITY: Link validation MUST load and verify the partner machine.
 * Never trust datastick NBT for ownership or compatibility checks.
 */
public interface ILinkedMultiblock extends IDataStickInteractable {

    /**
     * Role this machine prefers in links it creates.
     * Actual role is determined by negotiation with partner.
     */
    enum LinkRole {
        /** Bidirectional - both machines can query each other */
        PEER,
        /** This machine controls partners - can query them, they cannot query us */
        CONTROLLER,
        /** This machine is controlled by partners - they can query us, we cannot query them */
        REMOTE
    }

    // ==================== Configuration ====================

    /**
     * Check if this machine can link to the given partner.
     * Called AFTER partner machine is loaded and ownership is verified.
     * Called AFTER role negotiation succeeds.
     * <p>
     * Use for type compatibility, distance limits, dimension restrictions, etc.
     * Ownership and role checks are handled by the linking system.
     *
     * @param partner        The partner's position
     * @param partnerMachine The actual loaded partner machine
     */
    boolean canLinkTo(GlobalPos partner, ILinkedMultiblock partnerMachine);

    /**
     * Get the role this machine prefers when linking.
     * Actual effective role is determined by negotiation.
     *
     * @see com.ghostipedia.cosmiccore.common.machine.multiblock.LinkedMultiblockHelper#negotiateRoles
     */
    LinkRole getLinkRole();

    /**
     * Maximum number of partners this machine can link to.
     * Default: 4
     */
    default int getMaxPartners() {
        return 4;
    }

    // ==================== Lifecycle ====================

    /**
     * Called when a link is successfully established.
     * May be called immediately (if partner loaded) or deferred (on this machine's load).
     *
     * @param partner The linked partner's position
     */
    void onLinkEstablished(GlobalPos partner);

    /**
     * Called when a link is broken.
     * Reasons: partner destroyed, manual unlink, ownership change, etc.
     *
     * @param partner The unlinked partner's position
     */
    void onLinkBroken(GlobalPos partner);

    /**
     * Called during machine load to process deferred link notifications.
     * Implementation should compare SavedData links vs known partners.
     */
    void processLinkNotifications();

    // ==================== Query ====================

    /**
     * Get all currently linked partners from SavedData.
     */
    Set<GlobalPos> getLinkedPartners();

    /**
     * Get this machine's GlobalPos for link registration.
     */
    GlobalPos getGlobalPos();

    /**
     * Get the owner UUID (team or player) for access control.
     * Should use FTB Teams integration via existing getTeamUUID() pattern.
     */
    @Nullable
    UUID getTeamUUID();
}
