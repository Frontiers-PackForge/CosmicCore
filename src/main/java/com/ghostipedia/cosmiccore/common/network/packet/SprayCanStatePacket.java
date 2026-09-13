package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.behavior.ExtendedDyeColor;
import com.ghostipedia.cosmiccore.common.item.behavior.InfiniteSprayCanBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.SprayCanEventListener;
import com.ghostipedia.cosmiccore.common.item.behavior.SprayCanState;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public record SprayCanStatePacket(InteractionHand hand, Action action, int value) implements CustomPacketPayload {

    public static final Type<SprayCanStatePacket> TYPE = new Type<>(CosmicCore.id("spray_can_state"));
    public static final StreamCodec<FriendlyByteBuf, SprayCanStatePacket> CODEC = StreamCodec
            .ofMember(SprayCanStatePacket::encode, SprayCanStatePacket::new);

    private SprayCanStatePacket(FriendlyByteBuf buffer) {
        this(buffer.readEnum(InteractionHand.class), buffer.readEnum(Action.class), buffer.readVarInt());
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeEnum(action);
        buffer.writeVarInt(value);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
            ItemStack stack = player.getItemInHand(hand);
            if (!SprayCanEventListener.isSprayCan(stack)) return;
            SprayCanState before = SprayCanState.read(stack);
            SprayCanState after = apply(before, action, value);
            if (after.equals(before)) {
                if (before.locked() && action != Action.TOGGLE_LOCK && action != Action.SET_MODE) {
                    player.displayClientMessage(InfiniteSprayCanBehavior.lockedMessage(), true);
                }
                return;
            }
            after.write(stack);
            if (action == Action.SET_MODE) SprayCanState.clearSelection(stack);
            if (action == Action.TOGGLE_LOCK) {
                player.displayClientMessage(InfiniteSprayCanBehavior.lockMessage(after.locked()), true);
            } else if (action == Action.CYCLE || action == Action.SET_COLOR) {
                InfiniteSprayCanBehavior.printColorToActionBar(player, after.color());
            }
        });
    }

    public static SprayCanState apply(SprayCanState state, Action action, int value) {
        return switch (action) {
            case CYCLE -> state.locked() || value == 0 ? state : state.cycle(value);
            case SET_COLOR -> state.locked() || value < 0 || value >= ExtendedDyeColor.values().length ? state :
                    state.withColor(ExtendedDyeColor.values()[value]);
            case SET_RAIN_SEALANT -> state.locked() || value != 1 ? state : state.withRainSealant(true);
            case TOGGLE_LOCK -> state.withLocked(!state.locked());
            case SET_MODE -> value < 0 || value >= SprayCanState.SprayMode.values().length ? state :
                    state.withMode(SprayCanState.SprayMode.values()[value]);
        };
    }

    @Override
    public @NotNull Type<SprayCanStatePacket> type() {
        return TYPE;
    }

    public enum Action {
        CYCLE,
        SET_COLOR,
        TOGGLE_LOCK,
        SET_MODE,
        SET_RAIN_SEALANT
    }
}
