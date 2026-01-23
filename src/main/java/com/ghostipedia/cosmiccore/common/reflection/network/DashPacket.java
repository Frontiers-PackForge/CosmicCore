package com.ghostipedia.cosmiccore.common.reflection.network;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.CelesteDashHandler;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class DashPacket implements CCoreNetwork.INetPacket {

    private final float xRot;
    private final float yRot;
    private final float forwardInput;
    private final float strafeInput;

    public DashPacket(float xRot, float yRot, float forwardInput, float strafeInput) {
        this.xRot = xRot;
        this.yRot = yRot;
        this.forwardInput = forwardInput;
        this.strafeInput = strafeInput;
    }

    public DashPacket(FriendlyByteBuf buffer) {
        this.xRot = buffer.readFloat();
        this.yRot = buffer.readFloat();
        this.forwardInput = buffer.readFloat();
        this.strafeInput = buffer.readFloat();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(xRot);
        buffer.writeFloat(yRot);
        buffer.writeFloat(forwardInput);
        buffer.writeFloat(strafeInput);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        if (!QuakeMovementHandler.hasQuakeMovement(player)) return;

        CelesteDashHandler.executeDashServer(player, xRot, yRot, forwardInput, strafeInput);
    }
}
