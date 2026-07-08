package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.ArsSealClient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class SyncAbyssAttunementPacket implements CustomPacketPayload {

    public static final Type<SyncAbyssAttunementPacket> TYPE = new Type<>(CosmicCore.id("sync_abyss_attunement"));
    public static final StreamCodec<FriendlyByteBuf, SyncAbyssAttunementPacket> CODEC = StreamCodec
            .ofMember(SyncAbyssAttunementPacket::encode, SyncAbyssAttunementPacket::new);

    private final boolean attuned;

    public SyncAbyssAttunementPacket(boolean attuned) {
        this.attuned = attuned;
    }

    public SyncAbyssAttunementPacket(FriendlyByteBuf buf) {
        this.attuned = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(attuned);
    }

    public void execute(IPayloadContext context) {
        ArsSealClient.setAttuned(attuned);
    }

    @Override
    public @NotNull Type<SyncAbyssAttunementPacket> type() {
        return TYPE;
    }
}
