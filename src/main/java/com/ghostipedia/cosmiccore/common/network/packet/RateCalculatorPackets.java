package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.rate.RateCalculatorScreen;
import com.ghostipedia.cosmiccore.common.rate.RateCalculatorService;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class RateCalculatorPackets {

    private RateCalculatorPackets() {}

    public record Open(UUID tool, CompoundTag report) implements CustomPacketPayload {

        public static final Type<Open> TYPE = new Type<>(CosmicCore.id("rate_calculator_open"));
        public static final StreamCodec<FriendlyByteBuf, Open> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC,
                Open::tool, ByteBufCodecs.COMPOUND_TAG, Open::report, Open::new);

        public void execute(IPayloadContext context) {
            context.enqueueWork(() -> RateCalculatorScreen.open(tool, report));
        }

        @Override
        public @NotNull Type<Open> type() {
            return TYPE;
        }
    }

    public record Update(UUID tool, CompoundTag report) implements CustomPacketPayload {

        public static final Type<Update> TYPE = new Type<>(CosmicCore.id("rate_calculator_update"));
        public static final StreamCodec<FriendlyByteBuf, Update> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC,
                Update::tool, ByteBufCodecs.COMPOUND_TAG, Update::report, Update::new);

        public void execute(IPayloadContext context) {
            context.enqueueWork(() -> RateCalculatorScreen.update(tool, report));
        }

        @Override
        public @NotNull Type<Update> type() {
            return TYPE;
        }
    }

    public record Clear(UUID tool) implements CustomPacketPayload {

        public static final Type<Clear> TYPE = new Type<>(CosmicCore.id("rate_calculator_clear"));
        public static final StreamCodec<FriendlyByteBuf, Clear> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC,
                Clear::tool, Clear::new);

        public void execute(IPayloadContext context) {
            context.enqueueWork(() -> RateCalculatorScreen.clear(tool));
        }

        @Override
        public @NotNull Type<Clear> type() {
            return TYPE;
        }
    }

    public record Refresh(UUID tool) implements CustomPacketPayload {

        public static final Type<Refresh> TYPE = new Type<>(CosmicCore.id("rate_calculator_refresh"));
        public static final StreamCodec<FriendlyByteBuf, Refresh> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC,
                Refresh::tool, Refresh::new);

        public void execute(IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) RateCalculatorService.refresh(player, tool);
            });
        }

        @Override
        public @NotNull Type<Refresh> type() {
            return TYPE;
        }
    }

    public record Reset(UUID tool) implements CustomPacketPayload {

        public static final Type<Reset> TYPE = new Type<>(CosmicCore.id("rate_calculator_reset"));
        public static final StreamCodec<FriendlyByteBuf, Reset> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC,
                Reset::tool, Reset::new);

        public void execute(IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) RateCalculatorService.reset(player, tool);
            });
        }

        @Override
        public @NotNull Type<Reset> type() {
            return TYPE;
        }
    }

    public record Close(UUID tool) implements CustomPacketPayload {

        public static final Type<Close> TYPE = new Type<>(CosmicCore.id("rate_calculator_close"));
        public static final StreamCodec<FriendlyByteBuf, Close> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC,
                Close::tool, Close::new);

        public void execute(IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) RateCalculatorService.close(player, tool);
            });
        }

        @Override
        public @NotNull Type<Close> type() {
            return TYPE;
        }
    }
}
