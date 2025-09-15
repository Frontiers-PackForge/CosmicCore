package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.common.item.behavior.ExtendedDyeColor;
import com.ghostipedia.cosmiccore.common.item.behavior.InfiniteSprayCanBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.SprayCanEventListener;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record SprayCanColorPacket(int color) implements CCoreNetwork.INetPacket {

    public SprayCanColorPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(color);
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

            behavior.setColor(ExtendedDyeColor.getColorFromDyeId(color));
            behavior.sendColorToTag(player, behavior.getColor());
        });
        context.setPacketHandled(true);
    }
}
