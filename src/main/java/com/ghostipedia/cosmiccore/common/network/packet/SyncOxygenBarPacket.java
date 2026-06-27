package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.CosmicHudGuiOverlay;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class SyncOxygenBarPacket implements CustomPacketPayload {

    public static final Type<SyncOxygenBarPacket> TYPE = new Type<>(CosmicCore.id("sync_oxygen_bar"));
    public static final StreamCodec<FriendlyByteBuf, SyncOxygenBarPacket> CODEC = StreamCodec
            .ofMember(SyncOxygenBarPacket::encode, SyncOxygenBarPacket::new);

    private final long left;
    private final long max;
    private final boolean show;
    private final double ratePerSecond;

    public SyncOxygenBarPacket(long left, long max, boolean show, double ratePerSecond) {
        this.left = left;
        this.max = max;
        this.show = show;
        this.ratePerSecond = ratePerSecond;
    }

    public SyncOxygenBarPacket(FriendlyByteBuf buf) {
        this.left = buf.readVarLong();
        this.max = buf.readVarLong();
        this.show = buf.readBoolean();
        this.ratePerSecond = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarLong(left);
        buf.writeVarLong(max);
        buf.writeBoolean(show);
        buf.writeDouble(ratePerSecond);
    }

    public void execute(IPayloadContext context) {
        CosmicHudGuiOverlay.setOxygenBar(left, max, show, ratePerSecond);
    }

    @Override
    public @NotNull Type<SyncOxygenBarPacket> type() {
        return TYPE;
    }
}
