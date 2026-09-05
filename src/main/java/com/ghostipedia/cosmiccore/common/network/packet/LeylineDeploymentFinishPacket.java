package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.deployment.LeylineDeploymentClient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record LeylineDeploymentFinishPacket(UUID id, boolean committed) implements CustomPacketPayload {

    public static final Type<LeylineDeploymentFinishPacket> TYPE = new Type<>(CosmicCore.id("leyline_finish"));
    public static final StreamCodec<FriendlyByteBuf, LeylineDeploymentFinishPacket> CODEC = StreamCodec.ofMember(
            LeylineDeploymentFinishPacket::encode, LeylineDeploymentFinishPacket::new);

    public LeylineDeploymentFinishPacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(id);
        buffer.writeBoolean(committed);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> LeylineDeploymentClient.finish(this));
    }

    @Override
    public Type<LeylineDeploymentFinishPacket> type() {
        return TYPE;
    }
}
