package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimitSupport;
import com.ghostipedia.cosmiccore.common.compat.create.FluidGaugeSetItemMenuExtension;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemMenu;
import org.jetbrains.annotations.NotNull;

public record FactoryGaugeFluidSelectionPacket(int containerId, FluidStack fluid) implements CustomPacketPayload {

    public static final Type<FactoryGaugeFluidSelectionPacket> TYPE = new Type<>(CosmicCore.id("factory_gauge_fluid"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FactoryGaugeFluidSelectionPacket> CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.VAR_INT,
                    FactoryGaugeFluidSelectionPacket::containerId,
                    FluidStack.OPTIONAL_STREAM_CODEC,
                    FactoryGaugeFluidSelectionPacket::fluid,
                    FactoryGaugeFluidSelectionPacket::new);

    public FactoryGaugeFluidSelectionPacket {
        fluid = fluid.isEmpty() ? FluidStack.EMPTY : fluid.copyWithAmount(1000);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator() ||
                    player.containerMenu.containerId != containerId ||
                    !(player.containerMenu instanceof FactoryPanelSetItemMenu menu) ||
                    !(menu instanceof FluidGaugeSetItemMenuExtension extension) ||
                    !extension.cosmiccore$isFluidGauge()) {
                return;
            }
            FactoryPanelBehaviour behaviour = menu.contentHolder;
            if (!FactoryGaugePromiseLimitSupport.isFluid(behaviour) ||
                    player.distanceToSqr(behaviour.getPos().getCenter()) > 64.0 ||
                    !Create.LOGISTICS.mayInteract(behaviour.network, player)) {
                return;
            }
            extension.cosmiccore$setFluid(fluid);
        });
    }

    @Override
    public @NotNull Type<FactoryGaugeFluidSelectionPacket> type() {
        return TYPE;
    }
}
