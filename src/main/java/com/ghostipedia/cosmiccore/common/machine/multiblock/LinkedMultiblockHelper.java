package com.ghostipedia.cosmiccore.common.machine.multiblock;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock;
import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock.LinkRole;
import com.ghostipedia.cosmiccore.api.data.savedData.LinkedMultiblockSavedData;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.world.ForgeChunkManager;

import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Utilities for cross-dimensional multiblock access.
 * Handles chunk loading with proper lifecycle management.
 */
public class LinkedMultiblockHelper {

    /** Maximum forced chunks per requesting machine */
    public static final int MAX_FORCED_CHUNKS_PER_MACHINE = 4;

    /**
     * Tracks active force-load tickets.
     * Maps: Requester GlobalPos -> (Target GlobalPos -> Owner BlockPos used for ticket)
     * <p>
     * IMPORTANT: We use the requester's position as the ticket owner to ensure
     * each requester has independent tickets. This prevents one requester from
     * accidentally releasing another's ticket.
     * <p>
     * NOTE: Tickets are tracked in memory only. A hard crash while tickets are active
     * can leave chunks loaded until restart. Mitigation: tickets are short-lived
     * (released after use), and Forge clears orphaned tickets on dimension unload.
     */
    private static final Map<GlobalPos, Map<GlobalPos, BlockPos>> activeTickets = new HashMap<>();

    // ==================== Role Negotiation ====================

    /**
     * Result of role negotiation between two machines.
     */
    public record RolePair(LinkRole aRole, LinkRole bRole) {}

    /**
     * Negotiate effective roles for a link between two machines.
     * <p>
     * Rules:
     * <ul>
     * <li>PEER + PEER = PEER/PEER (bidirectional)</li>
     * <li>PEER adapts to stricter partner:
     * <ul>
     * <li>PEER + CONTROLLER → REMOTE/CONTROLLER</li>
     * <li>PEER + REMOTE → CONTROLLER/REMOTE</li>
     * </ul>
     * </li>
     * <li>CONTROLLER + REMOTE = CONTROLLER/REMOTE (asymmetric)</li>
     * <li>CONTROLLER + CONTROLLER = incompatible</li>
     * <li>REMOTE + REMOTE = incompatible</li>
     * </ul>
     *
     * @param aDeclared Machine A's declared role preference
     * @param bDeclared Machine B's declared role preference
     * @return Negotiated roles, or null if incompatible
     */
    @Nullable
    public static RolePair negotiateRoles(LinkRole aDeclared, LinkRole bDeclared) {
        // PEER + PEER = PEER/PEER
        if (aDeclared == LinkRole.PEER && bDeclared == LinkRole.PEER) {
            return new RolePair(LinkRole.PEER, LinkRole.PEER);
        }

        // PEER adapts to stricter partner (downgrade to preserve partner's intent)
        if (aDeclared == LinkRole.PEER && bDeclared == LinkRole.CONTROLLER) {
            return new RolePair(LinkRole.REMOTE, LinkRole.CONTROLLER);
        }
        if (aDeclared == LinkRole.PEER && bDeclared == LinkRole.REMOTE) {
            return new RolePair(LinkRole.CONTROLLER, LinkRole.REMOTE);
        }
        if (aDeclared == LinkRole.CONTROLLER && bDeclared == LinkRole.PEER) {
            return new RolePair(LinkRole.CONTROLLER, LinkRole.REMOTE);
        }
        if (aDeclared == LinkRole.REMOTE && bDeclared == LinkRole.PEER) {
            return new RolePair(LinkRole.REMOTE, LinkRole.CONTROLLER);
        }

        // CONTROLLER + REMOTE = valid asymmetric link
        if (aDeclared == LinkRole.CONTROLLER && bDeclared == LinkRole.REMOTE) {
            return new RolePair(LinkRole.CONTROLLER, LinkRole.REMOTE);
        }
        if (aDeclared == LinkRole.REMOTE && bDeclared == LinkRole.CONTROLLER) {
            return new RolePair(LinkRole.REMOTE, LinkRole.CONTROLLER);
        }

        // CONTROLLER + CONTROLLER = incompatible (conflict)
        // REMOTE + REMOTE = incompatible (deadlock)
        return null;
    }

    // ==================== Machine Access ====================

    /**
     * Safely retrieve a machine from any dimension.
     * Returns null if dimension doesn't exist or chunk isn't loaded.
     * Does NOT force-load the chunk.
     */
    @Nullable
    public static MetaMachine getMachine(MinecraftServer server, GlobalPos pos) {
        ServerLevel level = server.getLevel(pos.dimension());
        if (level == null) return null;

        if (!level.isLoaded(pos.pos())) {
            return null;
        }

        return MetaMachine.getMachine(level, pos.pos());
    }

    /**
     * Get machine as ILinkedMultiblock if it implements the interface.
     */
    @Nullable
    public static ILinkedMultiblock getLinkedMachine(MinecraftServer server, GlobalPos pos) {
        MetaMachine machine = getMachine(server, pos);
        if (machine instanceof ILinkedMultiblock linked) {
            return linked;
        }
        return null;
    }

    /**
     * Check if a linked partner is currently accessible (chunk loaded).
     */
    public static boolean isPartnerOnline(MinecraftServer server, GlobalPos pos) {
        return getMachine(server, pos) != null;
    }

    // ==================== Chunk Loading ====================

    /**
     * Force-load a partner's chunk for cross-dimensional access.
     * Tracks the ticket with proper owner position for later removal.
     * <p>
     * Uses the REQUESTER's position as the ticket owner to ensure each
     * requester has independent tickets.
     *
     * @param server    The server
     * @param requester The machine requesting the load (for ticket tracking)
     * @param target    The partner machine's position to load
     * @return true if successfully loaded (or already loaded), false if at limit or failed
     */
    public static boolean forceLoadPartnerChunk(MinecraftServer server, GlobalPos requester, GlobalPos target) {
        // Check per-machine limit
        Map<GlobalPos, BlockPos> existing = activeTickets.getOrDefault(requester, Collections.emptyMap());

        // Already loaded by this requester?
        if (existing.containsKey(target)) {
            return true;
        }

        if (existing.size() >= MAX_FORCED_CHUNKS_PER_MACHINE) {
            CosmicCore.LOGGER.warn("Machine at {} has reached force-load limit of {}",
                    requester, MAX_FORCED_CHUNKS_PER_MACHINE);
            return false;
        }

        ServerLevel level = server.getLevel(target.dimension());
        if (level == null) {
            CosmicCore.LOGGER.warn("Cannot force-load chunk: dimension {} does not exist",
                    target.dimension().location());
            return false;
        }

        ChunkPos chunkPos = new ChunkPos(target.pos());

        // Use REQUESTER position as the ticket owner (unique per requester)
        // This prevents one requester from releasing another's ticket
        BlockPos ownerPos = requester.pos();

        boolean success = ForgeChunkManager.forceChunk(
                level,
                CosmicCore.MOD_ID,
                ownerPos,
                chunkPos.x,
                chunkPos.z,
                true, // add
                true // ticking
        );

        if (success) {
            activeTickets.computeIfAbsent(requester, k -> new HashMap<>())
                    .put(target, ownerPos);
            CosmicCore.LOGGER.debug("Force-loaded chunk {} in {} for machine at {} (owner: {})",
                    chunkPos, target.dimension().location(), requester, ownerPos);
        } else {
            CosmicCore.LOGGER.warn("Failed to force-load chunk {} in {}",
                    chunkPos, target.dimension().location());
        }

        return success;
    }

    /**
     * Release a specific force-loaded chunk.
     * Uses the same owner position that was used when adding the ticket.
     */
    public static void releasePartnerChunk(MinecraftServer server, GlobalPos requester, GlobalPos target) {
        Map<GlobalPos, BlockPos> tickets = activeTickets.get(requester);
        if (tickets == null) return;

        BlockPos ownerPos = tickets.remove(target);
        if (ownerPos == null) return; // Wasn't loaded by this requester

        ServerLevel level = server.getLevel(target.dimension());
        if (level == null) return;

        ChunkPos chunkPos = new ChunkPos(target.pos());

        // Use the SAME owner position that was used for add
        ForgeChunkManager.forceChunk(
                level,
                CosmicCore.MOD_ID,
                ownerPos,
                chunkPos.x,
                chunkPos.z,
                false, // remove
                true);

        CosmicCore.LOGGER.debug("Released chunk {} in {} for machine at {} (owner: {})",
                chunkPos, target.dimension().location(), requester, ownerPos);

        if (tickets.isEmpty()) {
            activeTickets.remove(requester);
        }
    }

    /**
     * Release ALL force-loaded chunks for a machine.
     * MUST be called in onMachineRemoved() to prevent ticket leaks.
     */
    public static void releaseAllTickets(MinecraftServer server, GlobalPos requester) {
        Map<GlobalPos, BlockPos> tickets = activeTickets.remove(requester);
        if (tickets == null) return;

        int released = 0;
        for (Map.Entry<GlobalPos, BlockPos> entry : tickets.entrySet()) {
            GlobalPos target = entry.getKey();
            BlockPos ownerPos = entry.getValue();

            ServerLevel level = server.getLevel(target.dimension());
            if (level != null) {
                ChunkPos chunkPos = new ChunkPos(target.pos());
                ForgeChunkManager.forceChunk(
                        level,
                        CosmicCore.MOD_ID,
                        ownerPos,
                        chunkPos.x,
                        chunkPos.z,
                        false,
                        true);
                released++;
            }
        }

        CosmicCore.LOGGER.debug("Released {} force-load tickets for machine at {}",
                released, requester);
    }

    /**
     * Get number of active force-load tickets for a machine.
     */
    public static int getActiveTicketCount(GlobalPos requester) {
        return activeTickets.getOrDefault(requester, Collections.emptyMap()).size();
    }

    // ==================== Link Validation ====================

    /**
     * Validate that both machines still exist and link is valid.
     * Does not force-load chunks - returns false if either is unloaded.
     */
    public static boolean validateLink(MinecraftServer server, UUID owner, GlobalPos a, GlobalPos b) {
        // Check SavedData
        LinkedMultiblockSavedData savedData = LinkedMultiblockSavedData.getOrCreate(server);
        if (!savedData.isLinked(owner, a, b)) {
            return false;
        }

        // Check machines exist (if chunks loaded)
        MetaMachine machineA = getMachine(server, a);
        MetaMachine machineB = getMachine(server, b);

        // If chunk is loaded but machine is gone, link is invalid
        ServerLevel levelA = server.getLevel(a.dimension());
        if (levelA != null && levelA.isLoaded(a.pos()) && machineA == null) {
            return false;
        }

        ServerLevel levelB = server.getLevel(b.dimension());
        if (levelB != null && levelB.isLoaded(b.pos()) && machineB == null) {
            return false;
        }

        return true;
    }

    // ==================== Permission Helpers ====================

    /**
     * Check if requester can query the target (convenience method).
     */
    public static boolean canQuery(MinecraftServer server, UUID owner, GlobalPos requester, GlobalPos target) {
        return LinkedMultiblockSavedData.getOrCreate(server).canQuery(owner, requester, target);
    }

    // ==================== Partner Resource Query ====================

    /**
     * Functional interface for querying a partner machine.
     */
    @FunctionalInterface
    public interface PartnerQuery<T> {

        T query(WorkableElectricMultiblockMachine partner);
    }

    /**
     * Query a partner machine with temporary chunk loading.
     * Loads the partner's chunk if needed, executes the query, then releases.
     * <p>
     * IMPORTANT: This method handles short-lived queries. For sustained access,
     * use forceLoadPartnerChunk/releasePartnerChunk directly.
     *
     * @param server    The server
     * @param owner     Team/player UUID for permission check
     * @param requester The requesting machine's position
     * @param target    The partner machine's position
     * @param query     The query function to execute
     * @return Query result, or null if partner unavailable or permission denied
     */
    @Nullable
    public static <T> T queryPartner(
                                     MinecraftServer server,
                                     UUID owner,
                                     GlobalPos requester,
                                     GlobalPos target,
                                     PartnerQuery<T> query) {
        // Permission check
        if (!canQuery(server, owner, requester, target)) {
            CosmicCore.LOGGER.debug("Query denied: {} cannot query {}", requester, target);
            return null;
        }

        boolean needsUnload = false;
        if (!isPartnerOnline(server, target)) {
            if (!forceLoadPartnerChunk(server, requester, target)) {
                return null;
            }
            needsUnload = true;
        }

        try {
            MetaMachine machine = getMachine(server, target);
            if (machine instanceof WorkableElectricMultiblockMachine workable) {
                return query.query(workable);
            }
            return null;
        } finally {
            if (needsUnload) {
                releasePartnerChunk(server, requester, target);
            }
        }
    }

    /**
     * Get a partner's item handler capabilities.
     * Returns empty list if partner unavailable or permission denied.
     *
     * @param io IO.IN for input handlers, IO.OUT for output handlers
     */
    public static List<IRecipeHandler<?>> getPartnerItemHandlers(
                                                                 MinecraftServer server,
                                                                 UUID owner,
                                                                 GlobalPos requester,
                                                                 GlobalPos target,
                                                                 IO io) {
        List<IRecipeHandler<?>> result = queryPartner(server, owner, requester, target,
                partner -> partner.getCapabilitiesFlat(io, ItemRecipeCapability.CAP));
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Get a partner's fluid handler capabilities.
     * Returns empty list if partner unavailable or permission denied.
     *
     * @param io IO.IN for input handlers, IO.OUT for output handlers
     */
    public static List<IRecipeHandler<?>> getPartnerFluidHandlers(
                                                                  MinecraftServer server,
                                                                  UUID owner,
                                                                  GlobalPos requester,
                                                                  GlobalPos target,
                                                                  IO io) {
        List<IRecipeHandler<?>> result = queryPartner(server, owner, requester, target,
                partner -> partner.getCapabilitiesFlat(io, FluidRecipeCapability.CAP));
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Get a partner's energy container capabilities.
     * Returns empty list if partner unavailable or permission denied.
     *
     * @param io IO.IN for input energy, IO.OUT for output energy
     */
    public static List<IRecipeHandler<?>> getPartnerEnergyHandlers(
                                                                   MinecraftServer server,
                                                                   UUID owner,
                                                                   GlobalPos requester,
                                                                   GlobalPos target,
                                                                   IO io) {
        List<IRecipeHandler<?>> result = queryPartner(server, owner, requester, target,
                partner -> partner.getCapabilitiesFlat(io, EURecipeCapability.CAP));
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Check if a partner has a specific item in any of its input handlers.
     */
    public static boolean partnerHasItem(
                                         MinecraftServer server,
                                         UUID owner,
                                         GlobalPos requester,
                                         GlobalPos target,
                                         java.util.function.Predicate<net.minecraft.world.item.ItemStack> itemPredicate) {
        Boolean result = queryPartner(server, owner, requester, target, partner -> {
            var handlers = partner.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
            if (handlers == null) return false;

            for (Object handler : handlers) {
                if (handler instanceof net.minecraftforge.items.IItemHandler itemHandler) {
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        if (itemPredicate.test(itemHandler.getStackInSlot(i))) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
        return result != null && result;
    }

    /**
     * Check if a partner has a specific fluid in any of its input handlers.
     */
    public static boolean partnerHasFluid(
                                          MinecraftServer server,
                                          UUID owner,
                                          GlobalPos requester,
                                          GlobalPos target,
                                          java.util.function.Predicate<net.minecraftforge.fluids.FluidStack> fluidPredicate) {
        Boolean result = queryPartner(server, owner, requester, target, partner -> {
            var handlers = partner.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
            if (handlers == null) return false;

            for (Object handler : handlers) {
                if (handler instanceof net.minecraftforge.fluids.capability.IFluidHandler fluidHandler) {
                    for (int i = 0; i < fluidHandler.getTanks(); i++) {
                        if (fluidPredicate.test(fluidHandler.getFluidInTank(i))) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
        return result != null && result;
    }

    /**
     * Get total energy stored across all of a partner's energy containers.
     * Returns 0 if partner unavailable or permission denied.
     */
    public static long getPartnerEnergyStored(
                                              MinecraftServer server,
                                              UUID owner,
                                              GlobalPos requester,
                                              GlobalPos target) {
        Long result = queryPartner(server, owner, requester, target, partner -> {
            var handlers = partner.getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
            if (handlers == null) return 0L;

            long total = 0;
            for (Object handler : handlers) {
                if (handler instanceof com.gregtechceu.gtceu.api.capability.IEnergyContainer energyContainer) {
                    total += energyContainer.getEnergyStored();
                }
            }
            return total;
        });
        return result != null ? result : 0L;
    }

    /**
     * Check if partner's multiblock is formed and working.
     */
    public static boolean isPartnerFormed(
                                          MinecraftServer server,
                                          UUID owner,
                                          GlobalPos requester,
                                          GlobalPos target) {
        Boolean result = queryPartner(server, owner, requester, target,
                WorkableElectricMultiblockMachine::isFormed);
        return result != null && result;
    }

    /**
     * Check if partner is currently running a recipe.
     */
    public static boolean isPartnerWorking(
                                           MinecraftServer server,
                                           UUID owner,
                                           GlobalPos requester,
                                           GlobalPos target) {
        Boolean result = queryPartner(server, owner, requester, target, partner -> {
            RecipeLogic logic = partner.getRecipeLogic();
            return logic != null && logic.isWorking();
        });
        return result != null && result;
    }
}
