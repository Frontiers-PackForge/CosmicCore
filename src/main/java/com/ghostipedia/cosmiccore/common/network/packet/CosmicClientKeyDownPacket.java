package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.utils.input.SyncedKeyMapping;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;

public class CosmicClientKeyDownPacket implements CCoreNetwork.INetPacket {

    private Int2BooleanMap updateKeys;

    public CosmicClientKeyDownPacket(Int2BooleanMap updateKeys) {
        this.updateKeys = updateKeys;
    }

    public CosmicClientKeyDownPacket(FriendlyByteBuf buf) {
        this.updateKeys = new Int2BooleanOpenHashMap();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            updateKeys.put(buf.readInt(), buf.readBoolean());
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(updateKeys.size());
        for (var entry : updateKeys.int2BooleanEntrySet()) {
            buffer.writeInt(entry.getIntKey());
            buffer.writeBoolean(entry.getBooleanValue());
        }
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (context.getSender() != null) {
            for (var entry : updateKeys.int2BooleanEntrySet()) {
                SyncedKeyMapping keyMapping = SyncedKeyMapping.VALUES[entry.getIntKey()];
                keyMapping.serverActivate(entry.getBooleanValue(), context.getSender());
            }
        }
    }
}
