package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.transmission.PowerTowerWireClient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public record PowerTowerSpanPacket(ResourceLocation dimension, UUID id, int tier, List<Vec3> starts, List<Vec3> ends)
        implements CustomPacketPayload {

    public static final Type<PowerTowerSpanPacket> TYPE = new Type<>(CosmicCore.id("power_tower_span"));
    public static final StreamCodec<FriendlyByteBuf, PowerTowerSpanPacket> CODEC = StreamCodec.ofMember(
            PowerTowerSpanPacket::encode, PowerTowerSpanPacket::new);

    public PowerTowerSpanPacket {
        starts = List.copyOf(starts);
        ends = List.copyOf(ends);
        if (starts.size() != ends.size() || starts.size() > 4)
            throw new IllegalArgumentException("Invalid wire packet");
    }

    public PowerTowerSpanPacket(FriendlyByteBuf buffer) {
        this(buffer.readResourceLocation(), buffer.readUUID(), buffer.readVarInt(),
                buffer.readCollection(FriendlyByteBuf.limitValue(java.util.ArrayList::new, 4),
                        FriendlyByteBuf::readVec3),
                buffer.readCollection(FriendlyByteBuf.limitValue(java.util.ArrayList::new, 4),
                        FriendlyByteBuf::readVec3));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(dimension);
        buffer.writeUUID(id);
        buffer.writeVarInt(tier);
        buffer.writeCollection(starts, FriendlyByteBuf::writeVec3);
        buffer.writeCollection(ends, FriendlyByteBuf::writeVec3);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> PowerTowerWireClient.update(this));
    }

    @Override
    public Type<PowerTowerSpanPacket> type() {
        return TYPE;
    }
}
