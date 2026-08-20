package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import neoforge.nl.requios.effortlessbuilding.buildmode.BuildSettings;
import neoforge.nl.requios.effortlessbuilding.buildpipeline.BuildPipeline;
import neoforge.nl.requios.effortlessbuilding.config.ServerConfig;
import neoforge.nl.requios.effortlessbuilding.network.PlaceBuildModePacket;
import neoforge.nl.requios.effortlessbuilding.utilities.BlockEntry;
import neoforge.nl.requios.effortlessbuilding.utilities.BlockSet;
import neoforge.nl.requios.effortlessbuilding.utilities.InventoryHelper;
import neoforge.nl.requios.effortlessbuilding.utilities.PlacedBlockTracker;
import neoforge.nl.requios.effortlessbuilding.utilities.UndoManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EffortlessBuildingPlacementHandler {

    private EffortlessBuildingPlacementHandler() {}

    public static void placeBlocks(PlaceBuildModePacket packet, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockSet blockSet = BuildPipeline.SERVER.runServerPipeline(
                packet.buildMode(), packet.firstPos(), packet.secondPos(), packet.thirdPos(), player,
                BuildPipeline.BuildState.PLACING, packet.fill(), packet.cubeFill(), packet.raisedEdge(),
                packet.circleStart(), packet.protectTileEntities());
        if (blockSet == null) return;

        blockSet.sortByDistance();
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem blockItem)) return;
        ItemStack placementItem = held.copyWithCount(1);
        ItemStack offHand = player.getOffhandItem();
        boolean creative = player.isCreative();
        boolean networkEligible = placementItem.getComponentsPatch().isEmpty();
        double yFraction = packet.hitLocation().y - Math.floor(packet.hitLocation().y);
        Map<BlockPos, UndoManager.BlockChange> undoChanges = new LinkedHashMap<>();
        Map<BlockPos, EffortlessBuildingBlockChange> operationChanges = new LinkedHashMap<>();
        Map<BlockPos, EffortlessBuildingGTPipeSnapshot> previousPipeSnapshots = new LinkedHashMap<>();

        for (var mapEntry : blockSet.validEntries()) {
            BlockPos pos = mapEntry.getKey();
            if (!BuildSettings.canPlaceAt(level, pos, packet.replaceMode(), offHand)) continue;
            EffortlessBuildingBlockSnapshot before = null;
            Reservation reservation = null;
            boolean blockChanged = false;
            try {
                before = EffortlessBuildingBlockSnapshot.capture(level, pos);
                EffortlessBuildingGTPipeSnapshot previousPipe = EffortlessBuildingGTPipeCompat.snapshot(level, pos);
                List<ItemStack> displacedDrops = getDisplacedDrops(level, player, pos, before.state());
                reservation = creative ? Reservation.CREATIVE : reserve(player, placementItem, networkEligible);
                if (reservation == null) break;

                BlockState state = getPlacementState(level, player, pos, placementItem, blockItem, mapEntry.getValue(),
                        packet, yFraction);
                if (!level.setBlock(pos, state, 3)) {
                    refund(player, placementItem, reservation);
                    continue;
                }
                blockChanged = true;
                transferBlockItemData(level, player, pos, placementItem, blockItem);
                EffortlessBuildingBlockSnapshot after = EffortlessBuildingBlockSnapshot.capture(level, pos);
                BlockPos immutablePos = pos.immutable();
                undoChanges.put(immutablePos, new UndoManager.BlockChange(before.state(), after.state()));
                operationChanges.put(immutablePos, new EffortlessBuildingBlockChange(
                        before, after, placementItem, displacedDrops));
                if (previousPipe != null) previousPipeSnapshots.put(immutablePos, previousPipe);
                blockChanged = false;
                reservation = null;
                deliverDisplacedDrops(player, displacedDrops);
                damageTool(player, before.state());
            } catch (RuntimeException exception) {
                if (blockChanged && before != null) restoreAfterFailure(level, pos, before);
                if (reservation != null) refund(player, placementItem, reservation);
                CosmicCore.LOGGER.warn("Effortless Building placement failed at {}", pos, exception);
            }
        }

        if (undoChanges.isEmpty()) return;
        if (!creative && networkEligible) EffortlessBuildingAE2Bridge.restockMainHand(player, placementItem.getItem());

        EffortlessBuildingGTPipeOperation pipeOperation = null;
        try {
            pipeOperation = EffortlessBuildingGTPipeCompat.applyPlacement(
                    level, player, packet, undoChanges.keySet(), previousPipeSnapshots);
        } catch (RuntimeException exception) {
            CosmicCore.LOGGER.warn("Effortless Building GT pipe post-processing failed", exception);
        }
        operationChanges.replaceAll((pos, change) -> refreshAfterSnapshot(level, pos, change));
        EffortlessBuildingOperationMetadata metadata = new EffortlessBuildingOperationMetadata(
                operationChanges, pipeOperation);
        EffortlessBuildingUndoRecorder.record(player, level.dimension(), undoChanges, metadata);
        PlacedBlockTracker.trackAll(player.getUUID(), level.dimension(), undoChanges.keySet());
    }

    private static BlockState getPlacementState(
                                                ServerLevel level, ServerPlayer player, BlockPos pos,
                                                ItemStack placementItem, BlockItem blockItem, BlockEntry entry,
                                                PlaceBuildModePacket packet, double yFraction) {
        Vec3 localHit = new Vec3(packet.hitLocation().x, pos.getY() + yFraction, packet.hitLocation().z);
        BlockHitResult serverHit = new BlockHitResult(localHit, packet.hitFace(), pos, false);
        BlockPlaceContext context = new OpenBlockPlaceContext(
                level, player, InteractionHand.MAIN_HAND, placementItem, serverHit);
        BlockState state = blockItem.getBlock().getStateForPlacement(context);
        if (state == null) state = blockItem.getBlock().defaultBlockState();
        return entry.applyTransforms(state);
    }

    private static List<ItemStack> getDisplacedDrops(
                                                     ServerLevel level, ServerPlayer player, BlockPos pos,
                                                     BlockState state) {
        if (player.isCreative() || state.canBeReplaced()) return List.of();
        ItemStack tool = ServerConfig.INSTANCE.survivalRequireTools ?
                InventoryHelper.findCorrectTool(player, state) : player.getMainHandItem();
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack drop : Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, tool)) {
            drops.add(drop.copy());
        }
        return drops;
    }

    private static void deliverDisplacedDrops(ServerPlayer player, List<ItemStack> drops) {
        for (ItemStack drop : drops) EffortlessBuildingOperationExecutor.giveOrDrop(player, drop);
    }

    private static void damageTool(ServerPlayer player, BlockState state) {
        if (!player.isCreative() && !state.canBeReplaced() && ServerConfig.INSTANCE.survivalUseDurability) {
            InventoryHelper.damageCorrectTool(player, state);
        }
    }

    @Nullable
    private static Reservation reserve(ServerPlayer player, ItemStack placementItem, boolean networkEligible) {
        if (EffortlessBuildingOperationExecutor.consumeExact(player, placementItem)) return Reservation.INVENTORY;
        if (networkEligible && EffortlessBuildingAE2Bridge.extract(player, placementItem.getItem(), 1) == 1) {
            return Reservation.NETWORK;
        }
        return null;
    }

    private static void refund(ServerPlayer player, ItemStack placementItem, Reservation reservation) {
        if (reservation == Reservation.CREATIVE) return;
        if (reservation == Reservation.NETWORK &&
                EffortlessBuildingAE2Bridge.insert(player, placementItem.getItem(), 1) == 1)
            return;
        EffortlessBuildingOperationExecutor.giveOrDrop(player, placementItem);
    }

    private static void restoreAfterFailure(
                                            ServerLevel level, BlockPos pos,
                                            EffortlessBuildingBlockSnapshot before) {
        try {
            before.restore(level, pos);
        } catch (RuntimeException rollbackFailure) {
            CosmicCore.LOGGER.error("Effortless Building rollback failed at {}", pos, rollbackFailure);
        }
    }

    private static EffortlessBuildingBlockChange refreshAfterSnapshot(
                                                                      ServerLevel level, BlockPos pos,
                                                                      EffortlessBuildingBlockChange change) {
        try {
            return new EffortlessBuildingBlockChange(
                    change.before(), EffortlessBuildingBlockSnapshot.capture(level, pos), change.placedItem(),
                    change.displacedDrops());
        } catch (RuntimeException exception) {
            CosmicCore.LOGGER.warn("Effortless Building final block snapshot failed at {}", pos, exception);
            return change;
        }
    }

    private static void transferBlockItemData(
                                              ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack stack,
                                              BlockItem blockItem) {
        BlockState placedState = level.getBlockState(pos);
        if (!placedState.is(blockItem.getBlock())) return;
        BlockItem.updateCustomBlockEntityTag(level, player, pos, stack);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.applyComponentsFromItemStack(stack);
            blockEntity.setChanged();
        }
        placedState.getBlock().setPlacedBy(level, pos, placedState, player, stack);
    }

    private enum Reservation {
        CREATIVE,
        INVENTORY,
        NETWORK
    }

    private static final class OpenBlockPlaceContext extends BlockPlaceContext {

        private OpenBlockPlaceContext(
                                      Level level, Player player, InteractionHand hand, ItemStack stack,
                                      BlockHitResult hit) {
            super(level, player, hand, stack, hit);
        }
    }
}
