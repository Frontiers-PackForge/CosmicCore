package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.deployment.LeylineDeploymentClient;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record LeylineDeploymentStartPacket(UUID id, ResourceLocation dimension, ResourceLocation blueprint,
                                           BlockPos anchor, Direction facing, long startTick, double openingY)
        implements CustomPacketPayload {

    public static final Type<LeylineDeploymentStartPacket> TYPE = new Type<>(CosmicCore.id("leyline_start"));
    public static final StreamCodec<FriendlyByteBuf, LeylineDeploymentStartPacket> CODEC = StreamCodec.ofMember(
            LeylineDeploymentStartPacket::encode, LeylineDeploymentStartPacket::new);

    public LeylineDeploymentStartPacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readResourceLocation(), buffer.readResourceLocation(), buffer.readBlockPos(),
                buffer.readEnum(Direction.class), buffer.readVarLong(), buffer.readDouble());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(id);
        buffer.writeResourceLocation(dimension);
        buffer.writeResourceLocation(blueprint);
        buffer.writeBlockPos(anchor);
        buffer.writeEnum(facing);
        buffer.writeVarLong(startTick);
        buffer.writeDouble(openingY);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> LeylineDeploymentClient.start(this));
    }

    @Override
    public Type<LeylineDeploymentStartPacket> type() {
        return TYPE;
    }
}
