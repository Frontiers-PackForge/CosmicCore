package com.ghostipedia.cosmiccore.common.network;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.packet.AbyssTimeWarnPacket;
import com.ghostipedia.cosmiccore.common.network.packet.BootsControlPacket;
import com.ghostipedia.cosmiccore.common.network.packet.OxygenWarnPacket;
import com.ghostipedia.cosmiccore.common.network.packet.StarLadderUplinkPackets;
import com.ghostipedia.cosmiccore.common.network.packet.StellarUpgradePacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncOxygenBarPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncPredictedVeinsPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncTimeBarPacket;
import com.ghostipedia.cosmiccore.common.reflection.network.DashPacket;
import com.ghostipedia.cosmiccore.common.reflection.network.SoulSuperPacket;
import com.ghostipedia.cosmiccore.common.reflection.network.SyncQuakeMovementPacket;
import com.ghostipedia.cosmiccore.common.reflection.ui.ScarSelectionPackets;
import com.ghostipedia.cosmiccore.common.reflection.ui.VoidUIPackets;

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
    private static boolean INITIALIZED = false;

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

    public static void sendToAllPlayersTrackingEntity(Entity entity, boolean includeSelf,
                                                      CCoreNetwork.INetPacket packet) {
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

    public static void init() {
        if (INITIALIZED) return;
        INITIALIZED = true;
        register(SyncTimeBarPacket.class, SyncTimeBarPacket::new, NetworkDirection.PLAY_TO_CLIENT);
        register(AbyssTimeWarnPacket.class, AbyssTimeWarnPacket::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SyncOxygenBarPacket.class, SyncOxygenBarPacket::new, NetworkDirection.PLAY_TO_CLIENT);
        register(OxygenWarnPacket.class, OxygenWarnPacket::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SyncQuakeMovementPacket.class, SyncQuakeMovementPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Dash packet (client -> server) - for Celeste-style dash
        register(DashPacket.class, DashPacket::new, NetworkDirection.PLAY_TO_SERVER);

        // Soul Super packet (client -> server) - activate soul shape super ability
        register(SoulSuperPacket.class, SoulSuperPacket::new, NetworkDirection.PLAY_TO_SERVER);

        // Stellar IRIS upgrade packet (client -> server)
        register(StellarUpgradePacket.class, StellarUpgradePacket::new, NetworkDirection.PLAY_TO_SERVER);

        // Boots control packet (client -> server)
        register(BootsControlPacket.class, BootsControlPacket::new, NetworkDirection.PLAY_TO_SERVER);

        // Predicted veins sync (server -> client) for JourneyMap integration
        register(SyncPredictedVeinsPacket.class, SyncPredictedVeinsPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Void UI packets
        VoidUIPackets.register();

        // Scar selection packets (Cluster of Perpetuity)
        ScarSelectionPackets.register();

        // Star Ladder uplink fight packets
        StarLadderUplinkPackets.register();
    }
}
