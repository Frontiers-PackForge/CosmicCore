package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedLedger;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedTeams;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedsAPI;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MirrorWeavePacket implements CustomPacketPayload {

    public static final Type<MirrorWeavePacket> TYPE = new Type<>(CosmicCore.id("mirror_weave"));

    public static final StreamCodec<FriendlyByteBuf, MirrorWeavePacket> CODEC = StreamCodec
            .ofMember(MirrorWeavePacket::encode, MirrorWeavePacket::new);

    private final boolean heart;

    public MirrorWeavePacket(boolean heart) {
        this.heart = heart;
    }

    public MirrorWeavePacket(FriendlyByteBuf buf) {
        this.heart = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(heart);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                CosmicCore.LOGGER.warn("MirrorWeavePacket: no server player on context");
                return;
            }
            if (player.getServer() == null) return;
            if (heart) {
                var echo = DeedsAPI.weave(player, DeedRegistry.THE_ADDRESS.id(), false);
                CosmicCore.LOGGER.info("MirrorWeavePacket: heart weave for {} -> {}",
                        player.getScoreboardName(), echo != null ? "woven" : "already woven");
                return;
            }
            String teamKey = DeedTeams.teamKey(player);
            Set<ResourceLocation> pending = DeedLedger.get(player.getServer()).pendingOf(teamKey);
            CosmicCore.LOGGER.info("MirrorWeavePacket: weave request from {} team {} pending {}",
                    player.getScoreboardName(), teamKey, pending);
            for (ResourceLocation id : pending) {
                if (!id.equals(DeedRegistry.THE_ADDRESS.id())) {
                    var echo = DeedsAPI.weave(player, id, true);
                    CosmicCore.LOGGER.info("MirrorWeavePacket: weave {} -> {}", id,
                            echo != null ? "#" + echo.claimIndex() : "FAILED (already woven)");
                    return;
                }
            }
            CosmicCore.LOGGER.info("MirrorWeavePacket: nothing weavable pending");
        });
    }

    @Override
    public @NotNull Type<MirrorWeavePacket> type() {
        return TYPE;
    }
}
