package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.murkbloom.MurkbloomServerLogic;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public final class MurkbloomDevImmunityPacket implements CustomPacketPayload {

    public static final Type<MurkbloomDevImmunityPacket> TYPE = new Type<>(CosmicCore.id("murkbloom_dev_immunity"));
    public static final StreamCodec<FriendlyByteBuf, MurkbloomDevImmunityPacket> CODEC = StreamCodec
            .ofMember(MurkbloomDevImmunityPacket::encode, MurkbloomDevImmunityPacket::new);

    public MurkbloomDevImmunityPacket() {}

    public MurkbloomDevImmunityPacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.getServer() == null) return;
            boolean allowed = player.hasPermissions(2) ||
                    player.getServer().isSingleplayerOwner(player.getGameProfile());
            if (!allowed) {
                player.displayClientMessage(Component.translatable("cosmiccore.dev.murkbloom.immunity.denied"), true);
                CosmicCore.LOGGER.warn("Rejected Murkbloom dev immunity toggle from {}", player.getScoreboardName());
                return;
            }
            boolean enabled = MurkbloomServerLogic.toggleDevImmunity(player);
            player.displayClientMessage(Component.translatable(enabled ?
                    "cosmiccore.dev.murkbloom.immunity.enabled" :
                    "cosmiccore.dev.murkbloom.immunity.disabled"), true);
        });
    }

    @Override
    public @NotNull Type<MurkbloomDevImmunityPacket> type() {
        return TYPE;
    }
}
