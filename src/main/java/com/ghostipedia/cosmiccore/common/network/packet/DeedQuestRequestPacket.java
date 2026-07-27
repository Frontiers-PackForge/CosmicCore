package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DeedQuestCompatBridge;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DeedTask;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedLedger;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedTeams;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedsAPI;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import org.jetbrains.annotations.NotNull;

public final class DeedQuestRequestPacket implements CustomPacketPayload {

    public static final Type<DeedQuestRequestPacket> TYPE = new Type<>(CosmicCore.id("deed_quest_request"));
    public static final StreamCodec<FriendlyByteBuf, DeedQuestRequestPacket> CODEC = StreamCodec
            .ofMember(DeedQuestRequestPacket::encode, DeedQuestRequestPacket::new);

    private final long taskId;

    public DeedQuestRequestPacket(long taskId) {
        this.taskId = taskId;
    }

    private DeedQuestRequestPacket(FriendlyByteBuf buffer) {
        taskId = buffer.readLong();
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeLong(taskId);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            MinecraftServer server = player.getServer();
            ServerQuestFile file = ServerQuestFile.INSTANCE;
            if (server == null || file == null || file.server != server ||
                    !(file.getTask(taskId) instanceof DeedTask task) ||
                    DeedRegistry.get(task.deedId()) == null) {
                return;
            }
            TeamData teamData = file.getTeamData(player).orElse(null);
            String teamKey = DeedTeams.teamKey(player);
            if (teamData == null || !teamData.getTeamId().toString().equals(teamKey) || teamData.isLocked() ||
                    !task.requirementsComplete(teamData)) {
                return;
            }
            if (DeedsAPI.isWoven(server, teamKey, task.deedId())) {
                DeedQuestCompatBridge.syncTeam(server, teamKey);
                return;
            }
            DeedLedger ledger = DeedLedger.get(server);
            if (!ledger.pendingOf(teamKey).contains(task.deedId()) && !DeedsAPI.grantCoil(player, task.deedId())) {
                return;
            }
            CCoreNetwork.sendToPlayer(player, new DeedQuestOpenPacket(task.deedId(), task.getQuest().id));
        });
    }

    @Override
    public @NotNull Type<DeedQuestRequestPacket> type() {
        return TYPE;
    }
}
