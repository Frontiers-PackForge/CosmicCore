package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.transmission.PowerTowerWireRide;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PowerTowerRideRequestPacket(Action action, InteractionHand hand) implements CustomPacketPayload {

    public enum Action {
        GRAB,
        DROP,
        JUMP
    }

    public static final Type<PowerTowerRideRequestPacket> TYPE = new Type<>(CosmicCore.id("power_tower_ride_request"));
    public static final StreamCodec<FriendlyByteBuf, PowerTowerRideRequestPacket> CODEC = StreamCodec.ofMember(
            PowerTowerRideRequestPacket::encode, PowerTowerRideRequestPacket::new);

    public PowerTowerRideRequestPacket(FriendlyByteBuf buffer) {
        this(buffer.readEnum(Action.class), buffer.readEnum(InteractionHand.class));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(action);
        buffer.writeEnum(hand);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) PowerTowerWireRide.request(player, action, hand);
        });
    }

    @Override
    public Type<PowerTowerRideRequestPacket> type() {
        return TYPE;
    }
}
