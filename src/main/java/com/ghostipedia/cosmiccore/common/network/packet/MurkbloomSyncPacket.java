package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.murkbloom.MurkbloomClientState;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class MurkbloomSyncPacket implements CustomPacketPayload {

    public static final Type<MurkbloomSyncPacket> TYPE = new Type<>(CosmicCore.id("murkbloom_sync"));
    public static final StreamCodec<FriendlyByteBuf, MurkbloomSyncPacket> CODEC = StreamCodec
            .ofMember(MurkbloomSyncPacket::encode, MurkbloomSyncPacket::new);

    private final int stir;
    private final float bloomDensity;
    private final float lastLoudYaw;
    private final float flinch;
    private final byte impulseKind;

    public MurkbloomSyncPacket(int stir, float bloomDensity, float lastLoudYaw, float flinch, byte impulseKind) {
        this.stir = stir;
        this.bloomDensity = bloomDensity;
        this.lastLoudYaw = lastLoudYaw;
        this.flinch = flinch;
        this.impulseKind = impulseKind;
    }

    public MurkbloomSyncPacket(FriendlyByteBuf buf) {
        this.stir = buf.readVarInt();
        this.bloomDensity = buf.readFloat();
        this.lastLoudYaw = buf.readFloat();
        this.flinch = buf.readFloat();
        this.impulseKind = buf.readByte();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(stir);
        buf.writeFloat(bloomDensity);
        buf.writeFloat(lastLoudYaw);
        buf.writeFloat(flinch);
        buf.writeByte(impulseKind);
    }

    public void execute(IPayloadContext context) {
        MurkbloomClientState.applySync(stir, bloomDensity, lastLoudYaw);
        if (flinch > 0f) {
            MurkbloomClientState.flinch(flinch);
        }
        if (impulseKind > 0) {
            MurkbloomClientState.recordImpulse(impulseKind);
        }
    }

    @Override
    public @NotNull Type<MurkbloomSyncPacket> type() {
        return TYPE;
    }
}
