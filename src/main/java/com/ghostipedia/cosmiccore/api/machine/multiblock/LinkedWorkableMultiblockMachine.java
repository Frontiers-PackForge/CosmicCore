package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock;
import com.ghostipedia.cosmiccore.api.data.savedData.LinkEntry;
import com.ghostipedia.cosmiccore.api.data.savedData.LinkedMultiblockSavedData;
import com.ghostipedia.cosmiccore.common.machine.multiblock.LinkedMultiblockHelper;
import com.ghostipedia.cosmiccore.common.machine.multiblock.LinkedMultiblockHelper.RolePair;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Base class for non-electric multiblocks that support cross-dimensional linking.
 * <p>
 * For electric multiblocks, use {@link LinkedWorkableElectricMultiblockMachine} instead.
 * <p>
 * Subclasses should override:
 * <ul>
 * <li>{@link #canLinkTo(GlobalPos, ILinkedMultiblock)} - Type compatibility checks</li>
 * <li>{@link #getLinkRole()} - Define role preference (PEER, CONTROLLER, REMOTE)</li>
 * <li>{@link #getMaxPartners()} - Override if more/fewer than 4 partners needed</li>
 * <li>{@link #onLinkEstablished(GlobalPos)} - React to new links</li>
 * <li>{@link #onLinkBroken(GlobalPos)} - Cleanup when links break</li>
 * </ul>
 */
public abstract class LinkedWorkableMultiblockMachine extends WorkableMultiblockMachine
                                                      implements ILinkedMultiblock, IDisplayUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            LinkedWorkableMultiblockMachine.class,
            WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final String DATASTICK_TAG_KEY = "cosmiccore:link_data";
    private static final String TAG_POS = "Pos";
    private static final String TAG_OWNER = "Owner";

    /**
     * Local cache of known partners, rebuilt from SavedData on structure form.
     * Used to detect changes for lifecycle callbacks.
     * NOT persisted - rebuilt from SavedData which is the source of truth.
     */
    protected Set<GlobalPos> knownPartners = new HashSet<>();

    public LinkedWorkableMultiblockMachine(BlockEntityCreationInfo holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // ==================== ILinkedMultiblock Implementation ====================

    @Override
    public GlobalPos getGlobalPos() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            return GlobalPos.of(serverLevel.dimension(), getBlockPos());
        }
        return null;
    }

    @Override
    @Nullable
    public UUID getTeamUUID() {
        var team = ((FTBOwner) getOwner()).getPlayerTeam(getOwnerUUID());
        return team != null ? team.getTeamId() : getOwnerUUID();
    }

    @Override
    public Set<GlobalPos> getLinkedPartners() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return Collections.emptySet();
        }

        UUID owner = getTeamUUID();
        if (owner == null) return Collections.emptySet();

        LinkedMultiblockSavedData savedData = LinkedMultiblockSavedData.getOrCreate(serverLevel);
        return savedData.getPartnerPositions(owner, getGlobalPos());
    }

    @Override
    public void processLinkNotifications() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;

        UUID owner = getTeamUUID();
        if (owner == null) return;

        Set<GlobalPos> currentPartners = getLinkedPartners();

        // Find new partners (in SavedData but not in our cache)
        for (GlobalPos partner : currentPartners) {
            if (!knownPartners.contains(partner)) {
                onLinkEstablished(partner);
            }
        }

        // Find removed partners (in our cache but not in SavedData)
        Set<GlobalPos> removed = new HashSet<>(knownPartners);
        removed.removeAll(currentPartners);
        for (GlobalPos partner : removed) {
            onLinkBroken(partner);
        }

        // Update cache
        knownPartners = new HashSet<>(currentPartners);
    }

    // ==================== Lifecycle ====================

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // Process any pending link notifications from when we were unloaded
        processLinkNotifications();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        // Don't remove links when structure breaks - links persist to SavedData
        // They'll be cleaned up when the machine is actually destroyed
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();

        if (getLevel() instanceof ServerLevel serverLevel) {
            UUID owner = getTeamUUID();
            GlobalPos myPos = getGlobalPos();

            if (owner != null && myPos != null) {
                // Release any force-loaded chunks
                LinkedMultiblockHelper.releaseAllTickets(serverLevel.getServer(), myPos);

                // Remove all links from SavedData
                LinkedMultiblockSavedData savedData = LinkedMultiblockSavedData.getOrCreate(serverLevel);
                savedData.removeAllLinks(owner, myPos);

                // Notify partners (if loaded)
                for (GlobalPos partner : knownPartners) {
                    ILinkedMultiblock partnerMachine = LinkedMultiblockHelper.getLinkedMachine(
                            serverLevel.getServer(), partner);
                    if (partnerMachine != null) {
                        partnerMachine.onLinkBroken(myPos);
                    }
                }
            }
        }
    }

    // ==================== Datastick Handling ====================

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (isRemote()) {
            return InteractionResult.SUCCESS;
        }

        GlobalPos myPos = getGlobalPos();
        UUID owner = getTeamUUID();

        if (myPos == null || owner == null) {
            player.sendSystemMessage(Component.translatable("cosmiccore.link.not_ready")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        // Write link data to datastick
        CompoundTag linkData = new CompoundTag();
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, myPos)
                .result()
                .ifPresent(encoded -> linkData.put(TAG_POS, encoded));
        linkData.putUUID(TAG_OWNER, owner);

        // Store in namespaced tag to preserve other datastick data
        CompoundTag rootTag = dataStick.getOrCreateTag();
        rootTag.put(DATASTICK_TAG_KEY, linkData);

        // Update datastick name
        String machineName = getDefinition().getName();
        dataStick.setHoverName(Component.translatable("cosmiccore.datastick.link_copied", machineName));

        // Feedback
        player.sendSystemMessage(Component.translatable("cosmiccore.link.copied", machineName)
                .withStyle(ChatFormatting.GREEN));

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        if (isRemote()) {
            return InteractionResult.sidedSuccess(true);
        }

        CompoundTag rootTag = dataStick.getTag();
        if (rootTag == null || !rootTag.contains(DATASTICK_TAG_KEY)) {
            return InteractionResult.PASS; // Not our data, let other handlers try
        }

        CompoundTag linkData = rootTag.getCompound(DATASTICK_TAG_KEY);

        // Parse partner info from datastick
        GlobalPos partnerPos = GlobalPos.CODEC
                .decode(NbtOps.INSTANCE, linkData.get(TAG_POS))
                .result()
                .map(pair -> pair.getFirst())
                .orElse(null);

        if (partnerPos == null) {
            player.sendSystemMessage(Component.translatable("cosmiccore.link.invalid_data")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        UUID partnerOwner = linkData.getUUID(TAG_OWNER);

        // Attempt to establish link
        return tryLink(player, partnerPos, partnerOwner);
    }

    /**
     * Attempt to establish a link with the partner machine.
     * Handles all validation, negotiation, and persistence.
     */
    protected InteractionResult tryLink(Player player, GlobalPos partnerPos, UUID partnerOwner) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        MinecraftServer server = serverLevel.getServer();
        GlobalPos myPos = getGlobalPos();
        UUID myOwner = getTeamUUID();

        // === Validation ===

        // Self-link check
        if (myPos.equals(partnerPos)) {
            player.sendSystemMessage(Component.translatable("cosmiccore.link.cannot_self_link")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        // NOTE: We intentionally do NOT check partnerOwner from datastick NBT here.
        // The datastick may be stale (team changed since it was written).
        // Ownership is verified at runtime after loading the partner machine.

        // Partner limit check (this machine)
        Set<GlobalPos> currentPartners = getLinkedPartners();
        if (currentPartners.size() >= getMaxPartners() && !currentPartners.contains(partnerPos)) {
            player.sendSystemMessage(Component.translatable("cosmiccore.link.limit_reached_self")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        // Already linked check
        if (currentPartners.contains(partnerPos)) {
            player.sendSystemMessage(Component.translatable("cosmiccore.link.already_linked")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.FAIL;
        }

        // === Load and verify partner ===
        // SECURITY: Always load partner to verify ownership and compatibility
        boolean needsUnload = false;
        if (!LinkedMultiblockHelper.isPartnerOnline(server, partnerPos)) {
            // Try to force-load partner temporarily
            if (!LinkedMultiblockHelper.forceLoadPartnerChunk(server, myPos, partnerPos)) {
                player.sendSystemMessage(Component.translatable("cosmiccore.link.partner_not_loaded")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
            needsUnload = true;
        }

        try {
            MetaMachine rawPartner = LinkedMultiblockHelper.getMachine(server, partnerPos);
            if (rawPartner == null) {
                player.sendSystemMessage(Component.translatable("cosmiccore.link.partner_missing")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            if (!(rawPartner instanceof ILinkedMultiblock partnerMachine)) {
                player.sendSystemMessage(Component.translatable("cosmiccore.link.not_linkable")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            // Verify ownership matches at runtime
            UUID actualPartnerOwner = partnerMachine.getTeamUUID();
            if (!Objects.equals(myOwner, actualPartnerOwner)) {
                player.sendSystemMessage(Component.translatable("cosmiccore.link.different_owner")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            // === Partner capacity check ===
            Set<GlobalPos> partnerLinks = partnerMachine.getLinkedPartners();
            if (partnerLinks.size() >= partnerMachine.getMaxPartners() && !partnerLinks.contains(myPos)) {
                player.sendSystemMessage(Component.translatable("cosmiccore.link.limit_reached_partner")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            // === Role Negotiation ===
            RolePair roles = LinkedMultiblockHelper.negotiateRoles(getLinkRole(), partnerMachine.getLinkRole());
            if (roles == null) {
                player.sendSystemMessage(Component.translatable("cosmiccore.link.incompatible_roles",
                        getLinkRole().name(), partnerMachine.getLinkRole().name())
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            // === Type compatibility check ===
            if (!canLinkTo(partnerPos, partnerMachine)) {
                player.sendSystemMessage(Component.translatable("cosmiccore.link.incompatible_self")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            if (!partnerMachine.canLinkTo(myPos, this)) {
                player.sendSystemMessage(Component.translatable("cosmiccore.link.incompatible_partner")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            // === Persist link ===
            LinkedMultiblockSavedData savedData = LinkedMultiblockSavedData.getOrCreate(serverLevel);
            savedData.link(myOwner, myPos, partnerPos, roles.aRole(), roles.bRole());

            // === Notify both machines ===
            onLinkEstablished(partnerPos);
            knownPartners.add(partnerPos);

            partnerMachine.onLinkEstablished(myPos);

            // Success feedback
            String myName = getDefinition().getName();
            String partnerName = rawPartner.getDefinition().getName();
            player.sendSystemMessage(Component.translatable("cosmiccore.link.established", myName, partnerName)
                    .withStyle(ChatFormatting.GREEN));

            return InteractionResult.SUCCESS;

        } finally {
            // Release temporary chunk load
            if (needsUnload) {
                LinkedMultiblockHelper.releasePartnerChunk(server, myPos, partnerPos);
            }
        }
    }

    // ==================== Default Implementations ====================

    @Override
    public boolean canLinkTo(GlobalPos partner, ILinkedMultiblock partnerMachine) {
        // Default: allow linking to any ILinkedMultiblock
        // Subclasses should override for type-specific restrictions
        return true;
    }

    @Override
    public LinkRole getLinkRole() {
        // Default: bidirectional peer
        return LinkRole.PEER;
    }

    @Override
    public void onLinkEstablished(GlobalPos partner) {
        // Default: just log
        CosmicCore.LOGGER.debug("Link established: {} -> {}", getGlobalPos(), partner);
    }

    @Override
    public void onLinkBroken(GlobalPos partner) {
        // Default: just log and update cache
        CosmicCore.LOGGER.debug("Link broken: {} -> {}", getGlobalPos(), partner);
        knownPartners.remove(partner);
    }

    // ==================== Utility Methods ====================

    /**
     * Get a linked partner's machine instance.
     * Does NOT force-load chunks - returns null if partner is unloaded.
     */
    @Nullable
    protected ILinkedMultiblock getPartnerMachine(GlobalPos partner) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return LinkedMultiblockHelper.getLinkedMachine(serverLevel.getServer(), partner);
    }

    /**
     * Check if this machine can query the given partner (based on effective role).
     */
    protected boolean canQueryPartner(GlobalPos partner) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }
        UUID owner = getTeamUUID();
        if (owner == null) return false;
        return LinkedMultiblockHelper.canQuery(serverLevel.getServer(), owner, getGlobalPos(), partner);
    }

    /**
     * Get the effective role for this machine in relation to a specific partner.
     */
    @Nullable
    protected LinkRole getEffectiveRole(GlobalPos partner) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return null;
        }
        UUID owner = getTeamUUID();
        if (owner == null) return null;

        LinkedMultiblockSavedData savedData = LinkedMultiblockSavedData.getOrCreate(serverLevel);
        LinkEntry link = savedData.getLinkTo(owner, getGlobalPos(), partner);
        return link != null ? link.effectiveRole() : null;
    }

    // ==================== Partner Resource Queries ====================

    /**
     * Check if a partner has a specific item in its input handlers.
     * Handles chunk loading automatically.
     *
     * @param partner       The partner to query
     * @param itemPredicate Predicate to test items (e.g., stack -> stack.is(Items.DIAMOND))
     * @return true if partner has matching item, false otherwise or if unavailable
     */
    protected boolean partnerHasItem(GlobalPos partner, Predicate<ItemStack> itemPredicate) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }
        UUID owner = getTeamUUID();
        if (owner == null) return false;

        return LinkedMultiblockHelper.partnerHasItem(
                serverLevel.getServer(), owner, getGlobalPos(), partner, itemPredicate);
    }

    /**
     * Check if a partner has a specific fluid in its input handlers.
     * Handles chunk loading automatically.
     *
     * @param partner        The partner to query
     * @param fluidPredicate Predicate to test fluids (e.g., stack -> stack.getFluid().is(Fluids.LAVA))
     * @return true if partner has matching fluid, false otherwise or if unavailable
     */
    protected boolean partnerHasFluid(GlobalPos partner, Predicate<FluidStack> fluidPredicate) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }
        UUID owner = getTeamUUID();
        if (owner == null) return false;

        return LinkedMultiblockHelper.partnerHasFluid(
                serverLevel.getServer(), owner, getGlobalPos(), partner, fluidPredicate);
    }

    /**
     * Get total energy stored in a partner's energy containers.
     * Handles chunk loading automatically.
     *
     * @param partner The partner to query
     * @return Energy stored in EU, or 0 if unavailable
     */
    protected long getPartnerEnergyStored(GlobalPos partner) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return 0L;
        }
        UUID owner = getTeamUUID();
        if (owner == null) return 0L;

        return LinkedMultiblockHelper.getPartnerEnergyStored(
                serverLevel.getServer(), owner, getGlobalPos(), partner);
    }

    /**
     * Check if a partner's multiblock is formed.
     * Handles chunk loading automatically.
     *
     * @param partner The partner to query
     * @return true if partner is formed, false otherwise or if unavailable
     */
    protected boolean isPartnerFormed(GlobalPos partner) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }
        UUID owner = getTeamUUID();
        if (owner == null) return false;

        return LinkedMultiblockHelper.isPartnerFormed(
                serverLevel.getServer(), owner, getGlobalPos(), partner);
    }

    /**
     * Check if a partner is currently running a recipe.
     * Handles chunk loading automatically.
     *
     * @param partner The partner to query
     * @return true if partner is working, false otherwise or if unavailable
     */
    protected boolean isPartnerWorking(GlobalPos partner) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }
        UUID owner = getTeamUUID();
        if (owner == null) return false;

        return LinkedMultiblockHelper.isPartnerWorking(
                serverLevel.getServer(), owner, getGlobalPos(), partner);
    }

    /**
     * Execute a custom query on a partner machine.
     * Handles chunk loading and permission checks automatically.
     *
     * @param partner The partner to query
     * @param query   The query function
     * @return Query result, or null if unavailable
     */
    @Nullable
    protected <T> T queryPartner(GlobalPos partner, LinkedMultiblockHelper.PartnerQuery<T> query) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return null;
        }
        UUID owner = getTeamUUID();
        if (owner == null) return null;

        return LinkedMultiblockHelper.queryPartner(
                serverLevel.getServer(), owner, getGlobalPos(), partner, query);
    }

    /**
     * Check if ANY linked partner has a specific item.
     * Useful for recipe conditions that require "a linked partner has X".
     *
     * @param itemPredicate Predicate to test items
     * @return true if any partner has the item
     */
    protected boolean anyPartnerHasItem(Predicate<ItemStack> itemPredicate) {
        for (GlobalPos partner : getLinkedPartners()) {
            if (partnerHasItem(partner, itemPredicate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if ANY linked partner has a specific fluid.
     *
     * @param fluidPredicate Predicate to test fluids
     * @return true if any partner has the fluid
     */
    protected boolean anyPartnerHasFluid(Predicate<FluidStack> fluidPredicate) {
        for (GlobalPos partner : getLinkedPartners()) {
            if (partnerHasFluid(partner, fluidPredicate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if ANY linked partner is formed and working.
     *
     * @return true if any partner is actively working
     */
    public boolean anyPartnerWorking() {
        for (GlobalPos partner : getLinkedPartners()) {
            if (isPartnerWorking(partner)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count how many linked partners are currently formed.
     *
     * @return Number of formed partners
     */
    public int countFormedPartners() {
        int count = 0;
        for (GlobalPos partner : getLinkedPartners()) {
            if (isPartnerFormed(partner)) {
                count++;
            }
        }
        return count;
    }
}
