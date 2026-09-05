package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.transmission.PowerTowerChainClient;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerNode;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerRole;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.Nullable;

public record PowerTowerChainPacket(ResourceLocation dimension, @Nullable PowerTowerNode node)
        implements CustomPacketPayload {

    public static final Type<PowerTowerChainPacket> TYPE = new Type<>(CosmicCore.id("power_tower_chain"));
    public static final StreamCodec<FriendlyByteBuf, PowerTowerChainPacket> CODEC = StreamCodec.ofMember(
            PowerTowerChainPacket::encode, PowerTowerChainPacket::new);

    public PowerTowerChainPacket(FriendlyByteBuf buffer) {
        this(buffer.readResourceLocation(), buffer.readBoolean() ? new PowerTowerNode(buffer.readUUID(),
                buffer.readBlockPos(), buffer.readVec3(), PowerTowerRole.DUMMY, null, -1, true,
                buffer.readCollection(FriendlyByteBuf.limitValue(java.util.ArrayList::new, 4),
                        FriendlyByteBuf::readVec3)) :
                null);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(dimension);
        buffer.writeBoolean(node != null);
        if (node == null) return;
        buffer.writeUUID(node.id());
        buffer.writeBlockPos(node.controllerPos());
        buffer.writeVec3(node.wireAttachmentCenter());
        buffer.writeCollection(node.attachmentPoints(), FriendlyByteBuf::writeVec3);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> PowerTowerChainClient.update(this));
    }

    @Override
    public Type<PowerTowerChainPacket> type() {
        return TYPE;
    }
}
