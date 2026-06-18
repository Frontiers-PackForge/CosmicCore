package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderUplinkManager.DemandSlot;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.StarLadderUplinkState;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class StarLadderUplinkPackets {

    public static void register() {
        CCoreNetwork.register(UplinkActionPacket.class, UplinkActionPacket::new, NetworkDirection.PLAY_TO_SERVER);
        CCoreNetwork.register(CloseScreenPacket.class, CloseScreenPacket::new, NetworkDirection.PLAY_TO_CLIENT);
        CCoreNetwork.register(UplinkSyncPacket.class, UplinkSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT);
        CCoreNetwork.register(ObserverWhisperPacket.class, ObserverWhisperPacket::new, NetworkDirection.PLAY_TO_CLIENT);
    }

    // ---- Client → Server: Player actions ----

    public enum UplinkAction {
        INITIATE,
        CONFIRM,
        ABORT
    }

    public static class UplinkActionPacket implements CCoreNetwork.INetPacket {

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

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(machinePos);
            buf.writeEnum(action);
        }

        @Override
        public void execute(NetworkEvent.Context ctx) {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            if (!(player.level() instanceof ServerLevel serverLevel)) return;
            var machine = MetaMachine.getMachine(serverLevel, machinePos);
            if (!(machine instanceof StarLadderMachine starLadder)) return;

            switch (action) {
                case INITIATE -> starLadder.getUplinkManager().initiate(player);
                case CONFIRM -> starLadder.getUplinkManager().confirm(player);
                case ABORT -> starLadder.getUplinkManager().abort(player);
            }
        }
    }

    // ---- Server → Client: Force close the Star Ladder screen ----

    public static class CloseScreenPacket implements CCoreNetwork.INetPacket {

        public CloseScreenPacket() {}

        public CloseScreenPacket(FriendlyByteBuf buf) {}

        @Override
        public void encode(FriendlyByteBuf buf) {}

        @Override
        public void execute(NetworkEvent.Context ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().setScreen(null);
            });
        }
    }

    // ---- Server → Client: Sync uplink fight state ----

    public static class UplinkSyncPacket implements CCoreNetwork.INetPacket {

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

        public UplinkSyncPacket(FriendlyByteBuf buf) {
            this.state = buf.readEnum(StarLadderUplinkState.class);
            this.progress = buf.readVarInt();
            this.drainRate = buf.readVarInt();
            this.bulkItem = buf.readItem();
            this.bulkQtyRemaining = buf.readVarInt();
            this.bulkTimer = buf.readVarInt();
            this.bulkTimerMax = buf.readVarInt();
            this.complexItem = buf.readItem();
            this.complexQtyRemaining = buf.readVarInt();
            this.complexTimer = buf.readVarInt();
            this.complexTimerMax = buf.readVarInt();
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeEnum(state);
            buf.writeVarInt(progress);
            buf.writeVarInt(drainRate);
            buf.writeItemStack(bulkItem, false);
            buf.writeVarInt(bulkQtyRemaining);
            buf.writeVarInt(bulkTimer);
            buf.writeVarInt(bulkTimerMax);
            buf.writeItemStack(complexItem, false);
            buf.writeVarInt(complexQtyRemaining);
            buf.writeVarInt(complexTimer);
            buf.writeVarInt(complexTimerMax);
        }

        @Override
        public void execute(NetworkEvent.Context ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                StarLadderUplinkClientState.update(state, progress, drainRate,
                        bulkItem, bulkQtyRemaining, bulkTimer, bulkTimerMax,
                        complexItem, complexQtyRemaining, complexTimer, complexTimerMax);
            });
        }
    }

    // ---- Server → Client: Observer whisper message ----

    public enum WhisperStyle {
        REFLECTION,
        OBSERVER,
        AMBIENT
    }

    public static class ObserverWhisperPacket implements CCoreNetwork.INetPacket {

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

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(text);
            buf.writeEnum(style);
        }

        @Override
        public void execute(NetworkEvent.Context ctx) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var player = Minecraft.getInstance().player;
                if (player == null) return;

                Component translated = Component.translatable(text);
                Component message = switch (style) {
                    case REFLECTION -> Component.literal("...").append(translated).withStyle(REFLECTION_STYLE);
                    case OBSERVER -> translated.copy().withStyle(OBSERVER_STYLE);
                    case AMBIENT -> translated.copy().withStyle(AMBIENT_STYLE);
                };
                player.sendSystemMessage(message);
            });
        }
    }

    // ---- Client-side state holder for synced uplink data ----

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

    // ---- Sending helpers ----

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
