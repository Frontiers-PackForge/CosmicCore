package com.ghostipedia.cosmiccore.common.network;

import com.ghostipedia.cosmiccore.common.network.packet.AbyssTimeWarnPacket;
import com.ghostipedia.cosmiccore.common.network.packet.BootsControlPacket;
import com.ghostipedia.cosmiccore.common.network.packet.DeedSyncPacket;
import com.ghostipedia.cosmiccore.common.network.packet.MirrorWeavePacket;
import com.ghostipedia.cosmiccore.common.network.packet.MurkbloomSyncPacket;
import com.ghostipedia.cosmiccore.common.network.packet.OxygenWarnPacket;
import com.ghostipedia.cosmiccore.common.network.packet.RevealFieldsPacket;
import com.ghostipedia.cosmiccore.common.network.packet.StarLadderUplinkPackets;
import com.ghostipedia.cosmiccore.common.network.packet.StellarUpgradePacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncAbyssAttunementPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncFoodDataPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncOxygenBarPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncPredictedVeinsPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncTimeBarPacket;
import com.ghostipedia.cosmiccore.common.reflection.network.DashPacket;
import com.ghostipedia.cosmiccore.common.reflection.network.SoulSuperPacket;
import com.ghostipedia.cosmiccore.common.reflection.network.SyncQuakeMovementPacket;
import com.ghostipedia.cosmiccore.common.reflection.ui.ScarSelectionPackets;
import com.ghostipedia.cosmiccore.common.reflection.ui.VoidUIPackets;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class CCoreNetwork {

    private static final String PROTOCOL_VERSION = "1.0.0";

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(SyncTimeBarPacket.TYPE, SyncTimeBarPacket.CODEC, SyncTimeBarPacket::execute);
        registrar.playToClient(AbyssTimeWarnPacket.TYPE, AbyssTimeWarnPacket.CODEC, AbyssTimeWarnPacket::execute);
        registrar.playToClient(SyncOxygenBarPacket.TYPE, SyncOxygenBarPacket.CODEC, SyncOxygenBarPacket::execute);
        registrar.playToClient(SyncAbyssAttunementPacket.TYPE, SyncAbyssAttunementPacket.CODEC,
                SyncAbyssAttunementPacket::execute);
        registrar.playToClient(SyncFoodDataPacket.TYPE, SyncFoodDataPacket.CODEC, SyncFoodDataPacket::execute);
        registrar.playToClient(MurkbloomSyncPacket.TYPE, MurkbloomSyncPacket.CODEC, MurkbloomSyncPacket::execute);
        registrar.playToClient(OxygenWarnPacket.TYPE, OxygenWarnPacket.CODEC, OxygenWarnPacket::execute);
        registrar.playToClient(SyncQuakeMovementPacket.TYPE, SyncQuakeMovementPacket.CODEC,
                SyncQuakeMovementPacket::execute);
        registrar.playToClient(SyncPredictedVeinsPacket.TYPE, SyncPredictedVeinsPacket.CODEC,
                SyncPredictedVeinsPacket::execute);
        registrar.playToClient(RevealFieldsPacket.TYPE, RevealFieldsPacket.CODEC, RevealFieldsPacket::execute);
        registrar.playToClient(DeedSyncPacket.TYPE, DeedSyncPacket.CODEC, DeedSyncPacket::execute);

        registrar.playToServer(DashPacket.TYPE, DashPacket.CODEC, DashPacket::execute);
        registrar.playToServer(SoulSuperPacket.TYPE, SoulSuperPacket.CODEC, SoulSuperPacket::execute);
        registrar.playToServer(StellarUpgradePacket.TYPE, StellarUpgradePacket.CODEC, StellarUpgradePacket::execute);
        registrar.playToServer(BootsControlPacket.TYPE, BootsControlPacket.CODEC, BootsControlPacket::execute);
        registrar.playToServer(MirrorWeavePacket.TYPE, MirrorWeavePacket.CODEC, MirrorWeavePacket::execute);

        registrar.playToClient(VoidUIPackets.OpenVoidScreenPacket.TYPE, VoidUIPackets.OpenVoidScreenPacket.CODEC,
                VoidUIPackets.OpenVoidScreenPacket::execute);
        registrar.playToServer(VoidUIPackets.BargainChoicePacket.TYPE, VoidUIPackets.BargainChoicePacket.CODEC,
                VoidUIPackets.BargainChoicePacket::execute);
        registrar.playToClient(VoidUIPackets.ThresholdEncounterPacket.TYPE,
                VoidUIPackets.ThresholdEncounterPacket.CODEC, VoidUIPackets.ThresholdEncounterPacket::execute);
        registrar.playToClient(VoidUIPackets.OpenHubPacket.TYPE, VoidUIPackets.OpenHubPacket.CODEC,
                VoidUIPackets.OpenHubPacket::execute);
        registrar.playToServer(VoidUIPackets.DefianceChoicePacket.TYPE, VoidUIPackets.DefianceChoicePacket.CODEC,
                VoidUIPackets.DefianceChoicePacket::execute);
        registrar.playToServer(VoidUIPackets.SoulShapeChoicePacket.TYPE, VoidUIPackets.SoulShapeChoicePacket.CODEC,
                VoidUIPackets.SoulShapeChoicePacket::execute);

        registrar.playToClient(ScarSelectionPackets.OpenScarSelectionPacket.TYPE,
                ScarSelectionPackets.OpenScarSelectionPacket.CODEC,
                ScarSelectionPackets.OpenScarSelectionPacket::execute);
        registrar.playToServer(ScarSelectionPackets.ScarRemovalPacket.TYPE,
                ScarSelectionPackets.ScarRemovalPacket.CODEC, ScarSelectionPackets.ScarRemovalPacket::execute);

        registrar.playToServer(StarLadderUplinkPackets.UplinkActionPacket.TYPE,
                StarLadderUplinkPackets.UplinkActionPacket.CODEC,
                StarLadderUplinkPackets.UplinkActionPacket::execute);
        registrar.playToClient(StarLadderUplinkPackets.CloseScreenPacket.TYPE,
                StarLadderUplinkPackets.CloseScreenPacket.CODEC,
                StarLadderUplinkPackets.CloseScreenPacket::execute);
        registrar.playToClient(StarLadderUplinkPackets.UplinkSyncPacket.TYPE,
                StarLadderUplinkPackets.UplinkSyncPacket.CODEC,
                StarLadderUplinkPackets.UplinkSyncPacket::execute);
        registrar.playToClient(StarLadderUplinkPackets.ObserverWhisperPacket.TYPE,
                StarLadderUplinkPackets.ObserverWhisperPacket.CODEC,
                StarLadderUplinkPackets.ObserverWhisperPacket::execute);
    }
}
