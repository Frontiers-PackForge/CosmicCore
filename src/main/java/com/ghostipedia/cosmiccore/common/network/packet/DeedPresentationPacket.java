package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.mirror.ClientDeedCache;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class DeedPresentationPacket implements CustomPacketPayload {

    public static final Type<DeedPresentationPacket> TYPE = new Type<>(CosmicCore.id("deed_presentation"));
    public static final StreamCodec<FriendlyByteBuf, DeedPresentationPacket> CODEC = StreamCodec
            .ofMember(DeedPresentationPacket::encode, DeedPresentationPacket::new);

    private final ResourceLocation deedId;
    private final boolean live;

    public DeedPresentationPacket(ResourceLocation deedId, boolean live) {
        this.deedId = deedId;
        this.live = live;
    }

    public DeedPresentationPacket(FriendlyByteBuf buf) {
        this.deedId = buf.readResourceLocation();
        this.live = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(deedId);
        buf.writeBoolean(live);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (live) ClientDeedCache.markLive(deedId);
        });
    }

    @Override
    public @NotNull Type<DeedPresentationPacket> type() {
        return TYPE;
    }
}
