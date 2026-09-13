package com.ghostipedia.cosmiccore.common.transmission;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentBehavior;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentPlan;
import com.ghostipedia.cosmiccore.common.item.PowerTowerCoilItem;
import com.ghostipedia.cosmiccore.common.machine.transmission.PowerTowerMachine;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.PowerTowerChainPacket;
import com.ghostipedia.cosmiccore.common.transmission.geometry.PowerTowerAttachments;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;
import com.ghostipedia.cosmiccore.common.transmission.link.PowerTowerLinkService;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class PowerTowerChain {

    private static final Map<UUID, UUID> ANCHORS = new HashMap<>();

    public static void select(ServerPlayer player, UUID nodeId) {
        var node = PowerTowerSavedData.getOrCreate(player.serverLevel()).graph().node(nodeId);
        if (node == null) return;
        ANCHORS.put(player.getUUID(), nodeId);
        CCoreNetwork.sendToPlayer(player, new PowerTowerChainPacket(player.level().dimension().location(), node));
    }

    public static boolean clear(ServerPlayer player) {
        boolean existed = ANCHORS.remove(player.getUUID()) != null;
        CCoreNetwork.sendToPlayer(player, new PowerTowerChainPacket(player.level().dimension().location(), null));
        return existed;
    }

    public static LeylineDeploymentBehavior.Reservation prepare(ServerPlayer player, InteractionHand packageHand) {
        ItemStack coil = player.getItemInHand(
                packageHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        UUID anchor = ANCHORS.get(player.getUUID());
        return anchor == null || !(coil.getItem() instanceof PowerTowerCoilItem) ?
                LeylineDeploymentBehavior.Reservation.NONE : new Request(anchor, coil);
    }

    public static void placed(ServerPlayer player, BlockPos controller) {
        var node = PowerTowerSavedData.getOrCreate(player.serverLevel()).graph().nodeAtController(controller);
        if (node != null) select(player, node.id());
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        ANCHORS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        ANCHORS.clear();
    }

    private static final class Request implements LeylineDeploymentBehavior.Reservation {

        private final UUID source;
        private final ItemStack heldCoil;
        private final ItemStack reservedCoil;
        private UUID createdSpan;

        private Request(UUID source, ItemStack heldCoil) {
            this.source = source;
            this.heldCoil = heldCoil;
            this.reservedCoil = heldCoil.copyWithCount(1);
        }

        @Override
        public List<ItemStack> reservedItems() {
            return List.of(reservedCoil.copy());
        }

        @Override
        public void consume() {
            heldCoil.shrink(1);
        }

        @Override
        public boolean validate(ServerPlayer player, LeylineDeploymentPlan plan, Direction facing) {
            var data = PowerTowerSavedData.getOrCreate(player.serverLevel());
            var node = data.graph().node(source);
            if (node == null || !player.level().isLoaded(node.controllerPos()) ||
                    !(MetaMachine.getMachine(player.level(),
                            node.controllerPos()) instanceof PowerTowerMachine tower) ||
                    !tower.isFormed() || !MachineOwner.canBreakOwnerMachine(player, tower))
                return fail(player, "endpoint_not_found");
            int tier = ((PowerTowerCoilItem) reservedCoil.getItem()).getVoltageTier();
            if (!data.graph().acceptsCableTier(source, tier))
                return fail(player, "graph_invariant_rejected");
            var destination = PowerTowerAttachments.preview(plan.controllerPos(), facing, node.ownerId());
            var footprint = new HashSet<BlockPos>();
            plan.worldPlacements().forEach(placement -> footprint.add(placement.pos()));
            var failure = new PowerTowerLinkService(data.graph(), data).validatePath(player.level(), node, destination,
                    pos -> footprint.contains(pos) || tower.containsFormedStructurePosition(pos));
            return failure == null || fail(player, failure.name().toLowerCase(java.util.Locale.ROOT));
        }

        @Override
        public boolean afterFormation(ServerPlayer player, LeylineDeploymentPlan plan) {
            var data = PowerTowerSavedData.getOrCreate(player.serverLevel());
            var first = data.graph().node(source);
            var second = data.graph().nodeAtController(plan.controllerPos());
            if (first == null || second == null ||
                    !(MetaMachine.getMachine(player.level(), first.controllerPos()) instanceof PowerTowerMachine a) ||
                    !(MetaMachine.getMachine(player.level(), second.controllerPos()) instanceof PowerTowerMachine b) ||
                    !MachineOwner.canBreakOwnerMachine(player, a))
                return fail(player, "endpoint_not_found");
            var result = new PowerTowerLinkService(data.graph(), data).tryCreateSpan(player.level(), first.id(),
                    second.id(),
                    ((PowerTowerCoilItem) reservedCoil.getItem()).getVoltageTier(),
                    pos -> a.containsFormedStructurePosition(pos) || b.containsFormedStructurePosition(pos));
            if (!result.spanCreated()) return fail(player, result.status().name().toLowerCase(java.util.Locale.ROOT));
            createdSpan = result.spanId();
            data.loadedTerminals().wakeComponentTerminals(data.graph(), second.id());
            return true;
        }

        @Override
        public void rollback(ServerLevel level) {
            var data = PowerTowerSavedData.getOrCreate(level);
            if (createdSpan != null && data.graph().removeSpan(createdSpan)) data.markGraphDirty();
            createdSpan = null;
        }

        private static boolean fail(ServerPlayer player, String failure) {
            player.displayClientMessage(Component.translatable("cosmiccore.power_tower.line.error." + failure)
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
    }
}
