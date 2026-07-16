package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.worldgen.survey.VeinSurveyUtil.VeinConfidence;
import com.ghostipedia.cosmiccore.common.worldgen.survey.VeinSurveyUtil.VeinInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SyncPredictedVeinsPacket implements CustomPacketPayload {

    public static final Type<SyncPredictedVeinsPacket> TYPE = new Type<>(CosmicCore.id("sync_predicted_veins"));
    public static final StreamCodec<FriendlyByteBuf, SyncPredictedVeinsPacket> CODEC = StreamCodec
            .ofMember(SyncPredictedVeinsPacket::encode, SyncPredictedVeinsPacket::new);

    private final List<PredictedVeinData> veins;
    private final boolean clearExisting;

    public SyncPredictedVeinsPacket(List<VeinInfo> veins, boolean clearExisting) {
        this.veins = veins.stream()
                .filter(v -> v.confidence() == VeinConfidence.PREDICTED)
                .map(PredictedVeinData::fromVeinInfo)
                .toList();
        this.clearExisting = clearExisting;
    }

    public SyncPredictedVeinsPacket(FriendlyByteBuf buffer) {
        this.clearExisting = buffer.readBoolean();
        int count = buffer.readVarInt();
        this.veins = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            veins.add(PredictedVeinData.read(buffer));
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(clearExisting);
        buffer.writeVarInt(veins.size());
        for (PredictedVeinData vein : veins) {
            vein.write(buffer);
        }
    }

    public void execute(IPayloadContext context) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        // TODO(cosmiccore-42.14): route predicted-vein markers into the Xaero ore-field overlay. Journeymap Integration is unlikely something I will want to support.
    }

    @Override
    public @NotNull Type<SyncPredictedVeinsPacket> type() {
        return TYPE;
    }

    private record PredictedVeinData(
                                     int x,
                                     int z,
                                     int chunkX,
                                     int chunkZ,
                                     String veinName) {

        public static PredictedVeinData fromVeinInfo(VeinInfo info) {
            return new PredictedVeinData(
                    info.center().getX(),
                    info.center().getZ(),
                    info.originChunk().x,
                    info.originChunk().z,
                    info.getVeinName());
        }

        public VeinInfo toVeinInfo() {
            return new VeinInfo(
                    new BlockPos(x, 0, z),
                    new ChunkPos(chunkX, chunkZ),
                    null,
                    ResourceLocation.tryParse("gtceu:" + veinName),
                    0,
                    VeinConfidence.PREDICTED);
        }

        public void write(FriendlyByteBuf buffer) {
            buffer.writeVarInt(x);
            buffer.writeVarInt(z);
            buffer.writeVarInt(chunkX);
            buffer.writeVarInt(chunkZ);
            buffer.writeUtf(veinName);
        }

        public static PredictedVeinData read(FriendlyByteBuf buffer) {
            return new PredictedVeinData(
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readUtf());
        }
    }
}
