package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedsAPI;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class DeedPresentationAckPacket implements CustomPacketPayload {

    public static final Type<DeedPresentationAckPacket> TYPE = new Type<>(CosmicCore.id("deed_presentation_ack"));
    public static final StreamCodec<FriendlyByteBuf, DeedPresentationAckPacket> CODEC = StreamCodec
            .ofMember(DeedPresentationAckPacket::encode, DeedPresentationAckPacket::new);

    private final ResourceLocation deedId;

    public DeedPresentationAckPacket(ResourceLocation deedId) {
        this.deedId = deedId;
    }

    public DeedPresentationAckPacket(FriendlyByteBuf buf) {
        this.deedId = buf.readResourceLocation();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(deedId);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                DeedsAPI.acknowledgePresentation(player, deedId);
            }
        });
    }

    @Override
    public @NotNull Type<DeedPresentationAckPacket> type() {
        return TYPE;
    }
}
