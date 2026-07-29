package com.ghostipedia.cosmiccore.common.mirror.deed;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DeedQuestCompatBridge;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.DeedPresentationPacket;
import com.ghostipedia.cosmiccore.common.network.packet.DeedSyncPacket;

import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class DeedsAPI {

    private static final ResourceLocation NETHER_PERMIT_ADVANCEMENT = CosmicCore.id("nether_permit");
    private static final ResourceLocation ENTER_NETHER_ADVANCEMENT = ResourceLocation
            .fromNamespaceAndPath("minecraft", "story/enter_the_nether");
    private static final ResourceLocation NETHER_ROOT_ADVANCEMENT = ResourceLocation
            .fromNamespaceAndPath("minecraft", "nether/root");

    private DeedsAPI() {}

    public static boolean grantCoil(ServerPlayer player, ResourceLocation deedId) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        if (deedId.equals(DeedRegistry.THE_ADDRESS.id())) return false;
        String teamKey = DeedTeams.teamKey(player);
        boolean changed = DeedLedger.get(server).grantCoil(teamKey, deedId);
        if (changed) {
            syncTeam(server, teamKey);
        }
        return changed;
    }

    public static boolean revoke(ServerPlayer player, ResourceLocation deedId) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        String teamKey = DeedTeams.teamKey(player);
        DeedLedger ledger = DeedLedger.get(server);
        boolean changed = ledger.revoke(teamKey, deedId);
        ledger.clearPresentation(DeedTeams.teamMemberIds(player), deedId);
        if (changed) syncTeam(server, teamKey);
        return changed;
    }

    @Nullable
    public static DeedLedger.WovenEcho weave(ServerPlayer player, ResourceLocation deedId, boolean bindPosition) {
        return weave(player, deedId, bindPosition, false);
    }

    @Nullable
    public static DeedLedger.WovenEcho weave(ServerPlayer player, ResourceLocation deedId, boolean bindPosition,
                                             boolean forcedPresentation) {
        MinecraftServer server = player.getServer();
        if (server == null) return null;
        String teamKey = DeedTeams.teamKey(player);
        GlobalPos pos = bindPosition ? GlobalPos.of(player.level().dimension(), player.blockPosition()) : null;
        DeedLedger ledger = DeedLedger.get(server);
        DeedLedger.WovenEcho echo = ledger.weave(teamKey, deedId, player.getUUID(),
                player.level().getGameTime(), pos);
        if (echo != null) {
            Collection<UUID> members = DeedTeams.teamMemberIds(player);
            for (UUID memberId : members) {
                ledger.enqueuePresentation(memberId, deedId, forcedPresentation);
            }
            syncTeam(server, teamKey);
            for (ServerPlayer online : server.getPlayerList().getPlayers()) {
                if (members.contains(online.getUUID())) {
                    CCoreNetwork.sendToPlayer(online, new DeedPresentationPacket(deedId, true));
                }
            }
        }
        return echo;
    }

    public static boolean reconcilePatientZero(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        String teamKey = DeedTeams.teamKey(player);
        DeedLedger ledger = DeedLedger.get(server);
        ResourceLocation deedId = DeedRegistry.NETHER_PERMIT.id();
        if (ledger.isWoven(teamKey, deedId) || !hasPatientZeroTrigger(player)) return false;
        return grantCoil(player, deedId);
    }

    public static boolean hasPatientZeroTrigger(ServerPlayer player) {
        if (player.getInventory().contains(stack -> stack.is(CosmicItems.NETHER_PERMIT.get()))) return true;
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        return player.level().dimension().equals(Level.NETHER) ||
                hasAdvancement(player, NETHER_PERMIT_ADVANCEMENT) ||
                hasAdvancement(player, ENTER_NETHER_ADVANCEMENT) ||
                hasAdvancement(player, NETHER_ROOT_ADVANCEMENT) ||
                DeedQuestCompatBridge.hasPatientZeroQuestProgress(player);
    }

    private static boolean hasAdvancement(ServerPlayer player, ResourceLocation advancementId) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        var advancement = server.getAdvancements().get(advancementId);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public static boolean isWoven(MinecraftServer server, String teamKey, ResourceLocation deedId) {
        return DeedLedger.get(server).isWoven(teamKey, deedId);
    }

    public static void syncTeam(MinecraftServer server, String teamKey) {
        DeedQuestCompatBridge.syncTeam(server, teamKey);
        int sent = 0;
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (DeedTeams.teamKey(online).equals(teamKey)) {
                syncPlayer(online);
                sent++;
            }
        }
        DeedLedger ledger = DeedLedger.get(server);
        CosmicCore.LOGGER.info("Deed sync team {}: {} woven {} pending -> {} players", teamKey,
                ledger.wovenOf(teamKey).size(), ledger.pendingOf(teamKey).size(), sent);
    }

    public static void syncPlayer(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        String teamKey = DeedTeams.teamKey(player);
        DeedLedger ledger = DeedLedger.get(server);
        List<ResourceLocation> woven = new ArrayList<>();
        for (DeedLedger.WovenEcho echo : ledger.wovenOf(teamKey)) {
            woven.add(echo.deedId());
        }
        CCoreNetwork.sendToPlayer(player, new DeedSyncPacket(woven,
                new ArrayList<>(ledger.pendingOf(teamKey)), ledger.presentationsOf(player.getUUID())));
    }

    public static void acknowledgePresentation(ServerPlayer player, ResourceLocation deedId) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        if (DeedLedger.get(server).acknowledgePresentation(player.getUUID(), deedId)) {
            syncPlayer(player);
        }
    }

    public static void resetTeam(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        String teamKey = DeedTeams.teamKey(player);
        DeedLedger ledger = DeedLedger.get(server);
        Collection<UUID> members = DeedTeams.teamMemberIds(player);
        ledger.reset(teamKey);
        ledger.clearPresentations(members);
        if (hasPatientZeroTrigger(player)) {
            ledger.grantCoil(teamKey, DeedRegistry.NETHER_PERMIT.id());
        }
        syncTeam(server, teamKey);
    }
}
