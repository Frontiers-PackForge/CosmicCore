package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class OxygenWarnPacket implements CustomPacketPayload {

    public static final Type<OxygenWarnPacket> TYPE = new Type<>(CosmicCore.id("oxygen_warn"));
    public static final StreamCodec<FriendlyByteBuf, OxygenWarnPacket> CODEC = StreamCodec
            .ofMember(OxygenWarnPacket::encode, OxygenWarnPacket::new);

    private final String message;
    private final int seconds;

    public OxygenWarnPacket(String message, int seconds) {
        this.message = message;
        this.seconds = seconds;
    }

    public OxygenWarnPacket(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
        this.seconds = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(message);
        buffer.writeVarInt(seconds);
    }

    public void execute(IPayloadContext context) {
        Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(message, seconds), false);
    }

    @Override
    public @NotNull Type<OxygenWarnPacket> type() {
        return TYPE;
    }
}
