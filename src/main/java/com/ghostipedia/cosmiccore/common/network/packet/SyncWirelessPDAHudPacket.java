package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.behavior.WirelessPDABehavior;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public record SyncWirelessPDAHudPacket(boolean local, boolean available, BigInteger stored, BigInteger capacity,
                                       long input, long output)
        implements CustomPacketPayload {

    public static final Type<SyncWirelessPDAHudPacket> TYPE = new Type<>(CosmicCore.id("sync_wireless_pda_hud"));
    public static final StreamCodec<FriendlyByteBuf, SyncWirelessPDAHudPacket> CODEC = StreamCodec
            .ofMember(SyncWirelessPDAHudPacket::encode, SyncWirelessPDAHudPacket::new);

    public SyncWirelessPDAHudPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean(), new BigInteger(buf.readByteArray()),
                new BigInteger(buf.readByteArray()), buf.readVarLong(), buf.readVarLong());
    }

    public static SyncWirelessPDAHudPacket unavailable(boolean local) {
        return new SyncWirelessPDAHudPacket(local, false, BigInteger.ZERO, BigInteger.ZERO, 0, 0);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(local);
        buf.writeBoolean(available);
        buf.writeByteArray(stored.toByteArray());
        buf.writeByteArray(capacity.toByteArray());
        buf.writeVarLong(input);
        buf.writeVarLong(output);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> WirelessPDABehavior.setClientData(local, available, stored, capacity, input, output));
    }

    @Override
    public @NotNull Type<SyncWirelessPDAHudPacket> type() {
        return TYPE;
    }
}
