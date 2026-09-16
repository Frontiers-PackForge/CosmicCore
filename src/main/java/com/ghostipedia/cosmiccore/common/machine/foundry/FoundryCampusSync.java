package com.ghostipedia.cosmiccore.common.machine.foundry;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.HephaestusCauldronMachine;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.FoundryCampusSyncPacket;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class FoundryCampusSync {

    private FoundryCampusSync() {}

    public static void send(ServerLevel level, GlobalPos core) {
        if (!core.dimension().equals(level.dimension())) return;
        if (!(MetaMachine.getMachine(level, core.pos()) instanceof HephaestusCauldronMachine machine) ||
                !machine.isFormed()) {
            clear(level, core);
            return;
        }
        FoundryCampusSavedData.get(level.getServer()).snapshot(core, position -> endpoint(level, position))
                .ifPresent(snapshot -> PacketDistributor.sendToPlayersTrackingChunk(
                        level, new ChunkPos(core.pos()), new FoundryCampusSyncPacket(core, snapshot)));
    }

    public static void clear(ServerLevel level, GlobalPos core) {
        if (!core.dimension().equals(level.dimension())) return;
        PacketDistributor.sendToPlayersTrackingChunk(
                level, new ChunkPos(core.pos()), new FoundryCampusSyncPacket(core, null));
    }

    @SubscribeEvent
    public static void watch(ChunkWatchEvent.Sent event) {
        ServerLevel level = event.getLevel();
        FoundryCampusSavedData data = FoundryCampusSavedData.get(level.getServer());
        for (GlobalPos core : data.coresIn(level.dimension(), event.getPos())) {
            if (!(MetaMachine.getMachine(level, core.pos()) instanceof HephaestusCauldronMachine machine) ||
                    !machine.isFormed()) {
                continue;
            }
            data.snapshot(core, position -> endpoint(level, position))
                    .ifPresent(snapshot -> CCoreNetwork.sendToPlayer(
                            event.getPlayer(), new FoundryCampusSyncPacket(core, snapshot)));
        }
    }

    @SubscribeEvent
    public static void unwatch(ChunkWatchEvent.UnWatch event) {
        ServerLevel level = event.getLevel();
        FoundryCampusSavedData data = FoundryCampusSavedData.get(level.getServer());
        for (GlobalPos core : data.coresIn(level.dimension(), event.getPos())) {
            CCoreNetwork.sendToPlayer(event.getPlayer(), new FoundryCampusSyncPacket(core, null));
        }
    }

    private static FoundryFurnaceEndpoint endpoint(ServerLevel level, GlobalPos position) {
        if (!position.dimension().equals(level.dimension()) || !level.isLoaded(position.pos())) return null;
        return MetaMachine.getMachine(level, position.pos()) instanceof FoundryFurnaceEndpoint endpoint ?
                endpoint : null;
    }
}
