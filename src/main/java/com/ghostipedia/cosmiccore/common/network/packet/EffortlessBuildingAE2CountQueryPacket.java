package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingAE2Bridge;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EffortlessBuildingAE2CountQueryPacket implements CustomPacketPayload {

    public static final Type<EffortlessBuildingAE2CountQueryPacket> TYPE = new Type<>(
            CosmicCore.id("effortless_building_ae2_count_query"));
    public static final StreamCodec<FriendlyByteBuf, EffortlessBuildingAE2CountQueryPacket> CODEC = StreamCodec
            .ofMember(EffortlessBuildingAE2CountQueryPacket::encode, EffortlessBuildingAE2CountQueryPacket::new);
    private static final long MINIMUM_QUERY_INTERVAL_NANOS = 50_000_000L;
    private static final Map<UUID, Long> LAST_QUERIES = new ConcurrentHashMap<>();

    private final Item item;

    public EffortlessBuildingAE2CountQueryPacket(Item item) {
        this.item = item;
    }

    public EffortlessBuildingAE2CountQueryPacket(FriendlyByteBuf buffer) {
        this.item = BuiltInRegistries.ITEM.get(buffer.readResourceLocation());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item));
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            long now = System.nanoTime();
            Long previous = LAST_QUERIES.get(player.getUUID());
            if (!EffortlessBuildingAE2Bridge.isIntervalElapsed(
                    previous, now, MINIMUM_QUERY_INTERVAL_NANOS))
                return;
            LAST_QUERIES.put(player.getUUID(), now);
            int count = EffortlessBuildingAE2Bridge.count(player, item);
            CCoreNetwork.sendToPlayer(player, new EffortlessBuildingAE2CountSyncPacket(item, count));
        });
    }

    public static void clearPlayer(UUID playerId) {
        LAST_QUERIES.remove(playerId);
    }

    @Override
    public @NotNull Type<EffortlessBuildingAE2CountQueryPacket> type() {
        return TYPE;
    }
}
