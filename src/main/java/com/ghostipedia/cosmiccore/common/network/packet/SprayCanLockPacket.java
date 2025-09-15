package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.common.item.behavior.InfiniteSprayCanBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.SprayCanEventListener;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record SprayCanLockPacket(boolean isLocked) implements CCoreNetwork.INetPacket {

    public SprayCanLockPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(isLocked);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack can = player.getMainHandItem();
            if (can.isEmpty()) return;

            InfiniteSprayCanBehavior behavior = SprayCanEventListener.getSprayCanBehavior(can);
            if (behavior == null) return;

            behavior.setIsLocked(isLocked);
            behavior.sendColorToTag(player, behavior.getColor()); // still saves current color so NBT syncs
        });
        context.setPacketHandled(true);
    }
}
