package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.mirror.ClientDeedCache;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedLedger;

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
    private final List<DeedLedger.Presentation> presentations;

    public DeedSyncPacket(List<ResourceLocation> woven, List<ResourceLocation> pending,
                          List<DeedLedger.Presentation> presentations) {
        this.woven = woven;
        this.pending = pending;
        this.presentations = presentations;
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
        int presentationCount = buf.readVarInt();
        this.presentations = new ArrayList<>(presentationCount);
        for (int i = 0; i < presentationCount; i++) {
            this.presentations.add(new DeedLedger.Presentation(buf.readResourceLocation(), buf.readBoolean()));
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
        buf.writeVarInt(presentations.size());
        for (DeedLedger.Presentation presentation : presentations) {
            buf.writeResourceLocation(presentation.deedId());
            buf.writeBoolean(presentation.forced());
        }
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> ClientDeedCache.applySync(woven, pending, presentations));
    }

    @Override
    public @NotNull Type<DeedSyncPacket> type() {
        return TYPE;
    }
}
