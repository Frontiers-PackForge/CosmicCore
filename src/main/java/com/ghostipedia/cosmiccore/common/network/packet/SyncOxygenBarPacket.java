package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.client.CosmicHudGuiOverlay;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SyncOxygenBarPacket implements CCoreNetwork.INetPacket {
    private final long left, max;
    private final boolean show;
    private final double ratePerSecond; // signed ticks/sec; negative = draining

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

    @Override public void encode(FriendlyByteBuf buf) {
        buf.writeVarLong(left);
        buf.writeVarLong(max);
        buf.writeBoolean(show);
        buf.writeDouble(ratePerSecond);
    }

    @Override public void execute(NetworkEvent.Context ctx) {
        net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () ->
                com.ghostipedia.cosmiccore.client.CosmicHudGuiOverlay
                        .setOxygenBar(left, max, show, ratePerSecond)
        );
    }
}

