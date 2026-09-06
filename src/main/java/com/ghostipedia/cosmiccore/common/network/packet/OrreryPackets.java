package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.orrery.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.*;

public final class OrreryPackets {

    public static final int PAGE_SIZE = 48;

    private OrreryPackets() {}

    public enum Action {
        QUERY,
        ASSIGN,
        SELECT,
        RENAME,
        SETTINGS,
        SWAP,
        LOCK,
        CONFIRM,
        CANCEL,
        CRAFT
    }

    public record Proposal(UUID id, UUID design, BlockPos anchor, Direction face, Direction facing) {

        public void write(FriendlyByteBuf b) {
            b.writeUUID(id);
            b.writeUUID(design);
            b.writeBlockPos(anchor);
            b.writeEnum(face);
            b.writeEnum(facing);
        }

        public static Proposal read(FriendlyByteBuf b) {
            return new Proposal(b.readUUID(), b.readUUID(), b.readBlockPos(), b.readEnum(Direction.class),
                    b.readEnum(Direction.class));
        }
    }

    public record Request(int sequence, InteractionHand hand, UUID tool, Action action, int bar, int slot,
                          UUID design, String text, int value, boolean preview, boolean hud)
            implements CustomPacketPayload {

        public static final Type<Request> TYPE = new Type<>(CosmicCore.id("orrery_action"));
        public static final StreamCodec<FriendlyByteBuf, Request> CODEC = StreamCodec.of((b, p) -> {
            b.writeVarInt(p.sequence);
            b.writeEnum(p.hand);
            b.writeNullable(p.tool, (buffer, id) -> buffer.writeUUID(id));
            b.writeEnum(p.action);
            b.writeVarInt(p.bar);
            b.writeVarInt(p.slot);
            b.writeNullable(p.design, (buffer, id) -> buffer.writeUUID(id));
            b.writeUtf(p.text, 80);
            b.writeVarInt(p.value);
            b.writeBoolean(p.preview);
            b.writeBoolean(p.hud);
        }, b -> new Request(b.readVarInt(), b.readEnum(InteractionHand.class),
                b.readNullable(buffer -> buffer.readUUID()),
                b.readEnum(Action.class), b.readVarInt(), b.readVarInt(), b.readNullable(buffer -> buffer.readUUID()),
                b.readUtf(80), b.readVarInt(), b.readBoolean(), b.readBoolean()));

        @Override
        public Type<Request> type() {
            return TYPE;
        }

        public static void execute(Request packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) OrreryService.handle(player, packet);
            });
        }
    }

    public record Snapshot(int sequence, OrreryState state, OrreryNetwork.Status status, List<OrreryDesign> page,
                           List<OrreryDesign> assigned, int total, int pageIndex, Proposal proposal)
            implements CustomPacketPayload {

        public static final Type<Snapshot> TYPE = new Type<>(CosmicCore.id("orrery_snapshot"));
        public static final StreamCodec<FriendlyByteBuf, Snapshot> CODEC = StreamCodec.of((b, p) -> {
            b.writeVarInt(p.sequence);
            p.state.write(b);
            b.writeEnum(p.status);
            writeDesigns(b, p.page);
            writeDesigns(b, p.assigned);
            b.writeVarInt(p.total);
            b.writeVarInt(p.pageIndex);
            b.writeNullable(p.proposal, (buffer, proposal) -> proposal.write(buffer));
        }, b -> new Snapshot(b.readVarInt(), OrreryState.read(b), b.readEnum(OrreryNetwork.Status.class),
                readDesigns(b, PAGE_SIZE), readDesigns(b, OrreryState.LOADOUTS * OrreryState.SLOTS),
                b.readVarInt(), b.readVarInt(), b.readNullable(Proposal::read)));

        @Override
        public Type<Snapshot> type() {
            return TYPE;
        }

        public static void execute(Snapshot packet, IPayloadContext context) {
            context.enqueueWork(() -> com.ghostipedia.cosmiccore.client.orrery.OrreryClient.receive(packet));
        }
    }

    public record OpenConfig(UUID tool) implements CustomPacketPayload {

        public static final Type<OpenConfig> TYPE = new Type<>(CosmicCore.id("orrery_open_config"));
        public static final StreamCodec<FriendlyByteBuf, OpenConfig> CODEC = StreamCodec.of(
                (buffer, packet) -> buffer.writeUUID(packet.tool), buffer -> new OpenConfig(buffer.readUUID()));

        @Override
        public Type<OpenConfig> type() {
            return TYPE;
        }

        public static void execute(OpenConfig packet, IPayloadContext context) {
            context.enqueueWork(
                    () -> com.ghostipedia.cosmiccore.client.orrery.OrreryClient.returnToConfig(packet.tool));
        }
    }

    private static void writeDesigns(FriendlyByteBuf b, List<OrreryDesign> designs) {
        b.writeVarInt(designs.size());
        designs.forEach(d -> d.write(b));
    }

    private static List<OrreryDesign> readDesigns(FriendlyByteBuf b, int limit) {
        int size = b.readVarInt();
        if (size < 0 || size > limit) throw new IllegalArgumentException("Orrery catalogue page too large");
        var designs = new ArrayList<OrreryDesign>(size);
        for (int i = 0; i < size; i++) designs.add(OrreryDesign.read(b));
        return List.copyOf(designs);
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(Request.TYPE, Request.CODEC, Request::execute);
        registrar.playToClient(Snapshot.TYPE, Snapshot.CODEC, Snapshot::execute);
        registrar.playToClient(OpenConfig.TYPE, OpenConfig.CODEC, OpenConfig::execute);
    }
}
