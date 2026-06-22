package com.ghostipedia.cosmiccore.common.reflection.network;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class SyncQuakeMovementPacket implements CustomPacketPayload {

    public static final Type<SyncQuakeMovementPacket> TYPE = new Type<>(CosmicCore.id("sync_quake_movement"));
    public static final StreamCodec<FriendlyByteBuf, SyncQuakeMovementPacket> CODEC = StreamCodec
            .ofMember(SyncQuakeMovementPacket::encode, SyncQuakeMovementPacket::new);

    private final boolean hasQuakeMovement;

    public SyncQuakeMovementPacket(boolean hasQuakeMovement) {
        this.hasQuakeMovement = hasQuakeMovement;
    }

    public SyncQuakeMovementPacket(FriendlyByteBuf buffer) {
        this.hasQuakeMovement = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(hasQuakeMovement);
    }

    public void execute(IPayloadContext context) {
        QuakeMovementHandler.setClientHasQuakeMovement(hasQuakeMovement);
    }

    @Override
    public @NotNull Type<SyncQuakeMovementPacket> type() {
        return TYPE;
    }
}
