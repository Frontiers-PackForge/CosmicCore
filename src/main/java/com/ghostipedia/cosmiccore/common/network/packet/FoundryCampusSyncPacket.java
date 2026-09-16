package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.foundry.FoundryCampusClientState;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryCampusSnapshot;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.Nullable;

public record FoundryCampusSyncPacket(GlobalPos core, @Nullable FoundryCampusSnapshot snapshot)
        implements CustomPacketPayload {

    public static final Type<FoundryCampusSyncPacket> TYPE = new Type<>(CosmicCore.id("foundry_campus_sync"));
    public static final StreamCodec<FriendlyByteBuf, FoundryCampusSyncPacket> CODEC = StreamCodec.ofMember(
            FoundryCampusSyncPacket::write, FoundryCampusSyncPacket::read);

    private void write(FriendlyByteBuf buffer) {
        buffer.writeResourceKey(core.dimension());
        buffer.writeBlockPos(core.pos());
        buffer.writeBoolean(snapshot != null);
        if (snapshot != null) snapshot.write(buffer);
    }

    private static FoundryCampusSyncPacket read(FriendlyByteBuf buffer) {
        GlobalPos core = GlobalPos.of(buffer.readResourceKey(net.minecraft.core.registries.Registries.DIMENSION),
                buffer.readBlockPos());
        FoundryCampusSnapshot snapshot = buffer.readBoolean() ? FoundryCampusSnapshot.read(buffer) : null;
        if (snapshot != null && !snapshot.core().equals(core)) {
            throw new IllegalArgumentException("Foundry campus sync core mismatch");
        }
        return new FoundryCampusSyncPacket(core, snapshot);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (snapshot == null) FoundryCampusClientState.remove(core);
            else FoundryCampusClientState.apply(snapshot);
        });
    }

    @Override
    public Type<FoundryCampusSyncPacket> type() {
        return TYPE;
    }
}
