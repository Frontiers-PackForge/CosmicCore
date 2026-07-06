package com.ghostipedia.cosmiccore.common.data.worldgen.field;

import com.ghostipedia.cosmiccore.client.map.RevealedField;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedTeams;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.RevealFieldsPacket;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;

public final class FieldDiscoverySharing {

    private FieldDiscoverySharing() {}

    public static void shareWithTeam(ServerPlayer discoverer, ResourceKey<Level> dimension,
                                     List<RevealedField> revealed) {
        FieldDiscoveryData data = FieldDiscoveryData.get(discoverer.getServer());
        String teamKey = DeedTeams.teamKey(discoverer);
        List<RevealedField> newFields = data.addAll(teamKey, dimension.location(), revealed);
        if (newFields.isEmpty()) return;

        List<ServerPlayer> members = DeedTeams.onlineTeamMembers(discoverer);
        if (members.size() <= 1) return;

        RevealFieldsPacket sharePacket = new RevealFieldsPacket(dimension, newFields);
        MutableComponent announcement = Component.translatable("cosmiccore.dowsing.team_share",
                discoverer.getDisplayName(), newFields.size()).withStyle(ChatFormatting.GOLD);
        for (int i = 0; i < newFields.size(); i++) {
            announcement.append(Component.literal(i == 0 ? " " : ", ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(newFields.get(i).displayName()).withStyle(ChatFormatting.AQUA));
        }
        for (ServerPlayer member : members) {
            if (member.getUUID().equals(discoverer.getUUID())) continue;
            CCoreNetwork.sendToPlayer(member, sharePacket);
            member.sendSystemMessage(announcement);
        }
    }
}
