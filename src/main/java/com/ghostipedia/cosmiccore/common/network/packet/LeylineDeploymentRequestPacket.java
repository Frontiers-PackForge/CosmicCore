package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentService;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LeylineDeploymentRequestPacket(InteractionHand hand, BlockHitResult hit) implements CustomPacketPayload {

    public static final Type<LeylineDeploymentRequestPacket> TYPE = new Type<>(CosmicCore.id("leyline_deploy"));
    public static final StreamCodec<FriendlyByteBuf, LeylineDeploymentRequestPacket> CODEC = StreamCodec.ofMember(
            LeylineDeploymentRequestPacket::encode, LeylineDeploymentRequestPacket::new);

    public LeylineDeploymentRequestPacket(FriendlyByteBuf buffer) {
        this(buffer.readEnum(InteractionHand.class), buffer.readBlockHitResult());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeBlockHitResult(hit);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) LeylineDeploymentService.begin(player, hand, hit);
        });
    }

    @Override
    public Type<LeylineDeploymentRequestPacket> type() {
        return TYPE;
    }
}
