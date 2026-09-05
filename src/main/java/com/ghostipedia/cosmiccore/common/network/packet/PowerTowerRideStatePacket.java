package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.transmission.PowerTowerRideClient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record PowerTowerRideStatePacket(UUID player, boolean active, Vec3 start, Vec3 end, double progress)
        implements CustomPacketPayload {

    public static final Type<PowerTowerRideStatePacket> TYPE = new Type<>(CosmicCore.id("power_tower_ride_state"));
    public static final StreamCodec<FriendlyByteBuf, PowerTowerRideStatePacket> CODEC = StreamCodec.ofMember(
            PowerTowerRideStatePacket::encode, PowerTowerRideStatePacket::new);

    public PowerTowerRideStatePacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readBoolean(), buffer.readVec3(), buffer.readVec3(), buffer.readDouble());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(player);
        buffer.writeBoolean(active);
        buffer.writeVec3(start);
        buffer.writeVec3(end);
        buffer.writeDouble(progress);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> PowerTowerRideClient.update(this));
    }

    @Override
    public Type<PowerTowerRideStatePacket> type() {
        return TYPE;
    }
}
