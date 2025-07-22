package com.ghostipedia.cosmiccore.common.network;


import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.packet.CosmicClientKeyDownPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Function;

public class CCoreNetwork {
    private static final String PROTOCOL_VERSION = "1.0.0";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(CosmicCore.id("network"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static int nextPacketId = 0;

    public static void sendToServer(CCoreNetwork.INetPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToPlayersInLevel(ResourceKey<Level> level, CCoreNetwork.INetPacket packet) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> level), packet);
    }

    public static void sendToPlayersNearPoint(PacketDistributor.TargetPoint point, CCoreNetwork.INetPacket packet) {
        INSTANCE.send(PacketDistributor.NEAR.with(() -> point), packet);
    }

    public static void sendToAllPlayersTrackingEntity(Entity entity, boolean includeSelf, CCoreNetwork.INetPacket packet) {
        INSTANCE.send(includeSelf ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity) :
                PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }

    public static void sendToAllPlayersTrackingChunk(LevelChunk chunk, CCoreNetwork.INetPacket packet) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), packet);
    }

    public static void sendToAll(CCoreNetwork.INetPacket packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendToPlayer(ServerPlayer player, CCoreNetwork.INetPacket packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public interface INetPacket {

        void encode(FriendlyByteBuf buffer);

        void execute(NetworkEvent.Context context);
    }

    public static <T extends CCoreNetwork.INetPacket> void register(Class<T> cls, Function<FriendlyByteBuf, T> decode,
                                                                 NetworkDirection direction) {
        INSTANCE.registerMessage(nextPacketId++, cls, CCoreNetwork.INetPacket::encode, decode, (msg, ctx) -> {
            ctx.get().enqueueWork(() -> msg.execute(ctx.get()));
            ctx.get().setPacketHandled(true);
        }, Optional.ofNullable(direction));
    }

    public static void  init () {

        register(CosmicClientKeyDownPacket.class, CosmicClientKeyDownPacket::new, NetworkDirection.PLAY_TO_SERVER);

    }

}
