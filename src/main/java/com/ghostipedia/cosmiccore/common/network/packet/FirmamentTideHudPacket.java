package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.firmament.FirmamentTideHudOverlay;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public final class FirmamentTideHudPacket implements CustomPacketPayload {

    public static final Type<FirmamentTideHudPacket> TYPE = new Type<>(CosmicCore.id("firmament_tide_hud"));
    public static final StreamCodec<FriendlyByteBuf, FirmamentTideHudPacket> CODEC = StreamCodec
            .ofMember(FirmamentTideHudPacket::encode, FirmamentTideHudPacket::new);

    public static final byte HIDDEN = 0;
    public static final byte PROMPT = 1;
    public static final byte RETURNING = 2;
    public static final byte ARRIVED = 3;
    public static final byte ASCENDING = 4;
    public static final byte ENTERED = 5;

    private final byte mode;
    private final float progress;

    public FirmamentTideHudPacket(byte mode, float progress) {
        this.mode = mode;
        this.progress = progress;
    }

    private FirmamentTideHudPacket(FriendlyByteBuf buffer) {
        mode = buffer.readByte();
        progress = buffer.readFloat();
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeByte(mode);
        buffer.writeFloat(progress);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> FirmamentTideHudOverlay.setState(mode, progress));
    }

    @Override
    public @NotNull Type<FirmamentTideHudPacket> type() {
        return TYPE;
    }
}
