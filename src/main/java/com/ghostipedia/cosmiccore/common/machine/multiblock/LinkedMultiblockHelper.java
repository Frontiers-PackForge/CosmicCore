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
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LinkedMultiblockHelper {

    public static final int MAX_FORCED_CHUNKS_PER_MACHINE = 4;
    private static final Map<GlobalPos, Map<GlobalPos, BlockPos>> activeTickets = new HashMap<>();

    public record RolePair(LinkRole aRole, LinkRole bRole) {}

    @Nullable
    public static RolePair negotiateRoles(LinkRole aDeclared, LinkRole bDeclared) {
        if (aDeclared == LinkRole.PEER && bDeclared == LinkRole.PEER) {
            return new RolePair(LinkRole.PEER, LinkRole.PEER);
        }
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
        if (aDeclared == LinkRole.CONTROLLER && bDeclared == LinkRole.REMOTE) {
            return new RolePair(LinkRole.CONTROLLER, LinkRole.REMOTE);
        }
        if (aDeclared == LinkRole.REMOTE && bDeclared == LinkRole.CONTROLLER) {
            return new RolePair(LinkRole.REMOTE, LinkRole.CONTROLLER);
        }
        return null;
    }

    @Nullable
    public static MetaMachine getMachine(MinecraftServer server, GlobalPos pos) {
        ServerLevel level = server.getLevel(pos.dimension());
        if (level == null) return null;

        if (!level.isLoaded(pos.pos())) {
            return null;
        }

        return MetaMachine.getMachine(level, pos.pos());
    }

    @Nullable
    public static ILinkedMultiblock getLinkedMachine(MinecraftServer server, GlobalPos pos) {
        MetaMachine machine = getMachine(server, pos);
        return machine instanceof ILinkedMultiblock linked ? linked : null;
    }

    public static boolean isPartnerOnline(MinecraftServer server, GlobalPos pos) {
        return getMachine(server, pos) != null;
    }

    public static boolean forceLoadPartnerChunk(MinecraftServer server, GlobalPos requester, GlobalPos target) {
        Map<GlobalPos, BlockPos> existing = activeTickets.getOrDefault(requester, Collections.emptyMap());
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
        BlockPos ownerPos = requester.pos();
        boolean success = level.setChunkForced(chunkPos.x, chunkPos.z, true);

        activeTickets.computeIfAbsent(requester, k -> new HashMap<>())
                .put(target, ownerPos);
        CosmicCore.LOGGER.debug("Force-loaded chunk {} in {} for machine at {} (owner: {}, changed: {})",
                chunkPos, target.dimension().location(), requester, ownerPos, success);

        return true;
    }

    public static void releasePartnerChunk(MinecraftServer server, GlobalPos requester, GlobalPos target) {
        Map<GlobalPos, BlockPos> tickets = activeTickets.get(requester);
        if (tickets == null) return;

        BlockPos ownerPos = tickets.remove(target);
        if (ownerPos == null) return;

        ServerLevel level = server.getLevel(target.dimension());
        if (level == null) return;

        ChunkPos chunkPos = new ChunkPos(target.pos());
        level.setChunkForced(chunkPos.x, chunkPos.z, false);

        CosmicCore.LOGGER.debug("Released chunk {} in {} for machine at {} (owner: {})",
                chunkPos, target.dimension().location(), requester, ownerPos);

        if (tickets.isEmpty()) {
            activeTickets.remove(requester);
        }
    }

    public static void releaseAllTickets(MinecraftServer server, GlobalPos requester) {
        Map<GlobalPos, BlockPos> tickets = activeTickets.remove(requester);
        if (tickets == null) return;

        int released = 0;
        for (Map.Entry<GlobalPos, BlockPos> entry : tickets.entrySet()) {
            GlobalPos target = entry.getKey();

            ServerLevel level = server.getLevel(target.dimension());
            if (level != null) {
                ChunkPos chunkPos = new ChunkPos(target.pos());
                level.setChunkForced(chunkPos.x, chunkPos.z, false);
                released++;
            }
        }

        CosmicCore.LOGGER.debug("Released {} force-load tickets for machine at {}",
                released, requester);
    }

    public static int getActiveTicketCount(GlobalPos requester) {
        return activeTickets.getOrDefault(requester, Collections.emptyMap()).size();
    }

    public static boolean validateLink(MinecraftServer server, UUID owner, GlobalPos a, GlobalPos b) {
        LinkedMultiblockSavedData savedData = LinkedMultiblockSavedData.getOrCreate(server);
        if (!savedData.isLinked(owner, a, b)) {
            return false;
        }

        MetaMachine machineA = getMachine(server, a);
        MetaMachine machineB = getMachine(server, b);

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

    public static boolean canQuery(MinecraftServer server, UUID owner, GlobalPos requester, GlobalPos target) {
        return LinkedMultiblockSavedData.getOrCreate(server).canQuery(owner, requester, target);
    }

    @FunctionalInterface
    public interface PartnerQuery<T> {

        T query(WorkableElectricMultiblockMachine partner);
    }

    @Nullable
    public static <T> T queryPartner(
                                     MinecraftServer server,
                                     UUID owner,
                                     GlobalPos requester,
                                     GlobalPos target,
                                     PartnerQuery<T> query) {
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
                if (handler instanceof net.neoforged.neoforge.items.IItemHandler itemHandler) {
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

    public static boolean partnerHasFluid(
                                          MinecraftServer server,
                                          UUID owner,
                                          GlobalPos requester,
                                          GlobalPos target,
                                          java.util.function.Predicate<net.neoforged.neoforge.fluids.FluidStack> fluidPredicate) {
        Boolean result = queryPartner(server, owner, requester, target, partner -> {
            var handlers = partner.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
            if (handlers == null) return false;

            for (Object handler : handlers) {
                if (handler instanceof net.neoforged.neoforge.fluids.capability.IFluidHandler fluidHandler) {
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

    public static boolean isPartnerFormed(
                                          MinecraftServer server,
                                          UUID owner,
                                          GlobalPos requester,
                                          GlobalPos target) {
        Boolean result = queryPartner(server, owner, requester, target,
                WorkableElectricMultiblockMachine::isFormed);
        return result != null && result;
    }

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

    private static final Map<String, String> DIMENSION_NAMES = Map.of(
            "minecraft:overworld", "Overworld",
            "minecraft:the_nether", "The Nether",
            "minecraft:the_end", "The End",
            "ad_astra:earth_orbit", "Earth Orbit",
            "ad_astra:moon", "Moon",
            "ad_astra:mars", "Mars",
            "ad_astra:venus", "Venus",
            "ad_astra:mercury", "Mercury",
            "ad_astra:glacio", "Glacio");

    public static String getDimensionName(net.minecraft.resources.ResourceLocation dim) {
        return DIMENSION_NAMES.getOrDefault(dim.toString(), dim.getPath().replace("_", " "));
    }
}
