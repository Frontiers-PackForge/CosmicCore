package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class AbyssTimeWarnPacket implements CCoreNetwork.INetPacket {


    private final String message;

    public AbyssTimeWarnPacket(Component message) {
        this.message = message.getString();
    }

    public AbyssTimeWarnPacket(FriendlyByteBuf buffer) {
        this.message = buffer.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(message);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                Minecraft.getInstance().gui.setOverlayMessage(Component.literal(message), false));
    }
}
