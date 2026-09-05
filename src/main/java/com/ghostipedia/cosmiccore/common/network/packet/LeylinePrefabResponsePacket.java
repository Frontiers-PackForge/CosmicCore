package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.deployment.LeylinePrefabClient;
import com.ghostipedia.cosmiccore.common.deployment.LeylinePrefab;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record LeylinePrefabResponsePacket(UUID id, byte[] payload) implements CustomPacketPayload {

    public static final Type<LeylinePrefabResponsePacket> TYPE = new Type<>(CosmicCore.id("leyline_prefab_response"));
    public static final StreamCodec<FriendlyByteBuf, LeylinePrefabResponsePacket> CODEC = StreamCodec.ofMember(
            LeylinePrefabResponsePacket::encode,
            buffer -> new LeylinePrefabResponsePacket(buffer.readUUID(),
                    buffer.readByteArray(LeylinePrefab.MAX_PAYLOAD_BYTES)));

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(id);
        buffer.writeByteArray(payload);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> LeylinePrefabClient.receive(id, payload));
    }

    @Override
    public Type<LeylinePrefabResponsePacket> type() {
        return TYPE;
    }
}
