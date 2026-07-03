package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.mirror.ClientDeedCache;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DeedSyncPacket implements CustomPacketPayload {

    public static final Type<DeedSyncPacket> TYPE = new Type<>(CosmicCore.id("deed_sync"));

    public static final StreamCodec<FriendlyByteBuf, DeedSyncPacket> CODEC = StreamCodec
            .ofMember(DeedSyncPacket::encode, DeedSyncPacket::new);

    private final List<ResourceLocation> woven;
    private final List<ResourceLocation> pending;

    public DeedSyncPacket(List<ResourceLocation> woven, List<ResourceLocation> pending) {
        this.woven = woven;
        this.pending = pending;
    }

    public DeedSyncPacket(FriendlyByteBuf buf) {
        int wovenCount = buf.readVarInt();
        this.woven = new ArrayList<>(wovenCount);
        for (int i = 0; i < wovenCount; i++) {
            this.woven.add(buf.readResourceLocation());
        }
        int pendingCount = buf.readVarInt();
        this.pending = new ArrayList<>(pendingCount);
        for (int i = 0; i < pendingCount; i++) {
            this.pending.add(buf.readResourceLocation());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(woven.size());
        for (ResourceLocation id : woven) {
            buf.writeResourceLocation(id);
        }
        buf.writeVarInt(pending.size());
        for (ResourceLocation id : pending) {
            buf.writeResourceLocation(id);
        }
    }

    public void execute(IPayloadContext context) {
        ClientDeedCache.applySync(woven, pending);
    }

    @Override
    public @NotNull Type<DeedSyncPacket> type() {
        return TYPE;
    }
}
