package com.ghostipedia.cosmiccore.common.reflection.network;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulShape;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulSuper;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulSuperRegistry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class SoulSuperPacket implements CustomPacketPayload {

    public static final Type<SoulSuperPacket> TYPE = new Type<>(CosmicCore.id("soul_super"));
    public static final StreamCodec<FriendlyByteBuf, SoulSuperPacket> CODEC = StreamCodec
            .ofMember(SoulSuperPacket::encode, SoulSuperPacket::new);

    public SoulSuperPacket() {}

    public SoulSuperPacket(FriendlyByteBuf buffer) {}

    public void encode(FriendlyByteBuf buffer) {}

    public void execute(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        ReflectionCapability.get(player).ifPresent(reflection -> {
            SoulShape shape = reflection.getSoulShape();

            if (!shape.isShaped()) {
                player.displayClientMessage(
                        Component.literal("§7§o*Your soul is unshaped. There is no power to call upon.*"),
                        true);
                return;
            }

            SoulSuper soulSuper = SoulSuperRegistry.get(shape).orElse(null);
            if (soulSuper == null) {
                CosmicCore.LOGGER.warn("No super registered for soul shape: {}", shape.getId());
                return;
            }

            long currentTime = player.level().getGameTime();

            if (reflection.isSuperActive(currentTime)) {
                player.displayClientMessage(
                        Component.literal("§7§o*The power already courses through you.*"),
                        true);
                return;
            }

            if (!reflection.isSuperReady(currentTime, soulSuper.getCooldownTicks())) {
                long remaining = reflection.getSuperCooldownRemaining(currentTime, soulSuper.getCooldownTicks());
                int secondsRemaining = (int) (remaining / 20);
                player.displayClientMessage(
                        Component.literal("§7§o*" + secondsRemaining + "s until power returns.*"),
                        true);
                return;
            }

            if (!soulSuper.canActivate(player)) {
                player.displayClientMessage(
                        Component.literal("§7§o*The conditions are not right.*"),
                        true);
                return;
            }

            soulSuper.activate(player);
            reflection.setSuperCooldownStart(currentTime);
            if (soulSuper.getDurationTicks() > 0) {
                reflection.setSuperActiveUntil(currentTime + soulSuper.getDurationTicks());
            }
        });
    }

    @Override
    public @NotNull Type<SoulSuperPacket> type() {
        return TYPE;
    }
}
