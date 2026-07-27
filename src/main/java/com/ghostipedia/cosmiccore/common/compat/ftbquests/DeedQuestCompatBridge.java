package com.ghostipedia.cosmiccore.common.compat.ftbquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class DeedQuestCompatBridge {

    private DeedQuestCompatBridge() {}

    public static void register() {
        if (isLoaded()) Loaded.register();
    }

    public static void registerPayloads(PayloadRegistrar registrar) {
        if (isLoaded()) Loaded.registerPayloads(registrar);
    }

    public static void syncTeam(MinecraftServer server, String teamKey) {
        if (isLoaded()) Loaded.syncTeam(server, teamKey);
    }

    public static boolean canCommitWeave(ServerPlayer player, ResourceLocation deedId) {
        return !isLoaded() || Loaded.canCommitWeave(player, deedId);
    }

    private static boolean isLoaded() {
        return ModList.get().isLoaded("ftbquests");
    }

    private static final class Loaded {

        private static void register() {
            DeedQuestCompat.register();
        }

        private static void registerPayloads(PayloadRegistrar registrar) {
            registrar.playToServer(
                    com.ghostipedia.cosmiccore.common.network.packet.DeedQuestRequestPacket.TYPE,
                    com.ghostipedia.cosmiccore.common.network.packet.DeedQuestRequestPacket.CODEC,
                    com.ghostipedia.cosmiccore.common.network.packet.DeedQuestRequestPacket::execute);
            registrar.playToClient(
                    com.ghostipedia.cosmiccore.common.network.packet.DeedQuestOpenPacket.TYPE,
                    com.ghostipedia.cosmiccore.common.network.packet.DeedQuestOpenPacket.CODEC,
                    com.ghostipedia.cosmiccore.common.network.packet.DeedQuestOpenPacket::execute);
        }

        private static void syncTeam(MinecraftServer server, String teamKey) {
            DeedQuestCompat.reconcileTeam(server, teamKey);
        }

        private static boolean canCommitWeave(ServerPlayer player, ResourceLocation deedId) {
            var file = dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE;
            if (file == null || file.server != player.getServer()) return false;
            var teamData = file.getTeamData(player).orElse(null);
            if (teamData == null || teamData.isLocked()) return false;
            boolean found = false;
            for (var task : file.getAllTasks()) {
                if (!(task instanceof DeedTask deedTask) ||
                        !deedTask.deedId().equals(deedId)) {
                    continue;
                }
                found = true;
                if (deedTask.requirementsComplete(teamData)) return true;
            }
            return !found;
        }
    }
}
