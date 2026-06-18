package com.ghostipedia.cosmiccore.common.reflection.network;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SyncQuakeMovementPacket implements CCoreNetwork.INetPacket {

    private final boolean hasQuakeMovement;

    public SyncQuakeMovementPacket(boolean hasQuakeMovement) {
        this.hasQuakeMovement = hasQuakeMovement;
    }

    public SyncQuakeMovementPacket(FriendlyByteBuf buffer) {
        this.hasQuakeMovement = buffer.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(hasQuakeMovement);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            QuakeMovementHandler.setClientHasQuakeMovement(hasQuakeMovement);
        });
    }
}
