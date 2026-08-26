package com.ghostipedia.cosmiccore.common.network;

import com.ghostipedia.cosmiccore.common.compat.ftbquests.DeedQuestCompatBridge;
import com.ghostipedia.cosmiccore.common.network.packet.AbyssTimeWarnPacket;
import com.ghostipedia.cosmiccore.common.network.packet.BuildTieredMultiblockPacket;
import com.ghostipedia.cosmiccore.common.network.packet.DashPacket;
import com.ghostipedia.cosmiccore.common.network.packet.DeedPresentationAckPacket;
import com.ghostipedia.cosmiccore.common.network.packet.DeedPresentationPacket;
import com.ghostipedia.cosmiccore.common.network.packet.DeedSyncPacket;
import com.ghostipedia.cosmiccore.common.network.packet.EffortlessBuildingAE2CountQueryPacket;
import com.ghostipedia.cosmiccore.common.network.packet.EffortlessBuildingAE2CountSyncPacket;
import com.ghostipedia.cosmiccore.common.network.packet.FactoryGaugeFluidSelectionPacket;
import com.ghostipedia.cosmiccore.common.network.packet.FactoryGaugePromiseLimitPacket;
import com.ghostipedia.cosmiccore.common.network.packet.FirmamentTideHudPacket;
import com.ghostipedia.cosmiccore.common.network.packet.MirrorWeavePacket;
import com.ghostipedia.cosmiccore.common.network.packet.MurkbloomDevImmunityPacket;
import com.ghostipedia.cosmiccore.common.network.packet.MurkbloomSyncPacket;
import com.ghostipedia.cosmiccore.common.network.packet.OxygenWarnPacket;
import com.ghostipedia.cosmiccore.common.network.packet.RevealFieldsPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SetMultiblockStructureTierPacket;
import com.ghostipedia.cosmiccore.common.network.packet.StarLadderUplinkPackets;
import com.ghostipedia.cosmiccore.common.network.packet.StellarUpgradePacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncAbyssAttunementPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncFoodDataPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncOxygenBarPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncPredictedVeinsPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncTimeBarPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncWirelessPDAHudPacket;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class CCoreNetwork {

    private static final String PROTOCOL_VERSION = "1.16.0";

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
        registrar.playToClient(SyncPredictedVeinsPacket.TYPE, SyncPredictedVeinsPacket.CODEC,
                SyncPredictedVeinsPacket::execute);
        registrar.playToClient(RevealFieldsPacket.TYPE, RevealFieldsPacket.CODEC, RevealFieldsPacket::execute);
        registrar.playToClient(DeedSyncPacket.TYPE, DeedSyncPacket.CODEC, DeedSyncPacket::execute);
        registrar.playToClient(DeedPresentationPacket.TYPE, DeedPresentationPacket.CODEC,
                DeedPresentationPacket::execute);
        registrar.playToClient(FirmamentTideHudPacket.TYPE, FirmamentTideHudPacket.CODEC,
                FirmamentTideHudPacket::execute);
        registrar.playToClient(EffortlessBuildingAE2CountSyncPacket.TYPE, EffortlessBuildingAE2CountSyncPacket.CODEC,
                EffortlessBuildingAE2CountSyncPacket::execute);
        registrar.playToClient(SyncWirelessPDAHudPacket.TYPE, SyncWirelessPDAHudPacket.CODEC,
                SyncWirelessPDAHudPacket::execute);
        DeedQuestCompatBridge.registerPayloads(registrar);

        registrar.playToServer(DashPacket.TYPE, DashPacket.CODEC, DashPacket::execute);
        registrar.playToServer(StellarUpgradePacket.TYPE, StellarUpgradePacket.CODEC, StellarUpgradePacket::execute);
        registrar.playToServer(MirrorWeavePacket.TYPE, MirrorWeavePacket.CODEC, MirrorWeavePacket::execute);
        registrar.playToServer(DeedPresentationAckPacket.TYPE, DeedPresentationAckPacket.CODEC,
                DeedPresentationAckPacket::execute);
        registrar.playToServer(MurkbloomDevImmunityPacket.TYPE, MurkbloomDevImmunityPacket.CODEC,
                MurkbloomDevImmunityPacket::execute);
        registrar.playToServer(BuildTieredMultiblockPacket.TYPE, BuildTieredMultiblockPacket.CODEC,
                BuildTieredMultiblockPacket::execute);
        registrar.playToServer(SetMultiblockStructureTierPacket.TYPE, SetMultiblockStructureTierPacket.CODEC,
                SetMultiblockStructureTierPacket::execute);
        registrar.playToServer(EffortlessBuildingAE2CountQueryPacket.TYPE, EffortlessBuildingAE2CountQueryPacket.CODEC,
                EffortlessBuildingAE2CountQueryPacket::execute);
        registrar.playToServer(FactoryGaugePromiseLimitPacket.TYPE, FactoryGaugePromiseLimitPacket.CODEC,
                FactoryGaugePromiseLimitPacket::execute);
        registrar.playToServer(FactoryGaugeFluidSelectionPacket.TYPE, FactoryGaugeFluidSelectionPacket.CODEC,
                FactoryGaugeFluidSelectionPacket::execute);

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
