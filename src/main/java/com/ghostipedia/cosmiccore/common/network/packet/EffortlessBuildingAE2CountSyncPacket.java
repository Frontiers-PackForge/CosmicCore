package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingAE2Bridge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public final class EffortlessBuildingAE2CountSyncPacket implements CustomPacketPayload {

    public static final Type<EffortlessBuildingAE2CountSyncPacket> TYPE = new Type<>(
            CosmicCore.id("effortless_building_ae2_count_sync"));
    public static final StreamCodec<FriendlyByteBuf, EffortlessBuildingAE2CountSyncPacket> CODEC = StreamCodec
            .ofMember(EffortlessBuildingAE2CountSyncPacket::encode, EffortlessBuildingAE2CountSyncPacket::new);

    private final Item item;
    private final int count;

    public EffortlessBuildingAE2CountSyncPacket(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    public EffortlessBuildingAE2CountSyncPacket(FriendlyByteBuf buffer) {
        this.item = BuiltInRegistries.ITEM.get(buffer.readResourceLocation());
        this.count = buffer.readVarInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item));
        buffer.writeVarInt(count);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> EffortlessBuildingAE2Bridge.setCachedClientCount(item, count));
    }

    @Override
    public @NotNull Type<EffortlessBuildingAE2CountSyncPacket> type() {
        return TYPE;
    }
}
