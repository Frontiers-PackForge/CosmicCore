package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.client.CosmicHudGuiOverlay;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SyncOxygenBarPacket  implements CCoreNetwork.INetPacket {
    private final long ticksLeft, maxTicks;
    private final boolean show;

    public SyncOxygenBarPacket(long ticksLeft, long maxTicks, boolean show){
        this.ticksLeft = ticksLeft; this.maxTicks = maxTicks; this.show = show;
    }
    public SyncOxygenBarPacket(FriendlyByteBuf b){
        this.ticksLeft = b.readVarLong();
        this.maxTicks  = b.readVarLong();
        this.show      = b.readBoolean();
    }
    @Override public void encode(FriendlyByteBuf b){
        b.writeVarLong(ticksLeft); b.writeVarLong(maxTicks); b.writeBoolean(show);
    }
    @Override public void execute(NetworkEvent.Context ctx){
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                CosmicHudGuiOverlay.setOxygenBar(ticksLeft, maxTicks, show)
        );
    }
}

