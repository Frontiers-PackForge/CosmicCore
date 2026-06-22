package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderUplinkManager.DemandSlot;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderUplinkState;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class StarLadderUplinkPackets {

    public enum UplinkAction {
        INITIATE,
        CONFIRM,
        ABORT
    }

    public static class UplinkActionPacket implements CustomPacketPayload {

        public static final Type<UplinkActionPacket> TYPE = new Type<>(CosmicCore.id("uplink_action"));
        public static final StreamCodec<FriendlyByteBuf, UplinkActionPacket> CODEC = StreamCodec
                .ofMember(UplinkActionPacket::encode, UplinkActionPacket::new);

        private final BlockPos machinePos;
        private final UplinkAction action;

        public UplinkActionPacket(BlockPos machinePos, UplinkAction action) {
            this.machinePos = machinePos;
            this.action = action;
        }

        public UplinkActionPacket(FriendlyByteBuf buf) {
            this.machinePos = buf.readBlockPos();
            this.action = buf.readEnum(UplinkAction.class);
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(machinePos);
            buf.writeEnum(action);
        }

        public void execute(IPayloadContext ctx) {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            if (!(player.level() instanceof ServerLevel serverLevel)) return;
            var machine = MetaMachine.getMachine(serverLevel, machinePos);
            if (!(machine instanceof StarLadderMachine starLadder)) return;

            switch (action) {
                case INITIATE -> starLadder.getUplinkManager().initiate(player);
                case CONFIRM -> starLadder.getUplinkManager().confirm(player);
                case ABORT -> starLadder.getUplinkManager().abort(player);
            }
        }

        @Override
        public @NotNull Type<UplinkActionPacket> type() {
            return TYPE;
        }
    }

    public static class CloseScreenPacket implements CustomPacketPayload {

        public static final Type<CloseScreenPacket> TYPE = new Type<>(CosmicCore.id("uplink_close_screen"));
        public static final StreamCodec<FriendlyByteBuf, CloseScreenPacket> CODEC = StreamCodec
                .ofMember(CloseScreenPacket::encode, CloseScreenPacket::new);

        public CloseScreenPacket() {}

        public CloseScreenPacket(FriendlyByteBuf buf) {}

        public void encode(FriendlyByteBuf buf) {}

        public void execute(IPayloadContext ctx) {
            Minecraft.getInstance().setScreen(null);
        }

        @Override
        public @NotNull Type<CloseScreenPacket> type() {
            return TYPE;
        }
    }

    public static class UplinkSyncPacket implements CustomPacketPayload {

        public static final Type<UplinkSyncPacket> TYPE = new Type<>(CosmicCore.id("uplink_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, UplinkSyncPacket> CODEC = StreamCodec
                .ofMember(UplinkSyncPacket::encode, UplinkSyncPacket::new);

        private final StarLadderUplinkState state;
        private final int progress;
        private final int drainRate;
        private final ItemStack bulkItem;
        private final int bulkQtyRemaining;
        private final int bulkTimer;
        private final int bulkTimerMax;
        private final ItemStack complexItem;
        private final int complexQtyRemaining;
        private final int complexTimer;
        private final int complexTimerMax;

        public UplinkSyncPacket(StarLadderUplinkState state, int progress, int drainRate,
                                DemandSlot bulk, DemandSlot complex) {
            this.state = state;
            this.progress = progress;
            this.drainRate = drainRate;
            this.bulkItem = bulk.item;
            this.bulkQtyRemaining = bulk.remaining();
            this.bulkTimer = bulk.timer;
            this.bulkTimerMax = bulk.timerMax;
            this.complexItem = complex.item;
            this.complexQtyRemaining = complex.remaining();
            this.complexTimer = complex.timer;
            this.complexTimerMax = complex.timerMax;
        }

        public UplinkSyncPacket(RegistryFriendlyByteBuf buf) {
            this.state = buf.readEnum(StarLadderUplinkState.class);
            this.progress = buf.readVarInt();
            this.drainRate = buf.readVarInt();
            this.bulkItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            this.bulkQtyRemaining = buf.readVarInt();
            this.bulkTimer = buf.readVarInt();
            this.bulkTimerMax = buf.readVarInt();
            this.complexItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            this.complexQtyRemaining = buf.readVarInt();
            this.complexTimer = buf.readVarInt();
            this.complexTimerMax = buf.readVarInt();
        }

        public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeEnum(state);
            buf.writeVarInt(progress);
            buf.writeVarInt(drainRate);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, bulkItem);
            buf.writeVarInt(bulkQtyRemaining);
            buf.writeVarInt(bulkTimer);
            buf.writeVarInt(bulkTimerMax);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, complexItem);
            buf.writeVarInt(complexQtyRemaining);
            buf.writeVarInt(complexTimer);
            buf.writeVarInt(complexTimerMax);
        }

        public void execute(IPayloadContext ctx) {
            StarLadderUplinkClientState.update(state, progress, drainRate,
                    bulkItem, bulkQtyRemaining, bulkTimer, bulkTimerMax,
                    complexItem, complexQtyRemaining, complexTimer, complexTimerMax);
        }

        @Override
        public @NotNull Type<UplinkSyncPacket> type() {
            return TYPE;
        }
    }

    public enum WhisperStyle {
        REFLECTION,
        OBSERVER,
        AMBIENT
    }

    public static class ObserverWhisperPacket implements CustomPacketPayload {

        public static final Type<ObserverWhisperPacket> TYPE = new Type<>(CosmicCore.id("uplink_observer_whisper"));
        public static final StreamCodec<FriendlyByteBuf, ObserverWhisperPacket> CODEC = StreamCodec
                .ofMember(ObserverWhisperPacket::encode, ObserverWhisperPacket::new);

        private static final Style REFLECTION_STYLE = Style.EMPTY.withItalic(true).withColor(0x9966CC);
        private static final Style OBSERVER_STYLE = Style.EMPTY.withColor(0x88CCFF).withBold(false);
        private static final Style AMBIENT_STYLE = Style.EMPTY.withItalic(true).withColor(0xCC6644);

        private final String text;
        private final WhisperStyle style;

        public ObserverWhisperPacket(String text, WhisperStyle style) {
            this.text = text;
            this.style = style;
        }

        public ObserverWhisperPacket(FriendlyByteBuf buf) {
            this.text = buf.readUtf();
            this.style = buf.readEnum(WhisperStyle.class);
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(text);
            buf.writeEnum(style);
        }

        public void execute(IPayloadContext ctx) {
            var player = Minecraft.getInstance().player;
            if (player == null) return;

            Component translated = Component.translatable(text);
            Component message = switch (style) {
                case REFLECTION -> Component.literal("...").append(translated).withStyle(REFLECTION_STYLE);
                case OBSERVER -> translated.copy().withStyle(OBSERVER_STYLE);
                case AMBIENT -> translated.copy().withStyle(AMBIENT_STYLE);
            };
            player.sendSystemMessage(message);
        }

        @Override
        public @NotNull Type<ObserverWhisperPacket> type() {
            return TYPE;
        }
    }

    public static class ClientDemand {

        public ItemStack item = ItemStack.EMPTY;
        public int qtyRemaining = 0;
        public int timerTicks = 0;
        public int timerMax = 0;

        public boolean isActive() {
            return !item.isEmpty();
        }

        public float getTimerProgress() {
            return timerMax > 0 ? (float) timerTicks / timerMax : 0f;
        }

        public void reset() {
            item = ItemStack.EMPTY;
            qtyRemaining = 0;
            timerTicks = 0;
            timerMax = 0;
        }
    }

    public static class StarLadderUplinkClientState {

        private static StarLadderUplinkState state = StarLadderUplinkState.IDLE;
        private static int progress = 0;
        private static int drainRate = 0;
        private static final ClientDemand bulk = new ClientDemand();
        private static final ClientDemand complex = new ClientDemand();

        public static void update(StarLadderUplinkState newState, int newProgress, int newDrainRate,
                                  ItemStack bulkItem, int bulkQty, int bulkTimer, int bulkTimerMax,
                                  ItemStack complexItem, int complexQty, int complexTimer,
                                  int complexTimerMax) {
            state = newState;
            progress = newProgress;
            drainRate = newDrainRate;
            bulk.item = bulkItem;
            bulk.qtyRemaining = bulkQty;
            bulk.timerTicks = bulkTimer;
            bulk.timerMax = bulkTimerMax;
            complex.item = complexItem;
            complex.qtyRemaining = complexQty;
            complex.timerTicks = complexTimer;
            complex.timerMax = complexTimerMax;
        }

        public static StarLadderUplinkState getState() {
            return state;
        }

        public static int getProgress() {
            return progress;
        }

        public static int getDrainRate() {
            return drainRate;
        }

        public static ClientDemand getBulkDemand() {
            return bulk;
        }

        public static ClientDemand getComplexDemand() {
            return complex;
        }

        public static float getUplinkProgress() {
            return progress / 6000f;
        }

        public static void reset() {
            state = StarLadderUplinkState.IDLE;
            progress = 0;
            drainRate = 0;
            bulk.reset();
            complex.reset();
        }
    }

    public static void sendInitiate(BlockPos machinePos) {
        CCoreNetwork.sendToServer(new UplinkActionPacket(machinePos, UplinkAction.INITIATE));
    }

    public static void sendConfirm(BlockPos machinePos) {
        CCoreNetwork.sendToServer(new UplinkActionPacket(machinePos, UplinkAction.CONFIRM));
    }

    public static void sendAbort(BlockPos machinePos) {
        CCoreNetwork.sendToServer(new UplinkActionPacket(machinePos, UplinkAction.ABORT));
    }

    public static void sendCloseScreen(ServerPlayer player) {
        CCoreNetwork.sendToPlayer(player, new CloseScreenPacket());
    }

    public static void sendUplinkSync(ServerPlayer player, StarLadderUplinkState state, int progress,
                                      int drainRate, DemandSlot bulk, DemandSlot complex) {
        CCoreNetwork.sendToPlayer(player, new UplinkSyncPacket(state, progress, drainRate, bulk, complex));
    }

    public static void sendObserverWhisper(ServerPlayer player, String text, WhisperStyle style) {
        CCoreNetwork.sendToPlayer(player, new ObserverWhisperPacket(text, style));
    }
}
