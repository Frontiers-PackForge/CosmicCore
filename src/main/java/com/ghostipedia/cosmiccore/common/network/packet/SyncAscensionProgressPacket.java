package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.common.ascension.AscensionCap;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SyncAscensionProgressPacket implements CCoreNetwork.INetPacket {
    private final CompoundTag data;

    public SyncAscensionProgressPacket(CompoundTag data) {
        this.data = data == null ? new CompoundTag() : data;
    }

    public SyncAscensionProgressPacket(FriendlyByteBuf buf) {
        this.data = buf.readNbt();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(data);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            var mc = Minecraft.getInstance();
            var player = mc.player;
            if (player == null) return;
            player.getCapability(AscensionCap.CAP).ifPresent(cap -> cap.load(data));
        });
    }
}
