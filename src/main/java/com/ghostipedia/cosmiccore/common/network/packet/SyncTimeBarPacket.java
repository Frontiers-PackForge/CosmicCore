package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.CosmicHudGuiOverlay;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class SyncTimeBarPacket implements CustomPacketPayload {

    public static final Type<SyncTimeBarPacket> TYPE = new Type<>(CosmicCore.id("sync_time_bar"));
    public static final StreamCodec<FriendlyByteBuf, SyncTimeBarPacket> CODEC =
            StreamCodec.ofMember(SyncTimeBarPacket::encode, SyncTimeBarPacket::new);

    private final ResourceLocation dimension;
    private final long ticksLeft;
    private final long maxTicks;

    public SyncTimeBarPacket(ResourceLocation dimension, long ticksLeft, long maxTicks) {
        this.dimension = dimension;
        this.ticksLeft = ticksLeft;
        this.maxTicks = maxTicks;
    }

    public SyncTimeBarPacket(FriendlyByteBuf buffer) {
        this.dimension = buffer.readResourceLocation();
        this.ticksLeft = buffer.readVarLong();
        this.maxTicks = buffer.readVarLong();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(dimension);
        buffer.writeVarLong(ticksLeft);
        buffer.writeVarLong(maxTicks);
    }

    public void execute(IPayloadContext context) {
        CosmicHudGuiOverlay.setTimeBar(dimension, ticksLeft, maxTicks);
    }

    @Override
    public @NotNull Type<SyncTimeBarPacket> type() {
        return TYPE;
    }
}
