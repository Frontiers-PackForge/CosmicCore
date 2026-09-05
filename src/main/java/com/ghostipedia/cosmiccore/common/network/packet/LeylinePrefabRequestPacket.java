package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.deployment.LeylineFabricationLibrary;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public record LeylinePrefabRequestPacket(UUID id) implements CustomPacketPayload {

    public static final Type<LeylinePrefabRequestPacket> TYPE = new Type<>(CosmicCore.id("leyline_prefab_request"));
    public static final StreamCodec<FriendlyByteBuf, LeylinePrefabRequestPacket> CODEC = StreamCodec.ofMember(
            (packet, buffer) -> buffer.writeUUID(packet.id),
            buffer -> new LeylinePrefabRequestPacket(buffer.readUUID()));
    private static final Map<ServerPlayer, Long> LAST_REQUEST = new WeakHashMap<>();

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            long tick = player.server.getTickCount();
            Long previous = LAST_REQUEST.get(player);
            if (previous != null && tick - previous < 5) return;
            LAST_REQUEST.put(player, tick);
            var prefab = LeylineFabricationLibrary.get(player.serverLevel()).find(id);
            CCoreNetwork.sendToPlayer(player, new LeylinePrefabResponsePacket(id,
                    prefab == null ? new byte[0] : prefab.payload()));
        });
    }

    @Override
    public Type<LeylinePrefabRequestPacket> type() {
        return TYPE;
    }
}
