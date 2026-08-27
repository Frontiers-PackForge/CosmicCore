package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimit;
import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimitSupport;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import org.jetbrains.annotations.NotNull;

public record FactoryGaugePromiseLimitPacket(FactoryPanelPosition panel, int limit)
        implements CustomPacketPayload {

    public static final Type<FactoryGaugePromiseLimitPacket> TYPE = new Type<>(CosmicCore.id("factory_gauge_limit"));
    public static final StreamCodec<FriendlyByteBuf, FactoryGaugePromiseLimitPacket> CODEC = StreamCodec.ofMember(
            FactoryGaugePromiseLimitPacket::encode, FactoryGaugePromiseLimitPacket::new);

    public FactoryGaugePromiseLimitPacket(FriendlyByteBuf buffer) {
        this(FactoryPanelPosition.STREAM_CODEC.decode(buffer), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        FactoryPanelPosition.STREAM_CODEC.encode(buffer, panel);
        buffer.writeVarInt(limit);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
            if (!player.level().isLoaded(panel.pos()) || player.distanceToSqr(panel.pos().getCenter()) > 64.0) return;
            if (!(player.level().getBlockEntity(panel.pos()) instanceof FactoryPanelBlockEntity blockEntity)) return;
            FactoryPanelBehaviour behaviour = blockEntity.panels.get(panel.slot());
            if (behaviour == null || !behaviour.active || !FactoryGaugePromiseLimitSupport.supports(behaviour) ||
                    !(behaviour instanceof FactoryGaugePromiseLimit promiseLimit)) {
                return;
            }
            if (!Create.LOGISTICS.mayInteract(behaviour.network, player)) return;
            int normalized = FactoryGaugePromiseLimitSupport.normalize(behaviour, limit);
            if (promiseLimit.cosmiccore$getPromiseLimit() == normalized) return;
            promiseLimit.cosmiccore$setPromiseLimit(normalized);
            blockEntity.notifyUpdate();
        });
    }

    @Override
    public @NotNull Type<FactoryGaugePromiseLimitPacket> type() {
        return TYPE;
    }
}
