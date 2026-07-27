package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.mirror.MirrorScreen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public final class DeedQuestOpenPacket implements CustomPacketPayload {

    public static final Type<DeedQuestOpenPacket> TYPE = new Type<>(CosmicCore.id("deed_quest_open"));
    public static final StreamCodec<FriendlyByteBuf, DeedQuestOpenPacket> CODEC = StreamCodec
            .ofMember(DeedQuestOpenPacket::encode, DeedQuestOpenPacket::new);

    private final ResourceLocation deedId;
    private final long returnQuestId;

    public DeedQuestOpenPacket(ResourceLocation deedId, long returnQuestId) {
        this.deedId = deedId;
        this.returnQuestId = returnQuestId;
    }

    private DeedQuestOpenPacket(FriendlyByteBuf buffer) {
        deedId = buffer.readResourceLocation();
        returnQuestId = buffer.readLong();
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(deedId);
        buffer.writeLong(returnQuestId);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> MirrorScreen.openAutomatic(deedId, returnQuestId));
    }

    @Override
    public @NotNull Type<DeedQuestOpenPacket> type() {
        return TYPE;
    }
}
