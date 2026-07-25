package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedLedger;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedsAPI;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public class MirrorWeavePacket implements CustomPacketPayload {

    public static final Type<MirrorWeavePacket> TYPE = new Type<>(CosmicCore.id("mirror_weave"));

    public static final StreamCodec<FriendlyByteBuf, MirrorWeavePacket> CODEC = StreamCodec
            .ofMember(MirrorWeavePacket::encode, MirrorWeavePacket::new);

    private final ResourceLocation deedId;
    private final boolean bindPosition;

    public MirrorWeavePacket(ResourceLocation deedId, boolean bindPosition) {
        this.deedId = deedId;
        this.bindPosition = bindPosition;
    }

    public MirrorWeavePacket(FriendlyByteBuf buf) {
        this.deedId = buf.readResourceLocation();
        this.bindPosition = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(deedId);
        buf.writeBoolean(bindPosition);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                CosmicCore.LOGGER.warn("MirrorWeavePacket: no server player on context");
                return;
            }
            if (player.getServer() == null || DeedRegistry.get(deedId) == null) return;
            String teamKey = com.ghostipedia.cosmiccore.common.mirror.deed.DeedTeams.teamKey(player);
            DeedLedger ledger = DeedLedger.get(player.getServer());
            boolean address = deedId.equals(DeedRegistry.THE_ADDRESS.id());
            boolean allowed = address ? ledger.wovenOf(teamKey).size() >= 72 :
                    ledger.pendingOf(teamKey).contains(deedId);
            if (!allowed) {
                CosmicCore.LOGGER.warn("Rejected deed weave {} from {}", deedId, player.getScoreboardName());
                return;
            }
            DeedsAPI.weave(player, deedId, bindPosition);
        });
    }

    @Override
    public @NotNull Type<MirrorWeavePacket> type() {
        return TYPE;
    }
}
