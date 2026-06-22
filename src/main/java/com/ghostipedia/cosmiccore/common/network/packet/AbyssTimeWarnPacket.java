package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class AbyssTimeWarnPacket implements CustomPacketPayload {

    public static final Type<AbyssTimeWarnPacket> TYPE = new Type<>(CosmicCore.id("abyss_time_warn"));
    public static final StreamCodec<FriendlyByteBuf, AbyssTimeWarnPacket> CODEC = StreamCodec
            .ofMember(AbyssTimeWarnPacket::encode, AbyssTimeWarnPacket::new);

    private final String message;

    public AbyssTimeWarnPacket(Component message) {
        this.message = message.getString();
    }

    public AbyssTimeWarnPacket(FriendlyByteBuf buffer) {
        this.message = buffer.readUtf();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(message);
    }

    public void execute(IPayloadContext context) {
        Minecraft.getInstance().gui.setOverlayMessage(Component.literal(message), false);
    }

    @Override
    public @NotNull Type<AbyssTimeWarnPacket> type() {
        return TYPE;
    }
}
