package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class OxygenWarnPacket implements CCoreNetwork.INetPacket {

    private final String message;
    private final int seconds;

    public OxygenWarnPacket(String message, int seconds) {
        this.message = message;
        this.seconds = seconds;
    }

    public OxygenWarnPacket(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
        this.seconds = buf.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(message);
        buffer.writeVarInt(seconds);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                Minecraft.getInstance().execute(() ->
                        Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(message, seconds), false)
                )
        );
    }
}
