package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.client.CosmicHudGuiOverlay;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SyncTimeBarPacket implements CCoreNetwork.INetPacket {

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

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(dimension);
        buffer.writeVarLong(ticksLeft);
        buffer.writeVarLong(maxTicks);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> CosmicHudGuiOverlay.setTimeBar(dimension, ticksLeft, maxTicks));
    }
}
