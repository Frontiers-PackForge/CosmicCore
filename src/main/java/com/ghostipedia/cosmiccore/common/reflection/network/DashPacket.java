package com.ghostipedia.cosmiccore.common.reflection.network;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.CelesteDashHandler;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class DashPacket implements CustomPacketPayload {

    public static final Type<DashPacket> TYPE = new Type<>(CosmicCore.id("dash"));
    public static final StreamCodec<FriendlyByteBuf, DashPacket> CODEC = StreamCodec.ofMember(DashPacket::encode,
            DashPacket::new);

    private final float xRot;
    private final float yRot;
    private final float forwardInput;
    private final float strafeInput;

    public DashPacket(float xRot, float yRot, float forwardInput, float strafeInput) {
        this.xRot = xRot;
        this.yRot = yRot;
        this.forwardInput = forwardInput;
        this.strafeInput = strafeInput;
    }

    public DashPacket(FriendlyByteBuf buffer) {
        this.xRot = buffer.readFloat();
        this.yRot = buffer.readFloat();
        this.forwardInput = buffer.readFloat();
        this.strafeInput = buffer.readFloat();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(xRot);
        buffer.writeFloat(yRot);
        buffer.writeFloat(forwardInput);
        buffer.writeFloat(strafeInput);
    }

    public void execute(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        if (!QuakeMovementHandler.hasQuakeMovement(player)) return;

        CelesteDashHandler.executeDashServer(player, xRot, yRot, forwardInput, strafeInput);
    }

    @Override
    public @NotNull Type<DashPacket> type() {
        return TYPE;
    }
}
