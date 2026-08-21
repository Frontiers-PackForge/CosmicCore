package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import neoforge.nl.requios.effortlessbuilding.buildmode.BuildModeEnum;
import neoforge.nl.requios.effortlessbuilding.network.PlaceBuildModePacket;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class EffortlessBuildingGTPipeCompat {

    private EffortlessBuildingGTPipeCompat() {}

    @Nullable
    public static EffortlessBuildingGTPipeSnapshot snapshot(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IPipeNode<?, ?> pipe) {
            return new EffortlessBuildingGTPipeSnapshot(pipe.getConnections(), pipe.getBlockedConnections());
        }
        return null;
    }

    @Nullable
    public static EffortlessBuildingGTPipeOperation applyPlacement(
                                                                   ServerLevel level, ServerPlayer player,
                                                                   PlaceBuildModePacket packet, Set<BlockPos> positions,
                                                                   Map<BlockPos, EffortlessBuildingGTPipeSnapshot> previousSnapshots) {
        Direction flowDirection = flowDirection(packet, positions);
        if (flowDirection == null) return null;
        Map<BlockPos, IPipeNode<?, ?>> pipes = new LinkedHashMap<>();
        for (BlockPos pos : positions) {
            if (!(level.getBlockEntity(pos) instanceof IPipeNode<?, ?> pipe)) return null;
            pipes.put(pos.immutable(), pipe);
        }
        if (pipes.isEmpty()) return null;

        boolean shutter = player.getOffhandItem().canPerformAction(GTItemAbilities.WRENCH_CONNECT) &&
                ToolHelper.canUse(player.getOffhandItem());
        Set<IPipeNode<?, ?>> touched = new LinkedHashSet<>();
        EffortlessBuildingGTPipeAnchor anchor = connectAnchor(level, packet, pipes, shutter, touched);
        if (packet.buildMode() == BuildModeEnum.LINE) connectLine(pipes, flowDirection, touched);
        if (shutter) {
            for (IPipeNode<?, ?> pipe : pipes.values()) {
                if (pipe.canHaveBlockedFaces()) {
                    pipe.setBlocked(flowDirection, true);
                    touched.add(pipe);
                }
            }
        }
        scheduleSynchronization(level, touched);

        Map<BlockPos, EffortlessBuildingGTPipeChange> changes = new LinkedHashMap<>();
        for (BlockPos pos : positions) {
            BlockPos immutablePos = pos.immutable();
            changes.put(immutablePos, new EffortlessBuildingGTPipeChange(
                    previousSnapshots.get(immutablePos), snapshot(level, immutablePos)));
        }
        return new EffortlessBuildingGTPipeOperation(changes, packet.firstPos(), anchor);
    }

    public static void afterUndo(
                                 ServerLevel level, EffortlessBuildingGTPipeOperation operation,
                                 Set<BlockPos> restoredPositions) {
        Set<IPipeNode<?, ?>> touched = new LinkedHashSet<>();
        for (BlockPos pos : restoredPositions) {
            EffortlessBuildingGTPipeChange change = operation.changes().get(pos);
            if (change != null && change.before() != null) restore(level, pos, change.before(), touched);
        }
        if (restoredPositions.contains(operation.firstPos()) && operation.anchor() != null) {
            EffortlessBuildingGTPipeAnchor anchor = operation.anchor();
            restoreAnchor(level, anchor, anchor.connectedBefore(), anchor.blockedBefore(), touched);
        }
        scheduleSynchronization(level, touched);
    }

    public static void afterRedo(
                                 ServerLevel level, EffortlessBuildingGTPipeOperation operation,
                                 Set<BlockPos> restoredPositions) {
        Set<IPipeNode<?, ?>> touched = new LinkedHashSet<>();
        for (BlockPos pos : restoredPositions) {
            EffortlessBuildingGTPipeChange change = operation.changes().get(pos);
            if (change != null && change.after() != null) restore(level, pos, change.after(), touched);
        }
        if (restoredPositions.contains(operation.firstPos()) && operation.anchor() != null) {
            EffortlessBuildingGTPipeAnchor anchor = operation.anchor();
            restoreAnchor(level, anchor, anchor.connectedAfter(), anchor.blockedAfter(), touched);
        }
        scheduleSynchronization(level, touched);
    }

    @Nullable
    private static Direction flowDirection(PlaceBuildModePacket packet, Set<BlockPos> positions) {
        if (packet.buildMode() == BuildModeEnum.SINGLE) {
            return positions.size() == 1 && positions.contains(packet.firstPos()) ? packet.hitFace() : null;
        }
        if (packet.buildMode() != BuildModeEnum.LINE) return null;
        int dx = packet.secondPos().getX() - packet.firstPos().getX();
        int dy = packet.secondPos().getY() - packet.firstPos().getY();
        int dz = packet.secondPos().getZ() - packet.firstPos().getZ();
        Direction direction;
        if (dx != 0 && dy == 0 && dz == 0) {
            direction = dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (dy != 0 && dx == 0 && dz == 0) {
            direction = dy > 0 ? Direction.UP : Direction.DOWN;
        } else if (dz != 0 && dx == 0 && dy == 0) {
            direction = dz > 0 ? Direction.SOUTH : Direction.NORTH;
        } else {
            return null;
        }
        for (BlockPos pos : positions) {
            if (!isOnSegment(packet.firstPos(), packet.secondPos(), pos)) return null;
        }
        return direction;
    }

    private static boolean isOnSegment(BlockPos start, BlockPos end, BlockPos pos) {
        int minX = Math.min(start.getX(), end.getX());
        int maxX = Math.max(start.getX(), end.getX());
        int minY = Math.min(start.getY(), end.getY());
        int maxY = Math.max(start.getY(), end.getY());
        int minZ = Math.min(start.getZ(), end.getZ());
        int maxZ = Math.max(start.getZ(), end.getZ());
        return pos.getX() >= minX && pos.getX() <= maxX &&
                pos.getY() >= minY && pos.getY() <= maxY &&
                pos.getZ() >= minZ && pos.getZ() <= maxZ &&
                ((start.getX() == end.getX() && pos.getX() == start.getX()) ||
                        (start.getY() == end.getY() && pos.getY() == start.getY()) ||
                        (start.getZ() == end.getZ() && pos.getZ() == start.getZ()));
    }

    @Nullable
    private static EffortlessBuildingGTPipeAnchor connectAnchor(
                                                                ServerLevel level, PlaceBuildModePacket packet,
                                                                Map<BlockPos, IPipeNode<?, ?>> pipes, boolean shutter,
                                                                Set<IPipeNode<?, ?>> touched) {
        IPipeNode<?, ?> first = pipes.get(packet.firstPos());
        if (first == null) return null;
        BlockPos anchorPos = packet.firstPos().relative(packet.hitFace().getOpposite());
        if (pipes.containsKey(anchorPos)) return null;
        if (!(level.getBlockEntity(anchorPos) instanceof IPipeNode<?, ?> anchor)) return null;
        Direction anchorToFirst = packet.hitFace();
        Direction firstToAnchor = anchorToFirst.getOpposite();
        if (!canConnect(first, firstToAnchor, anchor)) return null;
        boolean connectedBefore = anchor.isConnected(anchorToFirst);
        boolean blockedBefore = anchor.isBlocked(anchorToFirst);
        first.setConnection(firstToAnchor, true, false);
        if (shutter && anchor.canHaveBlockedFaces()) anchor.setBlocked(anchorToFirst, true);
        touched.add(first);
        touched.add(anchor);
        return new EffortlessBuildingGTPipeAnchor(
                anchorPos, anchorToFirst, connectedBefore, anchor.isConnected(anchorToFirst), blockedBefore,
                anchor.isBlocked(anchorToFirst));
    }

    private static void connectLine(
                                    Map<BlockPos, IPipeNode<?, ?>> pipes, Direction direction,
                                    Set<IPipeNode<?, ?>> touched) {
        for (Map.Entry<BlockPos, IPipeNode<?, ?>> entry : pipes.entrySet()) {
            IPipeNode<?, ?> next = pipes.get(entry.getKey().relative(direction));
            if (next == null || !canConnect(entry.getValue(), direction, next)) continue;
            entry.getValue().setConnection(direction, true, false);
            touched.add(entry.getValue());
            touched.add(next);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean canConnect(IPipeNode<?, ?> first, Direction direction, IPipeNode<?, ?> second) {
        PipeBlock firstBlock = first.getPipeBlock();
        PipeBlock secondBlock = second.getPipeBlock();
        Direction opposite = direction.getOpposite();
        return firstBlock.canConnect((IPipeNode) first, direction) &&
                secondBlock.canConnect((IPipeNode) second, opposite) &&
                firstBlock.canPipesConnect((IPipeNode) first, direction, (IPipeNode) second) &&
                secondBlock.canPipesConnect((IPipeNode) second, opposite, (IPipeNode) first);
    }

    private static void restore(
                                ServerLevel level, BlockPos pos, EffortlessBuildingGTPipeSnapshot snapshot,
                                Set<IPipeNode<?, ?>> touched) {
        if (!(level.getBlockEntity(pos) instanceof IPipeNode<?, ?> pipe)) return;
        for (Direction direction : Direction.values()) {
            boolean connected = hasSide(snapshot.connections(), direction);
            if (pipe.isConnected(direction) != connected && (!connected || canConnectToNeighbor(pipe, direction))) {
                pipe.setConnection(direction, connected, false);
            }
        }
        if (pipe.canHaveBlockedFaces()) {
            for (Direction direction : Direction.values()) {
                boolean blocked = hasSide(snapshot.blockedConnections(), direction);
                if (pipe.isBlocked(direction) != blocked) pipe.setBlocked(direction, blocked);
            }
        }
        touched.add(pipe);
    }

    private static void restoreAnchor(
                                      ServerLevel level, EffortlessBuildingGTPipeAnchor anchorState,
                                      boolean connected, boolean blocked, Set<IPipeNode<?, ?>> touched) {
        if (!(level.getBlockEntity(anchorState.pos()) instanceof IPipeNode<?, ?> anchor)) return;
        Direction direction = anchorState.direction();
        boolean changed = false;
        if (anchor.isConnected(direction) != connected && (!connected || canConnectToNeighbor(anchor, direction))) {
            anchor.setConnection(direction, connected, false);
            changed = true;
        }
        if (anchor.canHaveBlockedFaces() && anchor.isBlocked(direction) != blocked) {
            anchor.setBlocked(direction, blocked);
            changed = true;
        }
        if (changed) touched.add(anchor);
    }

    private static boolean hasSide(int mask, Direction direction) {
        return (mask & 1 << direction.ordinal()) != 0;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean canConnectToNeighbor(IPipeNode<?, ?> pipe, Direction direction) {
        return ((PipeBlock) pipe.getPipeBlock()).canConnect((IPipeNode) pipe, direction);
    }

    private static void scheduleSynchronization(ServerLevel level, Set<IPipeNode<?, ?>> pipes) {
        EffortlessBuildingGTPipeRenderSync.schedule(level, pipes);
    }
}
