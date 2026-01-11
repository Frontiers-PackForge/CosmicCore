package com.ghostipedia.cosmiccore.common.reflection.network;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Syncs Quake movement bargain state from server to client.
 */
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
        // This runs on the client
        QuakeMovementHandler.setClientHasQuakeMovement(hasQuakeMovement);
    }
}
